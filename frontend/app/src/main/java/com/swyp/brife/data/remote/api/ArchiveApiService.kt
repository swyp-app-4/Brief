package com.swyp.brife.data.remote.api

import com.swyp.brife.data.model.AddArchiveItemRequest
import com.swyp.brife.data.model.ArchiveFolderResponse
import com.swyp.brife.data.model.ArchiveItemResponse
import com.swyp.brife.data.model.ArchiveSearchResponse
import com.swyp.brife.data.model.ArchiveStatsResponse
import com.swyp.brife.data.model.CreateArchiveRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ArchiveApiService {

    @GET("archives")
    suspend fun getArchives(
        @Header("Authorization") authorization: String
    ): Response<List<ArchiveFolderResponse>>

    @GET("archives/stats")
    suspend fun getArchiveStats(): ArchiveStatsResponse

    @GET("archives/search")
    suspend fun searchArchive(
        @Header("Authorization") authorization: String,
        @Query("keyword") keyword: String
    ): ArchiveSearchResponse

    @POST("archives")
    suspend fun createArchive(
        @Header("Authorization") authorization: String,
        @Body request: CreateArchiveRequest
    ): Response<ArchiveFolderResponse>

    @GET("archives/{archiveId}/items")
    suspend fun getArchiveItems(
        @Header("Authorization") authorization: String,
        @Path("archiveId") archiveId: Long,
        @Query("sort") sort: String = "LATEST"
    ): Response<List<ArchiveItemResponse>>

    @POST("archives/favorite/items")
    suspend fun addToFavorites(
        @Header("Authorization") authorization: String,
        @Body request: AddArchiveItemRequest
    ): Response<ArchiveItemResponse>

    @POST("archives/{archiveId}/items")
    suspend fun addToArchive(
        @Header("Authorization") authorization: String,
        @Path("archiveId") archiveId: Long,
        @Body request: AddArchiveItemRequest
    ): Response<ArchiveItemResponse>

    @PATCH("archives/{archiveId}")
    suspend fun renameArchive(
        @Header("Authorization") authorization: String,
        @Path("archiveId") archiveId: Long,
        @Body request: CreateArchiveRequest
    ): Response<ArchiveFolderResponse>

    @DELETE("archives/{archiveId}")
    suspend fun deleteArchive(
        @Header("Authorization") authorization: String,
        @Path("archiveId") archiveId: Long
    ): Response<Unit>

    @DELETE("archives/{archiveId}/items/{itemId}")
    suspend fun deleteArchiveItem(
        @Header("Authorization") authorization: String,
        @Path("archiveId") archiveId: Long,
        @Path("itemId") itemId: Long
    ): Response<Unit>

}
