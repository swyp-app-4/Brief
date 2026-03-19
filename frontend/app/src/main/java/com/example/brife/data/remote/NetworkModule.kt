package com.example.brife.data.remote

import com.example.brife.data.remote.api.AuthApiService
import com.example.brife.data.remote.api.OnboardingApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    // TODO: 백엔드 테스트 시 실제 서버 주소로 변경
    // 예) 공용 서버: "https://..."
    // 예) 로컬 서버(환경에 따라): "http://192.168.x.x:8080/api/v1/"
    private const val BASE_URL =
        "https://virtserver.swaggerhub.com/soongsiluniv-b78/brife-api/1.0.0/"

    // TODO: 로그인/토큰 저장 기능 추가 후 Authorization 헤더 연결
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            // .addHeader("Authorization", "Bearer $accessToken")
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val onboardingApiService: OnboardingApiService by lazy {
        retrofit.create(OnboardingApiService::class.java)
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}