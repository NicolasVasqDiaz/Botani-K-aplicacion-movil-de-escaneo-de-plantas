package com.botanik.app.ui.navigation

sealed class BotaniKRoute(val route: String) {
    data object Onboarding : BotaniKRoute("onboarding")
    data object Login : BotaniKRoute("login")
    data object Register : BotaniKRoute("register")
    data object SignIn : BotaniKRoute("sign_in")
    data object ScanHome : BotaniKRoute("scan_home")

    data object Catalog : BotaniKRoute("catalog?userPhotoUrl={userPhotoUrl}") {
        fun createRoute(userPhotoUrl: String? = null) =
            "catalog?userPhotoUrl=${userPhotoUrl.orEmpty()}"
        const val ARG_USER_PHOTO_URL = "userPhotoUrl"
    }

    data object AcercaDe : BotaniKRoute("acerca_de")

    data object PlantDetail : BotaniKRoute(
        "plant_detail/{plantId}?userPhotoUrl={userPhotoUrl}&mlConfidence={mlConfidence}"
    ) {
        fun createRoute(
            plantId: String,
            userPhotoUrl: String? = null,
            mlConfidence: Float? = null
        ) =
            "plant_detail/$plantId?userPhotoUrl=${userPhotoUrl.orEmpty()}&mlConfidence=${mlConfidence ?: ""}"
        const val ARG_PLANT_ID = "plantId"
        const val ARG_USER_PHOTO_URL = "userPhotoUrl"
        const val ARG_ML_CONFIDENCE = "mlConfidence"
    }

    data object Garden : BotaniKRoute("garden")
    data object Profile : BotaniKRoute("profile")
}