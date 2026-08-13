package com.download.maps.screens.main.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.download.maps.R
import com.download.maps.screens.common.ui.RegionsListState
import com.download.maps.screens.common.ui.RegionsListViewModel
import com.download.maps.screens.common.ui.components.RegionsListItem
import com.download.maps.screens.main.data.model.StorageInfo
import com.download.maps.ui.theme.appBarColor
import com.download.maps.ui.theme.dividerColor
import com.download.maps.ui.theme.iconsGrayColor
import com.download.maps.ui.theme.screenBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    regionsListViewModel: RegionsListViewModel,
    deviceStorageViewModel: DeviceStorageViewModel,
    navigateToChildRegions: (String, String) -> Unit
) {
    val regionsListState by regionsListViewModel.state.collectAsStateWithLifecycle()
    val deviceStorageState by deviceStorageViewModel.state.collectAsStateWithLifecycle()

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
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            deviceStorageState.storageInfo?.let {
                DeviceStorageInfo(
                    modifier = Modifier.fillMaxWidth(),
                    storageInfo = it
                )
            }
            CountriesList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = regionsListState,
                download = regionsListViewModel::download,
                cancel = regionsListViewModel::cancelDownload,
                reload = regionsListViewModel::reload,
                navigateToChildRegions = navigateToChildRegions
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun CountriesList(
    modifier: Modifier = Modifier,
    state: RegionsListState,
    download: (String, String) -> Unit,
    cancel: (String) -> Unit,
    reload: () -> Unit,
    navigateToChildRegions: (String, String) -> Unit
) {
    when (state) {
        is RegionsListState.Content -> CountriesListContent(
            modifier = modifier,
            content = state,
            download = download,
            cancel = cancel,
            navigateToChildRegions = navigateToChildRegions
        )

        RegionsListState.Error -> CountriesListError(
            modifier = modifier,
            errorMessage = stringResource(R.string.unknown_error),
            onReload = reload
        )

        RegionsListState.Loading -> CountriesListLoading(modifier = modifier)
    }
}

@Suppress("LongMethod")
@Composable
private fun CountriesListContent(
    modifier: Modifier = Modifier,
    content: RegionsListState.Content,
    download: (String, String) -> Unit,
    cancel: (String) -> Unit,
    navigateToChildRegions: (String, String) -> Unit
) {
    LazyColumn(
        modifier = modifier,
    ) {
        item {
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
private fun CountriesListError(
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
private fun CountriesListLoading(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = iconsGrayColor)
    }
}

@Composable
private fun DeviceStorageInfo(
    modifier: Modifier = Modifier,
    storageInfo: StorageInfo
) {
    Column(
        modifier = modifier
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 1.dp,
                    color = Color.Black.copy(alpha = 0.06f),
                    offset = DpOffset(x = 0.dp, y = (-1).dp)
                )
            )
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 2.dp,
                    color = Color.Black.copy(alpha = 0.15f),
                    offset = DpOffset(x = 0.dp, y = 1.dp)
                )
            )
            .background(color = Color.White)
            .padding(vertical = 11.dp, horizontal = 15.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.device_memory),
                fontSize = 14.sp,
                color = Color.Black
            )
            Text(
                text = buildString {
                    append(stringResource(R.string.free_memory_prefix))
                    append(' ')
                    append(bytesToReadableFormat(storageInfo.freeBytes))
                },
                fontSize = 14.sp,
                color = Color.Black
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        ) {
            drawRect(
                color = dividerColor,
                size = size
            )
            drawRect(
                color = appBarColor,
                size = Size(width = size.width * storageInfo.usedRatio, size.height)
            )
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
