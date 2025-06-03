package com.beinny.teamboard.ui.login

import android.os.Bundle
import com.beinny.teamboard.databinding.ActivityIntroBinding
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.common.launch
import com.beinny.teamboard.ui.common.setCustomFont

class IntroActivity : BaseActivity() {
    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinding()
        setupFont()
        setupClickListeners()
    }

    private fun initBinding() {
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupFont() {
        binding.tvIntroAppName.setCustomFont("carbon bl.ttf")
    }

    private fun setupClickListeners() {
        binding.btnIntroSignIn.setOnClickListener {
            launch<SignInActivity>()
        }
        binding.btnIntroSignUp.setOnClickListener {
            launch<SignUpActivity>()
        }
    }
}