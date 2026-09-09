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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.lg_remote_app.ui.theme.DarkCardSurface
import com.example.lg_remote_app.ui.theme.NetflixRed
import com.example.lg_remote_app.ui.theme.TextPrimary

@Composable
fun RemotePage3Shortcuts(
    onSearchClick: () -> Unit,
    onWebClick: () -> Unit,
    onListClick: () -> Unit,
    onOptClick: () -> Unit,
    onRewindClick: () -> Unit,
    onPauseClick: () -> Unit,
    onPlayClick: () -> Unit,
    onFastForwardClick: () -> Unit,
    onYouTubeClick: () -> Unit,
    onNetflixClick: () -> Unit
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
            ShortcutTextButton(text = "Search", onClick = onSearchClick, modifier = Modifier.weight(1f))
            ShortcutTextButton(text = "Web", onClick = onWebClick, modifier = Modifier.weight(1f))
            ShortcutTextButton(text = "List", onClick = onListClick, modifier = Modifier.weight(1f))
            ShortcutTextButton(text = "OPT", onClick = onOptClick, modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TransportIconButton(
                icon = Icons.Default.FastRewind,
                description = "Rewind",
                onClick = onRewindClick,
                modifier = Modifier.weight(1f)
            )
            TransportIconButton(
                icon = Icons.Default.Pause,
                description = "Pause",
                onClick = onPauseClick,
                modifier = Modifier.weight(1f)
            )
            TransportIconButton(
                icon = Icons.Default.PlayArrow,
                description = "Play",
                onClick = onPlayClick,
                modifier = Modifier.weight(1f)
            )
            TransportIconButton(
                icon = Icons.Default.FastForward,
                description = "Fast Forward",
                onClick = onFastForwardClick,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(DarkCardSurface)
                    .clickable {
                        performHapticFeedback(context)
                        onYouTubeClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "You",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Red)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Tube",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(DarkCardSurface)
                    .clickable {
                        performHapticFeedback(context)
                        onNetflixClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NETFLIX",
                    color = NetflixRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ShortcutTextButton(
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
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TransportIconButton(
    icon: ImageVector,
    description: String,
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
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}
