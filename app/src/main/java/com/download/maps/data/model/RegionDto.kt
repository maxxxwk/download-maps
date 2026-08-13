package com.download.maps.data.model

data class RegionDto(
    val name: String,
    val translate: String?,
    val type: String?,
    val map: String?,
    val downloadPrefix: String?,
    val innerDownloadPrefix: String?,
    val downloadSuffix: String?,
    val innerDownloadSuffix: String?,
    val children: List<RegionDto> = emptyList()
)
