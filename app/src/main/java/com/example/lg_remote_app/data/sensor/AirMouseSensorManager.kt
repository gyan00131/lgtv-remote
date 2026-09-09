package com.example.lg_remote_app.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class AirMouseSensorManager(
    context: Context,
    private val onPointerMove: (dx: Int, dy: Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var isListening = false
    private val sensitivity = 22f

    fun start() {
        if (isListening || gyroscope == null) return
        sensorManager?.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)
        isListening = true
        Log.d(TAG, "Air Mouse sensor started")
    }

    fun stop() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        Log.d(TAG, "Air Mouse sensor stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isListening || event?.sensor?.type != Sensor.TYPE_GYROSCOPE) return

        // Gyroscope values: [0] = rotation around X axis (pitch), [1] = rotation around Y axis (yaw), [2] = rotation around Z axis (roll)
        val gyroY = event.values[1] // Yaw -> horizontal cursor motion dx
        val gyroX = event.values[0] // Pitch -> vertical cursor motion dy

        val dx = (-gyroY * sensitivity).toInt()
        val dy = (-gyroX * sensitivity).toInt()

        if (dx != 0 || dy != 0) {
            onPointerMove(dx, dy)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val TAG = "AirMouseSensorManager"
    }
}
