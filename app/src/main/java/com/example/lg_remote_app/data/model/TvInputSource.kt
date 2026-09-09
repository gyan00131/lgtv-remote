package com.example.lg_remote_app.data.model

data class TvInputSource(
    val id: String,
    val label: String,
    val icon: String? = null,
    val isConnected: Boolean = true
)
