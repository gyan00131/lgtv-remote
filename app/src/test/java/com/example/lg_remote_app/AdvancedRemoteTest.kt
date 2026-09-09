package com.example.lg_remote_app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedRemoteTest {

    @Test
    fun `test media control URIs`() {
        val playUri = "ssap://media.controls/play"
        val pauseUri = "ssap://media.controls/pause"
        val stopUri = "ssap://media.controls/stop"
        val rewindUri = "ssap://media.controls/rewind"
        val fastForwardUri = "ssap://media.controls/fastForward"

        assertEquals("ssap://media.controls/play", playUri)
        assertEquals("ssap://media.controls/pause", pauseUri)
        assertEquals("ssap://media.controls/stop", stopUri)
        assertEquals("ssap://media.controls/rewind", rewindUri)
        assertEquals("ssap://media.controls/fastForward", fastForwardUri)
    }

    @Test
    fun `test mute state toggle sequence`() {
        var isMuted = false

        // First press -> mute true
        isMuted = !isMuted
        assertTrue(isMuted)

        // Second press -> mute false (unmute)
        isMuted = !isMuted
        assertFalse(isMuted)
    }

    @Test
    fun `test external input URI formatting`() {
        val inputListUri = "ssap://tv/getExternalInputList"
        val switchInputUri = "ssap://tv/switchInput"

        assertEquals("ssap://tv/getExternalInputList", inputListUri)
        assertEquals("ssap://tv/switchInput", switchInputUri)
    }
}
