package com.example.lg_remote_app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.lg_remote_app.ui.components.DPadControl
import com.example.lg_remote_app.ui.components.PageIndicator
import com.example.lg_remote_app.ui.components.RemotePage1Numpad
import com.example.lg_remote_app.ui.components.RemotePage2Controls
import com.example.lg_remote_app.ui.components.RemotePage3Shortcuts
import com.example.lg_remote_app.ui.components.SystemNavBar
import com.example.lg_remote_app.ui.theme.DarkBackground
import com.example.lg_remote_app.ui.viewmodel.TvViewModel
import org.json.JSONObject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteScreen(
    viewModel: TvViewModel,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> RemotePage1Numpad(
                    onNumberClick = { num ->
                        viewModel.sendPointerButton(num.toString())
                    },
                    onLiveClick = {
                        viewModel.sendSsapCommand("ssap://tv/openLiveTV")
                    },
                    onGuideClick = {
                        viewModel.sendSsapCommand("ssap://tv/openChannelGuide")
                    }
                )
                1 -> RemotePage2Controls(
                    onChannelUp = { viewModel.sendSsapCommand("ssap://tv/channelUp") },
                    onChannelDown = { viewModel.sendSsapCommand("ssap://tv/channelDown") },
                    onVolumeUp = { viewModel.sendSsapCommand("ssap://audio/volumeUp") },
                    onVolumeDown = { viewModel.sendSsapCommand("ssap://audio/volumeDown") },
                    onHeadphonesClick = { viewModel.deepLinkSettings("sound") },
                    // Mute via pointer button is more reliable than SSAP setMute without state
                    onMuteClick = { viewModel.sendPointerButton("MUTE") },
                    onSettingsClick = { viewModel.deepLinkSettings("picture") },
                    onInputClick = { viewModel.sendSsapCommand("ssap://tv/switchInput") },
                    onKeyboardClick = { viewModel.sendSsapCommand("ssap://com.webos.service.ime/showIME") },
                    onDropClick = { viewModel.turnOffScreen() }
                )
                2 -> RemotePage3Shortcuts(
                    // Use ssap://system.launcher/launch (not open) for voice search
                    onSearchClick = { viewModel.sendSsapCommand("ssap://system.launcher/launch", JSONObject().put("id", "com.webos.app.voice")) },
                    onWebClick = { viewModel.launchApp("com.webos.app.browser") },
                    onListClick = { viewModel.sendSsapCommand("ssap://tv/openChannelGuide") },
                    onOptClick = { viewModel.deepLinkSettings("general") },
                    onRewindClick = { viewModel.sendPointerButton("REWIND") },
                    onPauseClick = { viewModel.sendPointerButton("PAUSE") },
                    onPlayClick = { viewModel.sendPointerButton("PLAY") },
                    onFastForwardClick = { viewModel.sendPointerButton("FASTFORWARD") },
                    onYouTubeClick = { viewModel.launchApp("youtube.leanback.v4") },
                    onNetflixClick = { viewModel.launchApp("netflix") }
                )
            }
        }

        PageIndicator(
            pageCount = 3,
            currentPage = pagerState.currentPage
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            DPadControl(
                onDirectionClick = { direction ->
                    viewModel.sendPointerButton(direction)
                },
                onScreenOffClick = {
                    viewModel.turnOffScreen()
                }
            )
        }

        SystemNavBar(
            onBackClick = { viewModel.sendPointerButton("BACK") },
            onHomeClick = { viewModel.sendPointerButton("HOME") }
        )
    }
}
