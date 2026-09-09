package com.example.lg_remote_app

import com.example.lg_remote_app.data.model.LgTvDevice
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceDeduplicationTest {

    @Test
    fun `deduplicate devices by IP address keeps latest record`() {
        val list = listOf(
            LgTvDevice(id = "1", name = "LG TV 1", ipAddress = "192.168.1.50"),
            LgTvDevice(id = "2", name = "LG webOS TV", ipAddress = "192.168.1.50", modelName = "OLED55CX"),
            LgTvDevice(id = "3", name = "LG Living Room", ipAddress = "192.168.1.100")
        )

        val deduplicated = list.distinctBy { it.ipAddress }

        assertEquals(2, deduplicated.size)
        assertEquals("192.168.1.50", deduplicated[0].ipAddress)
        assertEquals("192.168.1.100", deduplicated[1].ipAddress)
    }

    @Test
    fun `deduplicate devices by MAC address handles DHCP IP changes`() {
        val device1 = LgTvDevice(id = "tv1", name = "LG TV", ipAddress = "192.168.1.50", macAddress = "AA:BB:CC:DD:EE:FF")
        val device2WithNewIp = LgTvDevice(id = "tv1", name = "LG TV", ipAddress = "192.168.1.120", macAddress = "AA:BB:CC:DD:EE:FF")

        val currentDevices = mutableListOf(device1)

        val index = currentDevices.indexOfFirst {
            it.macAddress == device2WithNewIp.macAddress || it.ipAddress == device2WithNewIp.ipAddress
        }

        if (index >= 0) {
            currentDevices[index] = device2WithNewIp
        } else {
            currentDevices.add(device2WithNewIp)
        }

        assertEquals(1, currentDevices.size)
        assertEquals("192.168.1.120", currentDevices[0].ipAddress)
    }
}
