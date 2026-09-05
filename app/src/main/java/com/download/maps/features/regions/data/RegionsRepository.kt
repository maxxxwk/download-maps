package com.download.maps.features.regions.data

import android.content.Context
import com.download.maps.di.qualifiers.DispatcherIO
import com.download.maps.features.regions.data.api.DownloadService
import com.download.maps.features.regions.domain.model.Region
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private var cachedRegions: List<Region>? = null
    private val mutex = Mutex()

    suspend fun getRegionsByParentId(
        parentId: String
    ): Result<List<Region>> = runCatching {
        withContext(dispatcher) {
            getCachedRegions().filter { it.parentId == parentId }
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

    private suspend fun getCachedRegions(): List<Region> = cachedRegions ?: run {
        mutex.withLock {
            cachedRegions ?: run {
                regionsMapper.mapToDomainList(
                    context.assets.open("regions.xml")
                        .use(regionsXmlParser::parse)
                ).also { cachedRegions = it }
            }
        }
    }

    private suspend fun isMapAvailable(region: Region): Boolean {
        val fileName = region.fileName ?: return false
        return availabilityCache[region.id] ?: run {
            val response = downloadService.isExistsMapFile(file = fileName)
            @Suppress("MagicNumber")
            when {
                response.isSuccessful -> true
                response.code() == 404 -> false
                else -> throw HttpException(response)
            }.also { availabilityCache[region.id] = it }
        }
    }
}
