package com.download.maps.features.regions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.download.maps.ui.theme.dividerColor
import com.download.maps.ui.theme.iconsGrayColor

@Composable
fun RegionsList(
    viewModel: RegionsListViewModel,
    navigateToChildRegions: (regionId: String, regionDisplayName: String) -> Unit,
    modifier: Modifier = Modifier,
    listHeader: (@Composable LazyItemScope.() -> Unit)? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val currentState = state) {
        is RegionsListViewState.Content -> RegionsScreenContent(
            modifier = modifier,
            content = currentState,
            download = viewModel::download,
            cancel = viewModel::cancelDownload,
            navigateToChildRegions = navigateToChildRegions,
            listHeader = listHeader
        )

        RegionsListViewState.Error -> RegionsScreenError(
            modifier = modifier,
            errorMessage = stringResource(R.string.unknown_error),
            onReload = viewModel::reload
        )

        RegionsListViewState.Loading ->
            RegionsScreenLoading(modifier = modifier)
    }
}

@Suppress("LongParameterList")
@Composable
private fun RegionsScreenContent(
    content: RegionsListViewState.Content,
    download: (String, String) -> Unit,
    cancel: (String) -> Unit,
    navigateToChildRegions: (regionId: String, regionDisplayName: String) -> Unit,
    modifier: Modifier = Modifier,
    listHeader: (@Composable LazyItemScope.() -> Unit)? = null
) {
    LazyColumn(modifier = modifier) {
        listHeader?.let { item { it.invoke(this) } }
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
                    region.fileName?.let { download(region.id, it) }
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
