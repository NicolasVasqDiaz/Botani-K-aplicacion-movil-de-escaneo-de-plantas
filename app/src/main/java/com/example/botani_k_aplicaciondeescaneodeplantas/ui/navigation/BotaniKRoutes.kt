package com.example.botani_k_aplicaciondeescaneodeplantas.ui.navigation

sealed class BotaniKRoute(val route: String) {
    data object Onboarding : BotaniKRoute("onboarding")
    data object Login : BotaniKRoute("login")
    data object ScanHome : BotaniKRoute("scan_home")
    data object PlantDetail : BotaniKRoute("plant_detail/{plantId}") {
        fun createRoute(plantId: String) = "plant_detail/$plantId"
        const val ARG_PLANT_ID = "plantId"
    }
    data object Garden : BotaniKRoute("garden")
    data object Profile : BotaniKRoute("profile")
}