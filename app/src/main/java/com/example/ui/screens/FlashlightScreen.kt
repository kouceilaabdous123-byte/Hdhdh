package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

enum class FlashMode {
    OFF,
    ON,
    STROBE_FAST,
    STROBE_SLOW,
    SOS
}

@Composable
fun FlashlightScreen(
    viewModel: DiagViewModel,
    onBack: () -> Unit
) {
    val testResults by viewModel.testResults.collectAsState()
    val status = testResults[DiagnosticTest.FLASHLIGHT] ?: TestStatus.NOT_TESTED
    val scope = rememberCoroutineScope()
    var flashMode by remember { mutableStateOf(FlashMode.OFF) }
    val hasFlash = remember { viewModel.hardwareHelper.hasFlashlight() }

    // Turn off when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.hardwareHelper.stopTorch()
        }
    }

    val bulbColor by animateColorAsState(
        targetValue = if (flashMode != FlashMode.OFF) DiagAmber else Color(0xFF263238),
        animationSpec = tween(durationMillis = 200),
        label = "bulbColor"
    )

    Scaffold(
        topBar = {
            TestTopBar(
                title = "Test Lampe Torche",
                status = status,
                onBack = onBack
            )
        },
        bottomBar = {
            TestVerdictBar(
                status = status,
                onPass = {
                    viewModel.setTestResult(DiagnosticTest.FLASHLIGHT, TestStatus.PASSED)
                    onBack()
                },
                onFail = {
                    viewModel.setTestResult(DiagnosticTest.FLASHLIGHT, TestStatus.FAILED)
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Objectif du test",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vérifiez que la LED du flash arrière s'allume correctement en continu, en mode clignotant et en signal de détresse SOS.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bulb Glow Visualizer
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(bulbColor.copy(alpha = if (flashMode != FlashMode.OFF) 0.35f else 0.1f))
                    .border(3.dp, bulbColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(bulbColor)
                        .shadow(elevation = if (flashMode != FlashMode.OFF) 24.dp else 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (flashMode != FlashMode.OFF) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Torch State",
                        tint = if (flashMode != FlashMode.OFF) Color.Black else Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (flashMode) {
                    FlashMode.OFF -> "Flash éteint"
                    FlashMode.ON -> "Flash ALLUMÉ continu"
                    FlashMode.STROBE_FAST -> "Stroboscope rapide (100ms)"
                    FlashMode.STROBE_SLOW -> "Clignotement lent (400ms)"
                    FlashMode.SOS -> "Signal de secours SOS (... --- ...)"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = if (flashMode != FlashMode.OFF) DiagAmber else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!hasFlash) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DiagRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = DiagRed)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Aucun flash matériel détecté ou disponible sur ce périphérique.",
                            color = DiagRed,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main toggle button
            Button(
                onClick = {
                    if (flashMode == FlashMode.ON) {
                        flashMode = FlashMode.OFF
                        viewModel.hardwareHelper.stopTorch()
                    } else {
                        flashMode = FlashMode.ON
                        viewModel.hardwareHelper.setTorch(true)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("flashlight_main_toggle"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (flashMode == FlashMode.ON) DiagAmber else MaterialTheme.colorScheme.primary,
                    contentColor = if (flashMode == FlashMode.ON) Color.Black else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (flashMode == FlashMode.ON) Icons.Default.FlashOff else Icons.Default.FlashOn,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (flashMode == FlashMode.ON) "ÉTEINDRE LA TORCHE" else "ALLUMER LA TORCHE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Modes avancés de diagnostic",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        flashMode = FlashMode.STROBE_FAST
                        viewModel.hardwareHelper.startStrobe(scope, 100)
                    },
                    modifier = Modifier.weight(1f).testTag("flash_strobe_fast"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Strobe 100ms", fontSize = 12.sp, textAlign = TextAlign.Center)
                }

                OutlinedButton(
                    onClick = {
                        flashMode = FlashMode.STROBE_SLOW
                        viewModel.hardwareHelper.startStrobe(scope, 400)
                    },
                    modifier = Modifier.weight(1f).testTag("flash_strobe_slow"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clignotant", fontSize = 12.sp, textAlign = TextAlign.Center)
                }

                OutlinedButton(
                    onClick = {
                        flashMode = FlashMode.SOS
                        viewModel.hardwareHelper.startSos(scope)
                    },
                    modifier = Modifier.weight(1f).testTag("flash_sos"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SOS Mode", fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
