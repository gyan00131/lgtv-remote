package com.example.lg_remote_app.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.lg_remote_app.model.ConnectionState
import com.example.lg_remote_app.model.ConnectionStatus
import com.example.lg_remote_app.model.TvApp
import com.example.lg_remote_app.model.TvDevice
import com.example.lg_remote_app.network.SsdpDiscoveryManager
import com.example.lg_remote_app.network.WakeOnLanManager
import com.example.lg_remote_app.network.WebOsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class TvRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("lg_tv_remote_prefs", Context.MODE_PRIVATE)
    private val ssdpManager = SsdpDiscoveryManager(context)
    val webOsClient = WebOsClient()
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<TvDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<TvDevice>> = _discoveredDevices.asStateFlow()

    private val _savedDevices = MutableStateFlow<List<TvDevice>>(emptyList())
    val savedDevices: StateFlow<List<TvDevice>> = _savedDevices.asStateFlow()

    val installedApps: StateFlow<List<TvApp>> = webOsClient.installedApps

    init {
        if (!prefs.getBoolean("key_v2_migrated", false)) {
            prefs.edit().clear().putBoolean("key_v2_migrated", true).apply()
        }
        loadSavedDevices()

        scope.launch {
            webOsClient.connectionStatus.collect { status ->
                // showPairingDialog is a legacy field — kept for state completeness but
                // the PinScreen is gated solely on showPinDialog from the client.
                _connectionState.value = _connectionState.value.copy(
                    status = status,
                    showPairingDialog = false // Pairing dialog state is driven by showPinDialog
                )
            }
        }

        scope.launch {
            webOsClient.showPinDialog.collect { showPin ->
                _connectionState.value = _connectionState.value.copy(
                    showPinDialog = showPin
                )
            }
        }

        scope.launch {
            webOsClient.clientKeyReceived.collect { key ->
                val currentDev = _connectionState.value.currentDevice
                if (currentDev != null) {
                    val updatedDev = currentDev.copy(clientKey = key)
                    saveDevice(updatedDev)
                    _connectionState.value = _connectionState.value.copy(currentDevice = updatedDev)
                }
            }
        }

        // Auto-connect to last saved device if present
        val lastDevice = _savedDevices.value.firstOrNull()
        if (lastDevice != null) {
            connectToDevice(lastDevice)
        }
    }

    fun startDiscovery() {
        scope.launch {
            ssdpManager.discoverTvs().collect { device ->
                val currentList = _discoveredDevices.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.ipAddress == device.ipAddress }
                val savedKey = device.clientKey ?: findSavedKey(device.ipAddress, device.macAddress)
                val deviceWithKey = device.copy(clientKey = savedKey)

                if (existingIndex >= 0) {
                    currentList[existingIndex] = deviceWithKey
                } else {
                    currentList.add(deviceWithKey)
                }
                _discoveredDevices.value = currentList

                val currentStatus = _connectionState.value.status
                if (currentStatus == ConnectionStatus.DISCONNECTED || _connectionState.value.currentDevice == null) {
                    connectToDevice(deviceWithKey)
                }
            }
        }
    }

    fun connectToDevice(device: TvDevice) {
        val savedKey = device.clientKey ?: findSavedKey(device.ipAddress, device.macAddress)
        val devToConnect = device.copy(clientKey = savedKey)
        _connectionState.value = ConnectionState(
            status = ConnectionStatus.CONNECTING,
            currentDevice = devToConnect
        )
        webOsClient.connect(devToConnect.ipAddress, devToConnect.clientKey)
    }

    fun connectByIp(ip: String, name: String = "[LG] webOS TV") {
        val savedKey = findSavedKey(ip, null)
        val device = TvDevice(ipAddress = ip, name = name, clientKey = savedKey, isDiscovered = false)
        connectToDevice(device)
    }

    fun forceRePair(device: TvDevice? = _connectionState.value.currentDevice) {
        val targetDev = device ?: return
        clearSavedKeys()
        val freshDev = targetDev.copy(clientKey = null)
        _connectionState.value = ConnectionState(
            status = ConnectionStatus.CONNECTING,
            currentDevice = freshDev
        )
        webOsClient.connect(freshDev.ipAddress, clientKey = null)
    }

    fun clearSavedKeys() {
        prefs.edit().clear().putBoolean("key_v2_migrated", true).apply()
        _savedDevices.value = emptyList()
    }

    fun sendWakeOnLan(device: TvDevice? = _connectionState.value.currentDevice) {
        val mac = device?.macAddress
        val ip = device?.ipAddress
        if (!mac.isNullOrEmpty()) {
            scope.launch {
                WakeOnLanManager.sendWakeOnLan(mac, ip)
            }
        }
    }

    fun sendPin(pinCode: String) {
        webOsClient.sendPin(pinCode)
    }

    fun powerOff() {
        webOsClient.sendCommand("ssap://system/turnOff")
    }

    fun turnOffScreen() {
        webOsClient.sendCommand("ssap://com.webos.service.tvpower/power/turnOffScreen")
    }

    fun sendPointerButton(buttonName: String) {
        webOsClient.sendPointerButton(buttonName)
    }

    fun sendCommand(uri: String, payload: JSONObject? = null) {
        webOsClient.sendCommand(uri, payload)
    }

    fun launchApp(appId: String) {
        webOsClient.launchApp(appId)
    }

    fun deepLinkSettings(target: String) {
        webOsClient.deepLinkSettings(target)
    }

    private fun findSavedKey(ip: String, mac: String?): String? {
        val list = _savedDevices.value
        val match = list.find { it.ipAddress == ip || (!mac.isNullOrEmpty() && it.macAddress == mac) }
        return match?.clientKey
    }

    private fun saveDevice(device: TvDevice) {
        val currentSaved = _savedDevices.value.toMutableList()
        val index = currentSaved.indexOfFirst { it.ipAddress == device.ipAddress || (it.macAddress != null && it.macAddress == device.macAddress) }
        if (index >= 0) {
            currentSaved[index] = device
        } else {
            currentSaved.add(device)
        }
        _savedDevices.value = currentSaved
        persistSavedDevices(currentSaved)
    }

    private fun persistSavedDevices(list: List<TvDevice>) {
        val array = JSONArray()
        for (dev in list) {
            val obj = JSONObject().apply {
                put("ipAddress", dev.ipAddress)
                put("name", dev.name)
                put("macAddress", dev.macAddress ?: "")
                put("clientKey", dev.clientKey ?: "")
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_tvs_json", array.toString()).apply()
    }

    private fun loadSavedDevices() {
        val jsonStr = prefs.getString("saved_tvs_json", null) ?: return
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<TvDevice>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val ip = obj.optString("ipAddress")
                val name = obj.optString("name", "[LG] webOS TV")
                val mac = obj.optString("macAddress").takeIf { it.isNotEmpty() }
                val key = obj.optString("clientKey").takeIf { it.isNotEmpty() }
                if (ip.isNotEmpty()) {
                    list.add(TvDevice(ipAddress = ip, name = name, macAddress = mac, clientKey = key, isDiscovered = false))
                }
            }
            _savedDevices.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
