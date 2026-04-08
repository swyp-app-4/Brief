package com.swyp.brife

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK



class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "63e69c8cefe7b697e4735e262f3fa332")

        NaverIdLoginSDK.initialize(
            this,
            "URRJA3uPTyDqH8g1Ynh3",
            "LoWMz9JBzR",
            "Brife"
        )
    }
}


