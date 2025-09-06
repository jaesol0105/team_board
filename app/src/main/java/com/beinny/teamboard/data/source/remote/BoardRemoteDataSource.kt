package com.beinny.teamboard.data.source.remote

import android.app.Activity
import android.net.Uri
import android.util.Log
import com.beinny.teamboard.data.model.Board
import com.beinny.teamboard.data.model.KakaoProfile
import com.beinny.teamboard.data.model.User
import com.beinny.teamboard.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.google.firebase.auth.OAuthProvider
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BoardRemoteDataSource {
    companion object {
        const val TAG = "BoardRemoteDataSource"
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseMessaging = FirebaseMessaging.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String = auth.currentUser?.uid.orEmpty()

    suspend fun signIn(email: String, password: String): Boolean {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user != null
    }

    suspend fun signUp(email: String, password: String): FirebaseUser? = withContext(Dispatchers.IO) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return@withContext result.user
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun registerUser(user: User) = withContext(Dispatchers.IO) {
        firestore.collection(Constants.USERS)
            .document(user.id)
            .set(user, SetOptions.merge()) // SetOptions.merge : 기존 데이터와 병합
            .await()
    }

    suspend fun loadUserData(): User = withContext(Dispatchers.IO) {
        val snapshot = firestore.collection(Constants.USERS)
            .document(getCurrentUserId()) // 현재 로그인 된 UID
            .get() // 정보 얻기
            .await()
        return@withContext snapshot.toObject(User::class.java)
            ?: throw Exception("User not found")
    }

    suspend fun getBoardsList(): List<Board> = withContext(Dispatchers.IO) {
        val snapshot = firestore.collection(Constants.BOARDS)
            // 현재 사용자가 멤버로 참여하고 있는 보드 목록을 요구하는 배열 쿼리
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .await()
        return@withContext snapshot.documents.mapNotNull { doc ->
            doc.toObject(Board::class.java)?.apply { documentId = doc.id }
        }
    }

    suspend fun updateBookmarks(userId: String, bookmarkBoards: ArrayList<String>) = withContext(Dispatchers.IO) {
        val bookmarkMap = HashMap<String, Any>()
        bookmarkMap[Constants.BOOKMARKED_BOARDS] = bookmarkBoards
        firestore.collection(Constants.USERS)
            .document(userId)
            .update(bookmarkMap)
            .await()
    }

    /** 유저 정보를 업데이트 */
    suspend fun updateUserData(userHashMap: HashMap<String, Any>): Boolean = withContext(Dispatchers.IO) {
        try {
            firestore.collection(Constants.USERS)
                .document(getCurrentUserId())
                .update(userHashMap)
                .await()
            true
        } catch (e: Exception) {
            Log.e("BoardRemoteDataSource", "FCM 토큰 업데이트 실패", e)
            false
        }
    }

    /** FCM 토큰을 얻고 사용자 정보에 추가한다 */
    suspend fun getFcmTokenAndUpdate(): Boolean {
        return try {
            val token = firebaseMessaging.token.await()
            val userHashMap = HashMap<String, Any>()
            userHashMap[Constants.FCM_TOKEN] = token
            updateUserData(userHashMap)
        } catch (e: Exception) {
            Log.e("BoardRemoteDataSource", "FCM 토큰 가져오기 실패", e)
            false
        }
    }

    /** 이미지 업로드 후 URL 반환 */
    suspend fun uploadImage(uri: Uri, fileName: String): String = withContext(Dispatchers.IO) {
        // storage 참조 객체
        val storageRef = FirebaseStorage.getInstance().reference.child(fileName)
        // 업로드 작업 객체
        storageRef.putFile(uri).await()
        // 이미지의 다운로드 가능한 url
        return@withContext storageRef.downloadUrl.await().toString()
    }

    /** 보드 테이블 생성 */
    suspend fun createBoard(board: Board) = withContext(Dispatchers.IO) {
        firestore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .await()
    }

    /** 특정 보드의 세부 정보를 가져옴 */
    suspend fun getBoardDetails(documentId: String): Board = withContext(Dispatchers.IO) {
        val snapshot = firestore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .await()

        return@withContext snapshot.toObject(Board::class.java)?.apply {
            this.documentId = snapshot.id
        } ?: throw Exception("Board not found")
    }

    /** 보드에 참여 중인 멤버들의 정보를 가져온다 */
    suspend fun getAssignedMembers(assignedTo: ArrayList<String>): List<User> = withContext(Dispatchers.IO) {
        val snapshot = firestore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .await()

        return@withContext snapshot.documents.mapNotNull { it.toObject(User::class.java) }
    }

    /** 보드의 태스크 목록(taskList)을 업데이트 */
    suspend fun updateTaskList(board: Board) = withContext(Dispatchers.IO) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        firestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .await()
    }

    /** 이메일을 통해 사용자 정보를 불러옴 */
    suspend fun searchUserByEmail(email: String): User = withContext(Dispatchers.IO) {
        val snapshot = firestore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email) // where 쿼리
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull() ?: throw Exception("user not found")
        return@withContext doc.toObject(User::class.java) ?: throw Exception("invalid user data")
    }

    /** 보드의 멤버 목록을 업데이트 */
    suspend fun assignMemberToBoard(board: Board) = withContext(Dispatchers.IO) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        firestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .await()
    }

    /** OIDC + 기본 프로필용 권장 스코프 */
    private val requiredScopes = listOf("openid", "profile_nickname", "profile_image", "account_email")

    /** 카카오 로그인(OIDC 보장): 성공 시 idToken 포함된 OAuthToken 반환
     * 카톡 SSO 우선, 실패 시 계정 로그인 */
    suspend fun loginWithKakaoOidc(activity: Activity): OAuthToken =
        suspendCoroutine { cont ->
            // 원자성 보장
            val once = AtomicBoolean(false)
            fun safeResume(block: () -> Unit) {
                if (once.compareAndSet(false, true)) block()
            }

            fun ensureIdTokenOrRequestScopes(initialToken: OAuthToken) {
                // 이미 idToken 있으면 바로 resume 하고 끝
                if (!initialToken.idToken.isNullOrBlank()) {
                    safeResume { cont.resume(initialToken) }
                    return
                }
                // 없으면 추가 스코프 동의 요청
                UserApiClient.instance.loginWithNewScopes(activity, requiredScopes) { t2, e2 ->
                    when {
                        e2 is ClientError && e2.reason == ClientErrorCause.Cancelled ->
                            safeResume { cont.resumeWithException(CancellationException("사용자 취소(스코프 동의)")) }

                        e2 != null ->
                            safeResume { cont.resumeWithException(e2) }

                        t2?.idToken.isNullOrBlank() ->
                            safeResume { cont.resumeWithException(IllegalStateException("스코프 동의 후에도 idToken 없음")) }

                        else ->
                            safeResume { cont.resume(t2!!) }
                    }
                }
            }

            // 공통 콜백 : 카카오계정 로그인
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                when {
                    error is ClientError && error.reason == ClientErrorCause.Cancelled ->
                        safeResume { cont.resumeWithException(CancellationException("사용자 취소(계정)")) }

                    error != null ->
                        safeResume { cont.resumeWithException(error) }

                    token == null ->
                        safeResume { cont.resumeWithException(IllegalStateException("카카오계정 로그인: 토큰 없음")) }

                    else ->
                        ensureIdTokenOrRequestScopes(token)
                }
            }

            // 카카오톡 앱 로그인 우선
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
                UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                    when {
                        // 에러 : 사용자가 직접 취소한 경우
                        error is ClientError && error.reason == ClientErrorCause.Cancelled ->
                            safeResume { cont.resumeWithException(CancellationException("사용자 취소(카톡)")) }
                        // 에러 : 실패한 경우
                        error != null -> {
                            UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
                        }
                        // 토큰이 없는 경우
                        token == null -> {
                            UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
                        }
                        // 성공한 경우
                        else -> ensureIdTokenOrRequestScopes(token)
                    }
                }
            }
            // 카카오톡 앱이 없을 경우 : 카카오계정 로그인
            else {
                UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
            }
        }

    /** OIDC Credential 생성 + Firebase 로그인 */
    suspend fun signInWithKakaoSsoOidc(activity: Activity): FirebaseUser? {
        val token = loginWithKakaoOidc(activity) // Kakao SDK에서 토큰 받기
        val idToken = token.idToken // OIDC id_token (필수)
        val accessToken = token.accessToken // 사용자 정보 (옵션)

        if (idToken.isNullOrEmpty()) {
            throw IllegalStateException("Kakao idToken is missing. Ensure 'openid' scope is granted.")
        }

        /** providerId, idToken, accessToken */
        val credential = OAuthProvider.getCredential("oidc.kakao", idToken, accessToken)

        val result = auth.signInWithCredential(credential).await()
        return result.user
    }


    /** 카카오 프로필 가져오기 — 이메일/닉네임/프사 */
    suspend fun fetchKakaoProfile(): KakaoProfile? = suspendCancellableCoroutine { cont ->
        UserApiClient.instance.me { user, error ->
            if (!cont.isActive) return@me // 코루틴이 이미 취소됐다면 무시

            if (error != null) {
                cont.resumeWithException(error) // 실패는 예외로 던짐
                return@me
            }

            val acc = user?.kakaoAccount
            cont.resume(
                KakaoProfile(
                    email = acc?.email,
                    nickname = acc?.profile?.nickname,
                    profileImageUrl = acc?.profile?.profileImageUrl
                )
            )
        }
    }

    /** 로그아웃 시 토큰 제거 */
    suspend fun detachAndDeleteFcmTokenForCurrentUser(): Boolean = withContext(Dispatchers.IO) {
        try {
            val uid = getCurrentUserId()
            // 현재 토큰 조회
            val token = firebaseMessaging.token.await()

            // 사용자 문서에서 이 토큰 매핑 해제
            val updates = hashMapOf<String, Any>()
            updates[Constants.FCM_TOKEN] = ""
            firestore.collection(Constants.USERS).document(uid).update(updates).await()

            // 기기 토큰 삭제 (이전 토큰 무효화)
            FirebaseMessaging.getInstance().deleteToken().await()
            true
        } catch (e: Exception) {
            Log.e("BoardRemoteDataSource", "FCM 토큰 해제/삭제 실패", e)
            false
        }
    }
}