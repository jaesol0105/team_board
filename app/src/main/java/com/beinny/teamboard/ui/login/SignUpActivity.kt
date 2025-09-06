package com.beinny.teamboard.ui.login

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.viewModels
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivitySignUpBinding
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.common.repeatOnStarted
import com.beinny.teamboard.ui.factory.ViewModelFactory
import com.beinny.teamboard.ui.state.UiState
import kotlinx.coroutines.flow.collectLatest

class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: AuthViewModel by viewModels { ViewModelFactory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinding()
        setupActionBar(binding.toolbarSignUp)
        setupListeners()
        setupObservers()
    }

    private fun initBinding() {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupListeners() {
        // 회원가입
        binding.btnSignUp.setOnClickListener {
            val name: String = binding.etSignUpName.text.toString().trim { it <= ' ' }
            val email: String = binding.etSignUpEmail.text.toString().trim { it <= ' ' }
            val password: String = binding.etSignUpPassword.text.toString().trim { it <= ' ' }
            if (validateForm(name,email,password)){
                viewModel.signUp(name, email, password)
            }
        }
    }

    private fun setupObservers() {
        repeatOnStarted {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> showProgressDialog(getString(R.string.please_wait))
                    is UiState.Success -> {
                        hideProgressDialog()
                        Toast.makeText(this@SignUpActivity, "회원 가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        viewModel.signOut()
                        finish()
                    }
                    is UiState.Error -> {
                        hideProgressDialog()
                        showErrorSnackBar(state.message)
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        val n = name.trim()
        val e = email.trim().lowercase() // 소문자로 변환
        val p = password.trim()

        return when {
            n.isEmpty() -> {
                showErrorSnackBar(getString(R.string.please_input_name)); false
            }
            !RegexRules.NAME.matches(n) -> {
                showErrorSnackBar(getString(R.string.invalid_name)); false
            }
            e.isEmpty() -> {
                showErrorSnackBar(getString(R.string.please_input_email)); false
            }
            !RegexRules.EMAIL.matches(e) -> {
                showErrorSnackBar(getString(R.string.invalid_email)); false
            }
            p.isEmpty() -> {
                showErrorSnackBar(getString(R.string.please_input_password)); false
            }
            !RegexRules.PASSWORD.matches(p) -> {
                showErrorSnackBar(getString(R.string.invalid_password)); false
            }
            else -> true
        }
    }
}