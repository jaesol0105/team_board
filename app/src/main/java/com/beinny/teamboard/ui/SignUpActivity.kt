package com.beinny.teamboard.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivitySignUpBinding
import com.beinny.teamboard.firebase.FirestoreClass
import com.beinny.teamboard.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** [전체 화면] */
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        /** [액션바 설정] */
        setupActionBar()

        binding.btnSignUp.setOnClickListener{
            registerUser()
        }
    }

    /** [액션바 설정] */
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarSignUp)

        val actionBar = supportActionBar // 액션 바가 존재 하는 지 확인
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true) // 뒤로 가기 버튼
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp) // 뒤로 가기 버튼 아이콘
        }

        /** [내비게이션 버튼 클릭 : back] */
        binding.toolbarSignUp.setNavigationOnClickListener { onBackPressed() }
    }

    /** [앱에서 회원 가입 : firebase] */
    /** [https://firebase.google.com/docs/auth/android/custom-auth] */
    private fun registerUser() {
        /** [공백 제거 처리] */
        val name: String = binding.etSignUpName.text.toString().trim { it <= ' ' }
        val email: String = binding.etSignUpEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etSignUpPassword.text.toString().trim { it <= ' ' }

        /** [입력 정보 유효성 검사] */
        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait)) // progress dialog 출력
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        if (task.isSuccessful) {
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            val registeredEmail = firebaseUser.email!!
                            val user = User(firebaseUser.uid, name, registeredEmail)

                            /** [firebase database에 사용자 등록] */
                            FirestoreClass().registerUser(this@SignUpActivity, user)
                        } else {
                            Toast.makeText(this@SignUpActivity, task.exception!!.message, Toast.LENGTH_SHORT).show()
                        }
                    })
        }
    }

    /** [입력 정보 유효성 검사] */
    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("name을 입력해 주세요.")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Email을 입력해 주세요.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("password를 입력해 주세요.")
                false
            }
            else -> {
                true
            }
        }
    }

    /** [firebase database에 사용자 등록 성공] */
    fun userRegisteredSuccess() {
        Toast.makeText(this@SignUpActivity, "You have successfully registered.", Toast.LENGTH_SHORT).show()
        hideProgressDialog() // progress bar 숨기기

        FirebaseAuth.getInstance().signOut() // 로그아웃(사용자 등록할 때 자동으로 로그인 됨), intro로 보내서 재 로그인 하도록
        finish() // signup 액티비티 종료
    }
}