package com.example.carpetneed.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object CuttingPlan : Screen("cutting_plan")
    data object MultipleRooms : Screen("multiple_rooms")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object About : Screen("about")
}
