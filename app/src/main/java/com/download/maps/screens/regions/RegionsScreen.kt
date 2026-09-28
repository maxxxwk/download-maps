package com.download.maps.screens.regions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import com.download.maps.features.regions.ui.RegionsList
import com.download.maps.features.regions.ui.RegionsListViewModel
import com.download.maps.screens.regions.components.RegionsScreenTopBar
import com.download.maps.ui.navigation.LocalNavigator
import com.download.maps.ui.theme.screenBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionsScreen(
    parentRegionName: String,
    viewModel: RegionsListViewModel
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val navigator = LocalNavigator.current

            RegionsScreenTopBar(
                parentRegionName = parentRegionName,
                onBack = dropUnlessResumed { navigator.pop() }
            )
        },
        containerColor = screenBackgroundColor
    ) { paddingValues ->
        RegionsList(
            viewModel = viewModel,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}
