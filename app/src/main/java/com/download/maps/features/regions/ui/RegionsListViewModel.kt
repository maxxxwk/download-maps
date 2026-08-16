package com.download.maps.features.regions.ui

import com.download.maps.core.viewmodel.BaseViewModel
import com.download.maps.features.regions.data.DownloadMapQueueManager
import com.download.maps.features.regions.data.RegionsRepository
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
) : BaseViewModel<RegionsListViewIntent, RegionsListViewStateMutation, RegionsListViewState>(
    initialState = RegionsListViewState.Loading
) {

    init {
        reload()
    }

    fun reload() {
        onIntent(RegionsListViewIntent.Reload)
    }

    fun download(regionId: String, fileName: String) {
        onIntent(RegionsListViewIntent.Download(regionId, fileName))
    }

    fun cancelDownload(regionId: String) {
        onIntent(RegionsListViewIntent.CancelDownload(regionId))
    }

    override fun executeIntent(intent: RegionsListViewIntent): Flow<RegionsListViewStateMutation> {
        return when (intent) {
            is RegionsListViewIntent.Reload -> loadData()

            is RegionsListViewIntent.Download -> flow {
                downloadMapQueueManager.downloadMap(intent.regionId, intent.fileName)
            }

            is RegionsListViewIntent.CancelDownload -> flow {
                downloadMapQueueManager.cancelDownload(intent.regionId)
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override fun reduceState(
        currentSTATE: RegionsListViewState,
        mutation: RegionsListViewStateMutation
    ): RegionsListViewState = when (mutation) {
        is RegionsListViewStateMutation.Loading -> RegionsListViewState.Loading

        is RegionsListViewStateMutation.Error -> RegionsListViewState.Error

        is RegionsListViewStateMutation.RegionsLoaded -> {
            val currentContent = currentSTATE as? RegionsListViewState.Content
            RegionsListViewState.Content(
                regions = mutation.regions,
                downloadedRegionIds = currentContent?.downloadedRegionIds ?: emptySet(),
                activeRegionId = currentContent?.activeRegionId,
                queuedRegionIds = currentContent?.queuedRegionIds ?: emptySet(),
                activeProgress = currentContent?.activeProgress ?: 0
            )
        }

        is RegionsListViewStateMutation.DownloadedUpdated -> {
            val currentContent = currentSTATE as? RegionsListViewState.Content
            currentContent?.copy(downloadedRegionIds = mutation.downloadedRegionIds) ?: currentSTATE
        }

        is RegionsListViewStateMutation.QueueUpdated -> {
            val currentContent = currentSTATE as? RegionsListViewState.Content
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

        is RegionsListViewStateMutation.ProgressUpdated -> {
            val currentContent = currentSTATE as? RegionsListViewState.Content
            if (currentContent != null && currentContent.activeRegionId == mutation.regionId) {
                currentContent.copy(activeProgress = mutation.progress)
            } else {
                currentSTATE
            }
        }
    }

    private fun loadData(): Flow<RegionsListViewStateMutation> = flow {
        emit(RegionsListViewStateMutation.Loading)
        regionsRepository.getRegionsByParentId(parentRegionId)
            .onSuccess { regions ->
                emit(RegionsListViewStateMutation.RegionsLoaded(regions))
                val downloadsFlow =
                    downloadMapQueueManager.observeDownloadedRegionIds(regions)
                        .map { downloadedIds ->
                            RegionsListViewStateMutation.DownloadedUpdated(downloadedIds)
                        }
                merge(downloadsFlow, observeQueueAndProgress())
                    .collect(::emit)
            }.onFailure {
                emit(RegionsListViewStateMutation.Error)
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeQueueAndProgress(): Flow<RegionsListViewStateMutation> {
        return downloadMapQueueManager.observeQueueInfo().flatMapLatest { queueInfo ->
            val queueMutation = flowOf(
                RegionsListViewStateMutation.QueueUpdated(
                    activeRegionId = queueInfo.activeRegionId,
                    queuedRegionIds = queueInfo.queuedRegionIds
                )
            )

            val progressMutation = if (queueInfo.activeRegionId != null) {
                downloadMapQueueManager.observeProgress(queueInfo.activeRegionId).map { progress ->
                    RegionsListViewStateMutation.ProgressUpdated(queueInfo.activeRegionId, progress)
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
