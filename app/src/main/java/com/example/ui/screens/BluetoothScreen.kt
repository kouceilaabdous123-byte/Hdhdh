package com.example.ui.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun BluetoothScreen(
    viewModel: DiagViewModel,
    onBack: () -> Unit
) {
    val testResults by viewModel.testResults.collectAsState()
    val status = testResults[DiagnosticTest.BLUETOOTH] ?: TestStatus.NOT_TESTED
    val context = LocalContext.current

    val bluetoothManager = remember { context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager }
    val bluetoothAdapter = remember { bluetoothManager?.adapter }

    val isBtSupported = bluetoothAdapter != null
    val hasBle = remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }

    var hasConnectPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasConnectPermission = granted
    }

    val isEnabled = bluetoothAdapter?.isEnabled == true

    // Retrieve paired devices
    val pairedDevices = remember(isEnabled, hasConnectPermission) {
        if (isEnabled && hasConnectPermission) {
            try {
                @Suppress("MissingPermission")
                bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
            } catch (_: SecurityException) {
                emptyList()
            }
        } else emptyList()
    }

    Scaffold(
        topBar = {
            TestTopBar(
                title = "Test Bluetooth",
                status = status,
                onBack = onBack
            )
        },
        bottomBar = {
            TestVerdictBar(
                status = status,
                onPass = {
                    viewModel.setTestResult(DiagnosticTest.BLUETOOTH, TestStatus.PASSED)
                    onBack()
                },
                onFail = {
                    viewModel.setTestResult(DiagnosticTest.BLUETOOTH, TestStatus.FAILED)
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
            // Main status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEnabled) DiagCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled,
                            contentDescription = null,
                            tint = if (isEnabled) DiagCyan else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isBtSupported) {
                            if (isEnabled) "Puce Bluetooth Active" else "Bluetooth Désactivé"
                        } else "Puce Bluetooth Absente",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isEnabled) DiagCyan else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isBtSupported) {
                            "L'adaptateur matériel répond aux requêtes du système."
                        } else "Ce terminal ne dispose pas d'adaptateur Bluetooth.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_bt_settings")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ouvrir Paramètres Bluetooth")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tech Specs
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Capacités matérielles",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ChecklistRow(
                        label = "Support matériel Bluetooth",
                        done = isBtSupported
                    )
                    ChecklistRow(
                        label = "Support Bluetooth Low Energy (BLE 4.0+)",
                        done = hasBle
                    )
                    ChecklistRow(
                        label = "Statut activé / opérationnel",
                        done = isEnabled
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Paired devices list
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DevicesOther, contentDescription = null, tint = DiagCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Appareils appairés (${pairedDevices.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        if (!hasConnectPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            OutlinedButton(
                                onClick = { permLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("request_bt_perm_btn")
                            ) {
                                Text("Autoriser", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (pairedDevices.isEmpty()) {
                        Text(
                            text = if (!isEnabled) "Activez le Bluetooth pour scanner les périphériques."
                            else if (!hasConnectPermission) "Autorisation BLUETOOTH_CONNECT requise pour lister les appareils."
                            else "Aucun appareil Bluetooth actuellement mémorisé.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        pairedDevices.take(6).forEach { device ->
                            @Suppress("MissingPermission")
                            val devName = try { device.name ?: "Appareil Bluetooth" } catch (_: SecurityException) { "Périphérique" }
                            @Suppress("MissingPermission")
                            val devAddress = try { device.address } catch (_: SecurityException) { "--:--:--" }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Bluetooth, contentDescription = null, tint = DiagCyan, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = devName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(text = devAddress, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
