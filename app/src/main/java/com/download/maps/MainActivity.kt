package com.download.maps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import com.download.maps.features.regions.ui.RegionsListViewModel
import com.download.maps.screens.main.MainScreen
import com.download.maps.screens.regions.RegionsScreen
import com.download.maps.ui.navigation.MainScreenRoute
import com.download.maps.ui.navigation.Navigation
import com.download.maps.ui.navigation.RegionsScreenRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RequestNotificationPermission()
            Navigation(
                entryProvider = entryProvider {
                    entry<MainScreenRoute> {
                        MainScreen(
                            regionsListViewModel = hiltViewModel<RegionsListViewModel, RegionsListViewModel.Factory> {
                                it.create("europe")
                            },
                            storageMemoryInfoViewModel = hiltViewModel()
                        )
                    }
                    entry<RegionsScreenRoute> { key ->
                        RegionsScreen(
                            parentRegionName = key.parentRegionName,
                            viewModel = hiltViewModel<RegionsListViewModel, RegionsListViewModel.Factory> {
                                it.create(key.parentRegionId)
                            }
                        )
                    }
                }
            )
        }
    }

    @Composable
    private fun RequestNotificationPermission() {
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
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionRequestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
