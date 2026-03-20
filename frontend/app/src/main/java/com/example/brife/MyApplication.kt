package com.example.brife

import android.app.Application
import com.kakao.sdk.common.KakaoSdk




class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "63e69c8cefe7b697e4735e262f3fa332")
    }
}


