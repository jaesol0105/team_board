package com.beinny.teamboard.data.source.remote.firebase

import com.google.firebase.auth.FirebaseAuth

class FirestoreClass {
    /** 현재 로그인 된 아이디 반환 */
    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }
}