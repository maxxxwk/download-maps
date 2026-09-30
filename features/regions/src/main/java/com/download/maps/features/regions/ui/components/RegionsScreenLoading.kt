package com.download.maps.features.regions.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.download.maps.common.ui.theme.iconsGrayColor

@Preview(showBackground = true, widthDp = 412, heightDp = 924)
@Composable
internal fun RegionsScreenLoading(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = iconsGrayColor)
    }
}
