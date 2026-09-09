package com.example.lg_remote_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import com.example.lg_remote_app.data.model.TvInputSource
import com.example.lg_remote_app.ui.components.performHapticFeedback
import com.example.lg_remote_app.ui.theme.DarkCardSurface
import com.example.lg_remote_app.ui.theme.PinkAccent
import com.example.lg_remote_app.ui.theme.TextPrimary

@Composable
fun MediaAndInputControl(
    inputs: List<TvInputSource>,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onRewind: () -> Unit,
    onFastForward: () -> Unit,
    onInputSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val displayInputs = if (inputs.isEmpty()) {
        listOf(
            TvInputSource("HDMI_1", "HDMI 1"),
            TvInputSource("HDMI_2", "HDMI 2"),
            TvInputSource("HDMI_3", "HDMI 3"),
            TvInputSource("TV", "Live TV")
        )
    } else inputs

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Media Controls", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

        Spacer(modifier = Modifier.height(12.dp))

        // Playback Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkCardSurface)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediaButton(icon = Icons.Default.FastRewind, contentDescription = "Rewind") {
                performHapticFeedback(context)
                onRewind()
            }
            MediaButton(icon = Icons.Default.PlayArrow, contentDescription = "Play") {
                performHapticFeedback(context)
                onPlay()
            }
            MediaButton(icon = Icons.Default.Pause, contentDescription = "Pause") {
                performHapticFeedback(context)
                onPause()
            }
            MediaButton(icon = Icons.Default.Stop, contentDescription = "Stop") {
                performHapticFeedback(context)
                onStop()
            }
            MediaButton(icon = Icons.Default.FastForward, contentDescription = "Fast Forward") {
                performHapticFeedback(context)
                onFastForward()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("External Inputs", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().height(160.dp)
        ) {
            items(displayInputs) { input ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkCardSurface)
                        .clickable {
                            performHapticFeedback(context)
                            onInputSelect(input.id)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.Input, contentDescription = null, tint = PinkAccent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = input.label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MediaButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF2A2A38))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}
