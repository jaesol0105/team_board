package com.beinny.teamboard.ui.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beinny.teamboard.data.model.Board
import com.beinny.teamboard.data.model.User
import com.beinny.teamboard.data.repository.BoardRepository
import com.beinny.teamboard.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MembersViewModel(
    private val repository: BoardRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _members = MutableStateFlow<List<User>>(emptyList())
    val members: StateFlow<List<User>> = _members.asStateFlow()

    private val _board = MutableStateFlow(Board())
    val board: StateFlow<Board> = _board.asStateFlow()

    var anyChangesDone: Boolean = false
        private set

    fun setBoard(board: Board) {
        _board.value = board
        loadMembers()
    }

    private fun loadMembers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val board = _board.value
                _members.value = repository.getAssignedMembers(ArrayList(board.assignedTo))
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "멤버 불러오기 실패")
            }
        }
    }

    /** 이메일로 사용자 검색, 해당 사용자를 보드 멤버로 추가 */
    fun searchMemberByEmail(email: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val user = repository.getUserByEmail(email)
                assignMemberToBoard(user)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("사용자를 찾을 수 없습니다")
            }
        }
    }

    /** 보드에 멤버를 등록하고 FCM 메세지를 전송 */
    fun assignMemberToBoard(user: User) {
        viewModelScope.launch {
            try {
                val updatedBoard = _board.value.copy(
                    assignedTo = ArrayList(_board.value.assignedTo + user.id)
                )
                repository.assignMemberToBoard(updatedBoard)
                _board.value = updatedBoard

                val updatedMembers = repository.getAssignedMembers(updatedBoard.assignedTo)
                _members.value = updatedMembers

                anyChangesDone = true

                notificationHelper.sendMemberInviteNotification(
                    updatedBoard.name,
                    user.fcmToken,
                    updatedMembers.firstOrNull()?.name.orEmpty()
                )
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("멤버 등록 실패")
            }
        }
    }
}