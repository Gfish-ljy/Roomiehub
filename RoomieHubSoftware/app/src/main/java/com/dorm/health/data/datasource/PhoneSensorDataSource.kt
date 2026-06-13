package com.dorm.health.data.datasource

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.dorm.health.data.model.EnvironmentSnapshot
import com.dorm.health.utils.ScoreCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * 手机传感器数据源：光线传感器 + 麦克风噪音 + 温湿度模拟
 */
class PhoneSensorDataSource(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _snapshot = MutableStateFlow(EnvironmentSnapshot(isSimulated = true))
    val snapshot: StateFlow<EnvironmentSnapshot> = _snapshot.asStateFlow()

    private var lightValue = 100f
    private var hasLightSensor = lightSensor != null
    private var audioRecord: AudioRecord? = null
    private var isMonitoring = false
    private val startTime = System.currentTimeMillis()

    fun start() {
        if (isMonitoring) return
        isMonitoring = true
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        startNoiseMonitoring()
        updateSimulatedValues()
    }

    fun stop() {
        isMonitoring = false
        sensorManager.unregisterListener(this)
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            lightValue = event.values[0]
            hasLightSensor = true
            publishSnapshot()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun refresh() {
        updateSimulatedValues()
        publishSnapshot()
    }

    private fun updateSimulatedValues() {
        val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
        val temp = (24 + kotlin.math.sin(elapsed / 120) * 3 + (Math.random() - 0.5) * 0.5).toFloat()
        val humi = (55 + kotlin.math.cos(elapsed / 180) * 12 + (Math.random() - 0.5) * 2).toFloat()
        val noise = readNoiseLevel()
        val light = if (hasLightSensor) lightValue else {
            (80 + kotlin.math.sin(elapsed / 90) * 40).toFloat()
        }
        val aqi = ScoreCalculator.calculateVirtualAqi(noise, humi, light)

        _snapshot.value = EnvironmentSnapshot(
            temp = temp.coerceIn(20f, 28f),
            humi = humi.coerceIn(40f, 70f),
            noise = noise.coerceIn(30f, 100f),
            light = light.coerceAtLeast(0f),
            aqi = aqi,
            isSimulated = !hasLightSensor,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun publishSnapshot() {
        val current = _snapshot.value
        _snapshot.value = current.copy(
            light = if (hasLightSensor) lightValue else current.light,
            timestamp = System.currentTimeMillis()
        )
    }

    @Suppress("MissingPermission")
    private fun startNoiseMonitoring() {
        try {
            val sampleRate = 44100
            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) return

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) return
            audioRecord?.startRecording()
        } catch (_: Exception) {
            audioRecord = null
        }
    }

    private fun readNoiseLevel(): Float {
        val record = audioRecord ?: return (35 + Math.random() * 15).toFloat()
        val bufferSize = 1024
        val buffer = ShortArray(bufferSize)
        val read = record.read(buffer, 0, bufferSize)
        if (read <= 0) return 40f

        var sum = 0.0
        for (i in 0 until read) {
            sum += buffer[i] * buffer[i]
        }
        val rms = sqrt(sum / read)
        val db = if (rms > 0) (20 * log10(rms / 32767.0) + 90).toFloat() else 30f
        return db.coerceIn(30f, 100f)
    }
}
