package com.beinny.teamboard.ui.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beinny.teamboard.data.model.NotificationEntity
import com.beinny.teamboard.data.repository.NotificationRepository
import com.beinny.teamboard.data.model.Board
import com.beinny.teamboard.data.model.User
import com.beinny.teamboard.data.repository.BoardRepository
import com.beinny.teamboard.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BoardListViewModel(
    private val boardRepository: BoardRepository,
    private val notificationRepository: NotificationRepository
): ViewModel() {
    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user.asStateFlow()

    private val _boardList = MutableStateFlow(arrayListOf<Board>())
    val boardList: StateFlow<ArrayList<Board>> = _boardList.asStateFlow()

    private val _bookmarkedBoardList = MutableStateFlow(arrayListOf<Board>())
    val bookmarkedBoardList: StateFlow<ArrayList<Board>> = _bookmarkedBoardList.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val notificationsList = notificationRepository.allNotifications
    val unreadNotificationsCount = notificationRepository.unreadCount

    fun setBoardList(boardList: ArrayList<Board>) {
        _boardList.value = boardList
    }

    fun setBookmarkedBoardList(bookmarkedBoardList: ArrayList<Board>) {
        _bookmarkedBoardList.value = bookmarkedBoardList
    }

    /** 모든 푸시알림 읽음 처리 */
    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    /** 특정 푸시알림 삭제 */
    fun deleteNotification(notificationEntity: NotificationEntity) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationEntity)
        }
    }

    /** 유저 정보 불러오기 */
    fun loadUser() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val user = boardRepository.loadUserData()
                _user.value = user
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("불러오기 실패")
            }
        }
    }

    /** 유저 정보 & 보드 정보 불러오기 */
    fun loadUserAndBoard() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val user = boardRepository.loadUserData()
                val boards = boardRepository.getBoardsList()

                _user.value = user

                // 북마크 상태 반영
                val bookmarked = boards.filter { user.bookmarkedBoards.contains(it.documentId) }
                bookmarked.forEach { it.bookmarked = true }

                _boardList.value = ArrayList(boards)
                _bookmarkedBoardList.value = ArrayList(bookmarked)

                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("불러오기 실패")
            }
        }
    }

    /** 북마크 상태 변경 */
    fun toggleBookmark(board: Board) {
        val currentBookmarks = _user.value.bookmarkedBoards
        val userId = _user.value.id

        val updatedBookmarks = if (board.bookmarked) {
            currentBookmarks.plus(board.documentId)
        } else {
            currentBookmarks.filterNot { it == board.documentId }
        }

        updateUserBookmarks(userId, ArrayList(updatedBookmarks))
        setBookmarkedBoardList(ArrayList(_boardList.value.filter { it.bookmarked }))
    }

    /** 유저의 북마크 목록을 서버에 업데이트 */
    fun updateUserBookmarks(userId: String, bookmarks: ArrayList<String>) {
        viewModelScope.launch {
            try {
                boardRepository.updateBookmarks(userId, bookmarks)
            } catch (e: Exception) {
                Log.e("BoardListViewModel", "bookmark update error", e)
            }
        }
    }

    /** FCM 토큰 체크 및 토큰 업데이트 */
    fun checkAndUpdateFCMToken() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val success = boardRepository.checkAndUpdateFcmToken()
            if (success) {
                loadUserAndBoard()
            } else {
                _uiState.value = UiState.Error("토큰 업데이트 실패")
            }
        }
    }

    /** 로그아웃 및 sharedPreference 초기화 */
    fun signOut(onComplete : () -> Unit) {
        viewModelScope.launch {
            boardRepository.signOut()
        }
        onComplete()
    }
}