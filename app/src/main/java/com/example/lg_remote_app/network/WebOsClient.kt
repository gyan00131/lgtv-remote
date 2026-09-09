package com.example.lg_remote_app.network

import android.util.Log
import com.example.lg_remote_app.model.ConnectionStatus
import com.example.lg_remote_app.model.TvApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class WebOsClient {

    private val client: OkHttpClient = createUnsafeOkHttpClient()

    private var webSocket: WebSocket? = null
    private var pointerSocket: WebSocket? = null
    private var pointerSocketReady = false
    private val reqId = AtomicInteger(1)

    private val pendingPointerCommands = ArrayDeque<String>()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    private val _clientKeyReceived = MutableSharedFlow<String>()
    val clientKeyReceived: SharedFlow<String> = _clientKeyReceived.asSharedFlow()

    private val _installedApps = MutableStateFlow<List<TvApp>>(emptyList())
    val installedApps: StateFlow<List<TvApp>> = _installedApps.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var currentIp: String? = null
    private var currentClientKey: String? = null
    private var autoReconnectJob: Job? = null
    private var useSsl = false

    private val POINTER_REQ_PREFIX = "ptr_req_"
    private val LIST_APPS_PREFIX = "list_apps_"
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

    fun connect(ipAddress: String, clientKey: String? = null) {
        currentIp = ipAddress
        currentClientKey = clientKey
        _showPinDialog.value = false
        pointerSocketReady = false
        pendingPointerCommands.clear()
        autoReconnectJob?.cancel()

        if (_connectionStatus.value != ConnectionStatus.CONNECTED) {
            _connectionStatus.value = ConnectionStatus.CONNECTING
        }

        connectInternal(ipAddress, clientKey, useSsl = useSsl)
    }

    private fun connectInternal(ipAddress: String, clientKey: String?, useSsl: Boolean) {
        disconnectSockets()

        val scheme = if (useSsl) "wss" else "ws"
        val port = if (useSsl) 3001 else 3000
        val url = "$scheme://$ipAddress:$port/"

        Log.d("WebOsClient", "Connecting to $url (ssl=$useSsl, hasKey=${!clientKey.isNullOrEmpty()})")

        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebOsClient", "Primary socket OPEN")
                this@WebOsClient.useSsl = useSsl
                _connectionStatus.value = ConnectionStatus.PAIRING
                sendRegisterPayload(clientKey)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleWebOsMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebOsClient", "Primary socket FAILURE: ${t.message} (ssl=$useSsl)")
                if (!useSsl) {
                    this@WebOsClient.useSsl = true
                    connectInternal(ipAddress, clientKey, useSsl = true)
                } else {
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                    scheduleReconnect()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("WebOsClient", "Primary socket CLOSED: $code $reason")
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        autoReconnectJob?.cancel()
        autoReconnectJob = scope.launch {
            delay(5000)
            val ip = currentIp
            if (ip != null && _connectionStatus.value == ConnectionStatus.DISCONNECTED) {
                connectInternal(ip, currentClientKey, useSsl)
            }
        }
    }

    private fun disconnectSockets() {
        try { webSocket?.close(1000, "Disconnecting") } catch (_: Exception) {}
        try { pointerSocket?.close(1000, "Disconnecting") } catch (_: Exception) {}
        webSocket = null
        pointerSocket = null
        pointerSocketReady = false
        pendingPointerCommands.clear()
    }

    fun disconnect() {
        autoReconnectJob?.cancel()
        currentIp = null
        currentClientKey = null
        disconnectSockets()
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _showPinDialog.value = false
    }

    private fun sendRegisterPayload(clientKey: String?) {
        val hasKey = !clientKey.isNullOrEmpty()
        val registerJson = JSONObject().apply {
            put("type", "register")
            put("id", REGISTER_ID)
            put("payload", JSONObject().apply {
                put("forcePairing", false)
                put("pairingType", "PIN")
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
        Log.d("WebOsClient", "TX Register (hasKey=$hasKey): $payloadStr")
        webSocket?.send(payloadStr)
    }

    fun sendPin(pinCode: String) {
        val setPinPayload = JSONObject().apply { put("pin", pinCode) }
        val req = JSONObject().apply {
            put("id", "pin_submit_${reqId.getAndIncrement()}")
            put("type", "request")
            put("uri", "ssap://pairing/setPin")
            put("payload", setPinPayload)
        }
        val reqStr = req.toString()
        Log.d("WebOsClient", "TX PIN submit: $reqStr")
        webSocket?.send(reqStr)
    }

    private fun handleWebOsMessage(text: String) {
        Log.d("WebOsClient", "RX: $text")
        try {
            val json = JSONObject(text)
            val type = json.optString("type")
            val id = json.optString("id")
            val payload = json.optJSONObject("payload")

            when (type) {
                "registered" -> {
                    Log.d("WebOsClient", "REGISTERED SUCCESSFULLY!")
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    _showPinDialog.value = false
                    val key = payload?.optString("client-key")
                    if (!key.isNullOrEmpty()) {
                        currentClientKey = key
                        scope.launch { _clientKeyReceived.emit(key) }
                    }
                    requestPointerSocket()
                    fetchInstalledApps()
                }

                "response" -> {
                    val returnValue = payload?.optBoolean("returnValue", true) ?: true
                    val pairingType = payload?.optString("pairingType") ?: ""

                    if (id == REGISTER_ID && (pairingType == "PIN" || pairingType == "PROMPT")) {
                        Log.d("WebOsClient", "TV requesting PIN pairing (type=$pairingType)")
                        _showPinDialog.value = true
                        return
                    }

                    if (id == REGISTER_ID && !returnValue) {
                        Log.d("WebOsClient", "Register response returnValue=false — showing PIN dialog")
                        _showPinDialog.value = true
                        return
                    }

                    if (id.startsWith(POINTER_REQ_PREFIX)) {
                        var socketPath = payload?.optString("socketPath") ?: ""
                        if (socketPath.isNotEmpty()) {
                            if (!socketPath.startsWith("ws://") && !socketPath.startsWith("wss://")) {
                                val scheme = if (useSsl) "wss" else "ws"
                                val port = if (useSsl) 3001 else 3000
                                if (!socketPath.startsWith("/")) socketPath = "/$socketPath"
                                socketPath = "$scheme://${currentIp}:$port$socketPath"
                            }
                            Log.d("WebOsClient", "Got pointer socket path: $socketPath")
                            connectPointerSocket(socketPath)
                        }
                        return
                    }

                    if (id.startsWith(LIST_APPS_PREFIX)) {
                        val launchPoints = payload?.optJSONArray("launchPoints")
                        if (launchPoints != null) {
                            val appList = mutableListOf<TvApp>()
                            for (i in 0 until launchPoints.length()) {
                                val item = launchPoints.getJSONObject(i)
                                val appId = item.optString("id")
                                val title = item.optString("title")
                                val icon = item.optString("icon")
                                val largeIcon = item.optString("largeIcon")
                                val iconUrl = if (largeIcon.isNotEmpty()) largeIcon else icon
                                if (appId.isNotEmpty() && title.isNotEmpty()) {
                                    appList.add(TvApp(id = appId, title = title, iconUrl = iconUrl))
                                }
                            }
                            _installedApps.value = appList
                        }
                        return
                    }
                }

                "error" -> {
                    val errorStr = json.optString("error")
                    Log.e("WebOsClient", "webOS Error (id=$id): $errorStr")

                    when {
                        errorStr.contains("401") ||
                        errorStr.contains("insufficient permissions", ignoreCase = true) -> {
                            Log.w("WebOsClient", "Permission error — clearing key and forcing re-pair")
                            currentClientKey = null
                            scope.launch { _clientKeyReceived.emit("") }
                            val ip = currentIp
                            if (ip != null) connect(ip, clientKey = null)
                        }

                        id == REGISTER_ID && (errorStr.contains("denied", ignoreCase = true) ||
                                errorStr.contains("rejected", ignoreCase = true)) -> {
                            _connectionStatus.value = ConnectionStatus.ERROR
                        }

                        id.startsWith(POINTER_REQ_PREFIX) -> {
                            scope.launch {
                                delay(2000)
                                if (_connectionStatus.value == ConnectionStatus.CONNECTED) {
                                    requestPointerSocket()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestPointerSocket() {
        val reqIdStr = "${POINTER_REQ_PREFIX}${reqId.getAndIncrement()}"
        val req = JSONObject().apply {
            put("id", reqIdStr)
            put("type", "request")
            put("uri", "ssap://com.webos.service.networkinput/getPointerInputSocket")
        }
        Log.d("WebOsClient", "TX requestPointerSocket id=$reqIdStr")
        webSocket?.send(req.toString())
    }

    private fun connectPointerSocket(socketUrl: String) {
        try { pointerSocket?.close(1000, "Reconnecting pointer") } catch (_: Exception) {}
        pointerSocket = null
        pointerSocketReady = false

        val request = Request.Builder().url(socketUrl).build()
        pointerSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebOsClient", "Pointer socket CONNECTED!")
                pointerSocketReady = true
                val cmdList = pendingPointerCommands.toList()
                pendingPointerCommands.clear()
                for (cmd in cmdList) {
                    webSocket.send(cmd)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebOsClient", "Pointer socket FAILURE: ${t.message}")
                pointerSocketReady = false
                this@WebOsClient.pointerSocket = null
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                pointerSocketReady = false
                this@WebOsClient.pointerSocket = null
            }
        })
    }

    fun sendPointerButton(buttonName: String) {
        val command = "type:button\nname:$buttonName\n\n"

        val pSocket = pointerSocket
        if (pSocket != null && pointerSocketReady) {
            Log.d("WebOsClient", "TX pointer button: $buttonName")
            pSocket.send(command)
        } else {
            Log.d("WebOsClient", "Pointer socket not ready, buffering: $buttonName")
            pendingPointerCommands.addLast(command)
            while (pendingPointerCommands.size > 10) pendingPointerCommands.removeFirst()

            if (_connectionStatus.value == ConnectionStatus.CONNECTED) {
                requestPointerSocket()
            }

            when (buttonName) {
                "HOME" -> sendCommand("ssap://system.launcher/open")
                "BACK" -> sendCommand("ssap://tv/openChannelGuide")
                "ENTER" -> sendCommand("ssap://com.webos.service.ime/sendEnterKey")
                else -> sendCommand("ssap://com.webos.service.ime/sendEnterKey")
            }
        }
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
        Log.d("WebOsClient", "TX SSAP: $reqStr")
        webSocket?.send(reqStr)
    }

    fun fetchInstalledApps() {
        val req = JSONObject().apply {
            put("id", "${LIST_APPS_PREFIX}${reqId.getAndIncrement()}")
            put("type", "request")
            put("uri", "ssap://com.webos.applicationManager/listLaunchPoints")
        }
        webSocket?.send(req.toString())
    }

    fun launchApp(appId: String) {
        val payload = JSONObject().apply { put("id", appId) }
        sendCommand("ssap://system.launcher/launch", payload)
    }

    fun deepLinkSettings(target: String) {
        val payload = JSONObject().apply {
            put("id", "com.palm.app.settings")
            put("params", JSONObject().apply {
                put("target", target)
            })
        }
        sendCommand("ssap://system.launcher/launch", payload)
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
}
