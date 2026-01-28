package com.example.wifianalyzer.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.cos
import kotlin.math.sin

class SensorManagerHelper(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    // Using Step Detector if available, else simple accelerometer threshold
    private val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    private var currentHeading: Float = 0f
    
    // Callback for when a step is detected with the current heading (in radians)
    var onStepDetected: ((Float) -> Unit)? = null

    private var lastAccelTimestamp: Long = 0
    private val stepThreshold = 12f // Threshold for step detection (m/s^2)
    private val minStepInterval = 300 // ms
    private var lastStepTime: Long = 0

    fun startListening() {
        val stepSensor = stepDetector
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values
            
            // Fallback step detection if no step detector available
            if (stepDetector == null) {
                detectStepFromAccel(event)
            }
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values
        }
        
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            // Step detected!
            onStepDetected?.invoke(currentHeading)
        }

        if (gravity != null && geomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                // orientation[0] is azimuth (heading) in radians
                currentHeading = orientation[0]
            }
        }
    }
    
    private fun detectStepFromAccel(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        val magnitude = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        
        // Simple peak detection
        if (magnitude > stepThreshold) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastStepTime > minStepInterval) {
                lastStepTime = currentTime
                onStepDetected?.invoke(currentHeading)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
