package com.example.lg_remote_app.model

data class TvDevice(
    val ipAddress: String,
    val name: String = "[LG] webOS TV",
    val macAddress: String? = null,
    val clientKey: String? = null,
    val isDiscovered: Boolean = true
)
