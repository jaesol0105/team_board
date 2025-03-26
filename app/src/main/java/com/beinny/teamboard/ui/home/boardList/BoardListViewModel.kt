package com.beinny.teamboard.ui.home.boardList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beinny.teamboard.data.local.NotificationEntity
import com.beinny.teamboard.data.repository.NotificationRepository
import com.beinny.teamboard.models.Board
import kotlinx.coroutines.launch

class BoardListViewModel(private val repository: NotificationRepository): ViewModel() {
    private val _boardList = MutableLiveData<ArrayList<Board>>() // 내부에서 변경 가능한 데이터
    val boardList: LiveData<ArrayList<Board>> get() = _boardList // 외부에서 관찰 가능한 LiveData

    private val _bookmarkedBoardList = MutableLiveData<ArrayList<Board>>()
    val bookmarkedBoardList: LiveData<ArrayList<Board>> get() = _bookmarkedBoardList

    val allNotifications: LiveData<List<NotificationEntity>> // 알림
    val unreadCount: LiveData<Int> // 미확인 알림 개수

    /** [데이터 업데이트 함수 : 보드 목록] */
    fun updateBoardList(boardList: ArrayList<Board>) {
        _boardList.value = boardList
    }

    /** [데이터 업데이트 함수 : 북마크한 보드 목록] */
    fun updateBookmarkedBoardList(bookmarkedBoardList: ArrayList<Board>) {
        _bookmarkedBoardList.value = bookmarkedBoardList
    }

    /** [북마크한 보드 목록(로컬) : 추가] */
    fun addBookmarkedBoard(board: Board) {
        _bookmarkedBoardList.value = _bookmarkedBoardList.value?.let { oldList ->
            ArrayList(oldList).apply { add(board) }
        }
    }

    /** [북마크한 보드 목록(로컬) : 삭제] */
    fun removeBookmarkedBoard(board: Board) {
        _bookmarkedBoardList.value = _bookmarkedBoardList.value?.let { oldList ->
            ArrayList(oldList).apply { remove(board) }
        }
    }


    /** [푸시 알림 모두 읽음 처리] */
    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    init {
        allNotifications = repository.allNotifications
        unreadCount = repository.unreadCount
    }
}