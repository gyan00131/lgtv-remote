package com.example.lg_remote_app.presentation.connect

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lg_remote_app.data.model.LgTvDevice
import com.example.lg_remote_app.domain.model.TvConnectionState
import com.example.lg_remote_app.presentation.components.ConnectionStatusBanner
import com.example.lg_remote_app.presentation.components.TvDeviceCard
import com.example.lg_remote_app.ui.theme.DarkBackground
import com.example.lg_remote_app.ui.theme.DarkCardSurface
import com.example.lg_remote_app.ui.theme.PinkAccent
import com.example.lg_remote_app.ui.theme.TextPrimary
import com.example.lg_remote_app.ui.theme.TextSecondary

@Composable
fun ConnectTvScreen(
    viewModel: ConnectTvViewModel,
    onReturnToRemote: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()

    var selectedDeviceForModal by remember { mutableStateOf<LgTvDevice?>(null) }
    var showWifiHelpDialog by remember { mutableStateOf(false) }

    val allDevices = (discoveredDevices + pairedDevices).distinctBy { it.ipAddress }

    val connectedDeviceIp = when (connectionState) {
        is TvConnectionState.Connected -> (connectionState as TvConnectionState.Connected).device.ipAddress
        is TvConnectionState.Connecting -> (connectionState as TvConnectionState.Connecting).device.ipAddress
        is TvConnectionState.WaitingForPairing -> (connectionState as TvConnectionState.WaitingForPairing).device.ipAddress
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Connect to Smart TV",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Devices on local Wi-Fi",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            if (connectionState is TvConnectionState.Connected && onReturnToRemote != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PinkAccent)
                        .clickable { onReturnToRemote() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Remote ->",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        ConnectionStatusBanner(connectionState = connectionState)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Available Devices",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (allDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = PinkAccent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Searching for TVs on Wi-Fi...", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allDevices) { device ->
                    val isSelected = device.ipAddress == connectedDeviceIp
                    TvDeviceCard(
                        device = device,
                        isSelected = isSelected,
                        onClick = {
                            selectedDeviceForModal = device
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.startDiscovery() },
            colors = ButtonDefaults.buttonColors(containerColor = PinkAccent),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Refresh", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showWifiHelpDialog = true },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Can't find your smart TV?", color = TextSecondary, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    val targetDev = selectedDeviceForModal
    if (targetDev != null) {
        AlertDialog(
            onDismissRequest = { selectedDeviceForModal = null },
            containerColor = DarkCardSurface,
            title = {
                Text("Connect Device", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Do you want to connect with ${targetDev.name} (${targetDev.ipAddress})?",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val dev = targetDev
                        selectedDeviceForModal = null
                        viewModel.connectToDevice(dev)
                    }
                ) {
                    Text("Connect", color = PinkAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDeviceForModal = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (connectionState is TvConnectionState.WaitingForPairing) {
        val pairingState = connectionState as TvConnectionState.WaitingForPairing
        if (pairingState.pairingType == "PIN") {
            var pinText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.disconnect() },
                containerColor = DarkCardSurface,
                title = { Text("Enter TV PIN Code", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Please enter the PIN code displayed on your LG TV screen:", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pinText,
                            onValueChange = { if (it.length <= 8) pinText = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PinkAccent,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (pinText.isNotBlank()) {
                                viewModel.sendPin(pinText)
                            }
                        }
                    ) {
                        Text("Submit PIN", color = PinkAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.disconnect() }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }

    if (showWifiHelpDialog) {
        AlertDialog(
            onDismissRequest = { showWifiHelpDialog = false },
            containerColor = DarkCardSurface,
            title = {
                Text("Same Wi-Fi Network Required", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("1. Ensure your LG webOS TV is powered on.", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("2. Connect your phone to the exact same Wi-Fi network as the TV.", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("3. Enable 'LG Connect Apps' in TV Network Settings.", color = TextSecondary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showWifiHelpDialog = false }) {
                    Text("Got It", color = PinkAccent)
                }
            }
        )
    }
}
