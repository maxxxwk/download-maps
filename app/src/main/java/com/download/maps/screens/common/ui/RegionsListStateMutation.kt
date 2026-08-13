package com.download.maps.screens.common.ui

import com.download.maps.domain.model.Region

sealed interface RegionsListStateMutation {
    data object Loading : RegionsListStateMutation
    data object Error : RegionsListStateMutation
    data class RegionsLoaded(val regions: List<Region>) : RegionsListStateMutation
    data class DownloadedUpdated(val downloadedRegionIds: Set<String>) : RegionsListStateMutation
    data class ProgressUpdated(val regionId: String, val progress: Int) : RegionsListStateMutation
    data class QueueUpdated(
        val activeRegionId: String?,
        val queuedRegionIds: Set<String>
    ) : RegionsListStateMutation
}
