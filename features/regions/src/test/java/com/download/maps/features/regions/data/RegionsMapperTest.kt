package com.download.maps.features.regions.data

import com.download.maps.features.regions.data.model.RegionDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegionsMapperTest {

    private val mapper = RegionsMapper()

    @Test
    fun `map to domain list flat hierarchy correctly maps regions`() {
        val child1 = RegionDto(
            name = "ukraine",
            translate = "name:en=Ukraine",
            type = "map",
            map = null,
            downloadPrefix = "europe",
            innerDownloadPrefix = null,
            downloadSuffix = "2026",
            innerDownloadSuffix = null,
            children = emptyList()
        )
        val root = RegionDto(
            name = "root",
            translate = null,
            type = null,
            map = null,
            downloadPrefix = null,
            innerDownloadPrefix = null,
            downloadSuffix = null,
            innerDownloadSuffix = null,
            children = listOf(child1)
        )

        val result = mapper.mapToDomainList(root)

        assertEquals(1, result.size)
        val region = result.first()
        assertEquals("ukraine", region.id)
        assertNull(region.parentId)
        assertEquals("ukraine", region.name)
        assertEquals("Ukraine", region.displayName)
        assertEquals("Europe_ukraine_2026_2.obf.zip", region.fileName)
        assertFalse(region.hasSubregions)
    }

    @Test
    fun `map to domain list nested hierarchy inherits prefix suffix and inner name`() {
        val grandChild = RegionDto(
            name = "kyiv",
            translate = null,
            type = null,
            map = "yes",
            downloadPrefix = null,
            innerDownloadPrefix = null,
            downloadSuffix = null,
            innerDownloadSuffix = null,
            children = emptyList()
        )
        val child = RegionDto(
            name = "ukraine",
            translate = "name:en=Ukraine",
            type = null,
            map = "no",
            downloadPrefix = "europe",
            innerDownloadPrefix = "\$name",
            downloadSuffix = "suffix",
            innerDownloadSuffix = "inner_suffix",
            children = listOf(grandChild)
        )
        val root = RegionDto(
            name = "root",
            translate = null,
            type = null,
            map = null,
            downloadPrefix = null,
            innerDownloadPrefix = null,
            downloadSuffix = null,
            innerDownloadSuffix = null,
            children = listOf(child)
        )

        val result = mapper.mapToDomainList(root)

        assertEquals(2, result.size)
        val parentRegion = result[0]
        val childRegion = result[1]

        assertEquals("ukraine", parentRegion.id)
        assertNull(parentRegion.fileName) // map = "no"
        assertTrue(parentRegion.hasSubregions)

        assertEquals("ukraine/kyiv", childRegion.id)
        assertEquals("ukraine", childRegion.parentId)
        assertEquals("Kyiv", childRegion.displayName)
        // innerDownloadPrefix "$name" -> "ukraine", innerDownloadSuffix "inner_suffix"
        assertEquals("Ukraine_kyiv_inner_suffix_2.obf.zip", childRegion.fileName)
        assertFalse(childRegion.hasSubregions)
    }

    @Test
    fun `parse display name handles different translate formats`() {
        val dto1 = createDto("test-region", translate = "name:en=Test Region")
        val dto2 = createDto("test-region", translate = "=Test Simple;foo=bar")
        val dto3 = createDto("test-region", translate = null)

        val root = createDto("root", children = listOf(dto1, dto2, dto3))

        val result = mapper.mapToDomainList(root)

        assertEquals("Test Region", result[0].displayName)
        assertEquals("Test Simple", result[1].displayName)
        assertEquals("Test region", result[2].displayName)
    }

    @Test
    fun `map availability evaluates map and type flags`() {
        val mapYes = createDto("r1", map = "yes")
        val mapNo = createDto("r2", map = "no")
        val typeMap = createDto("r3", type = "map")
        val typeOther = createDto("r4", type = "continent")

        val root = createDto("root", children = listOf(mapYes, mapNo, typeMap, typeOther))

        val result = mapper.mapToDomainList(root)

        assertEquals("R1_2.obf.zip", result[0].fileName)
        assertNull(result[1].fileName)
        assertEquals("R3_2.obf.zip", result[2].fileName)
        assertNull(result[3].fileName)
    }

    private fun createDto(
        name: String,
        translate: String? = null,
        type: String? = null,
        map: String? = null,
        children: List<RegionDto> = emptyList()
    ): RegionDto = RegionDto(
        name = name,
        translate = translate,
        type = type,
        map = map,
        downloadPrefix = null,
        innerDownloadPrefix = null,
        downloadSuffix = null,
        innerDownloadSuffix = null,
        children = children
    )
}
