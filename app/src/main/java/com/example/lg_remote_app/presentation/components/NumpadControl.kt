package com.example.lg_remote_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
fun NumpadControl(
    onNumberClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val grid = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (row in grid) {
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (digit in row) {
                    NumpadButton(digit = digit) {
                        performHapticFeedback(context)
                        onNumberClick(digit)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NumpadButton(digit = 0) {
                performHapticFeedback(context)
                onNumberClick(0)
            }
        }
    }
}

@Composable
private fun NumpadButton(
    digit: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(DarkCardSurface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit.toString(),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
