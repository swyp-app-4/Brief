package com.example.brife.data.remote

import com.example.brife.data.remote.api.OnboardingApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private const val BASE_URL =
        "https://virtserver.swaggerhub.com/soongsiluniv-b78/brife-api/1.0.0/"

    // 나중에 로그인 붙으면 토큰 저장소에서 꺼내서 넣기
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

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val onboardingApiService: OnboardingApiService =
        retrofit.create(OnboardingApiService::class.java)
}