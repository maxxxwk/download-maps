package com.download.maps.features.regions.ui

import com.download.maps.features.regions.domain.model.Region

sealed interface RegionsListViewStateMutation {
    data object Loading : RegionsListViewStateMutation
    data object Error : RegionsListViewStateMutation
    data class RegionsLoaded(val regions: List<Region>) : RegionsListViewStateMutation
    data class DownloadedUpdated(val downloadedRegionIds: Set<String>) : RegionsListViewStateMutation
    data class ProgressUpdated(val regionId: String, val progress: Int) : RegionsListViewStateMutation
    data class QueueUpdated(
        val activeRegionId: String?,
        val queuedRegionIds: Set<String>
    ) : RegionsListViewStateMutation
}
