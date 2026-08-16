package com.download.maps.features.regions.data.model

data class DownloadQueueInfo(
    val activeRegionId: String? = null,
    val queuedRegionIds: Set<String> = emptySet()
)
