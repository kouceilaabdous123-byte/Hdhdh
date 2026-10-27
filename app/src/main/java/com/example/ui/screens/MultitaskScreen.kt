package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiagnosticTest
import com.example.data.model.TestStatus
import com.example.ui.components.TestTopBar
import com.example.ui.components.TestVerdictBar
import com.example.ui.theme.DiagCyan
import com.example.ui.theme.DiagGreen
import com.example.ui.theme.DiagRed
import com.example.ui.viewmodel.DiagViewModel

@Composable
fun MultitaskScreen(
    viewModel: DiagViewModel,
    onBack: () -> Unit
) {
    val testResults by viewModel.testResults.collectAsState()
    val status = testResults[DiagnosticTest.MULTITASK] ?: TestStatus.NOT_TESTED
    val context = LocalContext.current
    val activity = context as? Activity
    val sysInfo by viewModel.systemInfo.collectAsState()

    val isInMultiWindow = activity?.isInMultiWindowMode == true

    Scaffold(
        topBar = {
            TestTopBar(
                title = "Multitâche & Spécifications",
                status = status,
                onBack = onBack
            )
        },
        bottomBar = {
            TestVerdictBar(
                status = status,
                onPass = {
                    viewModel.setTestResult(DiagnosticTest.MULTITASK, TestStatus.PASSED)
                    onBack()
                },
                onFail = {
                    viewModel.setTestResult(DiagnosticTest.MULTITASK, TestStatus.FAILED)
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
            // Multitasking & Multi-window card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isInMultiWindow) DiagGreen.copy(alpha = 0.2f) else DiagCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Splitscreen,
                                contentDescription = null,
                                tint = if (isInMultiWindow) DiagGreen else DiagCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Test Mode Multi-Fenêtre (Split-screen)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = if (isInMultiWindow) "Mode multi-fenêtre actif !" else "Actuellement en plein écran",
                                fontSize = 12.sp,
                                color = if (isInMultiWindow) DiagGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Pour tester le multitâche Android : ouvrez l'écran des applications récentes (geste du bas vers le haut avec maintien), appuyez sur l'icône de l'app puis sélectionnez 'Écran partagé' ou glissez une autre application à côté.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ChecklistRow(
                        label = "Prise en charge du redimensionnement dynamique",
                        done = true
                    )
                    ChecklistRow(
                        label = "Statut Split-screen détecté",
                        done = isInMultiWindow
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RAM Memory Section
            if (sysInfo != null) {
                val info = sysInfo!!
                val ramProgress = if (info.totalRamMb > 0) (info.usedRamMb.toFloat() / info.totalRamMb.toFloat()) else 0f

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
                                Icon(Icons.Default.Memory, contentDescription = null, tint = DiagCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mémoire Vive (RAM)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                text = "${info.usedRamMb} / ${info.totalRamMb} Mo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DiagCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { ramProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (ramProgress > 0.85f) DiagRed else DiagCyan,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${info.freeRamMb} Mo de mémoire disponible immédiatement",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Storage Section
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
                                Icon(Icons.Default.SdStorage, contentDescription = null, tint = DiagGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Stockage Interne",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                text = "${info.usedStorageGb} / ${info.totalStorageGb} Go",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DiagGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val totalSt = info.totalStorageGb.toDoubleOrNull() ?: 1.0
                        val usedSt = info.usedStorageGb.toDoubleOrNull() ?: 0.0
                        val storageProgress = (usedSt / totalSt.coerceAtLeast(0.1)).toFloat()

                        LinearProgressIndicator(
                            progress = { storageProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = DiagGreen,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${info.freeStorageGb} Go d'espace libre",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CPU & Hardware Board
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = DiagCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Processeur & Carte Mère",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        InfoRow("Cœurs CPU disponibles :", "${info.cpuCores} cœurs d'exécution")
                        InfoRow("Architecture & ABIs :", info.cpuAbis)
                        InfoRow("Carte / Carte mère :", info.board)
                        InfoRow("Périphérique matériel :", info.device)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // OS System
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = DiagCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Système d'exploitation & Modèle",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        InfoRow("Constructeur :", info.manufacturer)
                        InfoRow("Modèle commercial :", "${info.brand} ${info.model}")
                        InfoRow("Version Android :", "Android ${info.androidVersion} (API ${info.sdkInt})")
                        InfoRow("Correctif de sécurité :", info.securityPatch)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
