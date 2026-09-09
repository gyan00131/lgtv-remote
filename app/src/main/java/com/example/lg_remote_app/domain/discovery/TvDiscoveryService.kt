package com.example.lg_remote_app.domain.discovery

import com.example.lg_remote_app.data.model.LgTvDevice
import kotlinx.coroutines.flow.Flow

interface TvDiscoveryService {
    fun discoverDevices(): Flow<LgTvDevice>
}
