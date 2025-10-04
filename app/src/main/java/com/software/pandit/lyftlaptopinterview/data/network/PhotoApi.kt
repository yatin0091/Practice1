package com.software.pandit.lyftlaptopinterview.data.network

import com.software.pandit.lyftlaptopinterview.data.Photo
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotoApi {

    @GET("photos")
    suspend fun getPhotos(@Query("page") page: Int, @Query("client_id") clientId: String): List<Photo>
}