package com.download.maps.screens.main.impl.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.download.maps.common.R
import com.download.maps.common.ui.theme.appBarColor

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
internal fun MainScreenTopAppBar(modifier: Modifier = Modifier) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
            )
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = appBarColor,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}
