package com.download.maps.screens.main.data.model

data class StorageInfo(
    val totalBytes: Long,
    val freeBytes: Long
) {
    val usedRatio: Float =
        if (totalBytes > 0) (totalBytes - freeBytes).toFloat() / totalBytes else 0f
}
