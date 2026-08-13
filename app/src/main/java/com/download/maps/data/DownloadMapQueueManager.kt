package com.download.maps.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.download.maps.data.model.DownloadQueueInfo
import com.download.maps.domain.model.Region
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class DownloadMapQueueManager(
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context,
    scope: CoroutineScope
) {

    private data class DownloadRequest(val regionId: String, val fileName: String)

    private val requestChannel = Channel<DownloadRequest>(Channel.UNLIMITED)
    private val pendingIds = MutableStateFlow<Set<String>>(emptySet())
    private val cancelledWhilePending = MutableStateFlow<Set<String>>(emptySet())

    init {
        scope.launch {
            requestChannel.receiveAsFlow().collect { request ->
                pendingIds.update { it - request.regionId }
                val wasCancelled = cancelledWhilePending.value.contains(request.regionId)
                cancelledWhilePending.update { it - request.regionId }
                if (!wasCancelled) {
                    runSingleDownload(request)
                }
            }
        }
    }

    fun downloadMap(regionId: String, fileName: String) {
        pendingIds.update { it + regionId }
        requestChannel.trySend(DownloadRequest(regionId, fileName))
    }

    private suspend fun runSingleDownload(request: DownloadRequest) {
        val workRequest = OneTimeWorkRequestBuilder<MapDownloadWorker>()
            .addTag(TAG_ALL)
            .addTag("$REGIONS_ID_TAG_PREFIX${request.regionId}")
            .addTag("$FILE_NAME_TAG_PREFIX${request.fileName}")
            .setInputData(workDataOf(MapDownloadWorker.KEY_FILE_NAME to request.fileName))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest)
        workManager.getWorkInfoByIdFlow(workRequest.id)
            .filterNotNull()
            .first { it.state.isFinished }
    }

    @Suppress("MagicNumber")
    fun observeProgress(regionId: String): Flow<Int> =
        workManager.getWorkInfosByTagFlow("$REGIONS_ID_TAG_PREFIX$regionId")
            .map { list -> list.firstOrNull { !it.state.isFinished } ?: list.lastOrNull() }
            .map { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.RUNNING -> workInfo.progress.getInt(
                        MapDownloadWorker.KEY_PROGRESS,
                        0
                    )

                    WorkInfo.State.SUCCEEDED -> 100
                    else -> 0
                }
            }
            .distinctUntilChanged()

    fun observeQueueInfo(): Flow<DownloadQueueInfo> =
        combine(
            workManager.getWorkInfosByTagFlow(TAG_ALL),
            pendingIds
        ) { list, pending ->
            val active = list.firstOrNull { it.state == WorkInfo.State.RUNNING }
                ?.let { extractRegionId(it) }
            DownloadQueueInfo(
                activeRegionId = active,
                queuedRegionIds = pending + list.filter {
                    it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.BLOCKED
                }.mapNotNull { extractRegionId(it) }.toSet()
            )
        }.distinctUntilChanged()

    suspend fun cancelDownload(regionId: String) {
        if (pendingIds.value.contains(regionId)) {
            pendingIds.update { it - regionId }
            cancelledWhilePending.update { it + regionId }
            return
        }
        val currentWorks = runCatching {
            workManager.getWorkInfosByTagFlow("$REGIONS_ID_TAG_PREFIX$regionId")
                .first()
        }.getOrDefault(emptyList())
        val targetWork = currentWorks.firstOrNull { !it.state.isFinished } ?: return
        workManager.cancelWorkById(targetWork.id)
    }

    fun observeDownloadedRegionIds(regions: List<Region>): Flow<Set<String>> =
        observeQueueInfo()
            .map { checkFilesOnDisk(regions) }
            .onStart { emit(checkFilesOnDisk(regions)) }
            .distinctUntilChanged()

    private fun checkFilesOnDisk(regions: List<Region>): Set<String> {
        val folder = File(context.filesDir, "maps")
        return regions.mapNotNull { region ->
            val fileName = region.fileName ?: return@mapNotNull null
            val file = File(folder, fileName)
            if (file.exists() && file.length() > 0) region.id else null
        }.toSet()
    }

    private fun extractRegionId(workInfo: WorkInfo): String? =
        workInfo.tags.firstOrNull { it.startsWith(REGIONS_ID_TAG_PREFIX) }
            ?.removePrefix(REGIONS_ID_TAG_PREFIX)

    private companion object {
        const val WORK_NAME = "download_map_current"
        const val TAG_ALL = "download_map_tag_all"
        const val REGIONS_ID_TAG_PREFIX = "region_id_"
        const val FILE_NAME_TAG_PREFIX = "file_name_"
    }
}
