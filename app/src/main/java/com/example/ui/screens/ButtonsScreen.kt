package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.DiagRed
import com.example.ui.viewmodel.DiagViewModel

@Composable
fun ButtonsScreen(
    viewModel: DiagViewModel,
    onBack: () -> Unit
) {
    val testResults by viewModel.testResults.collectAsState()
    val status = testResults[DiagnosticTest.BUTTONS] ?: TestStatus.NOT_TESTED
    val buttonsState by viewModel.buttonsState.collectAsState()

    // Intercept back button while testing buttons
    BackHandler(enabled = !buttonsState.backPressed) {
        viewModel.onBackTriggered()
    }

    Scaffold(
        topBar = {
            TestTopBar(
                title = "Test des Boutons Physiques",
                status = status,
                onBack = onBack
            )
        },
        bottomBar = {
            TestVerdictBar(
                status = status,
                onPass = {
                    viewModel.setTestResult(DiagnosticTest.BUTTONS, TestStatus.PASSED)
                    onBack()
                },
                onFail = {
                    viewModel.setTestResult(DiagnosticTest.BUTTONS, TestStatus.FAILED)
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
                        text = "Instructions de test",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Appuyez successivement sur les touches physiques de votre téléphone pour vérifier leur réactivité et l'absence de faux contacts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress banner
            if (buttonsState.isComplete) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DiagGreen.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = DiagGreen, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tous les boutons physiques ont répondu avec succès !",
                            fontWeight = FontWeight.Bold,
                            color = DiagGreen,
                            fontSize = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Button 1: Volume Up
            ButtonTestItem(
                icon = Icons.Default.VolumeUp,
                title = "Touche Volume Haut (+)",
                subtitle = "Appuyez sur la touche d'augmentation du son",
                isPressed = buttonsState.volUpPressed,
                testTag = "btn_vol_up"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Button 2: Volume Down
            ButtonTestItem(
                icon = Icons.Default.VolumeDown,
                title = "Touche Volume Bas (-)",
                subtitle = "Appuyez sur la touche de diminution du son",
                isPressed = buttonsState.volDownPressed,
                testTag = "btn_vol_down"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Button 3: Back Key / Gesture
            ButtonTestItem(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                title = "Bouton / Geste Retour",
                subtitle = if (buttonsState.backPressed) "Geste ou touche retour validé" else "Faites le geste retour ou appuyez sur Retour",
                isPressed = buttonsState.backPressed,
                testTag = "btn_back",
                action = {
                    if (!buttonsState.backPressed) {
                        viewModel.onBackTriggered()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Power button notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Bouton Marche / Arrêt (Power)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Par sécurité, Android réserve l'interception de la touche Power au système d'exploitation. Vous pouvez tester sa coupure en verrouillant/déverrouillant l'écran.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.resetButtonsTest() },
                modifier = Modifier.testTag("reset_buttons_test"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recommencer le test des touches")
            }
        }
    }
}

@Composable
fun ButtonTestItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isPressed: Boolean,
    testTag: String,
    action: (() -> Unit)? = null
) {
    val borderColor by animateColorAsState(
        targetValue = if (isPressed) DiagGreen else MaterialTheme.colorScheme.outline,
        label = "btnBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isPressed) DiagGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        label = "btnBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isPressed) DiagGreen else MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPressed) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = if (isPressed) Color.Black else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isPressed) DiagGreen else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (action != null && !isPressed) {
                OutlinedButton(
                    onClick = action,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag(testTag)
                ) {
                    Text("Tester", fontSize = 12.sp)
                }
            } else {
                Text(
                    text = if (isPressed) "VALIDÉ" else "EN ATTENTE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isPressed) DiagGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
