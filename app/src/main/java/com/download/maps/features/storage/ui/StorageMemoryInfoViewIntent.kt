package com.download.maps.features.storage.ui

sealed interface StorageMemoryInfoViewIntent {
    data object OnLoadStorage : StorageMemoryInfoViewIntent
}
