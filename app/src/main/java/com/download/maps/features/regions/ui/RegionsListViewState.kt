package com.download.maps.features.regions.ui

import com.download.maps.features.regions.domain.model.Region

sealed interface RegionsListViewState {
    data object Loading : RegionsListViewState
    data object Error : RegionsListViewState
    data class Content(
        val regions: List<Region>,
        val downloadedRegionIds: Set<String> = emptySet(),
        val activeRegionId: String? = null,
        val queuedRegionIds: Set<String> = emptySet(),
        val activeProgress: Int = 0
    ) : RegionsListViewState
}
