package com.download.maps.features.storage.ui

import com.download.maps.core.viewmodel.BaseViewModel
import com.download.maps.features.storage.data.StorageMemoryInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@HiltViewModel
class StorageMemoryInfoViewModel @Inject constructor(
    private val repository: StorageMemoryInfoRepository
) : BaseViewModel<StorageMemoryInfoViewIntent, StorageMemoryInfoViewStateMutation, StorageMemoryInfoViewState>(
    StorageMemoryInfoViewState()
) {

    init {
        onIntent(StorageMemoryInfoViewIntent.OnLoadStorage)
    }

    override fun executeIntent(
        intent: StorageMemoryInfoViewIntent
    ): Flow<StorageMemoryInfoViewStateMutation> = when (intent) {
        is StorageMemoryInfoViewIntent.OnLoadStorage -> {
            repository.observeStorageMemoryInfo()
                .map { StorageMemoryInfoViewStateMutation.StorageLoaded(it) }
        }
    }

    override fun reduceState(
        currentSTATE: StorageMemoryInfoViewState,
        mutation: StorageMemoryInfoViewStateMutation
    ): StorageMemoryInfoViewState = when (mutation) {
        is StorageMemoryInfoViewStateMutation.StorageLoaded -> {
            mutation.storageMemoryInfo?.let {
                currentSTATE.copy(
                    freeSpace = bytesToReadableFormat(it.freeBytes),
                    usedRatio = if (it.totalBytes > 0) {
                        (it.totalBytes - it.freeBytes).toFloat() / it.totalBytes
                    } else {
                        0f
                    }
                )
            } ?: currentSTATE
        }
    }

    @Suppress("MagicNumber")
    private fun bytesToReadableFormat(
        bytes: Long
    ): String = when {
        bytes >= 1024 * 1024 * 1024 -> "%.2f Gb".format(bytes / 1024f / 1024f / 1024f)
        bytes >= 1024 * 1024 -> "%.2f Mb".format(bytes / 1024f / 1024f)
        bytes >= 1024 -> "%.2f Kb".format(bytes / 1024f)
        else -> "%.2f bytes".format(bytes.toFloat())
    }
}
