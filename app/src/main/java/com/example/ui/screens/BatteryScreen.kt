package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiagnosticTest
import com.example.data.model.TestStatus
import com.example.ui.components.TestTopBar
import com.example.ui.components.TestVerdictBar
import com.example.ui.theme.DiagAmber
import com.example.ui.theme.DiagCyan
import com.example.ui.theme.DiagGreen
import com.example.ui.theme.DiagRed
import com.example.ui.viewmodel.DiagViewModel

@Composable
fun BatteryScreen(
    viewModel: DiagViewModel,
    onBack: () -> Unit
) {
    val testResults by viewModel.testResults.collectAsState()
    val status = testResults[DiagnosticTest.BATTERY] ?: TestStatus.NOT_TESTED
    val battery by viewModel.batteryData.collectAsState()

    val animatedProgress by animateFloatAsState(
        targetValue = (battery.level / 100f).coerceIn(0f, 1f),
        label = "batteryProgress"
    )

    val batteryColor = when {
        battery.level > 50 -> DiagGreen
        battery.level > 20 -> DiagAmber
        else -> DiagRed
    }

    Scaffold(
        topBar = {
            TestTopBar(
                title = "Test Charge & Batterie",
                status = status,
                onBack = onBack
            )
        },
        bottomBar = {
            TestVerdictBar(
                status = status,
                onPass = {
                    viewModel.setTestResult(DiagnosticTest.BATTERY, TestStatus.PASSED)
                    onBack()
                },
                onFail = {
                    viewModel.setTestResult(DiagnosticTest.BATTERY, TestStatus.FAILED)
                    onBack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main gauge card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 12.dp,
                            color = if (battery.isCharging) DiagCyan else batteryColor,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (battery.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (battery.isCharging) DiagCyan else batteryColor,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${battery.level}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                            Text(
                                text = if (battery.isCharging) "En charge (${battery.chargeSource})" else "Sur batterie",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (battery.isCharging) DiagCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Power,
                            contentDescription = null,
                            tint = if (battery.isCharging) DiagCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Source : ${battery.chargeSource}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (battery.isCharging) DiagCyan else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Diagnostic telemetry grid
            Text(
                text = "Télémétrie en temps réel",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BatteryMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.HealthAndSafety,
                    iconTint = DiagGreen,
                    title = "Santé Batterie",
                    value = battery.health
                )
                BatteryMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DeviceThermostat,
                    iconTint = if (battery.temperatureC > 42) DiagRed else DiagCyan,
                    title = "Température",
                    value = "${battery.temperatureC}°C"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BatteryMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ElectricMeter,
                    iconTint = DiagAmber,
                    title = "Tension",
                    value = "${battery.voltageMv} mV (${String.format("%.2f", battery.voltageMv / 1000f)} V)"
                )
                BatteryMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.BatteryAlert,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Technologie",
                    value = battery.technology
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Conseil de validation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Branchez puis débranchez le câble du chargeur ou posez l'appareil sur un chargeur sans fil pour vérifier la réactivité immédiate de la détection de charge.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
