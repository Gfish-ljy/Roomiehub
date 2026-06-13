package com.dorm.health.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dorm.health.DormHealthApp
import com.dorm.health.data.model.MockUserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val demoAccounts: List<MockUserAccount> = emptyList()
)

class LoginViewModel : ViewModel() {
    private val authRepo = DormHealthApp.instance.authRepository

    private val _uiState = MutableStateFlow(
        LoginUiState(demoAccounts = authRepo.demoAccounts)
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessage = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = authRepo.login(state.username, state.password)
            _uiState.value = _uiState.value.copy(isLoading = false)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { _uiState.value = _uiState.value.copy(errorMessage = it.message) }
            )
        }
    }

    fun loginWithDemo(account: MockUserAccount, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                username = account.username,
                password = account.password,
                isLoading = true,
                errorMessage = null
            )
            authRepo.loginWithDemoAccount(account)
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess()
        }
    }
}
