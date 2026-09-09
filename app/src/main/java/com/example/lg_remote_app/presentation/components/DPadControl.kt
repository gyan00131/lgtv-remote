package com.example.lg_remote_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
fun DPadControl(
    onDirectionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(240.dp)
            .clip(CircleShape)
            .background(DarkCardSurface)
            .border(1.5.dp, Color(0xFF212F47), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Up
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(70.dp, 60.dp)
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .clickable {
                    performHapticFeedback(context)
                    onDirectionClick("UP")
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Up",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Down
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(70.dp, 60.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .clickable {
                    performHapticFeedback(context)
                    onDirectionClick("DOWN")
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Down",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Left
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(60.dp, 70.dp)
                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                .clickable {
                    performHapticFeedback(context)
                    onDirectionClick("LEFT")
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Left",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Right
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(60.dp, 70.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .clickable {
                    performHapticFeedback(context)
                    onDirectionClick("RIGHT")
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Right",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Center OK Button matching Image 2
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
                .border(1.5.dp, Color(0xFF2D3D58), CircleShape)
                .clickable {
                    performHapticFeedback(context)
                    onDirectionClick("ENTER")
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "OK",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
