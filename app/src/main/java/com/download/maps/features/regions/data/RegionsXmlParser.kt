package com.download.maps.features.regions.data

import com.download.maps.features.regions.data.model.RegionDto
import javax.inject.Inject
import javax.inject.Provider
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class RegionsXmlParser @Inject constructor(
    private val xmlPullParserProvider: Provider<XmlPullParser>
) {

    fun parse(inputStream: InputStream): RegionDto {
        val parser = xmlPullParserProvider.get()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "regions_list") {
                return parseRegionsList(parser)
            }
            eventType = parser.next()
        }
        throw IllegalArgumentException("Invalid regions.xml format")
    }

    private fun parseRegionsList(parser: XmlPullParser): RegionDto {
        val rootChildren = mutableListOf<RegionDto>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "region") {
                rootChildren.add(parseRegion(parser))
            } else {
                skip(parser)
            }
        }

        return RegionDto(
            name = "root",
            translate = null,
            type = null,
            map = null,
            downloadPrefix = null,
            innerDownloadPrefix = null,
            downloadSuffix = null,
            innerDownloadSuffix = null,
            children = rootChildren
        )
    }

    private fun parseRegion(parser: XmlPullParser): RegionDto {
        parser.require(XmlPullParser.START_TAG, null, "region")

        val name = parser.getAttributeValue(null, "name") ?: ""
        val translate = parser.getAttributeValue(null, "translate")
        val type = parser.getAttributeValue(null, "type")
        val map = parser.getAttributeValue(null, "map")
        val downloadPrefix = parser.getAttributeValue(null, "download_prefix")
        val innerDownloadPrefix = parser.getAttributeValue(null, "inner_download_prefix")
        val downloadSuffix = parser.getAttributeValue(null, "download_suffix")
        val innerDownloadSuffix = parser.getAttributeValue(null, "inner_download_suffix")

        val children = mutableListOf<RegionDto>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "region") {
                children.add(parseRegion(parser))
            } else {
                skip(parser)
            }
        }

        return RegionDto(
            name = name,
            translate = translate,
            type = type,
            map = map,
            downloadPrefix = downloadPrefix,
            innerDownloadPrefix = innerDownloadPrefix,
            downloadSuffix = downloadSuffix,
            innerDownloadSuffix = innerDownloadSuffix,
            children = children
        )
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) return
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
