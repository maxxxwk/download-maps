package com.download.maps.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.download.maps.R
import com.download.maps.features.regions.ui.RegionsList
import com.download.maps.features.regions.ui.RegionsListViewModel
import com.download.maps.features.storage.ui.StorageMemoryInfoView
import com.download.maps.features.storage.ui.StorageMemoryInfoViewModel
import com.download.maps.ui.theme.appBarColor
import com.download.maps.ui.theme.screenBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    regionsListViewModel: RegionsListViewModel,
    storageMemoryInfoViewModel: StorageMemoryInfoViewModel,
    navigateToChildRegions: (regionId: String, regionDisplayName: String) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = screenBackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appBarColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
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
                navigateToChildRegions = navigateToChildRegions,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                listHeader = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                            .background(Color.White)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = stringResource(R.string.europe),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            )
        }
    }
}
