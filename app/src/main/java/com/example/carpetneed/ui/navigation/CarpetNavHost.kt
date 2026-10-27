package com.example.carpetneed.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.carpetneed.ui.screens.*
import com.example.carpetneed.ui.viewmodel.CarpetNeedViewModel

@Composable
fun CarpetNavHost(
    viewModel: CarpetNeedViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                uiState = uiState,
                onNavigateToCuttingPlan = { navController.navigate(Screen.CuttingPlan.route) },
                onNavigateToMultipleRooms = { navController.navigate(Screen.MultipleRooms.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.CuttingPlan.route) {
            CuttingPlanScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MultipleRooms.route) {
            MultipleRoomsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onOpenCalculation = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
