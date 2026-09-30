package com.download.maps.screens.main.impl.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.download.maps.common.ui.navigation.LocalNavigator
import com.download.maps.common.ui.theme.screenBackgroundColor
import com.download.maps.features.regions.ui.RegionsList
import com.download.maps.screens.main.impl.ui.components.MainScreenListHeader
import com.download.maps.screens.main.impl.ui.components.MainScreenTopAppBar
import com.download.maps.screens.regions.api.RegionsScreenRoute
import com.download.maps.features.storageinfo.ui.StorageMemoryInfoView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen() {
    RequestNotificationPermission()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = screenBackgroundColor,
        topBar = { MainScreenTopAppBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val navigator = LocalNavigator.current

            StorageMemoryInfoView(modifier = Modifier.fillMaxWidth())

            RegionsList(
                parentRegionId = "europe",
                navigate = { parentRegionId: String, parentRegionName: String ->
                    navigator.navigate(
                        RegionsScreenRoute(
                            parentRegionId = parentRegionId,
                            parentRegionName = parentRegionName
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                listHeader = {
                    MainScreenListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun RequestNotificationPermission() {
    val context = LocalContext.current
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    val permissionRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { permissionRequested = true }
    )
    LaunchedEffect(Unit) {
        if (
            !permissionRequested &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionRequestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
