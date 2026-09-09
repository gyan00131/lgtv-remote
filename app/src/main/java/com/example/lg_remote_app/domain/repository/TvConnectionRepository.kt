package com.example.lg_remote_app.domain.repository

import com.example.lg_remote_app.data.model.LgTvDevice
import com.example.lg_remote_app.domain.model.TvConnectionState
import kotlinx.coroutines.flow.StateFlow

interface TvConnectionRepository {
    val connectionState: StateFlow<TvConnectionState>
    val discoveredDevices: StateFlow<List<LgTvDevice>>
    val pairedDevices: StateFlow<List<LgTvDevice>>

    fun startDiscovery()
    fun stopDiscovery()
    fun connectToDevice(device: LgTvDevice)
    fun disconnect()
    fun sendPin(pin: String)
    fun forgetPairedTv(device: LgTvDevice)
    fun getPairedTv(): LgTvDevice?

    fun powerOff()
    fun volumeUp()
    fun volumeDown()
    fun toggleMute()
    fun channelUp()
    fun channelDown()
    fun sendHome()
    fun sendBack()
    fun sendPointerButton(buttonName: String)
}
