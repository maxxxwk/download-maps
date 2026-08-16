package com.download.maps.screens.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute : NavKey {

    @Serializable
    data object MainScreen : NavRoute

    @Serializable
    data class RegionsScreen(
        val parentRegionId: String,
        val parentRegionName: String
    ) : NavRoute
}
