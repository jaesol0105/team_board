package com.beinny.teamboard.ui.createboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivityCreateBoardBinding
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.factory.ViewModelFactory
import com.beinny.teamboard.ui.state.UiState
import com.beinny.teamboard.utils.Constants
import com.bumptech.glide.Glide

class CreateBoardActivity : BaseActivity() {
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName : String

    private lateinit var binding: ActivityCreateBoardBinding
    private val viewModel: CreateBoardViewModel by viewModels { ViewModelFactory(applicationContext) }
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUserName = intent.getStringExtra(Constants.NAME) ?: ""

        initBinding()
        setupActionBar(binding.toolbarCreateBoard)
        setupListeners()
        setupObservers()
        setupLaunchers()

        createTransitionAnimation()
    }

    override fun finish() {
        super.finish()
        exitTransitionAnimation()
    }

    private fun initBinding() {
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupListeners() {
        // 보드 이미지 선택
        binding.ivCreateBoardImage.setOnClickListener {
            pickImageLauncher.launch(Constants.createGalleryIntent())
        }

        // 보드 생성 버튼
        binding.btnCreateBoard.setOnClickListener {
            val boardName = binding.etCreateBoardName.text.toString().trim()
            if (boardName.isBlank()) {
                Toast.makeText(this, getString(R.string.please_input_board_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 파일명
            val extension = Constants.getFileExtension(this, mSelectedImageFileUri)
            val fileName = if (extension.isNotBlank()) "BOARD_IMAGE_${System.currentTimeMillis()}.$extension" else ""

            viewModel.createBoard(boardName, mSelectedImageFileUri, mUserName, fileName) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> showProgressDialog(resources.getString(R.string.please_wait))
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