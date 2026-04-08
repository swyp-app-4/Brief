package com.swyp.brife.data.remote.api

import com.swyp.brife.data.model.GoogleLoginRequest
import com.swyp.brife.data.model.ReissueRequest
import com.swyp.brife.data.model.ReissueResponse
import com.swyp.brife.data.model.SocialAccessTokenRequest
import com.swyp.brife.data.model.TermsRequest
import com.swyp.brife.data.model.LoginResponse
import com.swyp.brife.data.model.LogoutRequest
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