package com.example.lg_remote_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.TvOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lg_remote_app.ui.theme.DarkBackground
import com.example.lg_remote_app.ui.theme.DarkCardSurface
import com.example.lg_remote_app.ui.theme.TextPrimary

@Composable
fun DPadControl(
    onDirectionClick: (String) -> Unit,
    onScreenOffClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 24.dp, y = 12.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkCardSurface)
                .clickable {
                    performHapticFeedback(context)
                    onScreenOffClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TvOff,
                contentDescription = "Turn Off Screen",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(CircleShape)
                .background(DarkCardSurface)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 120.dp, height = 75.dp)
                    .clickable {
                        performHapticFeedback(context)
                        onDirectionClick("UP")
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropUp,
                    contentDescription = "UP",
                    tint = TextPrimary,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .size(36.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = 120.dp, height = 75.dp)
                    .clickable {
                        performHapticFeedback(context)
                        onDirectionClick("DOWN")
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "DOWN",
                    tint = TextPrimary,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .size(36.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(width = 75.dp, height = 120.dp)
                    .clickable {
                        performHapticFeedback(context)
                        onDirectionClick("LEFT")
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                    contentDescription = "LEFT",
                    tint = TextPrimary,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(36.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(width = 75.dp, height = 120.dp)
                    .clickable {
                        performHapticFeedback(context)
                        onDirectionClick("RIGHT")
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = "RIGHT",
                    tint = TextPrimary,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(DarkBackground)
                    .clickable {
                        performHapticFeedback(context)
                        onDirectionClick("ENTER")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OK",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
