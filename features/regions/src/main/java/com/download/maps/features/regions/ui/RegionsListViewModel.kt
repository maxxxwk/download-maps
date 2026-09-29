package com.download.maps.features.regions.ui

import androidx.lifecycle.ViewModel
import com.download.maps.features.regions.data.DownloadMapQueueManager
import com.download.maps.features.regions.data.RegionsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
    )

    init {
        reload()
    }

    @Suppress("LongMethod")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun reload() {
        intent {
            reduce { RegionsListViewState.Loading }
            regionsRepository.getRegionsByParentId(parentRegionId)
                .onSuccess { regions ->
                    reduce {
                        val currentContent = state as? RegionsListViewState.Content
                        RegionsListViewState.Content(
                            regions = regions,
                            downloadedRegionIds = currentContent?.downloadedRegionIds ?: emptySet(),
                            activeRegionId = currentContent?.activeRegionId,
                            queuedRegionIds = currentContent?.queuedRegionIds ?: emptySet(),
                            activeProgress = currentContent?.activeProgress ?: 0
                        )
                    }
                    coroutineScope {
                        launch {
                            downloadMapQueueManager.observeDownloadedRegionIds(regions)
                                .collect { downloadedIds ->
                                    reduce {
                                        (state as? RegionsListViewState.Content)?.copy(
                                            downloadedRegionIds = downloadedIds
                                        ) ?: state
                                    }
                                }
                        }
                        launch {
                            downloadMapQueueManager.observeQueueInfo().flatMapLatest {
                                reduce {
                                    val currentContent = state as? RegionsListViewState.Content
                                    currentContent?.copy(
                                        activeRegionId = it.activeRegionId,
                                        queuedRegionIds = it.queuedRegionIds,
                                        activeProgress = if (
                                            it.activeRegionId != currentContent.activeRegionId
                                        ) {
                                            0
                                        } else {
                                            currentContent.activeProgress
                                        }
                                    ) ?: state
                                }
                                if (it.activeRegionId != null) {
                                    downloadMapQueueManager.observeProgress(
                                        it.activeRegionId
                                    ).map { progress -> it.activeRegionId to progress }
                                } else {
                                    emptyFlow()
                                }
                            }.collect {
                                reduce {
                                    val currentContent = state as? RegionsListViewState.Content
                                    if (currentContent != null && currentContent.activeRegionId == it.first) {
                                        currentContent.copy(activeProgress = it.second)
                                    } else {
                                        state
                                    }
                                }
                            }
                        }
                    }
                }.onFailure { reduce { RegionsListViewState.Error } }
        }
    }

    fun download(regionId: String, fileName: String) {
        intent {
            downloadMapQueueManager.downloadMap(regionId, fileName)
        }
    }

    fun cancelDownload(regionId: String) {
        intent {
            downloadMapQueueManager.cancelDownload(regionId)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(parentRegionId: String): RegionsListViewModel
    }
}
