package com.download.maps.screens.regions.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.download.maps.screens.regions.api.RegionsScreenRoute
import com.download.maps.screens.regions.impl.ui.RegionsScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object RegionsScreenModule {
    @Provides
    @IntoSet
    fun provideRegionsScreenBuilder(): EntryProviderScope<NavKey>.() -> Unit = {
        entry<RegionsScreenRoute> {
            RegionsScreen(
                parentRegionId = it.parentRegionId,
                parentRegionName = it.parentRegionName
            )
        }
    }
}
