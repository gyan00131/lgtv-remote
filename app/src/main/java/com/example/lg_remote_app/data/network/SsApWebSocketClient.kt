package com.example.lg_remote_app.data.network

import android.util.Log
import com.example.lg_remote_app.data.model.LgTvDevice
import com.example.lg_remote_app.domain.model.TvConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class SsApWebSocketClient {

    private val client: OkHttpClient = createUnsafeOkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var webSocket: WebSocket? = null
    private var pointerSocket: WebSocket? = null
    private var pointerSocketReady = false
    private val isConnectingOrConnected = AtomicBoolean(false)
    private val reqId = AtomicInteger(1)

    private val pendingPointerCommands = ArrayDeque<String>()

    private val _connectionState = MutableStateFlow<TvConnectionState>(TvConnectionState.Disconnected)
    val connectionState: StateFlow<TvConnectionState> = _connectionState.asStateFlow()

    private val _clientKeyReceived = MutableSharedFlow<Pair<LgTvDevice, String>>()
    val clientKeyReceived: SharedFlow<Pair<LgTvDevice, String>> = _clientKeyReceived.asSharedFlow()

    private var currentDevice: LgTvDevice? = null
    private var useSsl = false

    private val POINTER_REQ_PREFIX = "ptr_req_"
    private val REGISTER_ID = "register_0"

    private val fullPermissions = listOf(
        "LAUNCH", "LAUNCH_WEBAPP", "APP_TO_APP", "CLOSE",
        "TEST_OPEN", "TEST_PROTECTED", "CONTROL_AUDIO",
        "CONTROL_DISPLAY", "CONTROL_INPUT_JOYSTICK",
        "CONTROL_INPUT_MEDIA_RECORDING", "CONTROL_INPUT_MEDIA_PLAYBACK",
        "CONTROL_INPUT_TV", "CONTROL_POWER", "READ_APP_STATUS",
        "READ_CURRENT_CHANNEL", "READ_INPUT_DEVICE_LIST",
        "READ_NETWORK_STATE", "READ_RUNNING_APPS",
        "READ_TV_CHANNEL_LIST", "WRITE_NOTIFICATION_TOAST",
        "READ_POWER_STATE", "CHECK_BLUETOOTH_DEVICE",
        "READ_SETTINGS", "READ_TV_CURRENT_TIME",
        "CONTROL_INPUT_TEXT", "CONTROL_MOUSE_AND_KEYBOARD", "WRITE_SETTINGS"
    )

    fun connect(device: LgTvDevice) {
        val prevDevice = currentDevice
        if (isConnectingOrConnected.get() && prevDevice?.ipAddress == device.ipAddress) {
            Log.d(TAG, "Already connected/connecting to ${device.ipAddress}, ignoring duplicate connect call")
            return
        }

        disconnectInternal()
        currentDevice = device
        isConnectingOrConnected.set(true)
        _connectionState.value = TvConnectionState.Connecting(device)

        connectSocket(device, ssl = useSsl)
    }

    private fun connectSocket(device: LgTvDevice, ssl: Boolean) {
        val scheme = if (ssl) "wss" else "ws"
        val port = if (ssl) 3001 else 3000
        val url = "$scheme://${device.ipAddress}:$port/"

        Log.d(TAG, "Connecting SSAP WebSocket to $url (hasClientKey=${!device.clientKey.isNullOrEmpty()})")

        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected to $url")
                useSsl = ssl
                _connectionState.value = TvConnectionState.Connecting(device)
                sendRegisterPayload(device.clientKey)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleSsApMessage(text, device)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure on $url: ${t.message}")
                if (!ssl) {
                    Log.d(TAG, "Retrying via SSL (wss://:3001)...")
                    useSsl = true
                    connectSocket(device, ssl = true)
                } else {
                    isConnectingOrConnected.set(false)
                    _connectionState.value = TvConnectionState.ConnectionFailed(
                        device,
                        t.localizedMessage ?: "Unable to connect to LG TV"
                    )
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "WebSocket closed ($code): $reason")
                isConnectingOrConnected.set(false)
                _connectionState.value = TvConnectionState.Disconnected
            }
        })
    }

    fun sendCommand(uri: String, payload: JSONObject? = null) {
        val req = JSONObject().apply {
            put("id", "req_${reqId.getAndIncrement()}")
            put("type", "request")
            put("uri", uri)
            if (payload != null) {
                put("payload", payload)
            }
        }
        val reqStr = req.toString()
        Log.d(TAG, "TX SSAP command: $reqStr")
        webSocket?.send(reqStr)
    }

    fun volumeUp() = sendCommand("ssap://audio/volumeUp")
    fun volumeDown() = sendCommand("ssap://audio/volumeDown")
    fun toggleMute() = sendCommand("ssap://audio/setMute", JSONObject().apply { put("mute", true) })
    fun channelUp() = sendCommand("ssap://tv/channelUp")
    fun channelDown() = sendCommand("ssap://tv/channelDown")
    fun powerOff() = sendCommand("ssap://system/turnOff")

    fun sendPointerButton(buttonName: String) {
        val command = "type:button\nname:$buttonName\n\n"

        val pSocket = pointerSocket
        if (pSocket != null && pointerSocketReady) {
            Log.d(TAG, "TX pointer button: $buttonName")
            pSocket.send(command)
        } else {
            Log.d(TAG, "Pointer socket not ready, buffering: $buttonName")
            pendingPointerCommands.addLast(command)
            while (pendingPointerCommands.size > 10) pendingPointerCommands.removeFirst()

            if (_connectionState.value is TvConnectionState.Connected) {
                requestPointerSocket()
            }

            when (buttonName) {
                "HOME" -> sendCommand("ssap://system.launcher/open")
                "BACK" -> sendCommand("ssap://tv/openChannelGuide")
                "ENTER" -> sendCommand("ssap://com.webos.service.ime/sendEnterKey")
                "UP" -> sendCommand("ssap://com.webos.service.ime/sendEnterKey")
                else -> sendCommand("ssap://com.webos.service.ime/sendEnterKey")
            }
        }
    }

    fun sendHome() = sendPointerButton("HOME")
    fun sendBack() = sendPointerButton("BACK")

    fun sendPin(pinCode: String) {
        val setPinPayload = JSONObject().apply { put("pin", pinCode) }
        val req = JSONObject().apply {
            put("id", "pin_submit_${reqId.getAndIncrement()}")
            put("type", "request")
            put("uri", "ssap://pairing/setPin")
            put("payload", setPinPayload)
        }
        val reqStr = req.toString()
        Log.d(TAG, "TX PIN submit: $reqStr")
        webSocket?.send(reqStr)
    }

    fun disconnect() {
        disconnectInternal()
        _connectionState.value = TvConnectionState.Disconnected
    }

    private fun disconnectInternal() {
        isConnectingOrConnected.set(false)
        try { webSocket?.close(1000, "Disconnect requested") } catch (_: Exception) {}
        try { pointerSocket?.close(1000, "Disconnect requested") } catch (_: Exception) {}
        webSocket = null
        pointerSocket = null
        pointerSocketReady = false
        pendingPointerCommands.clear()
    }

    private fun sendRegisterPayload(clientKey: String?) {
        val hasKey = !clientKey.isNullOrEmpty()
        val registerJson = JSONObject().apply {
            put("type", "register")
            put("id", REGISTER_ID)
            put("payload", JSONObject().apply {
                put("forcePairing", false)
                put("pairingType", "PROMPT")
                if (hasKey) {
                    put("client-key", clientKey)
                }
                put("manifest", JSONObject().apply {
                    put("manifestVersion", 1)
                    put("permissions", JSONArray(fullPermissions))
                })
            })
        }
        val payloadStr = registerJson.toString()
        Log.d(TAG, "TX Register payload (hasKey=$hasKey)")
        webSocket?.send(payloadStr)
    }

    private fun handleSsApMessage(text: String, device: LgTvDevice) {
        Log.d(TAG, "RX SSAP: $text")
        try {
            val json = JSONObject(text)
            val type = json.optString("type")
            val id = json.optString("id")
            val payload = json.optJSONObject("payload")

            when (type) {
                "registered" -> {
                    Log.d(TAG, "Registered successfully with LG TV!")
                    val clientKey = payload?.optString("client-key") ?: ""
                    val updatedDevice = device.copy(
                        isConnected = true,
                        clientKey = if (clientKey.isNotEmpty()) clientKey else device.clientKey
                    )
                    currentDevice = updatedDevice
                    _connectionState.value = TvConnectionState.Connected(updatedDevice)

                    if (clientKey.isNotEmpty()) {
                        scope.launch {
                            _clientKeyReceived.emit(Pair(updatedDevice, clientKey))
                        }
                    }

                    requestPointerSocket()
                }

                "response" -> {
                    val pairingType = payload?.optString("pairingType") ?: ""
                    val returnValue = payload?.optBoolean("returnValue", true) ?: true

                    if (id == REGISTER_ID && (pairingType == "PROMPT" || pairingType == "PIN")) {
                        Log.d(TAG, "TV requires pairing confirmation (type=$pairingType)")
                        _connectionState.value = TvConnectionState.WaitingForPairing(device, pairingType)
                        return
                    }

                    if (id == REGISTER_ID && !returnValue) {
                        Log.w(TAG, "Registration returned false — prompt pairing required")
                        _connectionState.value = TvConnectionState.WaitingForPairing(device, "PROMPT")
                        return
                    }

                    if (id.startsWith(POINTER_REQ_PREFIX)) {
                        var socketPath = payload?.optString("socketPath") ?: ""
                        if (socketPath.isNotEmpty()) {
                            if (!socketPath.startsWith("ws://") && !socketPath.startsWith("wss://")) {
                                val scheme = if (useSsl) "wss" else "ws"
                                val port = if (useSsl) 3001 else 3000
                                if (!socketPath.startsWith("/")) socketPath = "/$socketPath"
                                socketPath = "$scheme://${device.ipAddress}:$port$socketPath"
                            }
                            Log.d(TAG, "Got pointer socket path: $socketPath")
                            connectPointerSocket(socketPath)
                        }
                        return
                    }
                }

                "error" -> {
                    val errorStr = json.optString("error")
                    Log.e(TAG, "SSAP Error (id=$id): $errorStr")

                    when {
                        errorStr.contains("401") || errorStr.contains("permission", ignoreCase = true) -> {
                            Log.w(TAG, "Invalid/Expired client key on TV — prompting re-pair")
                            _connectionState.value = TvConnectionState.PairingRequired(device)
                        }

                        id == REGISTER_ID && (errorStr.contains("denied", ignoreCase = true) || errorStr.contains("rejected", ignoreCase = true)) -> {
                            _connectionState.value = TvConnectionState.ConnectionFailed(
                                device,
                                "Pairing request was denied on TV"
                            )
                        }

                        else -> {
                            _connectionState.value = TvConnectionState.ConnectionFailed(device, errorStr)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing SSAP message", e)
        }
    }

    private fun requestPointerSocket() {
        val reqIdStr = "${POINTER_REQ_PREFIX}${reqId.getAndIncrement()}"
        val req = JSONObject().apply {
            put("id", reqIdStr)
            put("type", "request")
            put("uri", "ssap://com.webos.service.networkinput/getPointerInputSocket")
        }
        Log.d(TAG, "TX requestPointerSocket id=$reqIdStr")
        webSocket?.send(req.toString())
    }

    private fun connectPointerSocket(socketUrl: String) {
        try { pointerSocket?.close(1000, "Reconnecting pointer") } catch (_: Exception) {}
        pointerSocket = null
        pointerSocketReady = false

        val request = Request.Builder().url(socketUrl).build()
        pointerSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Pointer socket CONNECTED!")
                pointerSocketReady = true
                val cmdList = pendingPointerCommands.toList()
                pendingPointerCommands.clear()
                for (cmd in cmdList) {
                    webSocket.send(cmd)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Pointer socket FAILURE: ${t.message}")
                pointerSocketReady = false
                this@SsApWebSocketClient.pointerSocket = null
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                pointerSocketReady = false
                this@SsApWebSocketClient.pointerSocket = null
            }
        })
    }

    private fun createUnsafeOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .pingInterval(20, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .pingInterval(20, TimeUnit.SECONDS)
                .build()
        }
    }

    companion object {
        private const val TAG = "SsApWebSocketClient"
    }
}
