package com.download.maps.di.modules

import android.util.Xml
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.xmlpull.v1.XmlPullParser

@Module
@InstallIn(SingletonComponent::class)
object ParserModule {
    @Provides
    fun provideXmlPullParser(): XmlPullParser = Xml.newPullParser()
}
