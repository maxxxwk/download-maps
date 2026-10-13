package com.download.maps.features.regions.data

import android.content.Context
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import app.cash.turbine.test
import com.download.maps.features.regions.domain.model.Region
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.UUID

class DownloadMapQueueManagerTest {

    @TempDir
    lateinit var tempFolder: File

    private val workManager: WorkManager = mockk(relaxed = true)
    private val context: Context = mockk()

    @BeforeEach
    fun setUp() {
        every { context.filesDir } returns tempFolder
    }

    @Test
    fun `observe progress returns progress 100 when succeeded`() = runTest {
        val queueManager = DownloadMapQueueManager(workManager, context, backgroundScope)
        val workInfo = mockk<WorkInfo>()
        every { workInfo.state } returns WorkInfo.State.SUCCEEDED

        every { workManager.getWorkInfosByTagFlow("region_id_ukraine") } returns flowOf(
            listOf(workInfo)
        )

        queueManager.observeProgress("ukraine").test {
            assertEquals(100, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe progress returns running progress value`() = runTest {
        val queueManager = DownloadMapQueueManager(workManager, context, backgroundScope)
        val workInfo = mockk<WorkInfo>()
        val progressData = Data.Builder().putInt("key_progress", 45).build()
        every { workInfo.state } returns WorkInfo.State.RUNNING
        every { workInfo.progress } returns progressData

        every { workManager.getWorkInfosByTagFlow("region_id_ukraine") } returns flowOf(
            listOf(workInfo)
        )

        queueManager.observeProgress("ukraine").test {
            assertEquals(45, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe queue info combines active and queued region ids`() = runTest {
        val queueManager = DownloadMapQueueManager(workManager, context, backgroundScope)
        val activeWorkInfo = mockk<WorkInfo>()
        every { activeWorkInfo.state } returns WorkInfo.State.RUNNING
        every { activeWorkInfo.tags } returns setOf("region_id_ukraine")

        val queuedWorkInfo = mockk<WorkInfo>()
        every { queuedWorkInfo.state } returns WorkInfo.State.ENQUEUED
        every { queuedWorkInfo.tags } returns setOf("region_id_poland")

        every { workManager.getWorkInfosByTagFlow("download_map_tag_all") } returns flowOf(
            listOf(activeWorkInfo, queuedWorkInfo)
        )

        queueManager.observeQueueInfo().test {
            val item = awaitItem()
            assertEquals("ukraine", item.activeRegionId)
            assertTrue(item.queuedRegionIds.contains("poland"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe downloaded region ids detects files on disk`() = runTest {
        val queueManager = DownloadMapQueueManager(workManager, context, backgroundScope)
        val mapsFolder = File(tempFolder, "maps")
        mapsFolder.mkdirs()
        val downloadedFile = File(mapsFolder, "Ukraine_2.obf.zip")
        downloadedFile.writeText("sample data")

        val region1 = Region("ukraine", null, "ukraine", "Ukraine", "Ukraine_2.obf.zip", false)
        val region2 = Region("poland", null, "poland", "Poland", "Poland_2.obf.zip", false)

        every { workManager.getWorkInfosByTagFlow("download_map_tag_all") } returns flowOf(
            emptyList()
        )

        queueManager.observeDownloadedRegionIds(listOf(region1, region2)).test {
            val item = awaitItem()
            assertEquals(setOf("ukraine"), item)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancel download cancels work in work manager when running`() = runTest {
        val queueManager = DownloadMapQueueManager(workManager, context, backgroundScope)
        val workId = UUID.randomUUID()
        val workInfo = mockk<WorkInfo>()
        every { workInfo.state } returns WorkInfo.State.RUNNING
        every { workInfo.id } returns workId

        every { workManager.getWorkInfosByTagFlow("region_id_ukraine") } returns flowOf(
            listOf(workInfo)
        )

        queueManager.cancelDownload("ukraine")

        verify { workManager.cancelWorkById(workId) }
    }
}
