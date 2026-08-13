package com.download.maps.screens.main.ui

sealed interface DeviceStorageIntent {
    data object OnLoadStorage : DeviceStorageIntent
}
