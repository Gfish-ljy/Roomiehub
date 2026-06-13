package com.dorm.health.data.repository

import com.dorm.health.data.database.dao.DormInfoDao
import com.dorm.health.data.database.dao.EnvironmentalRecordDao
import com.dorm.health.data.database.dao.NightOwlEventDao
import com.dorm.health.data.database.entities.DormInfo
import com.dorm.health.data.database.entities.EnvironmentalRecord
import com.dorm.health.data.datasource.PhoneSensorDataSource
import com.dorm.health.data.datasource.ServerDataSource
import com.dorm.health.data.model.EnvironmentSnapshot
import com.dorm.health.data.model.HealthScore
import com.dorm.health.data.model.SensorMode
import com.dorm.health.data.model.ServerConnectionTestResult
import com.dorm.health.utils.NightOwlDetector
import com.dorm.health.utils.PreferencesManager
import com.dorm.health.utils.ScoreCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class EnvironmentRepository(
    private val recordDao: EnvironmentalRecordDao,
    private val dormInfoDao: DormInfoDao,
    private val nightOwlEventDao: NightOwlEventDao,
    private val phoneSensorDataSource: PhoneSensorDataSource,
    private val serverDataSource: ServerDataSource,
    private val preferencesManager: PreferencesManager,
    private val nightOwlDetector: NightOwlDetector,
    private val scope: CoroutineScope
) {
    private val _currentSnapshot = MutableStateFlow(EnvironmentSnapshot())
    val currentSnapshot: StateFlow<EnvironmentSnapshot> = _currentSnapshot.asStateFlow()

    private val _healthScore = MutableStateFlow(HealthScore())
    val healthScore: StateFlow<HealthScore> = _healthScore.asStateFlow()

    private val _isNightOwl = MutableStateFlow(false)
    val isNightOwl: StateFlow<Boolean> = _isNightOwl.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _sensorMode = MutableStateFlow(preferencesManager.sensorMode)
    val sensorMode: StateFlow<SensorMode> = _sensorMode.asStateFlow()

    private var pollingJob: Job? = null
    private var lastAlertTime = 0L
    private var serverHistorySynced = false

    val dormInfo = dormInfoDao.getDormInfo()

    init {
        scope.launch {
            ensureDefaultDormInfo()
        }
    }

    private suspend fun ensureDefaultDormInfo() {
        if (dormInfoDao.getDormInfoOnce() == null) {
            dormInfoDao.insert(DormInfo())
        }
    }

    fun startDataCollection() {
        pollingJob?.cancel()
        when (_sensorMode.value) {
            SensorMode.PHONE -> phoneSensorDataSource.start()
            SensorMode.SERVER -> {
                scope.launch {
                    if (!serverHistorySynced) {
                        syncHistoryFromServer()
                        serverHistorySynced = true
                    }
                }
            }
        }
        pollingJob = scope.launch {
            while (isActive) {
                collectOnce()
                delay(preferencesManager.refreshIntervalSeconds * 1000L)
            }
        }
    }

    fun stopDataCollection() {
        pollingJob?.cancel()
        phoneSensorDataSource.stop()
    }

    suspend fun refresh() {
        if (_sensorMode.value == SensorMode.SERVER) {
            syncHistoryFromServer()
        }
        collectOnce()
    }

    suspend fun switchMode(mode: SensorMode) {
        stopDataCollection()
        phoneSensorDataSource.stop()
        if (mode != SensorMode.SERVER) {
            serverDataSource.disconnect()
            serverHistorySynced = false
        }
        _sensorMode.value = mode
        preferencesManager.sensorMode = mode
        dormInfoDao.getDormInfoOnce()?.let {
            dormInfoDao.update(it.copy(sensorMode = mode.name))
        }
        startDataCollection()
    }

    suspend fun testServerConnection(): ServerConnectionTestResult =
        serverDataSource.testConnection()

    suspend fun syncHistoryFromServer() {
        val from = System.currentTimeMillis() - 7L * 24 * 3600_000
        val to = System.currentTimeMillis()
        val snapshots = serverDataSource.fetchHistory(from, to)
        for (snapshot in snapshots) {
            if (!recordDao.existsAtTimestamp(snapshot.timestamp)) {
                val detection = nightOwlDetector.evaluate(snapshot.light, snapshot.noise, snapshot.timestamp)
                recordDao.insert(
                    EnvironmentalRecord(
                        timestamp = snapshot.timestamp,
                        temp = snapshot.temp,
                        humi = snapshot.humi,
                        noise = snapshot.noise,
                        light = snapshot.light,
                        aqi = snapshot.aqi,
                        isNightOwl = detection.isNightOwl
                    )
                )
            }
        }
    }

    private suspend fun collectOnce() {
        val snapshot = when (_sensorMode.value) {
            SensorMode.PHONE -> {
                phoneSensorDataSource.refresh()
                phoneSensorDataSource.snapshot.value
            }
            SensorMode.SERVER -> {
                serverDataSource.fetchLatest() ?: _currentSnapshot.value
            }
        }

        val detection = nightOwlDetector.evaluate(snapshot.light, snapshot.noise, snapshot.timestamp)
        _isNightOwl.value = detection.isNightOwl

        val record = EnvironmentalRecord(
            timestamp = snapshot.timestamp,
            temp = snapshot.temp,
            humi = snapshot.humi,
            noise = snapshot.noise,
            light = snapshot.light,
            aqi = snapshot.aqi,
            isNightOwl = detection.isNightOwl
        )
        if (!recordDao.existsAtTimestamp(snapshot.timestamp)) {
            recordDao.insert(record)
        }
        _currentSnapshot.value = snapshot

        val hourAgo = System.currentTimeMillis() - 3600_000
        val recentRecords = recordDao.getRecordsSinceOnce(hourAgo)
        _healthScore.value = ScoreCalculator.calculateRealtimeScore(recentRecords)

        _suggestions.value = ScoreCalculator.generateSuggestions(
            snapshot.temp, snapshot.humi, snapshot.noise, snapshot.light,
            snapshot.aqi, detection.isNightOwl
        )

        if (detection.shouldAlert && System.currentTimeMillis() - lastAlertTime > 600_000) {
            lastAlertTime = System.currentTimeMillis()
            _alertEvents.tryEmit(AlertEvent.NightOwl(detection.alertMessage ?: "熬夜提醒"))
        }

        checkAnomalies(snapshot)
    }

    private fun checkAnomalies(snapshot: EnvironmentSnapshot) {
        if (snapshot.noise > 70) {
            _alertEvents.tryEmit(AlertEvent.NoiseHigh)
        }
        if (snapshot.aqi > 150) {
            _alertEvents.tryEmit(AlertEvent.AqiHigh)
        }
        if (snapshot.temp > 30) {
            _alertEvents.tryEmit(AlertEvent.TempHigh)
        }
    }

    private val _alertEvents = MutableSharedFlow<AlertEvent>(extraBufferCapacity = 5)
    val alertEvents = _alertEvents.asSharedFlow()

    suspend fun updateDormName(name: String) {
        dormInfoDao.getDormInfoOnce()?.let {
            dormInfoDao.update(it.copy(dormName = name))
        }
    }

    suspend fun getRecordsForRange(from: Long, to: Long = System.currentTimeMillis()) =
        recordDao.getRecordsBetween(from, to)

    suspend fun getRecentHourRecords() =
        recordDao.getRecordsSinceOnce(System.currentTimeMillis() - 3600_000)

    sealed class AlertEvent(val message: String) {
        object NoiseHigh : AlertEvent("噪音持续过高，请注意休息环境")
        object AqiHigh : AlertEvent("空气质量较差，建议通风")
        object TempHigh : AlertEvent("温度过高，请注意降温")
        class NightOwl(msg: String) : AlertEvent(msg)
    }
}
