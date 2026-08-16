package com.download.maps.features.regions.ui

sealed interface RegionsListViewIntent {
    data object Reload : RegionsListViewIntent
    data class Download(val regionId: String, val fileName: String) : RegionsListViewIntent
    data class CancelDownload(val regionId: String) : RegionsListViewIntent
}
