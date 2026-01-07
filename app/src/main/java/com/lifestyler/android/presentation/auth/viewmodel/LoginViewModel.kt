package com.lifestyler.android.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.lifestyler.android.domain.usecase.LoginUseCase
import com.lifestyler.android.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val sheetName: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loginUser(regCode: String, mobile: String) {
        if (regCode.isBlank() || mobile.isBlank()) {
            _state.value = LoginState.Error("Registration Code and Mobile Number cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _state.value = LoginState.Loading
            
            loginUseCase(regCode, mobile).onSuccess { result ->
                if (result.success && result.sheetName != null) {
                    _state.value = LoginState.Success(result.sheetName)
                } else {
                    _state.value = LoginState.Error(result.message ?: "Login failed")
                }
            }.onFailure { exception ->
                _state.value = LoginState.Error(exception.message ?: "Login failed")
            }
            
            _isLoading.value = false
        }
    }
    
    fun register(email: String, password: String, firstName: String, lastName: String) {
        // Keeping register method but it might need similar state update if used by UI
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
           // _authState.value = AuthResult.Error("All fields are required")
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            // _authState.value = null
            
            registerUseCase(email, password, firstName, lastName).onSuccess { result ->
                // _authState.value = result
            }.onFailure { exception ->
                // _authState.value = AuthResult.Error(exception.message ?: "Registration failed")
            }
            
            _isLoading.value = false
        }
    }
    
    fun clearAuthState() {
        _state.value = LoginState.Idle
    }
} 