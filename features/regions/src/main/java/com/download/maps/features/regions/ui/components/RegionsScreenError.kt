package com.download.maps.features.regions.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.download.maps.features.regions.R

@Composable
internal fun RegionsScreenError(
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

@Suppress("UnusedPrivateMember")
@Composable
@Preview(showBackground = true)
private fun RegionsScreenErrorPreview() {
    RegionsScreenError(
        modifier = Modifier.fillMaxSize(),
        errorMessage = "Error message",
        onReload = {}
    )
}
