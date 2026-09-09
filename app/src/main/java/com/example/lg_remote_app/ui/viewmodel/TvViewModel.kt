package com.example.lg_remote_app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.lg_remote_app.model.ConnectionState
import com.example.lg_remote_app.model.TvApp
import com.example.lg_remote_app.model.TvDevice
import com.example.lg_remote_app.repository.TvRepository
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class TvViewModel(application: Application) : AndroidViewModel(application) {

    val repository = TvRepository(application)

    val connectionState: StateFlow<ConnectionState> = repository.connectionState
    val discoveredDevices: StateFlow<List<TvDevice>> = repository.discoveredDevices
    val savedDevices: StateFlow<List<TvDevice>> = repository.savedDevices
    val installedApps: StateFlow<List<TvApp>> = repository.installedApps

    init {
        repository.startDiscovery()
    }

    fun startDiscovery() {
        repository.startDiscovery()
    }

    fun connectToDevice(device: TvDevice) {
        repository.connectToDevice(device)
    }

    fun connectByIp(ip: String) {
        repository.connectByIp(ip)
    }

    fun forceRePair(device: TvDevice? = null) {
        repository.forceRePair(device)
    }

    fun sendPin(pinCode: String) {
        repository.sendPin(pinCode)
    }

    fun powerOff() {
        repository.powerOff()
    }

    fun sendWakeOnLan() {
        repository.sendWakeOnLan()
    }

    fun turnOffScreen() {
        repository.turnOffScreen()
    }

    fun sendPointerButton(buttonName: String) {
        repository.sendPointerButton(buttonName)
    }

    fun sendSsapCommand(uri: String, payload: JSONObject? = null) {
        repository.sendCommand(uri, payload)
    }

    fun launchApp(appId: String) {
        repository.launchApp(appId)
    }

    fun deepLinkSettings(target: String) {
        repository.deepLinkSettings(target)
    }
}
