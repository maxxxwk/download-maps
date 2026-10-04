package com.download.maps.features.storageinfo.data

import android.os.Environment
import android.os.StatFs
import com.download.maps.common.di.qualifiers.DispatcherIO
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

internal class StorageMemoryInfoRepository @Inject constructor(
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher
) {
    fun observeStorageMemoryInfo(pollIntervalMs: Long = 2000L): Flow<StatFs?> = flow {
        while (currentCoroutineContext().isActive) {
            emit(runCatching { StatFs(Environment.getDataDirectory().absolutePath) }.getOrNull())
            delay(pollIntervalMs.milliseconds)
        }
    }.distinctUntilChanged { old, new ->
        old?.totalBytes == new?.totalBytes && old?.freeBytes == new?.freeBytes
    }.flowOn(dispatcher)
}
