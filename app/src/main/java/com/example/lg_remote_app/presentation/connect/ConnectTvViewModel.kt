package com.example.lg_remote_app.presentation.connect

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.lg_remote_app.data.model.LgTvDevice
import com.example.lg_remote_app.data.repository.TvConnectionRepositoryImpl
import com.example.lg_remote_app.domain.model.TvConnectionState
import com.example.lg_remote_app.domain.repository.TvConnectionRepository
import kotlinx.coroutines.flow.StateFlow

class ConnectTvViewModel(
    application: Application,
    private val repository: TvConnectionRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        repository = TvConnectionRepositoryImpl(application)
    )

    val connectionState: StateFlow<TvConnectionState> = repository.connectionState
    val discoveredDevices: StateFlow<List<LgTvDevice>> = repository.discoveredDevices
    val pairedDevices: StateFlow<List<LgTvDevice>> = repository.pairedDevices

    init {
        startDiscovery()
    }

    fun startDiscovery() {
        repository.startDiscovery()
    }

    fun connectToDevice(device: LgTvDevice) {
        repository.connectToDevice(device)
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun sendPin(pin: String) {
        repository.sendPin(pin)
    }

    fun forgetPairedTv(device: LgTvDevice) {
        repository.forgetPairedTv(device)
    }

    fun powerOff() = repository.powerOff()
    fun volumeUp() = repository.volumeUp()
    fun volumeDown() = repository.volumeDown()
    fun toggleMute() = repository.toggleMute()
    fun channelUp() = repository.channelUp()
    fun channelDown() = repository.channelDown()
    fun sendHome() = repository.sendHome()
    fun sendBack() = repository.sendBack()
    fun sendPointerButton(buttonName: String) = repository.sendPointerButton(buttonName)
}
