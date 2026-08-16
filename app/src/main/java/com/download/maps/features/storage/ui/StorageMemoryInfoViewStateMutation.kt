package com.download.maps.features.storage.ui

import com.download.maps.features.storage.data.model.StorageMemoryInfo

sealed interface StorageMemoryInfoViewStateMutation {
    data class StorageLoaded(val storageMemoryInfo: StorageMemoryInfo?) : StorageMemoryInfoViewStateMutation
}
