package com.beinny.teamboard.data.source.remote

import android.net.Uri
import android.util.Log
import com.beinny.teamboard.data.model.Board
import com.beinny.teamboard.data.model.User
import com.beinny.teamboard.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BoardRemoteDataSource {
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
        val bookmarkMap = HashMap<String,Any>()
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

        val doc = snapshot.documents.firstOrNull()?: throw Exception("user not found")
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
}