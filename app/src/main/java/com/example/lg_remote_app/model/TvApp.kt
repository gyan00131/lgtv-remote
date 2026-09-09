package com.example.lg_remote_app.model

data class TvApp(
    val id: String,
    val title: String,
    val iconUrl: String? = null,
    val bgImage: String? = null,
    val isSystemApp: Boolean = false
)

data class TvChannel(
    val id: String,
    val number: String,
    val name: String,
    val iconUrl: String? = null
)
