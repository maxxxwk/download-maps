package com.download.maps.screens.common.ui

sealed interface RegionsListIntent {
    data object Reload : RegionsListIntent
    data class Download(val regionId: String, val fileName: String) : RegionsListIntent
    data class CancelDownload(val regionId: String) : RegionsListIntent
}
