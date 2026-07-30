package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.UserEntity
import com.example.data.repository.PosRepository
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val pinInput: String = "",
    val currentUser: UserEntity? = null,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val availableUsers: List<UserEntity> = emptyList(),
    val hasAdminUser: Boolean = false,
    val isLoading: Boolean = true
)

class AuthViewModel(private val repository: PosRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            repository.getAllActiveUsers().collect { users ->
                val adminCount = try { repository.getAdminUserCount() } catch (e: Exception) { 0 }
                val hasAdmin = adminCount > 0 || users.any { it.role == UserRole.ADMIN }
                _uiState.value = _uiState.value.copy(
                    availableUsers = users,
                    hasAdminUser = hasAdmin,
                    isLoading = false
                )
            }
        }
    }

    fun onPinChange(pin: String) {
        if (pin.length <= 6) {
            _uiState.value = _uiState.value.copy(pinInput = pin, errorMessage = null)
            if (pin.length == 6) {
                authenticatePin(pin)
            }
        }
    }

    fun authenticatePin(pin: String) {
        viewModelScope.launch {
            val user = repository.getUserByPin(pin)
            if (user != null) {
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isAuthenticated = true,
                    pinInput = "",
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    pinInput = "",
                    errorMessage = "Invalid PIN code. Please try again."
                )
            }
        }
    }

    fun setAuthenticatedUser(user: UserEntity) {
        val currentUsers = _uiState.value.availableUsers.toMutableList()
        if (currentUsers.none { it.id == user.id || (it.name == user.name && it.pinCode == user.pinCode) }) {
            currentUsers.add(user)
        }
        val hasAdmin = currentUsers.any { it.role == UserRole.ADMIN } || user.role == UserRole.ADMIN
        _uiState.value = _uiState.value.copy(
            currentUser = user,
            isAuthenticated = true,
            availableUsers = currentUsers,
            hasAdminUser = hasAdmin,
            pinInput = "",
            errorMessage = null,
            isLoading = false
        )
    }

    fun logout() {
        _uiState.value = _uiState.value.copy(
            currentUser = null,
            isAuthenticated = false,
            pinInput = "",
            errorMessage = null
        )
    }

    fun resetAuthState() {
        _uiState.value = AuthUiState(
            pinInput = "",
            currentUser = null,
            isAuthenticated = false,
            errorMessage = null,
            availableUsers = emptyList(),
            hasAdminUser = false,
            isLoading = false
        )
        loadUsers()
    }

    fun quickLoginAs(user: UserEntity) {
        authenticatePin(user.pinCode)
    }

    fun updateCurrentUserPin(newPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = _uiState.value.currentUser ?: return
        if (newPin.length != 6 || !newPin.all { it.isDigit() }) {
            onError("PIN must be exactly 6 digits.")
            return
        }
        viewModelScope.launch {
            val existingUser = repository.getUserByPin(newPin)
            if (existingUser != null && existingUser.id != currentUser.id) {
                onError("This PIN code is already in use by another user.")
                return@launch
            }
            
            val updatedUser = currentUser.copy(pinCode = newPin)
            repository.updateUser(updatedUser)
            _uiState.value = _uiState.value.copy(
                currentUser = updatedUser,
                availableUsers = _uiState.value.availableUsers.map {
                    if (it.id == updatedUser.id) updatedUser else it
                }
            )
            onSuccess()
        }
    }
}
