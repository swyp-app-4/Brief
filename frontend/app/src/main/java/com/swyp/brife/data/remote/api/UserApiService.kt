package com.swyp.brife.data.remote.api

import com.swyp.brife.data.model.InterestRequest
import com.swyp.brife.data.model.UpdateProfileRequest
import com.swyp.brife.data.model.UserProfileResponse
import com.swyp.brife.data.model.WithdrawalRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApiService {

    // GET /users/me — 프로필 조회
    @GET("users/me")
    suspend fun getMyProfile(
        @Header("Authorization") authorization: String
    ): Response<UserProfileResponse>

    // POST /users/me/interests — 온보딩 최초 관심사 저장 (신규회원 전용)
    @POST("users/me/interests")
    suspend fun saveInterests(
        @Header("Authorization") authorization: String,
        @Body request: InterestRequest
    ): Response<Unit>

    // PUT /users/me/interests — 관심사 재설정 (온보딩 이후 변경 시)
    @PUT("users/me/interests")
    suspend fun updateInterests(
        @Header("Authorization") authorization: String,
        @Body request: InterestRequest
    ): Response<Unit>

    // PATCH /users/me — 프로필 수정 (닉네임, 이미지)
    @PATCH("users/me")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequest
    ): Response<Unit>

    // DELETE /users/me — 회원 탈퇴 (30일 후 완전 삭제)
    @DELETE("users/me")
    suspend fun deleteUser(
        @Header("Authorization") authorization: String
    ): Response<Unit>

    // DELETE /users/me — Naver 회원 탈퇴 (OAuth 연동 해제용 refresh token 포함)
    @HTTP(method = "DELETE", path = "users/me", hasBody = true)
    suspend fun deleteNaverUser(
        @Header("Authorization") authorization: String,
        @Body request: WithdrawalRequest
    ): Response<Unit>
}
