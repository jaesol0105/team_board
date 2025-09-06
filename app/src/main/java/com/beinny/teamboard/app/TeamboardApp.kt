package com.beinny.teamboard.app

import android.app.Application
import android.util.Log
import com.beinny.teamboard.BuildConfig
import com.beinny.teamboard.R
import com.kakao.sdk.common.KakaoSdk

class TeamboardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("TeamboardApp", "keyHash=" + com.kakao.sdk.common.util.Utility.getKeyHash(applicationContext))
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}