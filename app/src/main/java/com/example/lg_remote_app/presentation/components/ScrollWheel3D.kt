package com.example.lg_remote_app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lg_remote_app.ui.components.performStrongHapticFeedback
import kotlin.math.abs

@Composable
fun ScrollWheel3D(
    onScroll: (dy: Int) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 150.dp,
    height: Dp = 220.dp,
    bezelColor: Color = Color(0xFF3897F0),
    scrollThrottleMs: Long = 1400L
) {
    val context = LocalContext.current
    var scrollOffset by remember { mutableFloatStateOf(0f) }
    var lastScrollTimestamp by remember { mutableLongStateOf(0L) }

    // Outer Blue Bezel Frame
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .background(bezelColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner Black Cavity Slot
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF090A0F))
                .pointerInput(Unit) {
                    var accumulatedY = 0f
                    detectDragGestures(
                        onDragStart = { accumulatedY = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val deltaY = dragAmount.y
                            // Smooth continuous visual offset for 60fps rolling
                            scrollOffset += deltaY
                            accumulatedY += deltaY

                            // Threshold for triggering scroll command
                            if (abs(accumulatedY) >= 10f) {
                                val currentTime = System.currentTimeMillis()
                                if ((currentTime - lastScrollTimestamp) >= scrollThrottleMs) {
                                    lastScrollTimestamp = currentTime
                                    performStrongHapticFeedback(context, durationMs = 150L)

                                    // Proportional smooth scroll delta based on drag distance
                                    val dy = (accumulatedY * 0.6f).toInt().let {
                                        if (it != 0) it.coerceIn(-25, 25)
                                        else if (accumulatedY > 0) 10 else -10
                                    }
                                    onScroll(dy)
                                }
                                accumulatedY = 0f
                            }
                        },
                        onDragEnd = {
                            accumulatedY = 0f
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // 3D Wheel Canvas Rendering
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight / 2f

                // 1. Draw 3D Barrel Wheel Cylinder (Wider proportion)
                val maxWheelWidth = canvasWidth * 0.92f
                val steps = 40
                val stepHeight = canvasHeight / steps

                val wheelPath = Path()
                // Left curve (top to bottom)
                for (i in 0..steps) {
                    val y = i * stepHeight
                    val normY = (y - centerY) / (canvasHeight / 2f)
                    val currentWidth = maxWheelWidth * (1f - 0.12f * (normY * normY))
                    val xLeft = centerX - (currentWidth / 2f)
                    if (i == 0) wheelPath.moveTo(xLeft, y) else wheelPath.lineTo(xLeft, y)
                }
                // Right curve (bottom to top)
                for (i in steps downTo 0) {
                    val y = i * stepHeight
                    val normY = (y - centerY) / (canvasHeight / 2f)
                    val currentWidth = maxWheelWidth * (1f - 0.12f * (normY * normY))
                    val xRight = centerX + (currentWidth / 2f)
                    wheelPath.lineTo(xRight, y)
                }
                wheelPath.close()

                // Fill Wheel Base Body with horizontal metallic shading
                drawPath(
                    path = wheelPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF121318),
                            Color(0xFF2C2E3B),
                            Color(0xFF4A4D60),
                            Color(0xFF2C2E3B),
                            Color(0xFF121318)
                        )
                    )
                )

                // 2. Draw Fine Horizontal Textured Ridges/Ribs with smooth 3D motion
                val ribSpacing = 10f
                val totalRibs = (canvasHeight / ribSpacing).toInt() + 4
                val startY = (scrollOffset % ribSpacing) - ribSpacing

                for (r in -2..totalRibs) {
                    val y = startY + (r * ribSpacing)
                    if (y in 0f..canvasHeight) {
                        val normY = (y - centerY) / (canvasHeight / 2f)
                        val currentWidth = maxWheelWidth * (1f - 0.12f * (normY * normY))
                        val xLeft = centerX - (currentWidth / 2f)
                        val xRight = centerX + (currentWidth / 2f)

                        // Dark groove
                        drawLine(
                            color = Color(0xFF090A0D),
                            start = Offset(xLeft, y),
                            end = Offset(xRight, y),
                            strokeWidth = 3.2f
                        )
                        // Metallic highlight ridge
                        drawLine(
                            color = Color(0xFF7A7E94).copy(alpha = (1f - 0.45f * abs(normY)).coerceIn(0f, 1f)),
                            start = Offset(xLeft, y + 2f),
                            end = Offset(xRight, y + 2f),
                            strokeWidth = 2f
                        )
                    }
                }

                // 3. Top and Bottom Shadow Overlay for 3D Recessed Cavity Effect
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.88f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.88f)
                        )
                    ),
                    size = size
                )

                // 4. Specular Highlight band across center
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        startY = centerY - 25f,
                        endY = centerY + 25f
                    ),
                    topLeft = Offset(0f, centerY - 25f),
                    size = Size(canvasWidth, 50f)
                )
            }
        }
    }
}
