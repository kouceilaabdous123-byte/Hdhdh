package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Vibration
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiagnosticTest
import com.example.data.model.TestStatus
import com.example.ui.components.TestTopBar
import com.example.ui.components.TestVerdictBar
import com.example.ui.theme.DiagCyan
import com.example.ui.theme.DiagGreen
import com.example.ui.viewmodel.DiagViewModel
import kotlinx.coroutines.launch

@Composable
fun VibrationScreen(
    viewModel: DiagViewModel,
    onBack: () -> Unit
) {
    val testResults by viewModel.testResults.collectAsState()
    val status = testResults[DiagnosticTest.VIBRATION] ?: TestStatus.NOT_TESTED
    val scope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShakeAnimation() {
        scope.launch {
            shakeOffset.snapTo(0f)
            for (i in 0 until 4) {
                shakeOffset.animateTo(8f, tween(30))
                shakeOffset.animateTo(-8f, tween(30))
            }
            shakeOffset.animateTo(0f, tween(30))
        }
    }

    val hasVibrator = remember { viewModel.hardwareHelper.hasVibrator() }

    Scaffold(
        topBar = {
            TestTopBar(
                title = "Test Vibreur & Haptique",
                status = status,
                onBack = onBack
            )
        },
        bottomBar = {
            TestVerdictBar(
                status = status,
                onPass = {
                    viewModel.setTestResult(DiagnosticTest.VIBRATION, TestStatus.PASSED)
                    onBack()
                },
                onFail = {
                    viewModel.setTestResult(DiagnosticTest.VIBRATION, TestStatus.FAILED)
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
                        text = "Vérifiez que le moteur haptique vibre franchement sans bruit parasite et réagit aux différents profils de pulsation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Phone Icon with vibration effect
            Box(
                modifier = Modifier
                    .offset(x = shakeOffset.value.dp)
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(DiagCyan.copy(alpha = 0.15f))
                    .border(3.dp, DiagCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = "Vibreur",
                    tint = DiagCyan,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasVibrator) "Moteur de vibration détecté" else "Aucun vibreur matériel",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = if (hasVibrator) DiagGreen else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Modèles de vibration",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Short Pulse
            VibrationPatternButton(
                title = "Clic Haptique Court (100 ms)",
                subtitle = "Sensation de touche clavier ou de clic",
                onClick = {
                    triggerShakeAnimation()
                    viewModel.hardwareHelper.vibrateShort()
                },
                testTag = "vib_short"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Medium Pulse
            VibrationPatternButton(
                title = "Notification Moyenne (350 ms)",
                subtitle = "Sensation de notification standard",
                onClick = {
                    triggerShakeAnimation()
                    viewModel.hardwareHelper.vibrateMedium()
                },
                testTag = "vib_medium"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Long Buzz
            VibrationPatternButton(
                title = "Vibration Longue (800 ms)",
                subtitle = "Sensation d'appel entrant",
                onClick = {
                    triggerShakeAnimation()
                    viewModel.hardwareHelper.vibrateLong()
                },
                testTag = "vib_long"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. SOS Rhythmic Pattern
            VibrationPatternButton(
                title = "Motif Rythmé SOS (... --- ...)",
                subtitle = "Série cadencée de pulsations haptiques",
                onClick = {
                    triggerShakeAnimation()
                    viewModel.hardwareHelper.vibrateSos()
                },
                testTag = "vib_sos"
            )
        }
    }
}

@Composable
fun VibrationPatternButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag(testTag)
            ) {
                Text("Tester", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
