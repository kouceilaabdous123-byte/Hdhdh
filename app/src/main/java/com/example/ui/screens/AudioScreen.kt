package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.DiagnosticTest
import com.example.data.model.TestStatus
import com.example.ui.components.TestTopBar
import com.example.ui.components.TestVerdictBar
import com.example.ui.theme.DiagCyan
import com.example.ui.theme.DiagGreen
import com.example.ui.theme.DiagRed
import com.example.ui.viewmodel.DiagViewModel
import com.example.util.HardwareHelper
import kotlinx.coroutines.launch

@Composable
fun AudioScreen(
    viewModel: DiagViewModel,
    onBack: () -> Unit
) {
    val testResults by viewModel.testResults.collectAsState()
    val status = testResults[DiagnosticTest.AUDIO] ?: TestStatus.NOT_TESTED
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeToneFreq by remember { mutableStateOf<Int?>(null) }
    var currentPan by remember { mutableFloatStateOf(0f) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    var isRecording by remember { mutableStateOf(false) }
    var recordedSnippet by remember { mutableStateOf<HardwareHelper.AudioRecordResult?>(null) }
    var liveDecibel by remember { mutableFloatStateOf(0f) }

    val animatedDb by animateFloatAsState(targetValue = liveDecibel, label = "animatedDb")

    DisposableEffect(Unit) {
        onDispose {
            viewModel.hardwareHelper.stopTone()
        }
    }

    Scaffold(
        topBar = {
            TestTopBar(
                title = "Test Haut-parleurs & Microphone",
                status = status,
                onBack = onBack
            )
        },
        bottomBar = {
            TestVerdictBar(
                status = status,
                onPass = {
                    viewModel.setTestResult(DiagnosticTest.AUDIO, TestStatus.PASSED)
                    onBack()
                },
                onFail = {
                    viewModel.setTestResult(DiagnosticTest.AUDIO, TestStatus.FAILED)
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
            // Section 1: Speakers & Frequency tones
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = DiagCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test Haut-parleurs (Générateur de tonalité)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Écoutez la clarté du son à différentes fréquences et vérifiez l'équilibre stéréo gauche/droite.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Fréquences audibles :",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(250 to "Basses 250Hz", 1000 to "Médium 1kHz", 4000 to "Aigus 4kHz").forEach { (freq, label) ->
                            val isPlaying = activeToneFreq == freq
                            Button(
                                onClick = {
                                    if (isPlaying) {
                                        viewModel.hardwareHelper.stopTone()
                                        activeToneFreq = null
                                    } else {
                                        activeToneFreq = freq
                                        viewModel.hardwareHelper.playTone(freq, 2500, currentPan)
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("freq_$freq"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPlaying) DiagGreen else MaterialTheme.colorScheme.surface,
                                    contentColor = if (isPlaying) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Équilibre des canaux stéréo :",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(-1f to "Gauche", 0f to "Stéréo", 1f to "Droite").forEach { (pan, label) ->
                            val isSelected = currentPan == pan
                            OutlinedButton(
                                onClick = {
                                    currentPan = pan
                                    activeToneFreq?.let { freq ->
                                        viewModel.hardwareHelper.playTone(freq, 2500, currentPan)
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("pan_$label"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) DiagCyan.copy(alpha = 0.2f) else Color.Transparent
                                )
                            ) {
                                Text(label, fontSize = 12.sp, color = if (isSelected) DiagCyan else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Microphone & Audio Record test
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = DiagCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test Microphone & Enregistrement",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!hasMicPermission) {
                        Text(
                            text = "L'autorisation d'enregistrement audio est requise pour tester la sensibilité du microphone.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { micPermLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("request_mic_perm_btn")
                        ) {
                            Text("Autoriser le microphone")
                        }
                    } else {
                        Text(
                            text = "Enregistrez un court extrait vocal (3s) puis écoutez sa relecture pour confirmer la capture audio.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sound level VU-meter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Niveau sonore d'entrée :",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${animatedDb.toInt()} dB",
                                fontWeight = FontWeight.Bold,
                                color = DiagCyan,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { (animatedDb / 90f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (animatedDb > 70) DiagRed else if (animatedDb > 40) DiagGreen else DiagCyan,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isRecording) {
                                        isRecording = true
                                        scope.launch {
                                            val result = viewModel.hardwareHelper.recordAudioSnippet(3000) { db ->
                                                liveDecibel = db
                                            }
                                            recordedSnippet = result
                                            isRecording = false
                                        }
                                    }
                                },
                                enabled = !isRecording,
                                modifier = Modifier.weight(1f).testTag("record_audio_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRecording) DiagRed else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isRecording) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Capture (3s)...", fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Enregistrer 3s", fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    recordedSnippet?.let { snippet ->
                                        viewModel.hardwareHelper.playRecordedSnippet(snippet)
                                    }
                                },
                                enabled = recordedSnippet != null && !isRecording,
                                modifier = Modifier.weight(1f).testTag("playback_audio_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DiagGreen, contentColor = Color.Black)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Réécouter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
