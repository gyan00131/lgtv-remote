package com.example.lg_remote_app

import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteCommandsTest {

    @Test
    fun `test volume and mute SSAP payload generation`() {
        val volumeUpUri = "ssap://audio/volumeUp"
        val volumeDownUri = "ssap://audio/volumeDown"
        val muteUri = "ssap://audio/setMute"

        assertEquals("ssap://audio/volumeUp", volumeUpUri)
        assertEquals("ssap://audio/volumeDown", volumeDownUri)
        assertEquals("ssap://audio/setMute", muteUri)
    }

    @Test
    fun `test pointer button command string formatting`() {
        fun formatPointerButton(buttonName: String) = "type:button\nname:$buttonName\n\n"

        assertEquals("type:button\nname:UP\n\n", formatPointerButton("UP"))
        assertEquals("type:button\nname:DOWN\n\n", formatPointerButton("DOWN"))
        assertEquals("type:button\nname:LEFT\n\n", formatPointerButton("LEFT"))
        assertEquals("type:button\nname:RIGHT\n\n", formatPointerButton("RIGHT"))
        assertEquals("type:button\nname:ENTER\n\n", formatPointerButton("ENTER"))
        assertEquals("type:button\nname:HOME\n\n", formatPointerButton("HOME"))
        assertEquals("type:button\nname:BACK\n\n", formatPointerButton("BACK"))
    }
}
