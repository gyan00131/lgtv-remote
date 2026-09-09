package com.example.lg_remote_app.data.model

data class LgTvDevice(
    val id: String,
    val name: String = "[LG] webOS TV",
    val ipAddress: String,
    val port: Int = 3000,
    val macAddress: String? = null,
    val modelName: String? = null,
    val isConnected: Boolean = false,
    val clientKey: String? = null,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)
