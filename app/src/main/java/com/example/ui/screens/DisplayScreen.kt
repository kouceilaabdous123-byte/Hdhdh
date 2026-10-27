package com.example.ui.screens

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun DisplayScreen(
    viewModel: DiagViewModel,
    onBack: () -> Unit
) {
    val testResults by viewModel.testResults.collectAsState()
    val status = testResults[DiagnosticTest.DISPLAY] ?: TestStatus.NOT_TESTED
    val context = LocalContext.current
    val activity = context as? Activity

    var brightnessVal by remember { mutableFloatStateOf(0.7f) }
    var isFullscreenRgbMode by remember { mutableStateOf(false) }
    var colorIndex by remember { mutableIntStateOf(0) }

    val rgbColors = remember {
        listOf(
            Color.Red to "Rouge Pur",
            Color.Green to "Vert Pur",
            Color.Blue to "Bleu Pur",
            Color.White to "Blanc (Test Fuite & Uniformité)",
            Color.Black to "Noir (Test Pixels Bloqués)",
            Color.Yellow to "Jaune",
            Color.Cyan to "Cyan",
            Color.Magenta to "Magenta"
        )
    }

    // Restore default screen brightness when exiting
    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.attributes = activity?.window?.attributes?.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }
    }

    // Apply brightness
    fun setScreenBrightness(value: Float) {
        brightnessVal = value
        activity?.window?.let { window ->
            val lp = window.attributes
            lp.screenBrightness = value.coerceIn(0.01f, 1.0f)
            window.attributes = lp
        }
    }

    fun resetBrightnessToSystem() {
        activity?.window?.let { window ->
            val lp = window.attributes
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.attributes = lp
        }
    }

    // Screen specs
    val displayMetrics = remember {
        val dm = DisplayMetrics()
        @Suppress("DEPRECATION")
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
        dm
    }

    val refreshRate = remember {
        @Suppress("DEPRECATION")
        activity?.windowManager?.defaultDisplay?.refreshRate?.toInt() ?: 60
    }

    if (isFullscreenRgbMode) {
        BackHandler {
            isFullscreenRgbMode = false
        }
        val currentColor = rgbColors[colorIndex]
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(currentColor.first)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (colorIndex < rgbColors.size - 1) {
                        colorIndex++
                    } else {
                        isFullscreenRgbMode = false
                    }
                }
                .testTag("rgb_fullscreen_box")
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${currentColor.second} (${colorIndex + 1}/${rgbColors.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Touchez l'écran pour couleur suivante • Retour pour quitter",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TestTopBar(
                title = "Écran & Rétroéclairage",
                status = status,
                onBack = onBack
            )
        },
        bottomBar = {
            TestVerdictBar(
                status = status,
                onPass = {
                    viewModel.setTestResult(DiagnosticTest.DISPLAY, TestStatus.PASSED)
                    onBack()
                },
                onFail = {
                    viewModel.setTestResult(DiagnosticTest.DISPLAY, TestStatus.FAILED)
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
            // Intro
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
                        text = "Testez la plage du rétroéclairage de 0% à 100%, l'absence de scintillement et détectez les pixels morts ou les fuites de lumière.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Brightness Control Section
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
                            Icon(Icons.Default.Brightness7, contentDescription = null, tint = DiagCyan)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Luminosité de l'écran",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            text = "${(brightnessVal * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = DiagCyan,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = brightnessVal,
                        onValueChange = { setScreenBrightness(it) },
                        valueRange = 0.01f..1.0f,
                        modifier = Modifier.testTag("brightness_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = DiagCyan,
                            activeTrackColor = DiagCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Niveaux prédéfinis",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.05f to "Min", 0.35f to "35%", 0.70f to "70%", 1.0f to "100%").forEach { (valStep, label) ->
                            OutlinedButton(
                                onClick = { setScreenBrightness(valStep) },
                                modifier = Modifier.weight(1f).testTag("preset_$label"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { resetBrightnessToSystem() },
                        modifier = Modifier.fillMaxWidth().testTag("preset_auto"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Rétablir luminosité automatique du système")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dead Pixel Test Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = DiagCyan)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Test Pixels Morts & Couleurs RVB",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Affiche successivement un écran plein format rouge, vert, bleu, blanc, noir et autres teintes pour repérer tout pixel bloqué, décoloré ou anomalie de dalle.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            colorIndex = 0
                            isFullscreenRgbMode = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_rgb_pixel_test"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Lancer le test plein écran", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Hardware Characteristics
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = DiagCyan)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Spécifications de la dalle",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Résolution :", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${displayMetrics.widthPixels} x ${displayMetrics.heightPixels} px", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Densité :", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${displayMetrics.densityDpi} DPI (${String.format("%.1f", displayMetrics.density)}x)", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Taux de rafraîchissement :", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$refreshRate Hz", fontWeight = FontWeight.SemiBold, color = DiagCyan)
                    }
                }
            }
        }
    }
}
