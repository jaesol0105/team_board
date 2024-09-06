package com.beinny.teamboard.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.beinny.teamboard.databinding.ActivitySplashBinding
import com.beinny.teamboard.firebase.FirestoreClass
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
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // 폰트 설정
        val typeface: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        binding.tvSplashAppName.typeface = typeface

        /** 로그인 된 사용자는 main, 로그인 안된 사용자는 intro */
        CoroutineScope(Dispatchers.Main).launch{
            delay(1000L)

            val currentUserID = FirestoreClass().getCurrentUserID() // 현재 로그인 된 사용자 id

            if (currentUserID.isNotEmpty()) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            }

            finish() // 백버튼으로 돌아갈 수 없다
        }
    }
}