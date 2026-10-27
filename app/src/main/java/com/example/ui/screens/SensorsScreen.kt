package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun SensorsScreen(
    viewModel: DiagViewModel,
    onBack: () -> Unit
) {
    val testResults by viewModel.testResults.collectAsState()
    val status = testResults[DiagnosticTest.SENSORS] ?: TestStatus.NOT_TESTED
    val sensors by viewModel.sensorValues.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startListeningSensors()
        onDispose {
            viewModel.stopListeningSensors()
        }
    }

    val animatedAzimuth by animateFloatAsState(targetValue = sensors.compassAzimuth, label = "azimuth")

    Scaffold(
        topBar = {
            TestTopBar(
                title = "Test des Capteurs Matériels",
                status = status,
                onBack = onBack
            )
        },
        bottomBar = {
            TestVerdictBar(
                status = status,
                onPass = {
                    viewModel.setTestResult(DiagnosticTest.SENSORS, TestStatus.PASSED)
                    onBack()
                },
                onFail = {
                    viewModel.setTestResult(DiagnosticTest.SENSORS, TestStatus.FAILED)
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
            // Card 1: Accelerometer & Spirit Level
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = DiagCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Accéléromètre (Gravité)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            text = if (sensors.hasAccel) "Actif" else "Absent",
                            color = if (sensors.hasAccel) DiagGreen else DiagRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SensorAxisBadge("X", String.format("%.2f m/s²", sensors.accelX))
                        SensorAxisBadge("Y", String.format("%.2f m/s²", sensors.accelY))
                        SensorAxisBadge("Z", String.format("%.2f m/s²", sensors.accelZ))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Spirit level bubble
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            // Center target
                            drawCircle(color = Color.White.copy(alpha = 0.2f), radius = 20.dp.toPx(), center = Offset(centerX, centerY))
                            drawCircle(color = DiagCyan, radius = 20.dp.toPx(), center = Offset(centerX, centerY), style = Stroke(width = 1.dp.toPx()))

                            // Bubble offset based on accelX and accelY
                            val bubbleX = (centerX - (sensors.accelX / 9.8f) * (centerX - 30)).coerceIn(20f, size.width - 20f)
                            val bubbleY = (centerY + (sensors.accelY / 9.8f) * (centerY - 20)).coerceIn(15f, size.height - 15f)
                            drawCircle(color = DiagGreen, radius = 12.dp.toPx(), center = Offset(bubbleX, bubbleY))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card 2: Gyroscope
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RotateRight, contentDescription = null, tint = DiagCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gyroscope (Vitesse angulaire)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            text = if (sensors.hasGyro) "Actif" else "Absent",
                            color = if (sensors.hasGyro) DiagGreen else DiagRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SensorAxisBadge("Pitch (X)", String.format("%.2f rad/s", sensors.gyroX))
                        SensorAxisBadge("Roll (Y)", String.format("%.2f rad/s", sensors.gyroY))
                        SensorAxisBadge("Yaw (Z)", String.format("%.2f rad/s", sensors.gyroZ))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card 3: Proximity & Ambient Light (2 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Proximity
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = if (sensors.isNear) DiagGreen else DiagCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Proximité", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (!sensors.hasProximity) "Non disponible" else if (sensors.isNear) "PROCHE (Main/Oreille)" else "ÉLOIGNÉ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (sensors.isNear) DiagGreen else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (sensors.proximityCm >= 0) "${sensors.proximityCm.toInt()} cm" else "--",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Ambient Light
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrightnessMedium,
                            contentDescription = null,
                            tint = DiagAmber,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Luminosité", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (sensors.lightLux >= 0) "${sensors.lightLux.toInt()} Lux" else "Inconnu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DiagAmber
                        )
                        Text(
                            text = when {
                                sensors.lightLux < 10 -> "Nuit / Sombre"
                                sensors.lightLux < 500 -> "Éclairage intérieur"
                                else -> "Plein jour / Soleil"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card 4: Compass / Azimuth
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Explore, contentDescription = null, tint = DiagCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Boussole Numérique & Magnétomètre",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            text = "${sensors.compassAzimuth.toInt()}°",
                            fontWeight = FontWeight.Bold,
                            color = DiagCyan,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(2.dp, DiagCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().rotate(-animatedAzimuth)) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f

                            // North needle (Red)
                            drawLine(
                                color = DiagRed,
                                start = Offset(centerX, centerY),
                                end = Offset(centerX, centerY - 42.dp.toPx()),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            // South needle (White)
                            drawLine(
                                color = Color.White,
                                start = Offset(centerX, centerY),
                                end = Offset(centerX, centerY + 42.dp.toPx()),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(color = Color.Black, radius = 6.dp.toPx(), center = Offset(centerX, centerY))
                        }
                        Text(
                            text = "N",
                            color = DiagRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
                        )
                    }
                }
            }

            if (sensors.hasBarometer) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FilterDrama, contentDescription = null, tint = DiagCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Baromètre (Pression atmosphérique)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${sensors.pressureHpa} hPa", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = DiagCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorAxisBadge(axis: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = axis, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
