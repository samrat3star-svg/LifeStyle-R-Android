package com.lifestyler.android.presentation.main.clients.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.domain.usecase.GetAllClientsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val getAllClientsUseCase: GetAllClientsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ClientsUiState>(ClientsUiState.Loading)
    val uiState: StateFlow<ClientsUiState> = _uiState.asStateFlow()
    
    fun loadClients() {
        viewModelScope.launch {
            _uiState.value = ClientsUiState.Loading
            try {
                val clients = getAllClientsUseCase()
                _uiState.value = ClientsUiState.Success(clients)
            } catch (e: Exception) {
                _uiState.value = ClientsUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

sealed class ClientsUiState {
    object Loading : ClientsUiState()
    data class Success(val clients: List<Client>) : ClientsUiState()
    data class Error(val message: String) : ClientsUiState()
} 