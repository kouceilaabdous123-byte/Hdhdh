package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.DiagnosticTest
import com.example.ui.navigation.Screen
import com.example.ui.screens.AudioScreen
import com.example.ui.screens.BatteryScreen
import com.example.ui.screens.BluetoothScreen
import com.example.ui.screens.ButtonsScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.DisplayScreen
import com.example.ui.screens.FlashlightScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MultiTouchScreen
import com.example.ui.screens.MultitaskScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SensorsScreen
import com.example.ui.screens.VibrationScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DiagViewModel

class MainActivity : ComponentActivity() {

    private val diagViewModel: DiagViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DiagNavGraph(viewModel = diagViewModel)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                diagViewModel.onVolumeUpTriggered()
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                diagViewModel.onVolumeDownTriggered()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun DiagNavGraph(viewModel: DiagViewModel) {
    val navController = rememberNavController()

    fun navigateToTest(test: DiagnosticTest) {
        val route = when (test) {
            DiagnosticTest.FLASHLIGHT -> Screen.Flashlight.route
            DiagnosticTest.CAMERA -> Screen.Camera.route
            DiagnosticTest.DISPLAY -> Screen.Display.route
            DiagnosticTest.MULTITOUCH -> Screen.MultiTouch.route
            DiagnosticTest.BUTTONS -> Screen.Buttons.route
            DiagnosticTest.BATTERY -> Screen.Battery.route
            DiagnosticTest.BLUETOOTH -> Screen.Bluetooth.route
            DiagnosticTest.VIBRATION -> Screen.Vibration.route
            DiagnosticTest.AUDIO -> Screen.Audio.route
            DiagnosticTest.SENSORS -> Screen.Sensors.route
            DiagnosticTest.MULTITASK -> Screen.Multitask.route
        }
        navController.navigate(route)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToTest = { test -> navigateToTest(test) },
                onNavigateToReport = { navController.navigate(Screen.Report.route) }
            )
        }
        composable(Screen.Flashlight.route) {
            FlashlightScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Camera.route) {
            CameraScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Display.route) {
            DisplayScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.MultiTouch.route) {
            MultiTouchScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Buttons.route) {
            ButtonsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Battery.route) {
            BatteryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Bluetooth.route) {
            BluetoothScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Vibration.route) {
            VibrationScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Audio.route) {
            AudioScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Sensors.route) {
            SensorsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Multitask.route) {
            MultitaskScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Report.route) {
            ReportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
