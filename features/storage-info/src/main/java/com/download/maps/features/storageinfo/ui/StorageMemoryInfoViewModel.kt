package com.download.maps.features.storageinfo.ui

import androidx.lifecycle.ViewModel
import com.download.maps.features.storageinfo.data.StorageMemoryInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

@HiltViewModel
internal class StorageMemoryInfoViewModel @Inject constructor(
    private val storageMemoryInfoRepository: StorageMemoryInfoRepository
) : OrbitContainerHost<StorageMemoryInfoViewState, StorageMemoryInfoViewState, Nothing>,
    ViewModel() {

    override val container = orbitContainer<StorageMemoryInfoViewState, Nothing>(
        initialState = StorageMemoryInfoViewState()
    )

    init {
        intent(registerIdling = false) {
            repeatOnSubscription {
                storageMemoryInfoRepository.observeStorageMemoryInfo().filterNotNull().collect {
                    reduce {
                        state.copy(
                            freeSpace = bytesToReadableFormat(it.freeBytes),
                            usedRatio = if (it.totalBytes > 0) {
                                (it.totalBytes - it.freeBytes).toFloat() / it.totalBytes
                            } else {
                                0f
                            }
                        )
                    }
                }
            }
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
