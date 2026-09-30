package com.download.maps.screens.regions.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class RegionsScreenRoute(
    val parentRegionId: String,
    val parentRegionName: String
) : NavKey
