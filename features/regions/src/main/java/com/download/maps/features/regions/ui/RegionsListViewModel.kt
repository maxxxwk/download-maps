package com.download.maps.features.regions.ui

import androidx.lifecycle.ViewModel
import com.download.maps.features.regions.data.DownloadMapQueueManager
import com.download.maps.features.regions.data.RegionsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

@HiltViewModel(assistedFactory = RegionsListViewModel.Factory::class)
internal class RegionsListViewModel @AssistedInject constructor(
    @Assisted private val parentRegionId: String,
    private val regionsRepository: RegionsRepository,
    private val downloadMapQueueManager: DownloadMapQueueManager
) : OrbitContainerHost<RegionsListViewState, RegionsListViewState, Nothing>,
    ViewModel() {
    override val container = orbitContainer<RegionsListViewState, Nothing>(
        initialState = RegionsListViewState.Loading
    ) {
        reload()
    }

    private var observeJob: Job? = null

    @Suppress("LongMethod")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun reload() {
        observeJob?.cancel()
        intent(registerIdling = false) {
            reduce { RegionsListViewState.Loading }
            regionsRepository.getRegionsByParentId(parentRegionId)
                .onSuccess { regions ->
                    reduce { RegionsListViewState.Content(regions = regions) }
                    repeatOnSubscription {
                        observeJob = currentCoroutineContext()[Job]
                        launch {
                            downloadMapQueueManager.observeDownloadedRegionIds(regions)
                                .collect { downloadedIds ->
                                    runOn<RegionsListViewState.Content> {
                                        reduce {
                                            state.copy(downloadedRegionIds = downloadedIds)
                                        }
                                    }
                                }
                        }
                        launch {
                            downloadMapQueueManager.observeQueueInfo()
                                .onEach { queueInfo ->
                                    runOn<RegionsListViewState.Content> {
                                        reduce {
                                            state.copy(
                                                activeRegionId = queueInfo.activeRegionId,
                                                queuedRegionIds = queueInfo.queuedRegionIds,
                                                activeProgress = if (
                                                    queueInfo.activeRegionId != state.activeRegionId
                                                ) {
                                                    0
                                                } else {
                                                    state.activeProgress
                                                }
                                            )
                                        }
                                    }
                                }
                                .flatMapLatest { queueInfo ->
                                    if (queueInfo.activeRegionId != null) {
                                        downloadMapQueueManager.observeProgress(queueInfo.activeRegionId)
                                            .map { progress -> queueInfo.activeRegionId to progress }
                                    } else {
                                        emptyFlow()
                                    }
                                }
                                .collect { progressInfo ->
                                    runOn<RegionsListViewState.Content> {
                                        if (state.activeRegionId == progressInfo.first) {
                                            reduce {
                                                state.copy(activeProgress = progressInfo.second)
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }.onFailure { reduce { RegionsListViewState.Error } }
        }
    }

    fun download(regionId: String, fileName: String) {
        intent { downloadMapQueueManager.downloadMap(regionId, fileName) }
    }

    fun cancelDownload(regionId: String) {
        intent { downloadMapQueueManager.cancelDownload(regionId) }
    }

    @AssistedFactory
    interface Factory {
        fun create(parentRegionId: String): RegionsListViewModel
    }
}
