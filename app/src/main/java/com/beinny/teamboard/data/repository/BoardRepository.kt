package com.beinny.teamboard.data.repository

import android.app.Activity
import android.net.Uri
import com.beinny.teamboard.data.model.Board
import com.beinny.teamboard.data.model.User
import com.beinny.teamboard.data.source.local.SharedPrefsHelper
import com.beinny.teamboard.data.source.remote.BoardRemoteDataSource

class BoardRepository(
    private val remote: BoardRemoteDataSource,
    private val sharedPrefs: SharedPrefsHelper
) {
    suspend fun signIn(email: String, password: String): Boolean {
        return remote.signIn(email,password)
    }

    suspend fun signUp(email: String, password: String, name: String): Boolean {
        val result = remote.signUp(email, password)
        result?.let { user ->
            val newUser = User(
                id = user.uid,
                name = name,
                email = user.email.orEmpty()
            )
            remote.registerUser(newUser)
            return true
        }
        return false
    }

    /** 로그아웃 및 sharedPreference 초기화 */
    suspend fun signOut() {
        // auth.signOut() 전에 매핑 해제 + 토큰 삭제
        remote.detachAndDeleteFcmTokenForCurrentUser()
        remote.signOut()
        sharedPrefs.clearAll()
    }

    suspend fun loadUserData(): User = remote.loadUserData()

    suspend fun getBoardsList(): List<Board> = remote.getBoardsList()

    suspend fun updateBookmarks(userId: String, bookmarkBoards: ArrayList<String>) {
        remote.updateBookmarks(userId, bookmarkBoards)
    }

    fun getCurrentUserId(): String = remote.getCurrentUserId()

    /** FCM 토큰 유무를 체크하고, 없다면 갱신한다 */
    suspend fun checkAndUpdateFcmToken(): Boolean {
        if (!sharedPrefs.isFcmTokenUpdated()) {
            val success = remote.getFcmTokenAndUpdate()
            if (success) {
                sharedPrefs.setFcmTokenUpdated(true)
            }
            return success
        }
        return true // 이미 업데이트 되어 있었다면 true
    }

    /** 새 보드 생성 */
    suspend fun createBoard(boardName: String, imageUri: Uri?, userName: String, fileName: String) {
        val imageUrl = imageUri?.let {
            uploadImage(it, fileName)
        } ?: ""

        val board = Board(
            name = boardName,
            image = imageUrl,
            createdBy = userName,
            assignedTo = arrayListOf(getCurrentUserId())
        )

        remote.createBoard(board)
    }

    /** 이미지를 서버에 업로드 */
    suspend fun uploadImage(imageUri: Uri, fileName: String): String {
        return remote.uploadImage(imageUri, fileName)
    }

    /** 유저 정보를 업데이트 */
    suspend fun updateUserData(userHashMap: HashMap<String, Any>) {
        remote.updateUserData(userHashMap)
    }

    /** 보드 정보를 불러옴 */
    suspend fun getBoardDetails(documentId: String): Board {
        return remote.getBoardDetails(documentId)
    }

    /** 보드에 속한 멤버들 조회 */
    suspend fun getAssignedMembers(assignedTo: ArrayList<String>): List<User> {
        return remote.getAssignedMembers(assignedTo)
    }

    /** TaskList 변경사항을 반영하여 업데이트 */
    suspend fun updateTaskList(board: Board) {
        remote.updateTaskList(board)
    }

    /** 이메일을 통해 특정 유저의 정보 불러오기 */
    suspend fun getUserByEmail(email: String) : User {
        return remote.searchUserByEmail(email)
    }

    /** 멤버 등록을 업데이트 */
    suspend fun assignMemberToBoard(board: Board) {
        remote.assignMemberToBoard(board)
    }

    /** 카카오 SSO 로그인 */
    suspend fun signInWithKakaoSso(activity: Activity): Boolean {
        // OIDC 로그인
        val user = remote.signInWithKakaoSsoOidc(activity) ?: return false

        // 프로필 구성
        val profile = remote.fetchKakaoProfile()

        val newUser = User(
            id = user.uid,
            name = user.displayName ?: profile?.nickname ?: "",
            email = user.email ?: profile?.email ?: "",
            image = (user.photoUrl?.toString() ?: profile?.profileImageUrl.orEmpty()).forceHttps(),
            // mobile은 초기 0 유지, fcmToken은 getFcmTokenAndUpdate()가 저장
        )
        remote.registerUser(newUser)

        // FCM 토큰 갱신/저장
        val fcmUpdated = remote.getFcmTokenAndUpdate()
        // SharedPrefs 플래그(중복 갱신 방지용) 사용 중이면 세팅
        sharedPrefs.setFcmTokenUpdated(fcmUpdated)

        return true
    }

    /** http to https */
    private fun String.forceHttps(): String =
        if (startsWith("http://")) replaceFirst("http://", "https://") else this
}
