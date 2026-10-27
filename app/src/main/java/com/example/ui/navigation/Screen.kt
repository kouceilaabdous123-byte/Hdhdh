package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Flashlight : Screen("flashlight")
    data object Camera : Screen("camera")
    data object Display : Screen("display")
    data object MultiTouch : Screen("multitouch")
    data object Buttons : Screen("buttons")
    data object Battery : Screen("battery")
    data object Bluetooth : Screen("bluetooth")
    data object Vibration : Screen("vibration")
    data object Audio : Screen("audio")
    data object Sensors : Screen("sensors")
    data object Multitask : Screen("multitask")
    data object Report : Screen("report")
}
