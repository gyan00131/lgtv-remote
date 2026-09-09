package com.example.lg_remote_app.data.repository

import android.content.Context
import android.util.Log
import com.example.lg_remote_app.data.local.PairedTvStorage
import com.example.lg_remote_app.data.model.LgTvDevice
import com.example.lg_remote_app.data.model.TvInputSource
import com.example.lg_remote_app.data.network.SsApWebSocketClient
import com.example.lg_remote_app.data.network.SsdpDiscoveryService
import com.example.lg_remote_app.domain.discovery.TvDiscoveryService
import com.example.lg_remote_app.domain.model.TvConnectionState
import com.example.lg_remote_app.domain.repository.TvConnectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TvConnectionRepositoryImpl(
    context: Context,
    private val pairedStorage: PairedTvStorage = PairedTvStorage(context),
    private val discoveryService: TvDiscoveryService = SsdpDiscoveryService(context),
    private val webSocketClient: SsApWebSocketClient = SsApWebSocketClient()
) : TvConnectionRepository {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var discoveryJob: Job? = null

    override val connectionState: StateFlow<TvConnectionState> = webSocketClient.connectionState

    private val _discoveredDevices = MutableStateFlow<List<LgTvDevice>>(emptyList())
    override val discoveredDevices: StateFlow<List<LgTvDevice>> = _discoveredDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<LgTvDevice>>(emptyList())
    override val pairedDevices: StateFlow<List<LgTvDevice>> = _pairedDevices.asStateFlow()

    override val externalInputs: StateFlow<List<TvInputSource>> = webSocketClient.externalInputs

    init {
        loadPairedDevices()

        // Observe client key received from websocket and persist it securely
        scope.launch {
            webSocketClient.clientKeyReceived.collect { (device, key) ->
                Log.d(TAG, "Saving client key for TV: ${device.name} (${device.ipAddress})")
                val updated = device.copy(clientKey = key)
                pairedStorage.savePairedTv(updated)
                loadPairedDevices()
            }
        }

        // Auto-reconnect to last paired TV if available
        val lastPaired = getPairedTv()
        if (lastPaired != null) {
            Log.d(TAG, "Found previously paired TV (${lastPaired.name}), attempting auto-reconnect")
            connectToDevice(lastPaired)
        }
    }

    override fun startDiscovery() {
        discoveryJob?.cancel()
        _discoveredDevices.value = emptyList()

        discoveryJob = scope.launch {
            discoveryService.discoverDevices()
                .catch { e ->
                    Log.e(TAG, "Error in discovery stream", e)
                }
                .collect { device ->
                    val currentList = _discoveredDevices.value.toMutableList()
                    val existingIndex = currentList.indexOfFirst {
                        it.ipAddress == device.ipAddress || (it.macAddress != null && it.macAddress == device.macAddress)
                    }

                    // Check if this discovered device was previously paired and attach key/mac
                    val pairedMatch = _pairedDevices.value.find {
                        it.id == device.id || it.ipAddress == device.ipAddress || (it.macAddress != null && it.macAddress == device.macAddress)
                    }

                    val enrichedDevice = device.copy(
                        clientKey = device.clientKey ?: pairedMatch?.clientKey,
                        macAddress = device.macAddress ?: pairedMatch?.macAddress
                    )

                    // Handle DHCP IP change recovery
                    if (pairedMatch != null && pairedMatch.ipAddress != device.ipAddress) {
                        Log.d(TAG, "TV IP address changed from ${pairedMatch.ipAddress} to ${device.ipAddress}")
                        pairedStorage.updateTvIp(pairedMatch.id, device.ipAddress)
                        loadPairedDevices()
                    }

                    if (existingIndex >= 0) {
                        currentList[existingIndex] = enrichedDevice
                    } else {
                        currentList.add(enrichedDevice)
                    }
                    _discoveredDevices.value = currentList
                }
        }
    }

    override fun stopDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
    }

    override fun connectToDevice(device: LgTvDevice) {
        val pairedMatch = _pairedDevices.value.find {
            it.id == device.id || it.ipAddress == device.ipAddress || (it.macAddress != null && it.macAddress == device.macAddress)
        }
        val targetDevice = device.copy(
            clientKey = device.clientKey ?: pairedMatch?.clientKey,
            macAddress = device.macAddress ?: pairedMatch?.macAddress
        )

        webSocketClient.connect(targetDevice)
    }

    override fun disconnect() {
        webSocketClient.disconnect()
    }

    override fun sendPin(pin: String) {
        webSocketClient.sendPin(pin)
    }

    override fun forgetPairedTv(device: LgTvDevice) {
        pairedStorage.clearPairedTv(device.id)
        loadPairedDevices()
        if (connectionState.value is TvConnectionState.Connected) {
            val connectedDev = (connectionState.value as TvConnectionState.Connected).device
            if (connectedDev.id == device.id || connectedDev.ipAddress == device.ipAddress) {
                disconnect()
            }
        }
    }

    override fun getPairedTv(): LgTvDevice? {
        return pairedStorage.getPairedTv()
    }

    override fun powerOff() = webSocketClient.powerOff()
    override fun volumeUp() = webSocketClient.volumeUp()
    override fun volumeDown() = webSocketClient.volumeDown()
    override fun toggleMute() = webSocketClient.toggleMute()
    override fun channelUp() = webSocketClient.channelUp()
    override fun channelDown() = webSocketClient.channelDown()
    override fun sendHome() = webSocketClient.sendHome()
    override fun sendBack() = webSocketClient.sendBack()
    override fun sendPointerButton(buttonName: String) = webSocketClient.sendPointerButton(buttonName)

    override fun play() = webSocketClient.play()
    override fun pause() = webSocketClient.pause()
    override fun stop() = webSocketClient.stop()
    override fun rewind() = webSocketClient.rewind()
    override fun fastForward() = webSocketClient.fastForward()
    override fun sendNumber(digit: Int) = webSocketClient.sendNumber(digit)
    override fun fetchExternalInputs() = webSocketClient.fetchExternalInputs()
    override fun switchInput(inputId: String) = webSocketClient.switchInput(inputId)

    private fun loadPairedDevices() {
        _pairedDevices.value = pairedStorage.getPairedTvs()
    }

    companion object {
        private const val TAG = "TvConnectionRepoImpl"
    }
}
