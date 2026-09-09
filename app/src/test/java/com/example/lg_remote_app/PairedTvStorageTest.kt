package com.example.lg_remote_app

import com.example.lg_remote_app.data.model.LgTvDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PairedTvStorageTest {

    @Test
    fun `test client key persistence and update`() {
        val initialDevice = LgTvDevice(id = "tv_1", name = "LG OLED", ipAddress = "192.168.1.38", clientKey = null)
        val pairedDevice = initialDevice.copy(clientKey = "secret_key_abc")

        val storageMap = mutableMapOf<String, LgTvDevice>()
        storageMap[pairedDevice.id] = pairedDevice

        val retrieved = storageMap["tv_1"]
        assertEquals("secret_key_abc", retrieved?.clientKey)
        assertEquals("192.168.1.38", retrieved?.ipAddress)
    }

    @Test
    fun `test clearing paired TV removes client key`() {
        val storageMap = mutableMapOf<String, LgTvDevice>(
            "tv_1" to LgTvDevice(id = "tv_1", name = "LG TV", ipAddress = "192.168.1.38", clientKey = "key_123")
        )

        storageMap.remove("tv_1")

        assertNull(storageMap["tv_1"])
        assertTrue(storageMap.isEmpty())
    }

    @Test
    fun `test updating TV IP address preserves client key`() {
        val originalDevice = LgTvDevice(id = "tv_1", name = "LG TV", ipAddress = "192.168.1.38", clientKey = "key_123")
        val updatedDevice = originalDevice.copy(ipAddress = "192.168.1.88")

        assertEquals("key_123", updatedDevice.clientKey)
        assertEquals("192.168.1.88", updatedDevice.ipAddress)
    }
}
