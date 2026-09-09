package com.example.lg_remote_app.model

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    PAIRING,
    CONNECTED,
    ERROR
}

data class ConnectionState(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val currentDevice: TvDevice? = null,
    val errorMessage: String? = null,
    val showPairingDialog: Boolean = false,
    val showPinDialog: Boolean = false
)
