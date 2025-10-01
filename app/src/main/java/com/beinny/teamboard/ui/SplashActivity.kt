package com.beinny.teamboard.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.beinny.teamboard.data.source.local.SharedPrefsHelper
import com.beinny.teamboard.databinding.ActivitySplashBinding
import com.beinny.teamboard.data.source.remote.firebase.FirestoreClass
import com.beinny.teamboard.ui.common.hideStatusBar
import com.beinny.teamboard.ui.common.launch
import com.beinny.teamboard.ui.common.setCustomFont
import com.beinny.teamboard.ui.main.MainActivity
import com.beinny.teamboard.ui.login.IntroActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 전체 화면
        hideStatusBar()

        // 폰트 설정
        binding.tvSplashAppName.setCustomFont("carbon bl.ttf")

        // 로그인 된 사용자는 main, 로그인 안된 사용자는 intro
        CoroutineScope(Dispatchers.Main).launch{
            delay(1000L)

            // 현재 로그인 된 사용자 id
            val currentUserID = FirestoreClass().getCurrentUserID()

            if (currentUserID.isNotEmpty()) {
                launch<MainActivity>()
            } else {
                launch<IntroActivity>()
            }

            finish() // 백버튼으로 돌아갈 수 없도록
        }
    }
}