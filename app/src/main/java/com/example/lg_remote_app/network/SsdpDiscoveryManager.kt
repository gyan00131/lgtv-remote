package com.example.lg_remote_app.network

import android.content.Context
import android.net.wifi.WifiManager
import com.example.lg_remote_app.model.TvDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.URL

class SsdpDiscoveryManager(private val context: Context) {

    private val ssdpAddress = "239.255.255.250"
    private val ssdpPort = 1900

    fun discoverTvs(): Flow<TvDevice> = channelFlow {
        val discoveredIps = mutableSetOf<String>()

        // 1. Launch SSDP UDP Multicast Discovery
        launch(Dispatchers.IO) {
            discoverViaSsdp { device ->
                if (discoveredIps.add(device.ipAddress)) {
                    trySend(device)
                }
            }
        }

        // 2. Launch Subnet Port 3000 Scan Fallback
        launch(Dispatchers.IO) {
            scanSubnetForWebOsTvs { device ->
                if (discoveredIps.add(device.ipAddress)) {
                    trySend(device)
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun discoverViaSsdp(onDeviceFound: (TvDevice) -> Unit) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val multicastLock = wifiManager?.createMulticastLock("SsdpDiscoveryLock")?.apply {
            setReferenceCounted(true)
            acquire()
        }

        try {
            val targets = listOf(
                "urn:schemas-upnp-org:device:MediaRenderer:1",
                "urn:lge-com:service:webos-second-screen:1",
                "ssdp:all",
                "upnp:rootdevice"
            )

            val socket = DatagramSocket()
            socket.soTimeout = 2500

            val group = InetAddress.getByName(ssdpAddress)

            for (target in targets) {
                val query = "M-SEARCH * HTTP/1.1\r\n" +
                        "HOST: 239.255.255.250:1900\r\n" +
                        "MAN: \"ssdp:discover\"\r\n" +
                        "MX: 2\r\n" +
                        "ST: $target\r\n\r\n"

                val bytes = query.toByteArray()
                val packet = DatagramPacket(bytes, bytes.size, group, ssdpPort)
                socket.send(packet)
            }

            val buffer = ByteArray(2048)
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < 3500) {
                try {
                    val rxPacket = DatagramPacket(buffer, buffer.size)
                    socket.receive(rxPacket)
                    val response = String(rxPacket.data, 0, rxPacket.length)
                    val ip = rxPacket.address.hostAddress ?: continue

                    var modelName = parseModelNameFromSsdp(response)
                    var locationUrl = parseLocationHeader(response)

                    if (locationUrl != null && modelName == "[LG] webOS TV") {
                        val nameFromXml = fetchFriendlyNameFromXml(locationUrl)
                        if (!nameFromXml.isNullOrEmpty()) {
                            modelName = nameFromXml
                        }
                    }

                    val macAddress = parseMacAddress(response)
                    onDeviceFound(TvDevice(ipAddress = ip, name = modelName, macAddress = macAddress, isDiscovered = true))
                } catch (_: SocketTimeoutException) {
                    break
                } catch (_: Exception) {
                }
            }
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (multicastLock?.isHeld == true) {
                    multicastLock.release()
                }
            } catch (_: Exception) {}
        }
    }

    private suspend fun scanSubnetForWebOsTvs(onDeviceFound: (TvDevice) -> Unit) = coroutineScope {
        val localIp = getLocalWifiIpAddress() ?: return@coroutineScope
        val prefix = localIp.substringBeforeLast(".") + "."

        val jobs = (1..254).map { i ->
            async(Dispatchers.IO) {
                val targetIp = "$prefix$i"
                if (targetIp == localIp) return@async null

                if (isPortOpen(targetIp, 3000, 300)) {
                    val name = fetchTvNameFromIp(targetIp) ?: "[LG] webOS TV"
                    TvDevice(ipAddress = targetIp, name = name, isDiscovered = true)
                } else null
            }
        }

        jobs.awaitAll().filterNotNull().forEach { device ->
            onDeviceFound(device)
        }
    }

    private fun isPortOpen(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun getLocalWifiIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addrs = iface.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress
                        if (host != null && (host.startsWith("192.168.") || host.startsWith("10.") || host.startsWith("172."))) {
                            return host
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun fetchTvNameFromIp(ip: String): String? {
        val testUrls = listOf(
            "http://$ip:1828/",
            "http://$ip:1900/device-descriptor.xml",
            "http://$ip:1900/"
        )
        for (urlStr in testUrls) {
            val name = fetchFriendlyNameFromXml(urlStr)
            if (!name.isNullOrEmpty()) return name
        }
        return null
    }

    private fun fetchFriendlyNameFromXml(urlStr: String): String? {
        return try {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 1000
            conn.readTimeout = 1000
            val xml = conn.inputStream.bufferedReader().use { it.readText() }
            parseFriendlyNameFromXmlString(xml)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseFriendlyNameFromXmlString(xml: String): String? {
        val regex = Regex("<friendlyName>(.*?)</friendlyName>", RegexOption.IGNORE_CASE)
        val match = regex.find(xml)
        val name = match?.groupValues?.getOrNull(1)?.trim()
        return if (!name.isNullOrEmpty()) name else null
    }

    private fun parseLocationHeader(response: String): String? {
        val lines = response.split("\r\n")
        for (line in lines) {
            if (line.startsWith("LOCATION:", ignoreCase = true)) {
                return line.substringAfter(":").trim()
            }
        }
        return null
    }

    private fun parseModelNameFromSsdp(response: String): String {
        val lines = response.split("\r\n")
        for (line in lines) {
            if (line.startsWith("SERVER:", ignoreCase = true) || line.startsWith("USN:", ignoreCase = true)) {
                if (line.contains("webOS", ignoreCase = true) || line.contains("LG", ignoreCase = true)) {
                    val parts = line.split(" ")
                    val lgPart = parts.find { it.contains("LG", ignoreCase = true) || it.contains("webOS", ignoreCase = true) }
                    if (lgPart != null) return lgPart
                }
            }
        }
        return "[LG] webOS TV"
    }

    private fun parseMacAddress(response: String): String? {
        val macRegex = Regex("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})")
        val match = macRegex.find(response)
        return match?.value
    }
}
