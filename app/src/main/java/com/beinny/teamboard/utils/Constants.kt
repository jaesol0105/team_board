package com.beinny.teamboard.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    // Firebase collection name
    const val USERS: String = "users"
    const val BOARDS: String = "boards"

    // Firebase database field name
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val ASSIGNED_TO : String = "assignedTo"
    const val TASK_LIST : String = "taskList"
    const val BOOKMARKED_BOARDS : String = "bookmarkedBoards"
    const val FCM_TOKEN:String = "fcmToken"

    // Intent key
    const val EXTRA_DOCUMENT_ID : String = "extra_document_id"
    const val EXTRA_BOARD_DETAIL: String = "extra_board_detail"
    const val EXTRA_BOARD_MEMBERS : String = "extra_board_members"
    const val EXTRA_TASK_ITEM_POSITION: String = "extra_task_item_position"
    const val EXTRA_CARD_ITEM_POSITION: String = "extra_card_item_position"

    // SharedPreference
    const val TEAMBOARD_PREFERENCES = "Teamboard_preference"
    const val FCM_TOKEN_UPDATED:String = "fcmTokenUpdated"
    const val KEY_AUTO_LOGIN_ENABLED:String = "keyAutoLoginEnabled"

    // FCM
    const val FCM_HTTP_V1_API_URL: String = "https://fcm.googleapis.com/v1/projects/teamboard-1451c/messages:send"
    const val FCM_KEY_TITLE:String = "title"

    // Dialog
    const val DIALOG_LABEL_COLOR = "DialogLabelColor"
    const val DIALOG_MEMBER_LIST = "DialogMemberList"
    const val DIALOG_DATE_TIME = "DialogDateTimePicker"
    const val ARG_DATE = "date"
    const val DATE_FORMAT = "yyyy년 M월 d일 (E) a hh:mm"

    // Result Code
    const val PICK_IMAGE_REQUEST_CODE = 1

    // String
    const val ID : String = "id"
    const val EMAIL: String = "email"
    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"

    /** 이미지 선택 : 갤러리 인텐트 */
    fun createGalleryIntent(): Intent {
        return Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
    }

    /** 이미지 선택 : 갤러리 인텐트 (구) */
    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    /** 선택된 이미지 확장자 얻기 */
    fun getFileExtension(activity: Activity, uri: Uri?): String {
        /** MimeTypeMap: MIME type과 파일 확장자를 매핑하는 양방향 맵
         * getExtensionFromMimeType: 주어진 MIME type의 파일 확장자를 얻는다.
         * contentResolver.getType: 주어진 content URL의 MIME type을 얻는다. */

        return uri?.let {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(it))
        } ?: ""
    }
}