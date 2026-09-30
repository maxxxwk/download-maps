package com.download.maps.screens.regions.impl.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import com.download.maps.common.ui.navigation.LocalNavigator
import com.download.maps.common.ui.theme.screenBackgroundColor
import com.download.maps.features.regions.ui.RegionsList
import com.download.maps.screens.regions.api.RegionsScreenRoute
import com.download.maps.screens.regions.impl.ui.components.RegionsScreenTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RegionsScreen(
    parentRegionId: String,
    parentRegionName: String,
) {
    val navigator = LocalNavigator.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            RegionsScreenTopBar(
                parentRegionName = parentRegionName,
                onBack = dropUnlessResumed(block = navigator::pop)
            )
        },
        containerColor = screenBackgroundColor
    ) { paddingValues ->
        RegionsList(
            parentRegionId = parentRegionId,
            navigate = { parentRegionId: String, parentRegionName: String ->
                navigator.navigate(
                    RegionsScreenRoute(
                        parentRegionId = parentRegionId,
                        parentRegionName = parentRegionName
                    )
                )
            },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}
