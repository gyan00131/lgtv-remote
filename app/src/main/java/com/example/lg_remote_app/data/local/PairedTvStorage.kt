package com.example.lg_remote_app.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.lg_remote_app.data.model.LgTvDevice
import org.json.JSONArray
import org.json.JSONObject

class PairedTvStorage(context: Context) {

    private val prefs: SharedPreferences = createEncryptedSharedPreferences(context)

    private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences, falling back to standard prefs", e)
            context.getSharedPreferences("${PREFS_NAME}_fallback", Context.MODE_PRIVATE)
        }
    }

    fun savePairedTv(device: LgTvDevice) {
        val list = getPairedTvs().toMutableList()
        val index = list.indexOfFirst {
            it.id == device.id ||
                    (it.macAddress != null && device.macAddress != null && it.macAddress == device.macAddress) ||
                    it.ipAddress == device.ipAddress
        }

        val updated = device.copy(lastSeenTimestamp = System.currentTimeMillis())
        if (index >= 0) {
            list[index] = updated
        } else {
            list.add(updated)
        }

        persistList(list)
    }

    fun getPairedTv(): LgTvDevice? {
        return getPairedTvs().maxByOrNull { it.lastSeenTimestamp }
    }

    fun getPairedTvs(): List<LgTvDevice> {
        val jsonStr = prefs.getString(KEY_PAIRED_TVS_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<LgTvDevice>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", obj.optString("ipAddress"))
                val name = obj.optString("name", "[LG] webOS TV")
                val ip = obj.optString("ipAddress")
                val port = obj.optInt("port", 3000)
                val mac = obj.optString("macAddress").takeIf { it.isNotEmpty() }
                val model = obj.optString("modelName").takeIf { it.isNotEmpty() }
                val clientKey = obj.optString("clientKey").takeIf { it.isNotEmpty() }
                val timestamp = obj.optLong("lastSeenTimestamp", System.currentTimeMillis())

                if (ip.isNotEmpty()) {
                    list.add(
                        LgTvDevice(
                            id = id,
                            name = name,
                            ipAddress = ip,
                            port = port,
                            macAddress = mac,
                            modelName = model,
                            clientKey = clientKey,
                            lastSeenTimestamp = timestamp
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing paired TVs JSON", e)
            emptyList()
        }
    }

    fun updateTvIp(id: String, newIp: String) {
        val list = getPairedTvs().toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index >= 0) {
            list[index] = list[index].copy(ipAddress = newIp, lastSeenTimestamp = System.currentTimeMillis())
            persistList(list)
        }
    }

    fun clearPairedTv(id: String? = null) {
        if (id == null) {
            prefs.edit().remove(KEY_PAIRED_TVS_JSON).apply()
        } else {
            val list = getPairedTvs().filter { it.id != id }
            persistList(list)
        }
    }

    fun hasPairedTv(): Boolean {
        return getPairedTvs().any { !it.clientKey.isNullOrEmpty() }
    }

    private fun persistList(list: List<LgTvDevice>) {
        val array = JSONArray()
        for (dev in list) {
            val obj = JSONObject().apply {
                put("id", dev.id)
                put("name", dev.name)
                put("ipAddress", dev.ipAddress)
                put("port", dev.port)
                put("macAddress", dev.macAddress ?: "")
                put("modelName", dev.modelName ?: "")
                put("clientKey", dev.clientKey ?: "")
                put("lastSeenTimestamp", dev.lastSeenTimestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_PAIRED_TVS_JSON, array.toString()).apply()
    }

    companion object {
        private const val TAG = "PairedTvStorage"
        private const val PREFS_NAME = "secure_lg_paired_tvs"
        private const val KEY_PAIRED_TVS_JSON = "paired_tvs_v1"
    }
}
