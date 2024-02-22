package com.beinny.teamboard.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivitySignInBinding
import com.beinny.teamboard.firebase.FirestoreClass
import com.beinny.teamboard.models.User
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** [전체 화면] */
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        /** [액션바 설정] */
        setupActionBar()

        /** [로그인 버튼] */
        binding.btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }
    }

    /** [액션바 설정] */
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarSignIn)

        val actionBar = supportActionBar // 액션 바가 존재 하는 지 확인
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true) // 뒤로 가기 버튼
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp) // 뒤로 가기 버튼 아이콘
        }

        /** [내비게이션 버튼 클릭 : back] */
        binding.toolbarSignIn.setNavigationOnClickListener { onBackPressed() }
    }
    
    /** [이메일을 통한 로그인] */
    private fun signInRegisteredUser() {
        /** [공백 제거 처리] */
        val email: String = binding.etSignInEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etSignInPassword.text.toString().trim { it <= ' ' }

        /** [입력 정보 유효성 검사] */
        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait)) // progress dialog 출력

            /** [FirebaseAuth를 통한 로그인] */
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FirestoreClass().signInUser(this@SignInActivity)
                    } else {
                        Toast.makeText(this@SignInActivity, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    /** [입력 정보 유효성 검사] */
    private fun validateForm(email: String, password: String): Boolean {
        return if (TextUtils.isEmpty(email)) {
            showErrorSnackBar("Please enter email.")
            false
        } else if (TextUtils.isEmpty(password)) {
            showErrorSnackBar("Please enter password.")
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