package com.example.lg_remote_app.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object WakeOnLanManager {
    suspend fun sendWakeOnLan(macAddress: String, ipAddress: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val macBytes = parseMacAddress(macAddress) ?: return@withContext false
            val bytes = ByteArray(6 + 16 * macBytes.size)
            
            for (i in 0..5) {
                bytes[i] = 0xFF.toByte()
            }
            for (i in 6 until bytes.size) {
                bytes[i] = macBytes[(i - 6) % macBytes.size]
            }

            val targets = mutableListOf<InetAddress>()
            targets.add(InetAddress.getByName("255.255.255.255"))
            if (!ipAddress.isNullOrEmpty()) {
                try {
                    targets.add(InetAddress.getByName(ipAddress))
                } catch (_: Exception) {}
            }

            DatagramSocket().use { socket ->
                socket.broadcast = true
                for (target in targets) {
                    val packet = DatagramPacket(bytes, bytes.size, target, 9)
                    socket.send(packet)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun parseMacAddress(macStr: String): ByteArray? {
        val cleanMac = macStr.replace(":", "").replace("-", "").trim()
        if (cleanMac.length != 12) return null
        val bytes = ByteArray(6)
        for (i in 0 until 6) {
            bytes[i] = cleanMac.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return bytes
    }
}
