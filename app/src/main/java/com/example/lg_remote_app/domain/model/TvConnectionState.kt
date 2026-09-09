package com.example.lg_remote_app.domain.model

import com.example.lg_remote_app.data.model.LgTvDevice

sealed class TvConnectionState {
    data object Disconnected : TvConnectionState()
    data object Discovering : TvConnectionState()
    data class Connecting(val device: LgTvDevice) : TvConnectionState()
    data class WaitingForPairing(val device: LgTvDevice, val pairingType: String) : TvConnectionState()
    data class Connected(val device: LgTvDevice) : TvConnectionState()
    data class Reconnecting(val device: LgTvDevice, val attempt: Int = 1) : TvConnectionState()
    data class ConnectionFailed(val device: LgTvDevice?, val errorMessage: String) : TvConnectionState()
    data class PairingRequired(val device: LgTvDevice) : TvConnectionState()
}
