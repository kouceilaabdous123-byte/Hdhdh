package com.example.ui.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DiagnosticTest
import com.example.data.model.TestCategory
import com.example.data.model.TestStatus
import com.example.util.BatteryDiagnosticData
import com.example.util.HardwareHelper
import com.example.util.SystemHardwareInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SensorValues(
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val hasAccel: Boolean = false,
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 0f,
    val hasGyro: Boolean = false,
    val lightLux: Float = -1f,
    val hasLight: Boolean = false,
    val proximityCm: Float = -1f,
    val isNear: Boolean = false,
    val hasProximity: Boolean = false,
    val compassAzimuth: Float = 0f,
    val hasCompass: Boolean = false,
    val pressureHpa: Float = -1f,
    val hasBarometer: Boolean = false
)

data class ButtonsTestState(
    val volUpPressed: Boolean = false,
    val volDownPressed: Boolean = false,
    val backPressed: Boolean = false
) {
    val isComplete: Boolean get() = volUpPressed && volDownPressed && backPressed
}

class DiagViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    val hardwareHelper = HardwareHelper(application)
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private val _testResults = MutableStateFlow<Map<DiagnosticTest, TestStatus>>(
        DiagnosticTest.entries.associateWith { TestStatus.NOT_TESTED }
    )
    val testResults: StateFlow<Map<DiagnosticTest, TestStatus>> = _testResults.asStateFlow()

    private val _batteryData = MutableStateFlow(BatteryDiagnosticData())
    val batteryData: StateFlow<BatteryDiagnosticData> = _batteryData.asStateFlow()

    private val _sensorValues = MutableStateFlow(SensorValues())
    val sensorValues: StateFlow<SensorValues> = _sensorValues.asStateFlow()

    private val _buttonsState = MutableStateFlow(ButtonsTestState())
    val buttonsState: StateFlow<ButtonsTestState> = _buttonsState.asStateFlow()

    private val _systemInfo = MutableStateFlow<SystemHardwareInfo?>(null)
    val systemInfo: StateFlow<SystemHardwareInfo?> = _systemInfo.asStateFlow()

    private val _maxMultiTouchCount = MutableStateFlow(0)
    val maxMultiTouchCount: StateFlow<Int> = _maxMultiTouchCount.asStateFlow()

    // Sensors
    private var accelSensor: Sensor? = null
    private var gyroSensor: Sensor? = null
    private var lightSensor: Sensor? = null
    private var proximitySensor: Sensor? = null
    private var magSensor: Sensor? = null
    private var pressureSensor: Sensor? = null

    // For azimuth calculation
    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 0

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val sourceStr = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_AC -> "Secteur (AC)"
                BatteryManager.BATTERY_PLUGGED_USB -> "Port USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Sans fil (Induction)"
                else -> if (isCharging) "En charge" else "Sur batterie"
            }

            val statusStr = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "En charge"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "En décharge"
                BatteryManager.BATTERY_STATUS_FULL -> "Pleine"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Branché (Pas de charge)"
                else -> "Inconnu"
            }

            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            val healthStr = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Excellente (Good)"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Surchauffe !"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Batterie HS"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Surtension"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Défaillance"
                BatteryManager.BATTERY_HEALTH_COLD -> "Trop froide"
                else -> "Normale"
            }

            val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val tempC = tempTenths / 10.0f
            val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
            val present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true)

            _batteryData.value = BatteryDiagnosticData(
                level = batteryPct,
                isCharging = isCharging,
                chargeSource = sourceStr,
                status = statusStr,
                health = healthStr,
                temperatureC = tempC,
                voltageMv = voltageMv,
                technology = tech,
                isPresent = present
            )
        }
    }

    init {
        // Register battery receiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(batteryReceiver, filter)

        // Read system info
        viewModelScope.launch {
            _systemInfo.value = hardwareHelper.getSystemInfo()
        }

        // Initialize sensors
        setupSensors()
    }

    private fun setupSensors() {
        val sm = sensorManager ?: return
        accelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT)
        proximitySensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        magSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        pressureSensor = sm.getDefaultSensor(Sensor.TYPE_PRESSURE)

        _sensorValues.update {
            it.copy(
                hasAccel = accelSensor != null,
                hasGyro = gyroSensor != null,
                hasLight = lightSensor != null,
                hasProximity = proximitySensor != null,
                hasCompass = magSensor != null,
                hasBarometer = pressureSensor != null
            )
        }
    }

    fun startListeningSensors() {
        val sm = sensorManager ?: return
        accelSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        lightSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        proximitySensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        pressureSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stopListeningSensors() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravityValues, 0, 3)
                hasGravity = true
                _sensorValues.update {
                    it.copy(
                        accelX = event.values[0],
                        accelY = event.values[1],
                        accelZ = event.values[2]
                    )
                }
                updateOrientation()
            }
            Sensor.TYPE_GYROSCOPE -> {
                _sensorValues.update {
                    it.copy(
                        gyroX = event.values[0],
                        gyroY = event.values[1],
                        gyroZ = event.values[2]
                    )
                }
            }
            Sensor.TYPE_LIGHT -> {
                _sensorValues.update {
                    it.copy(lightLux = event.values[0])
                }
            }
            Sensor.TYPE_PROXIMITY -> {
                val dist = event.values[0]
                val maxRange = event.sensor.maximumRange
                val isNear = dist < 2.0f || (dist < maxRange && maxRange > 0)
                _sensorValues.update {
                    it.copy(proximityCm = dist, isNear = isNear)
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagneticValues, 0, 3)
                hasGeomagnetic = true
                updateOrientation()
            }
            Sensor.TYPE_PRESSURE -> {
                _sensorValues.update {
                    it.copy(pressureHpa = event.values[0])
                }
            }
        }
    }

    private fun updateOrientation() {
        if (hasGravity && hasGeomagnetic) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravityValues, geomagneticValues)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                // Convert azimuth from radians to degrees [0, 360]
                var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (azimuth < 0) azimuth += 360f
                _sensorValues.update { it.copy(compassAzimuth = azimuth) }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // Button testing
    fun onVolumeUpTriggered() {
        hardwareHelper.vibrateShort()
        _buttonsState.update { it.copy(volUpPressed = true) }
        checkButtonsComplete()
    }

    fun onVolumeDownTriggered() {
        hardwareHelper.vibrateShort()
        _buttonsState.update { it.copy(volDownPressed = true) }
        checkButtonsComplete()
    }

    fun onBackTriggered() {
        hardwareHelper.vibrateShort()
        _buttonsState.update { it.copy(backPressed = true) }
        checkButtonsComplete()
    }

    private fun checkButtonsComplete() {
        if (_buttonsState.value.isComplete) {
            setTestResult(DiagnosticTest.BUTTONS, TestStatus.PASSED)
        }
    }

    fun resetButtonsTest() {
        _buttonsState.value = ButtonsTestState()
    }

    // Multi-touch updates
    fun updateMultiTouchPointerCount(count: Int) {
        if (count > _maxMultiTouchCount.value) {
            _maxMultiTouchCount.value = count
        }
    }

    // Set test status
    fun setTestResult(test: DiagnosticTest, status: TestStatus) {
        _testResults.update { current ->
            current.toMutableMap().apply { put(test, status) }
        }
    }

    fun resetAllTests() {
        _testResults.value = DiagnosticTest.entries.associateWith { TestStatus.NOT_TESTED }
        _maxMultiTouchCount.value = 0
        _buttonsState.value = ButtonsTestState()
    }

    // Summary calculation
    fun getSummary(): DiagnosticSummary {
        val results = _testResults.value
        val passed = results.count { it.value == TestStatus.PASSED }
        val failed = results.count { it.value == TestStatus.FAILED }
        val notTested = results.count { it.value == TestStatus.NOT_TESTED }
        val total = results.size
        val scorePercent = if (total > 0) (passed * 100) / total else 0

        return DiagnosticSummary(
            total = total,
            passed = passed,
            failed = failed,
            notTested = notTested,
            scorePercent = scorePercent
        )
    }

    fun generateReportText(): String {
        val sb = StringBuilder()
        val info = _systemInfo.value
        val summary = getSummary()

        sb.appendLine("====================================")
        sb.appendLine("RAPPORT DE DIAGNOSTIC - DIAGPHONE")
        sb.appendLine("====================================")
        if (info != null) {
            sb.appendLine("Appareil : ${info.manufacturer} ${info.model} (${info.brand})")
            sb.appendLine("Android : v${info.androidVersion} (API ${info.sdkInt})")
            sb.appendLine("Processeur : ${info.cpuCores} cœurs, ${info.cpuAbis}")
            sb.appendLine("RAM : ${info.usedRamMb} Mo / ${info.totalRamMb} Mo")
            sb.appendLine("Stockage : ${info.usedStorageGb} Go / ${info.totalStorageGb} Go")
        }
        val battery = _batteryData.value
        sb.appendLine("Batterie : ${battery.level}% (${battery.status}, ${battery.health}, ${battery.temperatureC}°C)")
        sb.appendLine("------------------------------------")
        sb.appendLine("RÉSULTAT GLOBAL : ${summary.passed}/${summary.total} tests validés (${summary.scorePercent}%)")
        sb.appendLine("------------------------------------")

        for (test in DiagnosticTest.entries) {
            val status = _testResults.value[test] ?: TestStatus.NOT_TESTED
            val symbol = when (status) {
                TestStatus.PASSED -> "✅ REUSSI"
                TestStatus.FAILED -> "❌ ECHEC"
                TestStatus.NOT_TESTED -> "⚪ NON TESTE"
            }
            sb.appendLine("[${test.category.title}] ${test.title} : $symbol")
        }
        sb.appendLine("====================================")
        sb.appendLine("Généré par DiagPhone")
        return sb.toString()
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
        stopListeningSensors()
        hardwareHelper.stopTorch()
        hardwareHelper.stopTone()
    }
}

data class DiagnosticSummary(
    val total: Int,
    val passed: Int,
    val failed: Int,
    val notTested: Int,
    val scorePercent: Int
)
