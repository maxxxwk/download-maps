package com.download.maps.screens.regions.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.download.maps.R
import com.download.maps.screens.common.ui.RegionsListState
import com.download.maps.screens.common.ui.RegionsListViewModel
import com.download.maps.screens.common.ui.components.RegionsListItem
import com.download.maps.ui.theme.appBarColor
import com.download.maps.ui.theme.dividerColor
import com.download.maps.ui.theme.iconsGrayColor
import com.download.maps.ui.theme.screenBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionsScreen(
    parentRegionName: String,
    viewModel: RegionsListViewModel,
    onBack: () -> Unit,
    navigateToChildRegions: (String, String) -> Unit
) {
    BackHandler(onBack = onBack)

    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            RegionsScreenTopBar(parentRegionName = parentRegionName, onBack = onBack)
        },
        containerColor = screenBackgroundColor
    ) { paddingValues ->
        when (val currentState = state) {
            is RegionsListState.Content -> RegionsScreenContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                content = currentState,
                download = viewModel::download,
                cancel = viewModel::cancelDownload,
                navigateToChildRegions = navigateToChildRegions
            )

            RegionsListState.Error -> RegionsScreenError(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                errorMessage = stringResource(R.string.unknown_error),
                onReload = viewModel::reload
            )

            RegionsListState.Loading -> RegionsScreenLoading(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }

    }
}

@Composable
private fun RegionsScreenContent(
    modifier: Modifier = Modifier,
    content: RegionsListState.Content,
    download: (String, String) -> Unit,
    cancel: (String) -> Unit,
    navigateToChildRegions: (String, String) -> Unit
) {
    LazyColumn(
        modifier = modifier,
    ) {
        itemsIndexed(
            items = content.regions,
            key = { _, region -> region.id }
        ) { index, region ->
            RegionsListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .background(Color.White)
                    .then(
                        if (region.hasSubregions) {
                            Modifier.clickable(
                                onClick = {
                                    navigateToChildRegions(region.id, region.displayName)
                                }
                            )
                        } else {
                            Modifier
                        }
                    )
                    .then(
                        if (index < content.regions.lastIndex) {
                            Modifier.drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                val y = size.height - strokeWidth / 2

                                drawLine(
                                    color = dividerColor,
                                    start = Offset(x = 64.dp.toPx(), y = y),
                                    end = Offset(x = size.width, y = y),
                                    strokeWidth = strokeWidth
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                region = region,
                progress = if (content.activeRegionId == region.id) {
                    content.activeProgress
                } else {
                    null
                },
                isDownloaded = content.downloadedRegionIds.contains(region.id),
                isInQueue = content.queuedRegionIds.contains(region.id),
                download = {
                    region.fileName?.let {
                        download(region.id, it)
                    }
                },
                cancel = { cancel(region.id) }
            )
        }
    }
}

@Composable
private fun RegionsScreenError(
    modifier: Modifier = Modifier,
    errorMessage: String,
    onReload: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        IconButton(onClick = onReload) {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(R.drawable.ic_reload),
                contentDescription = "reload"
            )
        }
    }
}

@Composable
private fun RegionsScreenLoading(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = iconsGrayColor)
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
