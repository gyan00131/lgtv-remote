package com.example.lg_remote_app.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lg_remote_app.ui.theme.DarkBackground
import com.example.lg_remote_app.ui.theme.PinkAccent
import com.example.lg_remote_app.ui.theme.PowerRed
import com.example.lg_remote_app.ui.theme.TextPrimary
import com.example.lg_remote_app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(2500)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .clickable { onSplashFinished() }
            .padding(top = 32.dp, bottom = 56.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TV Icon Box with Cyan Glowing Border matching reference image
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF131A2B))
                        .border(2.5.dp, PinkAccent, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = PinkAccent,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = PowerRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "LG WEBOS",
                    color = Color(0xFF8C9BB4),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SMART TV REMOTE",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                CircularProgressIndicator(
                    color = PinkAccent,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Initializing Local Wi-Fi Network...",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Developer Footer Details with extra bottom padding & margin
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Developed by",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Gyan Prakash Ranjan",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Mail: gyan00131@yahoo.com",
                    color = PinkAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "LinkedIn: linkedin.com/in/gyan00131/",
                    color = PinkAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Mobile: +91-8287087429",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
