package com.simform.flutter_credit_card.gyroscope

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Display
import android.view.Surface
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink

internal class GyroscopeStreamHandler(
    private val display: Display?,
    private val sensorManager: SensorManager,
) : EventChannel.StreamHandler {
    private var sensorEventListener: SensorEventListener? = null

    // Direct initialization instead of lazy delegate
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    override fun onListen(arguments: Any?, events: EventSink) {
        sensorEventListener = createSensorEventListener(events)
        // Gyroscope Event sample period set at 60 fps, specified in microseconds.
        sensor?.let {
            sensorManager.registerListener(sensorEventListener, it, 16666)
        }
    }

    override fun onCancel(arguments: Any?) {
        sensorManager.unregisterListener(sensorEventListener)
    }

    private fun createSensorEventListener(events: EventSink): SensorEventListener {
        return object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

            override fun onSensorChanged(event: SensorEvent) {
                events.success(processForOrientation(event.values))
            }
        }
    }

    private fun processForOrientation(values: FloatArray): DoubleArray {
        return if (display == null) {
            values.map { it.toDouble() }.toDoubleArray()
        } else {
            val arr = DoubleArray(3)
            val x = values[0].toDouble()
            val y = values[1].toDouble()
            val z = values[2].toDouble()

            when (display.rotation) {
                Surface.ROTATION_0,
                Surface.ROTATION_180 -> {
                    arr[0] = x
                    arr[1] = y
                    arr[2] = z
                }

                Surface.ROTATION_270 -> {
                    arr[0] = y
                    arr[1] = -x
                    arr[2] = z
                }

                Surface.ROTATION_90 -> {
                    arr[0] = -y
                    arr[1] = x
                    arr[2] = z
                }
            }

            arr
        }
    }
}