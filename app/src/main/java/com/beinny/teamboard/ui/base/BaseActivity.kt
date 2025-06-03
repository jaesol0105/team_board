package com.beinny.teamboard.ui.base

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.DialogLoadingBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false

    private lateinit var mProgressDialog: Dialog // 진행 바
    private lateinit var binding: DialogLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /** progress bar dialog 출력 */
    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this,R.style.transparentDialog)

        binding = DialogLoadingBinding.inflate(layoutInflater)
        mProgressDialog.setContentView(binding.root)

        binding.tvProgressText.text = text

        mProgressDialog.show()
    }

    /** progress bar dialog 숨기기 */
    fun hideProgressDialog() {
        if (::mProgressDialog.isInitialized && mProgressDialog.isShowing) {
            mProgressDialog.dismiss()
        }
    }

    /** 액션바 설정 */
    fun setupActionBar(toolbar: androidx.appcompat.widget.Toolbar, title: String? = null) {
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_arrow_30)
            title?.let { setTitle(title) }
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    /** 백버튼 두 번 누르면 종료 */
    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, resources.getString(R.string.please_click_back_again_to_exit), Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    /** 스낵바(하단) 경고 메세지 출력 */
    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this@BaseActivity, R.color.snackbar_error_color))
        snackBar.show()
    }

    /** 애니메이션 (우측) */
    fun createTransitionAnimation() {
        applyAnimationOpen(R.anim.left_transition, R.anim.none)
    }

    fun exitTransitionAnimation() {
        applyAnimationClose(R.anim.none, R.anim.right_transition)
    }

    private fun applyAnimationOpen(enterResId: Int, exitResId: Int) {
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_OPEN, enterResId, exitResId
            )
        } else {
            overridePendingTransition(enterResId, exitResId)
        }
    }

    private fun applyAnimationClose(enterResId: Int, exitResId: Int) {
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_CLOSE, enterResId, exitResId
            )
        } else {
            overridePendingTransition(enterResId, exitResId)
        }
    }
}