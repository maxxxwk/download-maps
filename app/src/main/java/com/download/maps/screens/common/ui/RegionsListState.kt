package com.download.maps.screens.common.ui

import com.download.maps.domain.model.Region

sealed interface RegionsListState {
    data object Loading : RegionsListState
    data object Error : RegionsListState
    data class Content(
        val regions: List<Region>,
        val downloadedRegionIds: Set<String> = emptySet(),
        val activeRegionId: String? = null,
        val queuedRegionIds: Set<String> = emptySet(),
        val activeProgress: Int = 0
    ) : RegionsListState
}
