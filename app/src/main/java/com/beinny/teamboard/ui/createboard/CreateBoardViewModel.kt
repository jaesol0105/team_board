package com.beinny.teamboard.ui.createboard

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beinny.teamboard.data.repository.BoardRepository
import com.beinny.teamboard.ui.state.UiState
import kotlinx.coroutines.launch

class CreateBoardViewModel(
    private val boardRepository: BoardRepository
) : ViewModel() {
    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    /** 새 보드 생성 */
    fun createBoard(
        boardName: String,
        boardImageUri: Uri?,
        userName: String,
        fileName: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                boardRepository.createBoard(boardName, boardImageUri, userName, fileName)
                _uiState.value = UiState.Success
                onComplete() // 콜백
            } catch (e: Exception) {
                _uiState.value = UiState.Error("보드 생성 실패: ${e.message}")
                Log.e("CreateBoardViewModel", "보드 생성 실패", e)
            }
        }
    }
}