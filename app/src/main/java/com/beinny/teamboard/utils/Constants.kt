package com.beinny.teamboard.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.beinny.teamboard.ui.MyProfileActivity

object Constants {
    // Firebase collection name for USERS
    const val USERS: String = "users"

    // Firebase collection name for BOARDS
    const val BOARDS: String = "boards"

    // Firebase database field 명
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val ASSIGNED_TO : String = "assignedTo"

    const val READ_STORAGE_PERMISSION_CODE = 1 // 읽기 권한 요청
    const val PICK_IMAGE_REQUEST_CODE = 2 // 이미지 선택 결과

    const val DOCUMENT_ID : String = "documentId"

    const val TASK_LIST : String = "taskList"

    const val BOARD_DETAIL: String = "board_detail"

    const val ID : String = "id"
    const val EMAIL: String = "email"

    const val BOARD_MEMBERS_LIST : String = "board_members_list"

    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"

    const val TEAMBOARD_PREFERENCES = "Teamboard_preference"

    const val FCM_TOKEN:String = "fcmToken"
    const val FCM_TOKEN_UPDATED:String = "fcmTokenUpdated"

    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"

    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_HTTP_V1_API_URL: String = "https://fcm.googleapis.com/v1/projects/teamboard-1451c/messages:send"
    const val FCM_KEY:String = "key"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"

    /** [프로필 이미지 선택 : 갤러리 인텐트] */
    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    /** [선택된 이미지 확장자 얻기] */
    fun getFileExtension(activity: Activity,uri: Uri?): String? {
        // MimeTypeMap: MIME type과 파일 확장자를 매핑하는 양방향 맵
        // getSingleton(): MimeTypeMap의 싱글톤 객체를 가져온다.
        // getExtensionFromMimeType: 주어진 MIME type의 파일 확장자를 얻는다.
        // contentResolver.getType: 주어진 content URL의 MIME type을 얻는다.

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}