package com.example.brife.data.remote

import com.example.brife.data.remote.api.AuthApiService
import com.example.brife.data.remote.api.HomeApiService
import com.example.brife.data.remote.api.NewsApiService
import com.example.brife.data.remote.api.OnboardingApiService
import com.example.brife.data.remote.api.UserApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



object NetworkModule {

    // TODO: 백엔드 테스트 시 실제 서버 주소로 변경
    // 예) 공용 서버: "https://..."
    // 예) 로컬 서버(환경에 따라): "http://192.168.x.x:8080/api/v1/"
    private const val BASE_URL = "http://15.165.49.70:8080/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // TODO: /auth/reissue 연동 후 OkHttp Authenticator에 토큰 갱신 로직 추가 예정
    private val okHttpClient = OkHttpClient.Builder()
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

    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val homeApiService: HomeApiService by lazy {
        retrofit.create(HomeApiService::class.java)
    }

    val newsApiService: NewsApiService by lazy {
        retrofit.create(NewsApiService::class.java)
    }
}