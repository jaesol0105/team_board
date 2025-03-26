package com.beinny.teamboard.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivitySignInBinding
import com.beinny.teamboard.firebase.FirestoreClass
import com.beinny.teamboard.models.User
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.home.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 전체화면
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        binding.btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }
    }

    /** [액션바 설정] */
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarSignIn)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true) // 뒤로 가기 버튼
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_24_black) // 뒤로 가기 버튼 아이콘
        }

        // 네비게이션 버튼 클릭 : 뒤로 가기
        binding.toolbarSignIn.setNavigationOnClickListener { onBackPressed() }
    }
    
    /** [이메일을 통한 로그인] */
    private fun signInRegisteredUser() {
        // 공백 제거 처리
        val email: String = binding.etSignInEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etSignInPassword.text.toString().trim { it <= ' ' }

        // 입력 정보 유효성 검사
        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            // FirebaseAuth를 통한 로그인 수행
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FirestoreClass().loadUserData(this@SignInActivity)
                    } else {
                        Toast.makeText(this@SignInActivity, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    /** [입력 정보 유효성 검사] */
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

    /** [로그인 성공] */
    fun signInSuccess(user: User) {
        hideProgressDialog()
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }
}