package com.download.maps.screens.regions

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.download.maps.R
import com.download.maps.features.regions.ui.RegionsList
import com.download.maps.features.regions.ui.RegionsListViewModel
import com.download.maps.ui.theme.appBarColor
import com.download.maps.ui.theme.screenBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionsScreen(
    parentRegionName: String,
    viewModel: RegionsListViewModel,
    onBack: () -> Unit,
    navigateToChildRegions: (regionId: String, regionDisplayName: String) -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            RegionsScreenTopBar(
                parentRegionName = parentRegionName,
                onBack = onBack
            )
        },
        containerColor = screenBackgroundColor
    ) { paddingValues ->
        RegionsList(
            viewModel = viewModel,
            navigateToChildRegions = navigateToChildRegions,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionsScreenTopBar(
    parentRegionName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = parentRegionName,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "back",
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = appBarColor,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}
