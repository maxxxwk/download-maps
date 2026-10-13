package com.download.maps.features.regions.data

import android.content.Context
import android.content.res.AssetManager
import com.download.maps.features.regions.data.api.DownloadService
import com.download.maps.features.regions.data.model.RegionDto
import com.download.maps.features.regions.domain.model.Region
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class RegionsRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val context: Context = mockk()
    private val assetManager: AssetManager = mockk()
    private val xmlParser: RegionsXmlParser = mockk()
    private val mapper: RegionsMapper = mockk()
    private val downloadService: DownloadService = mockk()

    private lateinit var repository: RegionsRepository

    @BeforeEach
    fun setUp() {
        every { context.assets } returns assetManager
        every { assetManager.open("regions.xml") } answers { ByteArrayInputStream("<xml/>".toByteArray()) }

        repository = RegionsRepository(
            dispatcher = testDispatcher,
            context = context,
            regionsXmlParser = xmlParser,
            regionsMapper = mapper,
            downloadService = downloadService
        )
    }

    @Test
    fun `get regions by parent id returns regions matching parent id with available map`() = runTest {
        val rootDto = mockk<RegionDto>()
        every { xmlParser.parse(any()) } returns rootDto

        val region1 = Region(
            id = "europe/ukraine",
            parentId = "europe",
            name = "ukraine",
            displayName = "Ukraine",
            fileName = "Ukraine_2.obf.zip",
            hasSubregions = false
        )
        val region2 = Region(
            id = "asia/japan",
            parentId = "asia",
            name = "japan",
            displayName = "Japan",
            fileName = "Japan_2.obf.zip",
            hasSubregions = false
        )

        every { mapper.mapToDomainList(rootDto) } returns listOf(region1, region2)
        coEvery { downloadService.isExistsMapFile(any(), any()) } returns Response.success(Unit)

        val result = repository.getRegionsByParentId("europe")

        assertTrue(result.isSuccess)
        val regions = result.getOrNull()!!
        assertEquals(1, regions.size)
        assertEquals("europe/ukraine", regions.first().id)
        assertEquals("Ukraine_2.obf.zip", regions.first().fileName)
    }

    @Test
    fun `get regions by parent id sets file name to null when map 404 not found`() = runTest {
        val rootDto = mockk<RegionDto>()
        every { xmlParser.parse(any()) } returns rootDto

        val region = Region(
            id = "europe/ukraine",
            parentId = "europe",
            name = "ukraine",
            displayName = "Ukraine",
            fileName = "Ukraine_2.obf.zip",
            hasSubregions = false
        )

        every { mapper.mapToDomainList(rootDto) } returns listOf(region)
        coEvery { downloadService.isExistsMapFile(any(), any()) } returns Response.error(
            404,
            "".toResponseBody(null)
        )

        val result = repository.getRegionsByParentId("europe")

        assertTrue(result.isSuccess)
        val regions = result.getOrNull()!!
        assertEquals(1, regions.size)
        assertNull(regions.first().fileName)
    }

    @Test
    fun `get regions by parent id returns failure on server error`() = runTest {
        val rootDto = mockk<RegionDto>()
        every { xmlParser.parse(any()) } returns rootDto

        val region = Region(
            id = "europe/ukraine",
            parentId = "europe",
            name = "ukraine",
            displayName = "Ukraine",
            fileName = "Ukraine_2.obf.zip",
            hasSubregions = false
        )

        every { mapper.mapToDomainList(rootDto) } returns listOf(region)
        coEvery { downloadService.isExistsMapFile(any(), any()) } returns Response.error(
            500,
            "".toResponseBody(null)
        )

        val result = repository.getRegionsByParentId("europe")

        assertTrue(result.isFailure)
    }

    @Test
    fun `get regions by parent id caches regions xml and availability`() = runTest {
        val rootDto = mockk<RegionDto>()
        every { xmlParser.parse(any()) } returns rootDto

        val region = Region(
            id = "europe/ukraine",
            parentId = "europe",
            name = "ukraine",
            displayName = "Ukraine",
            fileName = "Ukraine_2.obf.zip",
            hasSubregions = false
        )

        every { mapper.mapToDomainList(rootDto) } returns listOf(region)
        coEvery { downloadService.isExistsMapFile(any(), any()) } returns Response.success(Unit)

        // Call twice
        repository.getRegionsByParentId("europe")
        repository.getRegionsByParentId("europe")

        verify(exactly = 1) { assetManager.open("regions.xml") }
        coVerify(exactly = 1) { downloadService.isExistsMapFile(any(), any()) }
    }
}
