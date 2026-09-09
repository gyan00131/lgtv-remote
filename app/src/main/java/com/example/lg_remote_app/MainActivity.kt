package com.example.lg_remote_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lg_remote_app.domain.model.TvConnectionState
import com.example.lg_remote_app.presentation.connect.ConnectTvScreen
import com.example.lg_remote_app.presentation.connect.ConnectTvViewModel
import com.example.lg_remote_app.presentation.remote.RemoteControlScreen
import com.example.lg_remote_app.presentation.splash.SplashScreen
import com.example.lg_remote_app.ui.theme.LgremoteappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LgremoteappTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    MainAppContent()
                }
            }
        }
    }
}

@Composable
fun MainAppContent(connectViewModel: ConnectTvViewModel = viewModel()) {
    val connectionState by connectViewModel.connectionState.collectAsState()
    var showConnectScreenOverride by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (connectionState is TvConnectionState.Connected && !showConnectScreenOverride) {
            RemoteControlScreen(
                viewModel = connectViewModel,
                onSwitchTvClick = { showConnectScreenOverride = true },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            ConnectTvScreen(
                viewModel = connectViewModel,
                onReturnToRemote = { showConnectScreenOverride = false },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
