package com.example.lg_remote_app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lg_remote_app.ui.components.performHapticFeedback
import com.example.lg_remote_app.ui.theme.DarkBackground
import com.example.lg_remote_app.ui.theme.DarkCardSurface
import com.example.lg_remote_app.ui.theme.PinkAccent
import com.example.lg_remote_app.ui.theme.TextPrimary
import com.example.lg_remote_app.ui.theme.TextSecondary
import com.example.lg_remote_app.ui.viewmodel.TvViewModel

@Composable
fun SettingsScreen(
    viewModel: TvViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "TV Display Settings",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkCardSurface)
        ) {
            SettingsRow(
                icon = Icons.Default.Image,
                title = "Picture",
                onClick = {
                    performHapticFeedback(context)
                    viewModel.deepLinkSettings("picture")
                }
            )
            SettingsRow(
                icon = Icons.Default.SportsEsports,
                title = "Gaming",
                onClick = {
                    performHapticFeedback(context)
                    viewModel.deepLinkSettings("game")
                }
            )
            SettingsRow(
                icon = Icons.Default.Settings,
                title = "Advanced",
                onClick = {
                    performHapticFeedback(context)
                    viewModel.deepLinkSettings("advanced")
                }
            )
            SettingsRow(
                icon = Icons.Default.CenterFocusWeak,
                title = "Calibration",
                onClick = {
                    performHapticFeedback(context)
                    viewModel.deepLinkSettings("calibration")
                }
            )
            SettingsRow(
                icon = Icons.Default.AspectRatio,
                title = "Aspect Ratio",
                onClick = {
                    performHapticFeedback(context)
                    viewModel.deepLinkSettings("aspectRatio")
                }
            )
            SettingsRow(
                icon = Icons.Default.Memory,
                title = "AI",
                onClick = {
                    performHapticFeedback(context)
                    viewModel.deepLinkSettings("ai")
                },
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Sound Settings",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkCardSurface)
        ) {
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Sound Settings",
                onClick = {
                    performHapticFeedback(context)
                    viewModel.deepLinkSettings("sound")
                },
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "TV Settings",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkCardSurface)
        ) {
            SettingsRow(
                icon = Icons.Default.Tune,
                title = "TV Settings",
                onClick = {
                    performHapticFeedback(context)
                    viewModel.deepLinkSettings("general")
                },
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PinkAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(DarkBackground.copy(alpha = 0.5f))
            )
        }
    }
}
