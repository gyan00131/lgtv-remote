package com.example.lg_remote_app

import org.junit.Assert.assertEquals
import org.junit.Test

class TouchpadGestureTest {

    @Test
    fun `test pointer move formatting`() {
        fun formatMove(dx: Int, dy: Int) = "type:move\ndx:$dx\ndy:$dy\ndown:0\n\n"

        assertEquals("type:move\ndx:15\ndy:-20\ndown:0\n\n", formatMove(15, -20))
        assertEquals("type:move\ndx:0\ndy:0\ndown:0\n\n", formatMove(0, 0))
    }

    @Test
    fun `test pointer click formatting`() {
        val clickCommand = "type:click\n\n"
        assertEquals("type:click\n\n", clickCommand)
    }

    @Test
    fun `test pointer scroll formatting`() {
        fun formatScroll(dx: Int, dy: Int) = "type:scroll\ndx:$dx\ndy:$dy\n\n"

        assertEquals("type:scroll\ndx:0\ndy:-5\n\n", formatScroll(0, -5))
    }
}
