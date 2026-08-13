package com.download.maps.screens.common.ui

sealed interface RegionsListIntent {
    data object OnReload : RegionsListIntent
    data class OnDownload(val regionId: String, val fileName: String) : RegionsListIntent
    data class OnCancelDownload(val regionId: String) : RegionsListIntent
}
