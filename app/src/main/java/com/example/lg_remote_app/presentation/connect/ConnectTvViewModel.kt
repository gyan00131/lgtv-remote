package com.example.lg_remote_app.presentation.connect

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.lg_remote_app.data.model.LgTvDevice
import com.example.lg_remote_app.data.model.TvInputSource
import com.example.lg_remote_app.data.repository.TvConnectionRepositoryImpl
import com.example.lg_remote_app.data.sensor.AirMouseSensorManager
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

    private val airMouseSensorManager = AirMouseSensorManager(application) { dx, dy ->
        movePointer(dx, dy)
    }

    val connectionState: StateFlow<TvConnectionState> = repository.connectionState
    val discoveredDevices: StateFlow<List<LgTvDevice>> = repository.discoveredDevices
    val pairedDevices: StateFlow<List<LgTvDevice>> = repository.pairedDevices
    val externalInputs: StateFlow<List<TvInputSource>> = repository.externalInputs

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

    fun play() = repository.play()
    fun pause() = repository.pause()
    fun stop() = repository.stop()
    fun rewind() = repository.rewind()
    fun fastForward() = repository.fastForward()
    fun sendNumber(digit: Int) = repository.sendNumber(digit)
    fun fetchExternalInputs() = repository.fetchExternalInputs()
    fun switchInput(inputId: String) = repository.switchInput(inputId)

    fun movePointer(dx: Int, dy: Int) = repository.movePointer(dx, dy)
    fun clickPointer() = repository.clickPointer()
    fun scrollPointer(dx: Int, dy: Int) = repository.scrollPointer(dx, dy)

    fun startAirMouse() = airMouseSensorManager.start()
    fun stopAirMouse() = airMouseSensorManager.stop()

    override fun onCleared() {
        super.onCleared()
        airMouseSensorManager.stop()
    }
}
