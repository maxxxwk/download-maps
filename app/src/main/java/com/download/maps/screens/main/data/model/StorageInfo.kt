package com.download.maps.screens.main.data.model

data class StorageInfo(
    val totalBytes: Long,
    val freeBytes: Long
) {
    val usedBytes: Long = totalBytes - freeBytes
    val usedRatio: Float = if (totalBytes > 0) usedBytes.toFloat() / totalBytes else 0f
}
