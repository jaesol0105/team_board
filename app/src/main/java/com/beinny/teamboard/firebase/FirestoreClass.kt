package com.beinny.teamboard.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.beinny.teamboard.models.Board
import com.beinny.teamboard.models.Task
import com.beinny.teamboard.models.User
import com.beinny.teamboard.ui.CardDetailsActivity
import com.beinny.teamboard.ui.CreateBoardActivity
import com.beinny.teamboard.ui.MainActivity
import com.beinny.teamboard.ui.MembersActivity
import com.beinny.teamboard.ui.MyProfileActivity
import com.beinny.teamboard.ui.SignInActivity
import com.beinny.teamboard.ui.SignUpActivity
import com.beinny.teamboard.ui.TaskListActivity
import com.beinny.teamboard.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/** [Firebase database] */
class FirestoreClass {

    /** [Firestore 인스턴스] */
    private val mFireStore = FirebaseFirestore.getInstance()

    /** [현재 로그인 된 유저의 아이디 반환] */
    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    /** [새 사용자를 데이터베이스에 등록] */
    fun registerUser(activity: SignUpActivity, userInfo: User) {
        // users/UID에 사용자 정보를 등록
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID()) // UID
            .set(userInfo, SetOptions.merge()) // SetOptions.merge : 기존 데이터와 병합
            .addOnSuccessListener {
                activity.userRegisteredSuccess() // 액티비티에 성공 결과 전달
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error writing document",e)
            }
    }

    /** [Firebase로 부터 로그인 된 사용자 정보 불러오기] */
    fun loadUserData(activity: Activity,isToReadBoardsList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID()) // 현재 로그인 된 UID
            .get() // 정보 얻기
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                // document.toObject : 정보를 원하는 객체(User)에 맵핑
                val loggedInUser = document.toObject(User::class.java)
                if (loggedInUser != null) {
                    when (activity) {
                        is SignInActivity -> {
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser,isToReadBoardsList)
                        }
                        is MyProfileActivity -> {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }
            }.addOnFailureListener { e ->
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while getting loggedIn user details", e)
            }
    }

    /** [데이터베이스에 프로필 변경사항 반영]
     * @param userHashMap 업데이트 할 필드의 해시 맵 */
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                when (activity) {
                    // 토큰 업데이트
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    // 프로필 업데이트
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    /** [데이터베이스에 보드 테이블을 생성하고 엔트리를 추가] */
    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.",e)
            }
    }

    /** [데이터베이스에서 보드 목록 불러오기] */
    fun getBoardsList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS) // Firestore DB의 boards 컬렉션 참조
            // 현재 사용자가 멤버로 참여하고있는 보드 목록을 요구하는 배열 쿼리
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get() // 문서 읽기
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val boardsList: ArrayList<Board> = ArrayList()
                // 문서 형식의 보드 목록 -> ArrayList<Board>
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardsList.add(board)
                }
                // base activity로 결과 전달
                activity.populateBoardsListToUI(boardsList)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    /** [보드 디테일 정보 불러오기] */
    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                // 문서 형태로 불러온 보드 디테일을 Board 객체로 변환하고 document id 를 할당한다.
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id

                activity.boardDetails(board) // 액티비티로 결과 전달
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    /** [특정 보드의 작업목록을 업데이트하기] */
    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener { e ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                } else if (activity is CardDetailsActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)

            }
    }

    /** [보드에 등록된 멤버들의 세부정보 가져오기] */
    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()
                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                if (activity is MembersActivity)
                    activity.setupMembersList(usersList)
                else if (activity is TaskListActivity)
                    activity.boardMembersDetailList(usersList)
            }
            .addOnFailureListener { e ->
                if (activity is MembersActivity)
                    activity.hideProgressDialog()
                else if (activity is TaskListActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,  "Error while creating a board.", e)
            }
    }

    /** [이메일을 통해서 사용자 정보를 가져오기] */
    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email) // where 쿼리
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                if (document.documents.size > 0) {
                    // 0번째 요소만 가져오는 이유 : 이메일은 고유하기 때문에 어차피 결과 값은 하나임
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("사용자를 찾을 수 없습니다")
                }

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details",
                    e
                )
            }
    }

    /** [특정 사용자를 보드의 멤버로 추가하여 데이터베이스에 갱신]
     * @param board 해당 사용자를 멤버로 추가한 보드 */
    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

}