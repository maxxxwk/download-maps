package com.download.maps.screens.regions.impl.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.download.maps.common.ui.theme.appBarColor
import com.download.maps.screens.regions.impl.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RegionsScreenTopBar(
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

@Composable
@Preview
private fun RegionsScreenTopBarPreview() {
    RegionsScreenTopBar(
        parentRegionName = "Parent region",
        onBack = {}
    )
}
