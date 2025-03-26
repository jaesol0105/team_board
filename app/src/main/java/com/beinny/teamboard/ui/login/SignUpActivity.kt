package com.beinny.teamboard.ui.login

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivitySignUpBinding
import com.beinny.teamboard.firebase.FirestoreClass
import com.beinny.teamboard.models.User
import com.beinny.teamboard.ui.base.BaseActivity
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

        // 전체 화면
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        binding.btnSignUp.setOnClickListener{
            registerUser()
        }
    }

    /** [액션바 설정] */
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarSignUp)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true) // 뒤로 가기 버튼
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_24_black) // 뒤로 가기 버튼 아이콘
        }

        // 네비게이션 버튼 클릭 : 뒤로 가기
        binding.toolbarSignUp.setNavigationOnClickListener { onBackPressed() }
    }

    /** [앱에서 회원 가입 : https://firebase.google.com/docs/auth/android/custom-auth] */
    private fun registerUser() {
        // 공백 제거 처리
        val name: String = binding.etSignUpName.text.toString().trim { it <= ' ' }
        val email: String = binding.etSignUpEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etSignUpPassword.text.toString().trim { it <= ' ' }

        // 입력 정보 유효성 검사
        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        if (task.isSuccessful) {
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            val registeredEmail = firebaseUser.email!!
                            val user = User(firebaseUser.uid, name, registeredEmail)

                            /** firebase database에 사용자 등록 */
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
                showErrorSnackBar(getString(R.string.please_input_name))
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar(getString(R.string.please_input_email))
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(getString(R.string.please_input_password))
                false
            }
            else -> {
                true
            }
        }
    }

    /** [firebase database에 사용자 등록 성공] */
    fun userRegisteredSuccess() {
        Toast.makeText(this@SignUpActivity, "회원 가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
        hideProgressDialog()

        // TODO : 테스트해보고 정상작동되면 지우기
        // 로그아웃 처리 (사용자 등록하면 자동으로 로그인 됨), intro로 보내서 재 로그인 하도록
        // FirebaseAuth.getInstance().signOut()
        finish() // 액티비티 종료
    }
}