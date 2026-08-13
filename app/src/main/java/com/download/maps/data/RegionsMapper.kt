package com.download.maps.data

import com.download.maps.data.model.RegionDto
import com.download.maps.domain.model.Region
import javax.inject.Inject

class RegionsMapper @Inject constructor() {
    private data class InheritedContext(
        val parentId: String?,
        val effectivePrefix: String?,
        val effectiveSuffix: String?
    )

    fun mapToDomainList(rootDto: RegionDto): List<Region> {
        val initialContext = InheritedContext(
            parentId = null,
            effectivePrefix = null,
            effectiveSuffix = null
        )
        return rootDto.children.flatMap { child ->
            mapRecursive(child, initialContext)
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun mapRecursive(
        dto: RegionDto,
        context: InheritedContext
    ): List<Region> {
        val currentId =
            if (context.parentId == null) dto.name else "${context.parentId}/${dto.name}"

        val isMapAvailable = when {
            dto.map == "yes" -> true
            dto.map == "no" -> false
            dto.type == "map" -> true
            dto.type != null -> false
            else -> true
        }

        val effectiveSuffix = dto.downloadSuffix ?: context.effectiveSuffix
        val effectivePrefix = dto.downloadPrefix ?: context.effectivePrefix

        val innerPrefixResolved = dto.innerDownloadPrefix?.let {
            if (it == "\$name") dto.name else it
        }

        val currentRegion = Region(
            id = currentId,
            parentId = context.parentId,
            name = dto.name,
            displayName = parseDisplayName(dto.translate, dto.name),
            fileName = if (isMapAvailable) {
                buildFileName(
                    name = dto.name,
                    prefix = effectivePrefix,
                    suffix = effectiveSuffix
                )
            } else {
                null
            },
            hasSubregions = dto.children.isNotEmpty()
        )

        val childContext = InheritedContext(
            parentId = currentId,
            effectivePrefix = innerPrefixResolved ?: effectivePrefix,
            effectiveSuffix = dto.innerDownloadSuffix ?: effectiveSuffix
        )

        val childrenRegions = dto.children.flatMap { child ->
            mapRecursive(child, childContext)
        }

        return listOf(currentRegion) + childrenRegions
    }

    private fun buildFileName(name: String, prefix: String?, suffix: String?): String {
        val rawFileName = when {
            !prefix.isNullOrEmpty() && !suffix.isNullOrEmpty() -> "${prefix}_${name}_${suffix}"
            !suffix.isNullOrEmpty() -> "${name}_${suffix}"
            !prefix.isNullOrEmpty() -> "${prefix}_${name}"
            else -> name
        }
        return rawFileName.replaceFirstChar { it.uppercase() } + "_2.obf.zip"
    }

    @Suppress("ReturnCount")
    private fun parseDisplayName(translate: String?, defaultName: String): String {
        if (translate == null) return defaultName.replace("-", " ")
            .replaceFirstChar { it.uppercase() }

        val enMatch = Regex("name:en=([^;]+)").find(translate)
        if (enMatch != null) return enMatch.groupValues[1]

        val simpleMatch = Regex("^=([^;]+)").find(translate) ?: Regex("^([^;=]+)").find(translate)
        return simpleMatch?.groupValues?.get(1) ?: defaultName
    }
}
