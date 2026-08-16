package com.download.maps.features.storage.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.download.maps.R
import com.download.maps.ui.theme.appBarColor
import com.download.maps.ui.theme.dividerColor

@Composable
fun StorageMemoryInfoView(
    viewModel: StorageMemoryInfoViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StorageMemoryInfoViewContent(
        state = state,
        modifier = modifier
    )
}

@Composable
private fun StorageMemoryInfoViewContent(
    state: StorageMemoryInfoViewState,
    modifier: Modifier = Modifier
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
                text = stringResource(id = R.string.free_memory_prefix, state.freeSpace),
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
                size = Size(width = size.width * state.usedRatio, size.height)
            )
        }
    }
}
