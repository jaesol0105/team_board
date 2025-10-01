package com.beinny.teamboard.ui.login

import android.os.Bundle
import androidx.activity.viewModels
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivityIntroBinding
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.common.launch
import com.beinny.teamboard.ui.common.repeatOnStarted
import com.beinny.teamboard.ui.common.setCustomFont
import com.beinny.teamboard.ui.factory.ViewModelFactory
import com.beinny.teamboard.ui.main.MainActivity
import com.beinny.teamboard.ui.state.UiState

class IntroActivity : BaseActivity() {
    private lateinit var binding: ActivityIntroBinding
    private val viewModel: AuthViewModel by viewModels { ViewModelFactory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinding()
        setupFont()
        setupListeners()
        setupObservers()
    }

    private fun initBinding() {
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupFont() {
        binding.tvIntroAppName.setCustomFont("carbon bl.ttf")
    }

    private fun setupListeners() {
        binding.btnSignInKakao.setOnClickListener {
            viewModel.signInWithKakaoSso(this)
        }
        binding.btnSignInEmail.setOnClickListener {
            launch<SignInActivity>()
        }
    }

    private fun setupObservers() {
        repeatOnStarted {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> showProgressDialog(getString(R.string.please_wait))
                    is UiState.Success -> {
                        launch<MainActivity>()
                        finish()
                    }
                    is UiState.Error -> {
                        showErrorSnackBar(state.message)
                    }
                    else -> Unit
                }
            }
        }
    }
}