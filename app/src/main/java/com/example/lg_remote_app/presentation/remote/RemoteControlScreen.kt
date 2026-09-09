package com.example.lg_remote_app.presentation.remote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lg_remote_app.data.model.LgTvDevice
import com.example.lg_remote_app.domain.model.TvConnectionState
import com.example.lg_remote_app.presentation.components.DPadControl
import com.example.lg_remote_app.presentation.components.MediaAndInputControl
import com.example.lg_remote_app.presentation.components.NumpadControl
import com.example.lg_remote_app.presentation.components.TouchpadControl
import com.example.lg_remote_app.presentation.components.VolumeChannelControl
import com.example.lg_remote_app.presentation.connect.ConnectTvViewModel
import com.example.lg_remote_app.ui.components.performHapticFeedback
import com.example.lg_remote_app.ui.theme.DarkBackground
import com.example.lg_remote_app.ui.theme.DarkCardSurface
import com.example.lg_remote_app.ui.theme.PinkAccent
import com.example.lg_remote_app.ui.theme.PowerRed
import com.example.lg_remote_app.ui.theme.TextPrimary
import com.example.lg_remote_app.ui.theme.TextSecondary

enum class RemoteTab {
    CONTROLS,
    TOUCHPAD,
    NUMPAD,
    MEDIA
}

@Composable
fun RemoteControlScreen(
    viewModel: ConnectTvViewModel,
    onSwitchTvClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val connectionState by viewModel.connectionState.collectAsState()
    val externalInputs by viewModel.externalInputs.collectAsState()

    var selectedTab by remember { mutableStateOf(RemoteTab.CONTROLS) }

    val isConnected = connectionState is TvConnectionState.Connected
    val connectedDevice = when (connectionState) {
        is TvConnectionState.Connected -> (connectionState as TvConnectionState.Connected).device
        else -> LgTvDevice(id = "", name = "LG webOS TV", ipAddress = "192.168.1.33")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header matching Image 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Power Off Button (Left Circular)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C1418))
                    .border(1.5.dp, PowerRed.copy(alpha = 0.5f), CircleShape)
                    .clickable {
                        performHapticFeedback(context)
                        viewModel.powerOff()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power Off",
                    tint = PowerRed,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title: SMART REMOTE
            Text(
                text = "SMART REMOTE",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )

            // Tune/Settings Button (Right Circular)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E283C))
                    .border(1.5.dp, Color(0xFF2E3D5C), CircleShape)
                    .clickable {
                        performHapticFeedback(context)
                        onSwitchTvClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Connection Status Banner Pill matching Image 2
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(DarkCardSurface)
                .border(1.5.dp, Color(0xFF212F47), RoundedCornerShape(24.dp))
                .clickable {
                    performHapticFeedback(context)
                    onSwitchTvClick()
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Color(0xFF00E676) else PowerRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isConnected) "Connected to ${connectedDevice.name} (${connectedDevice.ipAddress})"
                           else "Could not connect to TV at ${connectedDevice.ipAddress.ifEmpty { "192.168.1.33" }}",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkCardSurface)
                .border(1.dp, Color(0xFF212F47), RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabButton("REMOTE", selectedTab == RemoteTab.CONTROLS) {
                performHapticFeedback(context)
                selectedTab = RemoteTab.CONTROLS
            }
            TabButton("TOUCHPAD", selectedTab == RemoteTab.TOUCHPAD) {
                performHapticFeedback(context)
                selectedTab = RemoteTab.TOUCHPAD
            }
            TabButton("NUMPAD", selectedTab == RemoteTab.NUMPAD) {
                performHapticFeedback(context)
                selectedTab = RemoteTab.NUMPAD
            }
            TabButton("MEDIA", selectedTab == RemoteTab.MEDIA) {
                performHapticFeedback(context)
                selectedTab = RemoteTab.MEDIA
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Content Area based on Selected Tab
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                RemoteTab.CONTROLS -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        DPadControl(
                            onDirectionClick = { button ->
                                viewModel.sendPointerButton(button)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(0.7f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(DarkCardSurface)
                                    .border(1.5.dp, Color(0xFF212F47), CircleShape)
                                    .clickable {
                                        performHapticFeedback(context)
                                        viewModel.sendBack()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(DarkCardSurface)
                                    .border(1.5.dp, Color(0xFF212F47), CircleShape)
                                    .clickable {
                                        performHapticFeedback(context)
                                        viewModel.sendHome()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        VolumeChannelControl(
                            onVolumeUp = { viewModel.volumeUp() },
                            onVolumeDown = { viewModel.volumeDown() },
                            onMuteToggle = { viewModel.toggleMute() },
                            onChannelUp = { viewModel.channelUp() },
                            onChannelDown = { viewModel.channelDown() }
                        )
                    }
                }

                RemoteTab.TOUCHPAD -> {
                    TouchpadControl(
                        onMovePointer = { dx, dy -> viewModel.movePointer(dx, dy) },
                        onClickPointer = { viewModel.clickPointer() },
                        onScrollPointer = { dx, dy -> viewModel.scrollPointer(dx, dy) },
                        onStartAirMouse = { viewModel.startAirMouse() },
                        onStopAirMouse = { viewModel.stopAirMouse() },
                        onBackClick = { viewModel.sendBack() }
                    )
                }

                RemoteTab.NUMPAD -> {
                    NumpadControl(
                        onNumberClick = { digit ->
                            viewModel.sendNumber(digit)
                        }
                    )
                }

                RemoteTab.MEDIA -> {
                    MediaAndInputControl(
                        inputs = externalInputs,
                        onPlay = { viewModel.play() },
                        onPause = { viewModel.pause() },
                        onStop = { viewModel.stop() },
                        onRewind = { viewModel.rewind() },
                        onFastForward = { viewModel.fastForward() },
                        onInputSelect = { inputId -> viewModel.switchInput(inputId) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PinkAccent else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
