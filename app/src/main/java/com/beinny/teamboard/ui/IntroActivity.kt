package com.beinny.teamboard.ui

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.beinny.teamboard.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** [전체 화면] */
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        /** [폰트 설정] */
        val typeface: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        binding.tvIntroAppName.typeface = typeface

        /** [로그인 버튼] */
        binding.btnIntroSignIn.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
        }

        /** [회원가입 버튼] */
        binding.btnIntroSignUp.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignUpActivity::class.java))
        }
    }
}