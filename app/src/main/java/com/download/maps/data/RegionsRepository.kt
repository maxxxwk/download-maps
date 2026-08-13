package com.download.maps.data

import android.content.Context
import com.download.maps.data.api.DownloadService
import com.download.maps.di.qualifiers.DispatcherIO
import com.download.maps.domain.model.Region
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.ConcurrentHashMap

@Singleton
class RegionsRepository @Inject constructor(
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher,
    @param:ApplicationContext private val context: Context,
    private val regionsXmlParser: RegionsXmlParser,
    private val regionsMapper: RegionsMapper,
    private val downloadService: DownloadService
) {

    private val availabilityCache = ConcurrentHashMap<String, Boolean>()

    suspend fun getRegionsByParentId(
        parentId: String
    ): Result<List<Region>> = runCatching {
        withContext(dispatcher) {
            val parsedRegions = regionsXmlParser.parse(context.assets.open("regions.xml"))
            val domainRegions = regionsMapper.mapToDomainList(parsedRegions)
            domainRegions
                .filter { it.parentId == parentId }
                .map { region ->
                    async {
                        if (!isMapAvailable(region)) {
                            region.copy(fileName = null)
                        } else {
                            region
                        }
                    }
                }.awaitAll()
        }
    }

    @Suppress("ReturnCount", "MagicNumber")
    private suspend fun isMapAvailable(region: Region): Boolean {
        val fileName = region.fileName ?: return false
        availabilityCache[region.id]?.let { return it }
        val response = downloadService.isExistsMapFile(file = fileName)
        val isAvailable = when {
            response.isSuccessful -> true
            response.code() == 404 -> false
            else -> throw HttpException(response)
        }
        availabilityCache[region.id] = isAvailable
        return isAvailable
    }
}
