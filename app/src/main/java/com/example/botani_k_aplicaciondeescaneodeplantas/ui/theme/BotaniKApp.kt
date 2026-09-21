package com.example.botani_k_aplicaciondeescaneodeplantas.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.auth.LoginScreen
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.garden.GardenScreen
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.navigation.BottomBar
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.onboarding.OnboardingScreen
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.plantdetail.PlantDetailScreen
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.profile.ProfileScreen
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.scan.ScanScreen
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.navigation.BotaniKRoute

private val bottomBarRoutes = setOf(
    BotaniKRoute.ScanHome.route,
    BotaniKRoute.Garden.route,
    BotaniKRoute.Profile.route
)

@Composable
fun BotaniKApp() {
    BotaniKTheme {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.hierarchy?.firstOrNull()?.route

        Scaffold(
            bottomBar = {
                if (currentRoute in bottomBarRoutes) {
                    BottomBar(navController = navController, currentRoute = currentRoute)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BotaniKRoute.Onboarding.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BotaniKRoute.Onboarding.route) {
                    OnboardingScreen(
                        onFinished = {
                            navController.navigate(BotaniKRoute.Login.route) {
                                popUpTo(BotaniKRoute.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(BotaniKRoute.Login.route) {
                    LoginScreen(
                        onAuthenticated = {
                            navController.navigate(BotaniKRoute.ScanHome.route) {
                                popUpTo(BotaniKRoute.Login.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(BotaniKRoute.ScanHome.route) {
                    ScanScreen(
                        onPlantIdentified = { plantId ->
                            navController.navigate(BotaniKRoute.PlantDetail.createRoute(plantId))
                        }
                    )
                }
                composable(
                    route = BotaniKRoute.PlantDetail.route,
                    arguments = listOf(
                        navArgument(BotaniKRoute.PlantDetail.ARG_PLANT_ID) {
                            type = NavType.StringType
                        }
                    )
                ) {
                    PlantDetailScreen(
                        onBack = { navController.popBackStack() },
                        onGoToGarden = {
                            navController.navigate(BotaniKRoute.Garden.route) {
                                popUpTo(BotaniKRoute.ScanHome.route)
                            }
                        }
                    )
                }
                composable(BotaniKRoute.Garden.route) {
                    GardenScreen(
                        onPlantClick = { plantId ->
                            navController.navigate(BotaniKRoute.PlantDetail.createRoute(plantId))
                        }
                    )
                }
                composable(BotaniKRoute.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            navController.navigate(BotaniKRoute.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}