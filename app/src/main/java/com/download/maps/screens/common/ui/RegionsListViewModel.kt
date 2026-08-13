package com.download.maps.screens.common.ui

import com.download.maps.core.viewmodel.BaseViewModel
import com.download.maps.data.DownloadMapQueueManager
import com.download.maps.data.RegionsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

@HiltViewModel(assistedFactory = RegionsListViewModel.Factory::class)
class RegionsListViewModel @AssistedInject constructor(
    @Assisted private val parentRegionId: String,
    private val regionsRepository: RegionsRepository,
    private val downloadMapQueueManager: DownloadMapQueueManager
) : BaseViewModel<RegionsListIntent, RegionsListStateMutation, RegionsListState>(RegionsListState.Loading) {

    init {
        reload()
    }

    fun reload() {
        onIntent(RegionsListIntent.Reload)
    }

    fun download(regionId: String, fileName: String) {
        onIntent(RegionsListIntent.Download(regionId, fileName))
    }

    fun cancelDownload(regionId: String) {
        onIntent(RegionsListIntent.CancelDownload(regionId))
    }

    override fun executeIntent(intent: RegionsListIntent): Flow<RegionsListStateMutation> {
        return when (intent) {
            is RegionsListIntent.Reload -> loadData()

            is RegionsListIntent.Download -> flow {
                downloadMapQueueManager.downloadMap(intent.regionId, intent.fileName)
            }

            is RegionsListIntent.CancelDownload -> flow {
                downloadMapQueueManager.cancelDownload(intent.regionId)
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override fun reduceState(
        currentSTATE: RegionsListState,
        mutation: RegionsListStateMutation
    ): RegionsListState = when (mutation) {
        is RegionsListStateMutation.Loading -> RegionsListState.Loading

        is RegionsListStateMutation.Error -> RegionsListState.Error

        is RegionsListStateMutation.RegionsLoaded -> {
            val currentContent = currentSTATE as? RegionsListState.Content
            RegionsListState.Content(
                regions = mutation.regions,
                downloadedRegionIds = currentContent?.downloadedRegionIds ?: emptySet(),
                activeRegionId = currentContent?.activeRegionId,
                queuedRegionIds = currentContent?.queuedRegionIds ?: emptySet(),
                activeProgress = currentContent?.activeProgress ?: 0
            )
        }

        is RegionsListStateMutation.DownloadedUpdated -> {
            val currentContent = currentSTATE as? RegionsListState.Content
            currentContent?.copy(downloadedRegionIds = mutation.downloadedRegionIds) ?: currentSTATE
        }

        is RegionsListStateMutation.QueueUpdated -> {
            val currentContent = currentSTATE as? RegionsListState.Content
            if (currentContent != null) {
                val isNewActiveRegion = mutation.activeRegionId != currentContent.activeRegionId
                currentContent.copy(
                    activeRegionId = mutation.activeRegionId,
                    queuedRegionIds = mutation.queuedRegionIds.toSet(),
                    activeProgress = if (isNewActiveRegion) 0 else currentContent.activeProgress
                )
            } else {
                currentSTATE
            }
        }

        is RegionsListStateMutation.ProgressUpdated -> {
            val currentContent = currentSTATE as? RegionsListState.Content
            if (currentContent != null && currentContent.activeRegionId == mutation.regionId) {
                currentContent.copy(activeProgress = mutation.progress)
            } else {
                currentSTATE
            }
        }
    }

    private fun loadData(): Flow<RegionsListStateMutation> = flow {
        emit(RegionsListStateMutation.Loading)
        regionsRepository.getRegionsByParentId(parentRegionId)
            .onSuccess { regions ->
                emit(RegionsListStateMutation.RegionsLoaded(regions))
                val downloadsFlow =
                    downloadMapQueueManager.observeDownloadedRegionIds(regions)
                        .map { downloadedIds ->
                            RegionsListStateMutation.DownloadedUpdated(downloadedIds)
                        }
                merge(downloadsFlow, observeQueueAndProgress())
                    .collect(::emit)
            }.onFailure {
                emit(RegionsListStateMutation.Error)
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeQueueAndProgress(): Flow<RegionsListStateMutation> {
        return downloadMapQueueManager.observeQueueInfo().flatMapLatest { queueInfo ->
            val queueMutation = flowOf(
                RegionsListStateMutation.QueueUpdated(
                    activeRegionId = queueInfo.activeRegionId,
                    queuedRegionIds = queueInfo.queuedRegionIds
                )
            )

            val progressMutation = if (queueInfo.activeRegionId != null) {
                downloadMapQueueManager.observeProgress(queueInfo.activeRegionId).map { progress ->
                    RegionsListStateMutation.ProgressUpdated(queueInfo.activeRegionId, progress)
                }
            } else {
                emptyFlow()
            }

            merge(queueMutation, progressMutation)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(parentRegionId: String): RegionsListViewModel
    }
}
