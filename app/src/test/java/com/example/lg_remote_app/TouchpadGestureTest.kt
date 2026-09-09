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

    @Test
    fun `test scroll throttle logic`() {
        var lastScrollTimestamp = 0L
        val throttleMs = 1400L
        var scrollCount = 0

        fun triggerScroll(currentTime: Long): Boolean {
            if (currentTime - lastScrollTimestamp >= throttleMs) {
                lastScrollTimestamp = currentTime
                scrollCount++
                return true
            }
            return false
        }

        // First scroll at t = 1400ms (diff from 0L is 1400ms -> succeeds)
        assertEquals(true, triggerScroll(1400L))
        assertEquals(1, scrollCount)

        // Second scroll attempt at t = 2000ms (600ms elapsed) -> should be throttled
        assertEquals(false, triggerScroll(2000L))
        assertEquals(1, scrollCount)

        // Third scroll attempt at t = 2799ms (1399ms elapsed) -> should be throttled
        assertEquals(false, triggerScroll(2799L))
        assertEquals(1, scrollCount)

        // Fourth scroll attempt at t = 2800ms (1400ms elapsed since 1400ms) -> should succeed
        assertEquals(true, triggerScroll(2800L))
        assertEquals(2, scrollCount)
    }
}
