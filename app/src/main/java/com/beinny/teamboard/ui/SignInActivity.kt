package com.beinny.teamboard.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        binding.btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }
    }

    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarSignIn)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding.toolbarSignIn.setNavigationOnClickListener { onBackPressed() }
    }
    
    /** [이메일로 로그인] */
    private fun signInRegisteredUser() {
        // 공백처리
        val email: String = binding.etSignInEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etSignInPassword.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            // progress dialog
            showProgressDialog(resources.getString(R.string.please_wait))

            // FirebaseAuth를 통한 로그인
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        Toast.makeText(this@SignInActivity, "You have successfully signed in.", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    } else {
                        Toast.makeText(this@SignInActivity, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    /** [입력 검증] */
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
}