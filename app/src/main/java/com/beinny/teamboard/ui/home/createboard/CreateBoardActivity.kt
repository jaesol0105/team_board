package com.beinny.teamboard.ui.home.createboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivityCreateBoardBinding
import com.beinny.teamboard.firebase.FirestoreClass
import com.beinny.teamboard.models.Board
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.utils.Constants
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateBoardBinding
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName : String
    private var mBoardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        binding.ivCreateBoardImage.setOnClickListener {
            // 보드 이미지 선택 : 갤러리 인텐트
            Constants.showImageChooser(this)
        }

        binding.btnCreateBoard.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadBoardImage() // 이미지 업로드 및 보드 생성
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard() // 보드 생성
            }
        }

        createTransitionAnimation()
    }

    override fun finish() {
        super.finish()
        exitTransitionAnimation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /** 갤러리 인텐트를 통해 이미지를 선택했을 경우 */
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data // 선택된 이미지의 uri
            try {
                Glide
                    .with(this@CreateBoardActivity)
                    .load(Uri.parse(mSelectedImageFileUri.toString())) // 이미지의 uri
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder) // 디폴트 이미지
                    .into(binding.ivCreateBoardImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /** [액션바 설정] */
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarCreateBoard)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow_30)
        }

        binding.toolbarCreateBoard.setNavigationOnClickListener { onBackPressed() }
    }

    /** [선택된 이미지를 firebase storage에 업로드 & 데이터베이스에 보드 생성] */
    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        // 1. [storage reference]
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(this@CreateBoardActivity, mSelectedImageFileUri)
        )

        // 2. [reference에 이미지 파일 추가]
        sRef.putFile(mSelectedImageFileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // 3. [task snapshot으로 부터 이미지의 다운로드 가능한 url 얻기 & 데이터베이스에 보드 생성]
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())
                        mBoardImageURL = uri.toString() // 글로벌 변수에 할당

                        createBoard()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@CreateBoardActivity, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
    }


    /** [데이터베이스에 보드 생성] */
    private fun createBoard() {
        val assignedUsersArrayList: ArrayList<String> = ArrayList() // 멤버 구성원 리스트
        assignedUsersArrayList.add(getCurrentUserID()) // 현재 사용자 id 추가

        val board = Board(
            binding.etCreateBoardName.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )

        FirestoreClass().createBoard(this@CreateBoardActivity, board)
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

}