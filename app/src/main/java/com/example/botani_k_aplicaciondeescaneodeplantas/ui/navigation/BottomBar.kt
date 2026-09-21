package com.example.botani_k_aplicaciondeescaneodeplantas.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.theme.BackgroundCream
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.theme.PrimaryGreen
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.theme.TextPrimary

private data class BottomBarItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val items = listOf(
    BottomBarItem(BotaniKRoute.Garden.route, "Mi Jardín", Icons.Filled.Yard),
    BottomBarItem(BotaniKRoute.ScanHome.route, "Escanear", Icons.Filled.CameraAlt),
    BottomBarItem(BotaniKRoute.Profile.route, "Mi Perfil", Icons.Filled.AccountCircle)
)

@Composable
fun BottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = BackgroundCream) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(BotaniKRoute.ScanHome.route)
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = TextPrimary.copy(alpha = 0.6f),
                    unselectedTextColor = TextPrimary.copy(alpha = 0.6f),
                    indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                )
            )
        }
    }
}