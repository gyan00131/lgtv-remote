package com.example.lg_remote_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lg_remote_app.domain.model.TvConnectionState

@Composable
fun ConnectionStatusBanner(
    connectionState: TvConnectionState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor, showProgress) = when (connectionState) {
        is TvConnectionState.Disconnected -> Triple("Disconnected", Color(0xFF888899), false)
        is TvConnectionState.Discovering -> Triple("Searching for LG TVs...", Color(0xFFE91E63), true)
        is TvConnectionState.Connecting -> Triple("Connecting to ${connectionState.device.name}...", Color(0xFF2196F3), true)
        is TvConnectionState.WaitingForPairing -> Triple("Please accept prompt on TV screen", Color(0xFFFF9800), true)
        is TvConnectionState.Connected -> Triple("Connected to ${connectionState.device.name}", Color(0xFF4CAF50), false)
        is TvConnectionState.Reconnecting -> Triple("Reconnecting to ${connectionState.device.name}...", Color(0xFFFF9800), true)
        is TvConnectionState.ConnectionFailed -> Triple("Connection Failed: ${connectionState.errorMessage}", Color(0xFFF44336), false)
        is TvConnectionState.PairingRequired -> Triple("Pairing required with ${connectionState.device.name}", Color(0xFFFF9800), false)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E28))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showProgress) {
                CircularProgressIndicator(
                    color = statusColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = statusText,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
