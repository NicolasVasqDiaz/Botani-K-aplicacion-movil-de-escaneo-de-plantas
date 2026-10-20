package com.botanik.app.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.hilt.navigation.compose.hiltViewModel
import com.botanik.app.domain.model.AuthState
import com.botanik.app.ui.auth.LoginScreen
import com.botanik.app.ui.auth.RegisterScreen
import com.botanik.app.ui.auth.SignInScreen
import com.botanik.app.ui.catalog.CatalogScreen
import com.botanik.app.ui.garden.GardenScreen
import com.botanik.app.ui.navigation.BottomBar
import com.botanik.app.ui.onboarding.OnboardingScreen
import com.botanik.app.ui.plantdetail.PlantDetailScreen
import com.botanik.app.ui.profile.ProfileScreen
import com.botanik.app.ui.scan.ScanScreen
import com.botanik.app.ui.navigation.BotaniKRoute
import com.botanik.app.ui.navigation.AuthViewModel

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
        val authViewModel: AuthViewModel = hiltViewModel()
        val authState by authViewModel.authState.collectAsState()

        LaunchedEffect(authState) {
            when (authState) {
                is com.botanik.app.domain.model.AuthState.Authenticated -> {
                    navController.navigate(BotaniKRoute.ScanHome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                is com.botanik.app.domain.model.AuthState.Unauthenticated -> {
                    if (currentRoute != BotaniKRoute.Login.route && currentRoute != BotaniKRoute.Onboarding.route) {
                        navController.navigate(BotaniKRoute.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                else -> { /* Loading / Error: no navegar */ }
            }
        }

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
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(BotaniKRoute.Register.route)
                        },
                        onNavigateToSignIn = {
                            navController.navigate(BotaniKRoute.SignIn.route)
                        }
                    )
                }
                composable(BotaniKRoute.SignIn.route) {
                    SignInScreen(
                        onSignedIn = {
                            navController.navigate(BotaniKRoute.ScanHome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(BotaniKRoute.Register.route) {
                    RegisterScreen(
                        onRegistered = {
                            navController.navigate(BotaniKRoute.ScanHome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(BotaniKRoute.ScanHome.route) {
                    ScanScreen(
                        onBrowseCatalog = {
                            navController.navigate(BotaniKRoute.Catalog.route)
                        },
                        onPhotoReady = { photoUri ->
                            navController.navigate(BotaniKRoute.Catalog.createRoute(photoUri))
                        }
                    )
                }
                composable(
                    route = BotaniKRoute.Catalog.route,
                    arguments = listOf(
                        navArgument(BotaniKRoute.Catalog.ARG_USER_PHOTO_URL) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val userPhotoUrl = backStackEntry.arguments?.getString(BotaniKRoute.Catalog.ARG_USER_PHOTO_URL)
                    CatalogScreen(
                        userPhotoUrl = userPhotoUrl?.ifEmpty { null },
                        onBack = { navController.popBackStack() },
                        onPlantSelected = { plant ->
                            navController.navigate(BotaniKRoute.PlantDetail.createRoute(plant.id, userPhotoUrl?.ifEmpty { null }))
                        }
                    )
                }
                composable(
                    route = BotaniKRoute.PlantDetail.route,
                    arguments = listOf(
                        navArgument(BotaniKRoute.PlantDetail.ARG_PLANT_ID) {
                            type = NavType.StringType
                        },
                        navArgument(BotaniKRoute.PlantDetail.ARG_USER_PHOTO_URL) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val plantId = backStackEntry.arguments?.getString(BotaniKRoute.PlantDetail.ARG_PLANT_ID)
                    val userPhotoUrl = backStackEntry.arguments?.getString(BotaniKRoute.PlantDetail.ARG_USER_PHOTO_URL)
                    if (plantId == null) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        PlantDetailScreen(
                            plantId = plantId,
                            userPhotoUrl = userPhotoUrl?.ifEmpty { null },
                            onBack = { navController.popBackStack() },
                            onGoToGarden = {
                                navController.navigate(BotaniKRoute.Garden.route) {
                                    popUpTo(BotaniKRoute.ScanHome.route)
                                }
                            }
                        )
                    }
                }
                composable(BotaniKRoute.Garden.route) {
                    GardenScreen(
                        onPlantClick = { plantId ->
                            navController.navigate(BotaniKRoute.PlantDetail.createRoute(plantId))
                        },
                        onScanClick = {
                            navController.navigate(BotaniKRoute.ScanHome.route) {
                                launchSingleTop = true
                            }
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
