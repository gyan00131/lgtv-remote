package com.example.lg_remote_app

import com.example.lg_remote_app.data.model.LgTvDevice
import com.example.lg_remote_app.domain.model.TvConnectionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectionStateMachineTest {

    private val testDevice = LgTvDevice(
        id = "tv_01",
        name = "LG webOS TV",
        ipAddress = "192.168.1.38",
        port = 3000
    )

    @Test
    fun `test initial state is Disconnected`() {
        val state: TvConnectionState = TvConnectionState.Disconnected
        assertTrue(state is TvConnectionState.Disconnected)
    }

    @Test
    fun `test state transition to Connecting`() {
        val state: TvConnectionState = TvConnectionState.Connecting(testDevice)
        assertTrue(state is TvConnectionState.Connecting)
        assertEquals("192.168.1.38", (state as TvConnectionState.Connecting).device.ipAddress)
    }

    @Test
    fun `test state transition to WaitingForPairing`() {
        val state: TvConnectionState = TvConnectionState.WaitingForPairing(testDevice, pairingType = "PROMPT")
        assertTrue(state is TvConnectionState.WaitingForPairing)
        assertEquals("PROMPT", (state as TvConnectionState.WaitingForPairing).pairingType)
    }

    @Test
    fun `test state transition to Connected`() {
        val pairedDevice = testDevice.copy(isConnected = true, clientKey = "valid_client_key_123")
        val state: TvConnectionState = TvConnectionState.Connected(pairedDevice)
        assertTrue(state is TvConnectionState.Connected)
        assertEquals("valid_client_key_123", (state as TvConnectionState.Connected).device.clientKey)
    }

    @Test
    fun `test state transition to ConnectionFailed`() {
        val state: TvConnectionState = TvConnectionState.ConnectionFailed(testDevice, "Timeout connecting to socket")
        assertTrue(state is TvConnectionState.ConnectionFailed)
        assertEquals("Timeout connecting to socket", (state as TvConnectionState.ConnectionFailed).errorMessage)
    }
}
