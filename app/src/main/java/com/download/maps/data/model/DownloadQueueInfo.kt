package com.download.maps.data.model

data class DownloadQueueInfo(
    val activeRegionId: String? = null,
    val queuedRegionIds: Set<String> = emptySet()
)
