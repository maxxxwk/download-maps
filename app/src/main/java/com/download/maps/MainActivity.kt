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
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.ui.NavDisplay
import com.download.maps.screens.common.ui.RegionsListViewModel
import com.download.maps.screens.main.ui.MainScreen
import com.download.maps.screens.regions.ui.RegionsScreen
import com.download.maps.ui.navigation.NavRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RequestNotificationPermission()

            val backStack = rememberSerializable(
                serializer = NavBackStackSerializer()
            ) { NavBackStack<NavRoute>(NavRoute.MainScreen) }

            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<NavRoute.MainScreen> {
                        MainScreen(
                            regionsListViewModel = hiltViewModel<RegionsListViewModel, RegionsListViewModel.Factory>(
                                creationCallback = { it.create("europe") }
                            ),
                            deviceStorageViewModel = hiltViewModel(),
                            navigateToChildRegions = { id, displayName ->
                                backStack.add(NavRoute.RegionsScreen(id, displayName))
                            }
                        )
                    }
                    entry<NavRoute.RegionsScreen> { key ->
                        RegionsScreen(
                            parentRegionName = key.parentRegionName,
                            viewModel = hiltViewModel<RegionsListViewModel, RegionsListViewModel.Factory>(
                                creationCallback = { it.create(key.parentRegionId) }
                            ),
                            onBack = {
                                backStack.removeIf { it == key }
                            },
                            navigateToChildRegions = { id, displayName ->
                                backStack.add(NavRoute.RegionsScreen(id, displayName))
                            }
                        )
                    }
                }
            )
        }
    }

    @Composable
    private fun RequestNotificationPermission() {
        var permissionRequested by rememberSaveable {
            mutableStateOf(false)
        }

        val permissionRequestLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = {}
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
                permissionRequested = true
                permissionRequestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
