package com.download.maps.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.download.maps.features.regions.ui.RegionsList
import com.download.maps.features.regions.ui.RegionsListViewModel
import com.download.maps.features.storage.ui.StorageMemoryInfoView
import com.download.maps.features.storage.ui.StorageMemoryInfoViewModel
import com.download.maps.screens.main.components.MainScreenListHeader
import com.download.maps.screens.main.components.MainScreenTopAppBar
import com.download.maps.ui.theme.screenBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    regionsListViewModel: RegionsListViewModel,
    storageMemoryInfoViewModel: StorageMemoryInfoViewModel
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = screenBackgroundColor,
        topBar = { MainScreenTopAppBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StorageMemoryInfoView(
                viewModel = storageMemoryInfoViewModel,
                modifier = Modifier.fillMaxWidth()
            )

            RegionsList(
                viewModel = regionsListViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                listHeader = {
                    MainScreenListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                    )
                }
            )
        }
    }
}
