package com.beinny.teamboard.ui.myprofile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivityMyProfileBinding
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.factory.ViewModelFactory
import com.beinny.teamboard.ui.state.UiState
import com.beinny.teamboard.utils.Constants

class MyProfileActivity : BaseActivity() {
    private var mSelectedImageFileUri: Uri? = null

    private lateinit var binding: ActivityMyProfileBinding
    private val viewModel: MyProfileViewModel by viewModels { ViewModelFactory(applicationContext) }
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinding()
        setupActionBar(binding.toolbarMyProfile, resources.getString(R.string.nav_my_profile))
        setupListeners()
        setupObservers()
        setupLaunchers()

        viewModel.loadUser()
    }

    private fun initBinding() {
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)
    }

    private fun setupListeners() {
        // 프로필 이미지 선택
        binding.ivMyProfileUserImage.setOnClickListener {
            pickImageLauncher.launch(Constants.createGalleryIntent())
        }

        // 프로필 업데이트 버튼
        binding.btnMyProfileUpdate.setOnClickListener {
            val name = binding.etMyProfileName.text.toString()
            val mobile = binding.etMyProfileMobile.text.toString()
            // 파일명
            val extension = Constants.getFileExtension(this, mSelectedImageFileUri)
            val fileName = if (extension.isNotBlank()) "USER_IMAGE_${System.currentTimeMillis()}.$extension" else ""

            viewModel.updateUserData(name, mobile, mSelectedImageFileUri, fileName) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> showProgressDialog(getString(R.string.please_wait))
                is UiState.Success, is UiState.Idle -> hideProgressDialog()
                is UiState.Error -> {
                    hideProgressDialog()
                    showErrorSnackBar(state.message)
                }
            }
        }
    }

    private fun setupLaunchers() {
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
                val imageUri = result.data!!.data
                mSelectedImageFileUri = imageUri
                binding.imageUri = imageUri.toString()
            }
        }
    }
}