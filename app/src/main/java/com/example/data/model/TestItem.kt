package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.ui.graphics.vector.ImageVector

enum class TestStatus {
    NOT_TESTED,
    PASSED,
    FAILED
}

enum class TestCategory(val title: String) {
    DISPLAY_TOUCH("Écran & Tactile"),
    CAM_LIGHT("Caméra & Torche"),
    HARDWARE("Boutons & Vibreur"),
    POWER_CONNECTIVITY("Batterie & Réseau"),
    AUDIO_SENSORS("Audio & Capteurs"),
    SYSTEM("Système & Multitâche")
}

enum class DiagnosticTest(
    val id: String,
    val title: String,
    val description: String,
    val category: TestCategory,
    val icon: ImageVector
) {
    FLASHLIGHT(
        id = "flashlight",
        title = "Lampe Torche",
        description = "Test du flash LED, stroboscope & SOS",
        category = TestCategory.CAM_LIGHT,
        icon = Icons.Default.FlashlightOn
    ),
    CAMERA(
        id = "camera",
        title = "Caméra",
        description = "Test capteurs avant/arrière & mise au point",
        category = TestCategory.CAM_LIGHT,
        icon = Icons.Default.CameraAlt
    ),
    DISPLAY(
        id = "display",
        title = "Écran & Rétroéclairage",
        description = "Luminosité, pixels morts & taux de rafraîchissement",
        category = TestCategory.DISPLAY_TOUCH,
        icon = Icons.Default.Brightness6
    ),
    MULTITOUCH(
        id = "multitouch",
        title = "Multitouche & Tactile",
        description = "Points de contact simultanés & zones mortes",
        category = TestCategory.DISPLAY_TOUCH,
        icon = Icons.Default.TouchApp
    ),
    BUTTONS(
        id = "buttons",
        title = "Boutons Physiques",
        description = "Volume haut, volume bas et retour",
        category = TestCategory.HARDWARE,
        icon = Icons.Default.Games
    ),
    BATTERY(
        id = "battery",
        title = "Charge & Batterie",
        description = "État de charge, santé, tension & température",
        category = TestCategory.POWER_CONNECTIVITY,
        icon = Icons.Default.BatteryChargingFull
    ),
    BLUETOOTH(
        id = "bluetooth",
        title = "Bluetooth",
        description = "Adaptateur, périphériques appairés & BLE",
        category = TestCategory.POWER_CONNECTIVITY,
        icon = Icons.Default.Bluetooth
    ),
    VIBRATION(
        id = "vibration",
        title = "Vibreur & Haptique",
        description = "Impulsions, intensité & motifs vibratoires",
        category = TestCategory.HARDWARE,
        icon = Icons.Default.Vibration
    ),
    AUDIO(
        id = "audio",
        title = "Haut-parleur & Micro",
        description = "Test fréquences, stéréo & enregistrement micro",
        category = TestCategory.AUDIO_SENSORS,
        icon = Icons.Default.Hearing
    ),
    SENSORS(
        id = "sensors",
        title = "Capteurs",
        description = "Accéléromètre, gyroscope, proximité & boussole",
        category = TestCategory.AUDIO_SENSORS,
        icon = Icons.Default.Sensors
    ),
    MULTITASK(
        id = "multitask",
        title = "Multitâche & Système",
        description = "Mode multi-fenêtre, CPU, RAM & stockage",
        category = TestCategory.SYSTEM,
        icon = Icons.Default.ViewQuilt
    );
}
