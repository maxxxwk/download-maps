package com.download.maps.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object MainScreenRoute : NavKey

@Serializable
data class RegionsScreenRoute(
    val parentRegionId: String,
    val parentRegionName: String
) : NavKey
