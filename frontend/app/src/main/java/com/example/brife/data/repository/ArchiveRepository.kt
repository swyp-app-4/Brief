package com.example.brife.data.repository

import android.util.Log
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.model.AddArchiveItemRequest
import com.example.brife.data.model.ArchiveItemResponse
import com.example.brife.data.model.CreateArchiveRequest
import com.example.brife.data.model.NewsDetailResponse
import com.example.brife.data.remote.api.ArchiveApiService
import com.example.brife.data.remote.api.NewsApiService
import com.example.brife.feature.archive.ArchiveFolderUiModel

class ArchiveRepository(
    private val api: ArchiveApiService,
    private val newsApi: NewsApiService,
    private val authLocalStorage: AuthLocalStorage
) {
    private fun bearerToken(): String? =
        authLocalStorage.getAccessToken()?.let { "Bearer $it" }

    suspend fun getFolders(): Result<List<ArchiveFolderUiModel>> {
        val token = bearerToken() ?: return Result.failure(Exception("로그인이 필요합니다"))
        return try {
            val response = api.getArchives(token)
            if (response.isSuccessful) {
                val folders = response.body()?.map {
                    ArchiveFolderUiModel(
                        archiveId = it.id,
                        folderName = it.folderName,
                        itemCount = it.itemCount,
                        isFavorite = it.favorite
                    )
                } ?: emptyList()
                Result.success(folders)
            } else {
                Result.failure(Exception("폴더 목록 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFolder(folderName: String): Result<ArchiveFolderUiModel> {
        val token = bearerToken() ?: return Result.failure(Exception("로그인이 필요합니다"))
        return try {
            val response = api.createArchive(token, CreateArchiveRequest(folderName))
            if (response.isSuccessful) {
                val body = response.body()!!
                Result.success(
                    ArchiveFolderUiModel(
                        archiveId = body.id,
                        folderName = body.folderName,
                        itemCount = body.itemCount,
                        isFavorite = body.favorite
                    )
                )
            } else {
                Result.failure(Exception("폴더 생성 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFolder(archiveId: Long): Result<Unit> {
        val token = bearerToken() ?: return Result.failure(Exception("로그인이 필요합니다"))
        return try {
            val response = api.deleteArchive(token, archiveId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("폴더 삭제 실패: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameFolder(archiveId: Long, newName: String): Result<ArchiveFolderUiModel> {
        val token = bearerToken() ?: return Result.failure(Exception("로그인이 필요합니다"))
        return try {
            val response = api.renameArchive(token, archiveId, CreateArchiveRequest(newName))
            if (response.isSuccessful) {
                val body = response.body()!!
                Result.success(
                    ArchiveFolderUiModel(
                        archiveId = body.id,
                        folderName = body.folderName,
                        itemCount = body.itemCount,
                        isFavorite = body.favorite
                    )
                )
            } else {
                Result.failure(Exception("폴더 수정 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItems(archiveId: Long, sort: String = "LATEST"): Result<List<ArchiveItemResponse>> {
        val token = bearerToken() ?: return Result.failure(Exception("로그인이 필요합니다"))
        return try {
            val response = api.getArchiveItems(token, archiveId, sort)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("아이템 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToFavorites(contentId: Long): Result<Unit> {
        val token = bearerToken() ?: return Result.failure(Exception("로그인이 필요합니다"))
        return try {
            val response = api.addToFavorites(token, AddArchiveItemRequest(contentId))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("즐겨찾기 추가 실패: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToFolder(archiveId: Long, contentId: Long): Result<Unit> {
        val token = bearerToken() ?: return Result.failure(Exception("로그인이 필요합니다"))
        return try {
            val response = api.addToArchive(token, archiveId, AddArchiveItemRequest(contentId))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("폴더 저장 실패: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNewsDetail(contentId: Long): Result<NewsDetailResponse> {
        return try {
            val response = newsApi.getNewsDetailAsync(contentId)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("뉴스 상세 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFromFavorites(itemId: Long): Result<Unit> {
        val token = bearerToken() ?: return Result.failure(Exception("로그인이 필요합니다"))
        return try {
            val response = api.deleteFavoriteItem(token, itemId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("즐겨찾기 아이템 삭제 실패: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteArchiveItem(archiveId: Long, itemId: Long): Result<Unit> {
        val token = bearerToken() ?: return Result.failure(Exception("로그인이 필요합니다"))
        return try {
            val response = api.deleteArchiveItem(token, archiveId, itemId)


            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("아이템 삭제 실패: ${response.code()} / $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}