package com.download.maps.features.regions.ui

import com.download.maps.features.regions.data.DownloadMapQueueManager
import com.download.maps.features.regions.data.RegionsRepository
import com.download.maps.features.regions.data.model.DownloadQueueInfo
import com.download.maps.features.regions.domain.model.Region
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orbitmvi.orbit.test.testWithInternalState

class RegionsListViewModelTest {

    private val regionsRepository: RegionsRepository = mockk()
    private val queueManager: DownloadMapQueueManager = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        every { queueManager.observeDownloadedRegionIds(any()) } returns flowOf(emptySet())
        every { queueManager.observeQueueInfo() } returns flowOf(
            DownloadQueueInfo(activeRegionId = null, queuedRegionIds = emptySet())
        )
    }

    @Test
    fun `reload success sets content state and observes queue`() = runTest {
        val region = Region("ukraine", "europe", "ukraine", "Ukraine", "Ukraine_2.obf.zip", false)
        val regions = listOf(region)

        coEvery { regionsRepository.getRegionsByParentId("europe") } returns Result.success(regions)
        every { queueManager.observeDownloadedRegionIds(regions) } returns flowOf(setOf("ukraine"))

        val viewModel = RegionsListViewModel("europe", regionsRepository, queueManager)

        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState(RegionsListViewState.Content(regions = regions))
            expectInternalState(
                RegionsListViewState.Content(
                    regions = regions,
                    downloadedRegionIds = setOf("ukraine")
                )
            )
        }
    }

    @Test
    fun `reload failure sets error state`() = runTest {
        coEvery { regionsRepository.getRegionsByParentId("europe") } returns Result.failure(Exception("Network error"))

        val viewModel = RegionsListViewModel("europe", regionsRepository, queueManager)

        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState(RegionsListViewState.Error)
        }
    }

    @Test
    fun `download invokes queue manager download map`() = runTest {
        coEvery { regionsRepository.getRegionsByParentId("europe") } returns Result.success(emptyList())

        val viewModel = RegionsListViewModel("europe", regionsRepository, queueManager)

        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState(RegionsListViewState.Content(regions = emptyList()))
            containerHost.download("ukraine", "Ukraine_2.obf.zip")
        }
        verify { queueManager.downloadMap("ukraine", "Ukraine_2.obf.zip") }
    }

    @Test
    fun `cancel download invokes queue manager cancel download`() = runTest {
        coEvery { regionsRepository.getRegionsByParentId("europe") } returns Result.success(emptyList())

        val viewModel = RegionsListViewModel("europe", regionsRepository, queueManager)

        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState(RegionsListViewState.Content(regions = emptyList()))
            containerHost.cancelDownload("ukraine")
        }
        coVerify { queueManager.cancelDownload("ukraine") }
    }
}
