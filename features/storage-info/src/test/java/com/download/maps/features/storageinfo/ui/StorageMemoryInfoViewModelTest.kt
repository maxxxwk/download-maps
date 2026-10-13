package com.download.maps.features.storageinfo.ui

import android.os.StatFs
import com.download.maps.features.storageinfo.data.StorageMemoryInfoRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orbitmvi.orbit.test.testWithInternalState
import java.util.Locale

class StorageMemoryInfoViewModelTest {

    private val repository: StorageMemoryInfoRepository = mockk()

    @BeforeEach
    fun setUp() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `initial state is default`() = runTest {
        every { repository.observeStorageMemoryInfo() } returns flowOf()

        val viewModel = StorageMemoryInfoViewModel(repository)

        viewModel.testWithInternalState(this) {
            // Initial state is automatically verified
        }
    }

    @Test
    fun `observe memory info updates state with gigabytes format and used ratio`() = runTest {
        val statFs: StatFs = mockk()
        val totalBytes = 10L * 1024 * 1024 * 1024 // 10 GB
        val freeBytes = 2L * 1024 * 1024 * 1024  // 2 GB
        every { statFs.totalBytes } returns totalBytes
        every { statFs.freeBytes } returns freeBytes

        every { repository.observeStorageMemoryInfo() } returns flowOf(statFs)

        val viewModel = StorageMemoryInfoViewModel(repository)

        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState(
                StorageMemoryInfoViewState(
                    freeSpace = "2.00 Gb",
                    usedRatio = 0.8f
                )
            )
        }
    }

    @Test
    fun `observe memory info updates state with megabytes format`() = runTest {
        val statFs: StatFs = mockk()
        val totalBytes = 10L * 1024 * 1024 // 10 MB
        val freeBytes = 5L * 1024 * 1024  // 5 MB
        every { statFs.totalBytes } returns totalBytes
        every { statFs.freeBytes } returns freeBytes

        every { repository.observeStorageMemoryInfo() } returns flowOf(statFs)

        val viewModel = StorageMemoryInfoViewModel(repository)

        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState(
                StorageMemoryInfoViewState(
                    freeSpace = "5.00 Mb",
                    usedRatio = 0.5f
                )
            )
        }
    }

    @Test
    fun `observe memory info updates state with kilobytes format`() = runTest {
        val statFs: StatFs = mockk()
        val totalBytes = 4L * 1024 // 4 KB
        val freeBytes = 2L * 1024  // 2 KB
        every { statFs.totalBytes } returns totalBytes
        every { statFs.freeBytes } returns freeBytes

        every { repository.observeStorageMemoryInfo() } returns flowOf(statFs)

        val viewModel = StorageMemoryInfoViewModel(repository)

        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState(
                StorageMemoryInfoViewState(
                    freeSpace = "2.00 Kb",
                    usedRatio = 0.5f
                )
            )
        }
    }

    @Test
    fun `observe memory info updates state with bytes format and zero total bytes`() = runTest {
        val statFs: StatFs = mockk()
        every { statFs.totalBytes } returns 0L
        every { statFs.freeBytes } returns 500L

        every { repository.observeStorageMemoryInfo() } returns flowOf(statFs)

        val viewModel = StorageMemoryInfoViewModel(repository)

        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState(
                StorageMemoryInfoViewState(
                    freeSpace = "500.00 bytes",
                    usedRatio = 0f
                )
            )
        }
    }
}
