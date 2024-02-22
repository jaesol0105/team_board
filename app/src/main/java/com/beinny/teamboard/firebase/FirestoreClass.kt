package com.beinny.teamboard.firebase

import android.util.Log
import com.beinny.teamboard.models.User
import com.beinny.teamboard.ui.SignInActivity
import com.beinny.teamboard.ui.SignUpActivity
import com.beinny.teamboard.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/** [Firebase database] */
class FirestoreClass {

    /** [Firebase 기반의 Firestore 인스턴스] */
    private val mFireStore = FirebaseFirestore.getInstance()

    /** [새 사용자를 데이터베이스에 등록] */
    fun registerUser(activity: SignUpActivity, userInfo: User) {
        /** 핵심 : 사용자를 인증 모듈에 직접 등록할 뿐 아니라,
         * database에도 등록한다. (사용자의 식별자, 생성시기, UID) */

        /** [users/UID에 사용자 정보를 등록] */
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID()) // UID
            .set(userInfo, SetOptions.merge()) // SetOptions.merge : 기존 데이터와 병합
            .addOnSuccessListener {
                /** [액티비티에 성공 결과 전달] */
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error writing document",e)
            }
    }

    /** [로그인 한 사용자 정보를 얻어온다] */
    fun signInUser(activity: SignInActivity) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID()) // UID
            .get() // 정보 얻기
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                /** [로그인한 사용자 정보를 User 클래스로 맵핑] */
                val loggedInUser = document.toObject(User::class.java) // document.toObject : 원하는 객체에 맵핑
                if (loggedInUser != null) {
                    activity.signInSuccess(loggedInUser) // 액티비티로 결과 전달하기
                }
            }.addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error while getting loggedIn user details", e)
            }
    }

    /** [현재 로그인 된 유저의 아이디 가져오기] */
    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }
}