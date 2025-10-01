package com.beinny.teamboard.ui.login

import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivitySignInBinding
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.common.launch
import com.beinny.teamboard.ui.common.repeatOnStarted
import com.beinny.teamboard.ui.factory.ViewModelFactory
import com.beinny.teamboard.ui.main.MainActivity
import com.beinny.teamboard.ui.state.UiState

class SignInActivity : BaseActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val viewModel: AuthViewModel by viewModels { ViewModelFactory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        setupActionBar(binding.toolbarSignIn)
        setupListeners()
        setupObservers()
    }

    private fun initBinding() {
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupListeners() {
        binding.btnSignIn.setOnClickListener {
            val email: String = binding.etSignInEmail.text.toString().trim { it <= ' ' }
            val password: String = binding.etSignInPassword.text.toString().trim { it <= ' ' }
            if (validateForm(email, password)) {
                viewModel.signIn(email, password)
            }
        }
        binding.btnSignInKakao.setOnClickListener {
            viewModel.signInWithKakaoSso(this)
        }
        binding.btnSignUp.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                launch<SignUpActivity>()
            }
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

    /** 입력 정보 유효성 검사 */
    private fun validateForm(email: String, password: String): Boolean {
        return if (TextUtils.isEmpty(email)) {
            showErrorSnackBar(getString(R.string.please_input_email))
            false
        } else if (TextUtils.isEmpty(password)) {
            showErrorSnackBar(getString(R.string.please_input_password))
            false
        } else {
            true
        }
    }
}