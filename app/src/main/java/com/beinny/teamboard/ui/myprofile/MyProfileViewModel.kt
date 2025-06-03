package com.beinny.teamboard.ui.myprofile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.beinny.teamboard.data.model.User
import com.beinny.teamboard.data.repository.BoardRepository
import com.beinny.teamboard.ui.state.UiState
import com.beinny.teamboard.utils.Constants
import kotlinx.coroutines.launch

class MyProfileViewModel(
    private val boardRepository: BoardRepository
) : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> get() = _uiState

    fun loadUser() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                _user.value = boardRepository.loadUserData()
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("사용자 정보 불러오기 실패")
                Log.e("MyProfileViewModel", "loadUser 실패", e)
            }
        }
    }

    fun updateUserData(
        name: String,
        mobile: String,
        imageUri: Uri?, // 갤러리에서 선택한 URI
        fileName: String, // 갤러리에서 선택한 URI를 저장할 파일 이름
        onComplete: () -> Unit
    ) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val currentUser = user.value ?: return@launch
                val userHashMap = HashMap<String, Any>()

                // 변경 사항 체크 후 변경이 있으면 해시 맵에 추가
                if (name != currentUser.name) {
                    userHashMap[Constants.NAME] = name
                }

                if (mobile != currentUser.mobile.toString()) {
                    userHashMap[Constants.MOBILE] = mobile.toLong()
                }

                if (imageUri != null && fileName.isNotBlank()) {
                    val imageUrl = boardRepository.uploadImage(imageUri, fileName)
                    if (imageUrl.isNotBlank()) {
                        userHashMap[Constants.IMAGE] = imageUrl
                    }
                }

                if (userHashMap.isNotEmpty()) {
                    boardRepository.updateUserData(userHashMap)
                }

                _uiState.value = UiState.Success
                onComplete()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("프로필 업데이트 실패")
                Log.e("MyProfileViewModel", "updateUserProfile 실패", e)
            }
        }
    }
}
