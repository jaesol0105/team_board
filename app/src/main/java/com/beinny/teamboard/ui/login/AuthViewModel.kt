package com.beinny.teamboard.ui.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beinny.teamboard.data.repository.BoardRepository
import com.beinny.teamboard.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel (
    private val repository: BoardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val success = repository.signIn(email, password)
                _uiState.value = if (success) UiState.Success else UiState.Error("로그인에 실패했습니다.")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류 발생")
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val success = repository.signUp(email, password, name)
                _uiState.value = if (success) UiState.Success else UiState.Error("회원가입에 실패했습니다.")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류 발생")
            }
        }
    }

    suspend fun signOut() {
        repository.signOut()
    }

    fun signInWithKakaoSso(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val ok = repository.signInWithKakaoSso(activity)
                _uiState.value = if (ok) UiState.Success else UiState.Error("카카오 로그인 실패")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류 발생")
            }
        }
    }
}