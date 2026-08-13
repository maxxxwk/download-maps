package com.download.maps.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Query
import retrofit2.http.Streaming

interface DownloadService {
    @GET("download.php")
    @Streaming
    suspend fun downloadFile(
        @Query("standard") standard: String = "yes",
        @Query("file") file: String
    ): ResponseBody

    @HEAD("download.php")
    suspend fun isExistsMapFile(
        @Query("standard") standard: String = "yes",
        @Query("file") file: String
    ) : Response<Unit>
}
