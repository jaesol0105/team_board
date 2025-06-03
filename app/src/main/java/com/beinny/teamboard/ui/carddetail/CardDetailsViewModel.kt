package com.beinny.teamboard.ui.carddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beinny.teamboard.data.model.Board
import com.beinny.teamboard.data.model.CardParams
import com.beinny.teamboard.data.repository.BoardRepository
import com.beinny.teamboard.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class CardDetailsViewModel(
    private val boardRepository: BoardRepository
) : ViewModel() {

    private val _cardParams = MutableStateFlow<CardParams?>(null)
    val cardParams: StateFlow<CardParams?> = _cardParams.asStateFlow()

    private val _selectedDate = MutableStateFlow(0L)
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _selectedColor = MutableStateFlow("")
    val selectedColor: StateFlow<String> = _selectedColor.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun setCardParams(params: CardParams) {
        _cardParams.value = params
        _selectedColor.value = params.card.labelColor
        _selectedDate.value = params.card.dueDate
    }

    fun setSelectedColor(color: String) {
        _selectedColor.value = color
    }

    fun setSelectedDate(date: Date) {
        _selectedDate.value = date.time
    }

    /** 카드 업데이트 */
    fun updateCard(name: String) {
        val params = _cardParams.value ?: return
        val updatedBoard = params.board.copy().apply {
            val updatedCard = taskList[params.taskPosition].cards[params.cardPosition].copy(
                name = name,
                labelColor = _selectedColor.value,
                dueDate = _selectedDate.value
            )
            taskList[params.taskPosition].cards[params.cardPosition] = updatedCard
        }
        updateTaskList(updatedBoard)
    }

    /** 카드 삭제 */
    fun deleteCard() {
        val params = _cardParams.value ?: return
        val updatedBoard = params.board.copy().apply {
            taskList[params.taskPosition].cards.removeAt(params.cardPosition)
        }
        updateTaskList(updatedBoard)
    }

    /** 업데이트를 원격 저장소에 반영 */
    fun updateTaskList(updatedBoard: Board) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                boardRepository.updateTaskList(updatedBoard)
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "업데이트 실패")
            }
        }
    }

    /** 라벨 색상 값 불러오기 */
    fun getColorOptions(): List<String> = LabelColorProvider.getAvailableColors()
}