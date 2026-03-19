package com.example.brife.data.remote.api

import com.example.brife.data.model.GoogleLoginRequest
import com.example.brife.data.model.LoginResponse
import com.example.brife.data.model.LogoutRequest
import com.example.brife.data.model.ReissueRequest
import com.example.brife.data.model.ReissueResponse
import com.example.brife.data.model.SocialAccessTokenRequest
import com.example.brife.data.model.TermsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login/kakao")
    suspend fun loginWithKakao(
        @Body request: SocialAccessTokenRequest
    ): Response<LoginResponse>

    @POST("auth/login/naver")
    suspend fun loginWithNaver(
        @Body request: SocialAccessTokenRequest
    ): Response<LoginResponse>

    @POST("auth/login/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): Response<LoginResponse>

    @POST("auth/terms")
    suspend fun agreeTerms(
        @Header("Authorization") authorization: String,
        @Body request: TermsRequest
    ): Response<Unit>

    @POST("auth/reissue")
    suspend fun reissueToken(
        @Body request: ReissueRequest
    ): Response<ReissueResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<Unit>
}