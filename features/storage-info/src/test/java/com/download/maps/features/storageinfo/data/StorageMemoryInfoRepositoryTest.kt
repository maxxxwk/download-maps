package com.download.maps.features.storageinfo.data

import android.os.Environment
import android.os.StatFs
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class StorageMemoryInfoRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val repository = StorageMemoryInfoRepository(testDispatcher)

    @BeforeEach
    fun setUp() {
        mockkStatic(Environment::class)
        val mockDataDir = File("/mock/data")
        every { Environment.getDataDirectory() } returns mockDataDir
        mockkConstructor(StatFs::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `observe storage memory info emits item on start`() = testScope.runTest {
        repository.observeStorageMemoryInfo(pollIntervalMs = 1000L).test {
            val item = awaitItem()
            assertNotNull(item)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
