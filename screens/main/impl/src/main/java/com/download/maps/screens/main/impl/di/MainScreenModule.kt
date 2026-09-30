package com.download.maps.screens.main.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.download.maps.screens.main.api.MainScreenRoute
import com.download.maps.screens.main.impl.ui.MainScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object MainScreenModule {
    @Provides
    @IntoSet
    fun provideMainScreenBuilder(): EntryProviderScope<NavKey>.() -> Unit = {
        entry<MainScreenRoute> { MainScreen() }
    }
}
