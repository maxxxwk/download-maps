package com.download.maps.screens.main.ui

import com.download.maps.screens.main.data.model.StorageInfo

sealed interface DeviceStorageMutation {
    data class StorageLoaded(val storageInfo: StorageInfo?) : DeviceStorageMutation
}
