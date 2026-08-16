package com.download.maps.features.regions.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.download.maps.R
import com.download.maps.features.regions.domain.model.Region
import com.download.maps.ui.theme.dividerColor
import com.download.maps.ui.theme.downloadedRegionMapIconColor
import com.download.maps.ui.theme.iconsGrayColor
import com.download.maps.ui.theme.progressBarFillColor

@Suppress("LongParameterList", "LongMethod")
@Composable
fun RegionsListItem(
    modifier: Modifier = Modifier,
    region: Region,
    progress: Int?,
    isDownloaded: Boolean,
    isInQueue: Boolean,
    download: () -> Unit,
    cancel: () -> Unit
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_map),
            contentDescription = "map",
            tint = if (isDownloaded) {
                downloadedRegionMapIconColor
            } else {
                iconsGrayColor
            }
        )

        if (progress != null) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = region.displayName,
                    color = Color.Black,
                    fontSize = 16.sp
                )
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                ) {
                    drawRect(
                        color = dividerColor,
                        size = size
                    )
                    drawRect(
                        color = progressBarFillColor,
                        size = Size(
                            width = size.width * (progress / 100f),
                            height = size.height
                        )
                    )
                }
            }
        } else {
            Text(
                modifier = Modifier.weight(1f),
                text = region.displayName,
                color = Color.Black,
                fontSize = 16.sp
            )
        }

        when {
            progress != null || isInQueue -> IconButton(
                onClick = cancel
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_action_remove_dark),
                    contentDescription = "cancel",
                    tint = iconsGrayColor
                )
            }

            region.fileName != null && !isDownloaded -> IconButton(
                onClick = download
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_action_import),
                    contentDescription = "download",
                    tint = iconsGrayColor
                )
            }

            else -> Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

@Suppress("LongMethod")
@Composable
@Preview(showBackground = true)
fun RegionsListItemPreview() {
    val downloadableRegion = Region(
        id = "parent_region/region1",
        parentId = "parent_region",
        name = "region1",
        displayName = "Region 1",
        fileName = "stub",
        hasSubregions = false
    )
    val anotherRegion = Region(
        id = "parent_region/region2",
        parentId = "parent_region",
        name = "region2",
        displayName = "Region 2",
        fileName = null,
        hasSubregions = true
    )
    Column(modifier = Modifier.fillMaxSize()) {
        RegionsListItem(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            region = downloadableRegion,
            progress = null,
            isDownloaded = false,
            isInQueue = false,
            download = {},
            cancel = {}
        )
        RegionsListItem(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            region = downloadableRegion,
            progress = null,
            isDownloaded = true,
            isInQueue = false,
            download = {},
            cancel = {}
        )
        RegionsListItem(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            region = downloadableRegion,
            progress = 45,
            isDownloaded = false,
            isInQueue = false,
            download = {},
            cancel = {}
        )
        RegionsListItem(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            region = downloadableRegion,
            progress = null,
            isDownloaded = false,
            isInQueue = true,
            download = {},
            cancel = {}
        )
        RegionsListItem(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            region = anotherRegion,
            progress = null,
            isDownloaded = false,
            isInQueue = false,
            download = {},
            cancel = {}
        )
    }
}
