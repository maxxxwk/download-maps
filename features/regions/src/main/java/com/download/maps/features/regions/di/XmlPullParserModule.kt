package com.download.maps.features.regions.di

import android.util.Xml
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.xmlpull.v1.XmlPullParser

@Module
@InstallIn(SingletonComponent::class)
internal object XmlPullParserModule {
    @Provides
    fun provideXmlPullParser(): XmlPullParser = Xml.newPullParser()
}
