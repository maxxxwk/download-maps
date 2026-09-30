package com.download.maps.features.regions.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.download.maps.common.ui.theme.dividerColor
import com.download.maps.features.regions.domain.model.Region
import com.download.maps.features.regions.ui.RegionsListItem
import com.download.maps.features.regions.ui.RegionsListViewState

@Suppress("LongParameterList")
@Composable
internal fun RegionsScreenContent(
    content: RegionsListViewState.Content,
    download: (String, String) -> Unit,
    cancel: (String) -> Unit,
    navigate: (parentRegionId: String, parentRegionName: String) -> Unit,
    modifier: Modifier = Modifier,
    listHeader: (@Composable LazyItemScope.() -> Unit)? = null
) {
    LazyColumn(modifier = modifier) {
        listHeader?.let { item { it.invoke(this) } }
        itemsIndexed(items = content.regions, key = { _, region -> region.id }) { index, region ->
            RegionsListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .background(Color.White)
                    .then(
                        if (region.hasSubregions) {
                            Modifier.clickable(
                                onClick = dropUnlessResumed {
                                    navigate(region.id, region.displayName)
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
                download = { region.fileName?.let { download(region.id, it) } },
                cancel = { cancel(region.id) }
            )
        }
    }
}

@Suppress("MagicNumber")
private class RegionsListViewStateContentParameterProvider :
    PreviewParameterProvider<RegionsListViewState.Content> {
    private val regionsForPreview = List(10) {
        Region(
            id = it.toString(),
            parentId = null,
            name = "region$it",
            displayName = "Region $it",
            fileName = if (it % 2 == 0) null else "",
            hasSubregions = false
        )
    }
    override val values: Sequence<RegionsListViewState.Content> = sequenceOf(
        RegionsListViewState.Content(
            regions = regionsForPreview
        ),
        RegionsListViewState.Content(
            regions = regionsForPreview,
            downloadedRegionIds = regionsForPreview.take(3).map(Region::id).toSet(),
            activeRegionId = regionsForPreview.drop(3).first().id,
            queuedRegionIds = regionsForPreview.drop(4).take(2).map(Region::id).toSet(),
            activeProgress = 43
        ),
    )
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun RegionsScreenContentPreview(
    @PreviewParameter(RegionsListViewStateContentParameterProvider::class) content: RegionsListViewState.Content
) {
    RegionsScreenContent(
        content = content,
        download = { _, _ -> },
        cancel = {},
        navigate = { _, _ -> },
        modifier = Modifier.fillMaxSize(),
        listHeader = null
    )
}
