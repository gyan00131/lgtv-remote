package com.example.lg_remote_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lg_remote_app.ui.components.performHapticFeedback
import com.example.lg_remote_app.ui.theme.DarkCardSurface
import com.example.lg_remote_app.ui.theme.PinkAccent
import com.example.lg_remote_app.ui.theme.TextPrimary
import com.example.lg_remote_app.ui.theme.TextSecondary

@Composable
fun TouchpadControl(
    onMovePointer: (dx: Int, dy: Int) -> Unit,
    onClickPointer: () -> Unit,
    onScrollPointer: (dx: Int, dy: Int) -> Unit,
    onStartAirMouse: () -> Unit,
    onStopAirMouse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isAirMouseActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Trackpad Touch Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(DarkCardSurface)
                .pointerInput(Unit) {
                    detectTapGestures {
                        performHapticFeedback(context)
                        onClickPointer()
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val dx = dragAmount.x.toInt()
                        val dy = dragAmount.y.toInt()
                        onMovePointer(dx, dy)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Mouse,
                    contentDescription = null,
                    tint = Color(0xFF888899),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Drag to Move Cursor", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tap anywhere to Click", color = TextSecondary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Air Mouse Hold Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(if (isAirMouseActive) PinkAccent else Color(0xFF2A2A38))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            performHapticFeedback(context)
                            isAirMouseActive = true
                            onStartAirMouse()
                            try {
                                awaitRelease()
                            } finally {
                                isAirMouseActive = false
                                onStopAirMouse()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Adjust,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isAirMouseActive) "Air Mouse Active (Move Phone)" else "Hold for Air Mouse",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
