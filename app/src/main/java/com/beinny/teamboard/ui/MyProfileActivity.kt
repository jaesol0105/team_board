package com.beinny.teamboard.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivityMyProfileBinding
import com.beinny.teamboard.firebase.FirestoreClass
import com.beinny.teamboard.models.User
import com.beinny.teamboard.utils.Constants
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private lateinit var binding: ActivityMyProfileBinding

    private lateinit var mUserDetails: User
    private var mProfileImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()

        FirestoreClass().loadUserData(this)

        binding.ivMyProfileUserImage.setOnClickListener {
            /** 프로필 이미지 선택 : 갤러리 인텐트 */
            Constants.showImageChooser(this)
        }

        binding.btnMyProfileUpdate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_my_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.done -> {
                if (mSelectedImageFileUri != null) {
                    uploadUserImage()
                } else {
                    showProgressDialog(resources.getString(R.string.please_wait))
                    updateUserProfileData()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /** 프로필 이미지 선택 : 갤러리 인텐트 */
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data
            try {
                Glide
                    .with(this@MyProfileActivity)
                    .load(Uri.parse(mSelectedImageFileUri.toString())) // 이미지의 URI
                    .centerCrop() // 이미지의 scale type
                    .placeholder(R.drawable.ic_user_place_holder) // 이미지의 디폴트 표시
                    .into(binding.ivMyProfileUserImage) // 이미지가 들어갈 뷰
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /** [액션바 설정] */
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarMyProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_24)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }

        binding.toolbarMyProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    /** [선택된 이미지를 firebase storage에 업로드 & 프로필 변경사항 데이터베이스 반영] */
    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {
            // 1. [storage 참조를 생성, storage에 저장할 파일명 설정]
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(this,mSelectedImageFileUri)
            )

            // 2. [파일을 storage 참조에 추가]
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.e("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())

                        // 3. [업로드한 이미지의 uri]
                        mProfileImageURL = uri.toString()

                        // 4. [데이터베이스에 프로필 변경사항 반영]
                        updateUserProfileData()
                    }
            }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@MyProfileActivity,exception.message,Toast.LENGTH_LONG).show()
                    hideProgressDialog()
                }
        }
    }

    /** [프로필 변경사항 데이터베이스 반영] */
    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>() // 해시 맵을 이용

        // 변경 사항 체크, 업데이트할 필드를 해시 맵에 추가
        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (binding.etMyProfileName.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = binding.etMyProfileName.text.toString()
        }

        if (binding.etMyProfileMobile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = binding.etMyProfileMobile.text.toString().toLong()
        }

        // 데이터베이스에 반영
        FirestoreClass().updateUserProfileData(this@MyProfileActivity, userHashMap)
    }

    /** [사용자 데이터를 UI에 반영] */
    fun setUserDataInUI(user: User) {
        mUserDetails = user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivMyProfileUserImage)

        binding.etMyProfileName.setText(user.name)
        binding.etMyProfileEmail.setText(user.email)
        if (user.mobile != 0L){
            binding.etMyProfileMobile.setText(user.mobile.toString())
        }
    }

    /** [프로필 업데이트 성공 : FirestoreClass()에서 호출] */
    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}