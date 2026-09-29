package com.download.maps.features.regions.domain.model

internal data class Region(
    val id: String,
    val parentId: String?,
    val name: String,
    val displayName: String,
    val fileName: String?,
    val hasSubregions: Boolean
)
