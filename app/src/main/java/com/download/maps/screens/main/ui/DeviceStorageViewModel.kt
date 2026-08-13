package com.download.maps.screens.main.ui

import com.download.maps.core.viewmodel.BaseViewModel
import com.download.maps.screens.main.data.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@HiltViewModel
class DeviceStorageViewModel @Inject constructor(
    private val storageRepository: StorageRepository
) : BaseViewModel<DeviceStorageIntent, DeviceStorageMutation, DeviceStorageState>(
    DeviceStorageState()
) {

    init {
        onIntent(DeviceStorageIntent.OnLoadStorage)
    }

    override fun executeIntent(
        intent: DeviceStorageIntent
    ): Flow<DeviceStorageMutation> = when (intent) {
        is DeviceStorageIntent.OnLoadStorage -> {
            storageRepository.observeStorageInfo()
                .map { DeviceStorageMutation.StorageLoaded(it) }
        }
    }

    override fun reduceState(
        currentSTATE: DeviceStorageState,
        mutation: DeviceStorageMutation
    ): DeviceStorageState = when (mutation) {
        is DeviceStorageMutation.StorageLoaded -> {
            currentSTATE.copy(storageInfo = mutation.storageInfo)
        }
    }
}
