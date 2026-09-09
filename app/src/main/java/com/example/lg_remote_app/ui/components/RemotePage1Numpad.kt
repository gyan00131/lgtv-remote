package com.example.lg_remote_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
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
import com.example.lg_remote_app.ui.theme.DarkCardSurface
import com.example.lg_remote_app.ui.theme.TextPrimary

@Composable
fun RemotePage1Numpad(
    onNumberClick: (Int) -> Unit,
    onLiveClick: () -> Unit,
    onGuideClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumpadButton(text = "1", onClick = { onNumberClick(1) }, modifier = Modifier.weight(1f))
            NumpadButton(text = "2", onClick = { onNumberClick(2) }, modifier = Modifier.weight(1f))
            NumpadButton(text = "3", onClick = { onNumberClick(3) }, modifier = Modifier.weight(1f))
            NumpadButton(text = "4", onClick = { onNumberClick(4) }, modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumpadButton(text = "5", onClick = { onNumberClick(5) }, modifier = Modifier.weight(1f))
            NumpadButton(text = "6", onClick = { onNumberClick(6) }, modifier = Modifier.weight(1f))
            NumpadButton(text = "7", onClick = { onNumberClick(7) }, modifier = Modifier.weight(1f))
            NumpadButton(text = "8", onClick = { onNumberClick(8) }, modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkCardSurface)
                    .clickable {
                        performHapticFeedback(context)
                        onLiveClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = "LIVE",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "LIVE",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            NumpadButton(text = "9", onClick = { onNumberClick(9) }, modifier = Modifier.weight(1f))
            NumpadButton(text = "0", onClick = { onNumberClick(0) }, modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkCardSurface)
                    .clickable {
                        performHapticFeedback(context)
                        onGuideClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Guide",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NumpadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(DarkCardSurface)
            .clickable {
                performHapticFeedback(context)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
