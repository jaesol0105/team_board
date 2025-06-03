package com.beinny.teamboard.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beinny.teamboard.data.model.Board
import com.beinny.teamboard.data.model.Card
import com.beinny.teamboard.data.model.Task
import com.beinny.teamboard.data.model.User
import com.beinny.teamboard.data.repository.BoardRepository
import com.beinny.teamboard.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskListViewModel(
    private val boardRepository: BoardRepository
) : ViewModel() {

    private val _boardWithMembers = MutableStateFlow(Board() to emptyList<User>())
    val boardWithMembers: StateFlow<Pair<Board, List<User>>> = _boardWithMembers.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val board: Board
        get() = boardWithMembers.value.first

    val members: List<User>
        get() = boardWithMembers.value.second

    /** 보드 정보와, 해당 보드에 속한 멤버 정보를 불러옴 */
    fun loadBoard(documentId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val board = boardRepository.getBoardDetails(documentId)
                val members = boardRepository.getAssignedMembers(board.assignedTo)
                _boardWithMembers.value = board to members
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error")
            }
        }
    }

    /** 새 태스크 생성 */
    fun createTaskList(name: String) {
        val (board, _) = boardWithMembers.value
        val newTask = Task(name, getCurrentUserId())
        val newTaskList = ArrayList(board.taskList).apply {
            add(0,newTask) // 리스트의 맨 앞에 추가한다
        }
        val newBoard = board.copy(taskList = newTaskList)
        updateTaskList(newBoard)
    }

    /** 태스크 이름 변경 */
    fun editTaskList(position: Int, name: String, task: Task) {
        val (board, _) = boardWithMembers.value
        val newTask = Task(name, task.createdBy, task.cards)
        val newTaskList = ArrayList(board.taskList).apply {
            set(position, newTask)
        }
        val newBoard = board.copy(taskList = newTaskList)
        updateTaskList(newBoard)
    }

    /** 테스크 삭제 */
    fun deleteTaskList(position: Int) {
        val (board, _) = boardWithMembers.value
        val newTaskList = ArrayList(board.taskList).apply {
            removeAt(position)
        }
        val newBoard = board.copy(taskList = newTaskList)
        updateTaskList(newBoard)
    }

    /** 태스크에 카드를 추가 */
    fun addCardToTaskList(position: Int, name: String) {
        val (board, _) = boardWithMembers.value
        val newCards = ArrayList(board.taskList[position].cards).apply {
            add(Card(name, getCurrentUserId(), arrayListOf(getCurrentUserId())))
        }
        val newTask = board.taskList[position].copy(cards = newCards)
        val newTaskList = ArrayList(board.taskList).apply {
            set(position, newTask)
        }
        val newBoard = board.copy(taskList = newTaskList)
        updateTaskList(newBoard)
    }

    /** 드래그를 통한 카드 순서 변경 */
    fun reorderCardsInTaskList(position: Int, cards: ArrayList<Card>) {
        val (board, _) = boardWithMembers.value
        val newTask = board.taskList[position].copy(cards = cards)
        val newTaskList = ArrayList(board.taskList).apply {
            set(position, newTask)
        }
        val newBoard = board.copy(taskList = newTaskList)
        updateTaskList(newBoard)
    }

    /** 태스크 업데이트를 원격 저장소에 반영 */
    fun updateTaskList(updatedBoard: Board) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val (_, members) = boardWithMembers.value
                boardRepository.updateTaskList(updatedBoard)
                _boardWithMembers.value = updatedBoard to members
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "업데이트 실패")
            }
        }
    }

    private fun getCurrentUserId(): String = boardRepository.getCurrentUserId()
}