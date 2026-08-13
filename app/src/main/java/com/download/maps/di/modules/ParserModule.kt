package com.download.maps.di.modules

import android.util.Xml
import com.download.maps.data.RegionsXmlParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ParserModule {
    @Provides
    @Singleton
    fun provideRegionsXmlParser() = RegionsXmlParser(Xml.newPullParser())
}
