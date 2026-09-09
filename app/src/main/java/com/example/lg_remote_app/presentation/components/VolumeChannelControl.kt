package com.example.lg_remote_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lg_remote_app.ui.components.performHapticFeedback
import com.example.lg_remote_app.ui.theme.DarkCardSurface

@Composable
fun VolumeChannelControl(
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onMuteToggle: () -> Unit,
    onChannelUp: () -> Unit,
    onChannelDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Volume Rocker Capsule matching Image 2
        Column(
            modifier = Modifier
                .size(72.dp, 160.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(DarkCardSurface)
                .border(1.5.dp, Color(0xFF212F47), RoundedCornerShape(36.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp, 50.dp)
                    .clickable {
                        performHapticFeedback(context)
                        onVolumeUp()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Volume Up", tint = Color.White)
            }

            Box(
                modifier = Modifier
                    .size(72.dp, 40.dp)
                    .clickable {
                        performHapticFeedback(context)
                        onMuteToggle()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Mute", tint = Color(0xFF8C9BB4), modifier = Modifier.size(20.dp))
            }

            Box(
                modifier = Modifier
                    .size(72.dp, 50.dp)
                    .clickable {
                        performHapticFeedback(context)
                        onVolumeDown()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Volume Down", tint = Color.White)
            }
        }

        // Channel Rocker Capsule matching Image 2
        Column(
            modifier = Modifier
                .size(72.dp, 160.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(DarkCardSurface)
                .border(1.5.dp, Color(0xFF212F47), RoundedCornerShape(36.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp, 50.dp)
                    .clickable {
                        performHapticFeedback(context)
                        onChannelUp()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Channel Up", tint = Color.White)
            }

            Text("CH", color = Color(0xFF8C9BB4), fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .size(72.dp, 50.dp)
                    .clickable {
                        performHapticFeedback(context)
                        onChannelDown()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Channel Down", tint = Color.White)
            }
        }
    }
}
