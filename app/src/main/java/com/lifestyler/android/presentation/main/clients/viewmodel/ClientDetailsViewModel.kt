package com.lifestyler.android.presentation.main.clients.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.domain.usecase.UpdateClientUseCase
import com.lifestyler.android.data.model.ClientUpdateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientDetailsViewModel @Inject constructor(
    private val updateClientUseCase: UpdateClientUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ClientDetailsUiState>(ClientDetailsUiState.Loading)
    val uiState: StateFlow<ClientDetailsUiState> = _uiState.asStateFlow()

    fun loadClientDetails(client: Client) {
        _uiState.value = ClientDetailsUiState.Success(client)
    }

    fun updateClient(clientId: String, request: ClientUpdateRequest) {
        viewModelScope.launch {
            _uiState.value = ClientDetailsUiState.Saving
            
            // Construct a dummy Client with updated fields
            // Repository only uses name, email, phone from this object for updateClient
            val client = Client(
                id = clientId,
                name = request.name,
                email = request.email,
                phone = request.phone,
                age = 0, gender = "", weight = 0.0, height = 0.0, goal = "", 
                medicalConditions = "", medications = "", allergies = "", 
                emergencyContact = "", emergencyPhone = "", status = "", 
                registrationDate = "", notes = ""
            )
            
            val result = updateClientUseCase(clientId, client)
            result.fold(
                onSuccess = {
                    _uiState.value = ClientDetailsUiState.Saved
                },
                onFailure = { err ->
                    _uiState.value = ClientDetailsUiState.Error(err.message ?: "Unknown error")
                }
            )
        }
    }
}

sealed class ClientDetailsUiState {
    object Loading : ClientDetailsUiState()
    data class Success(val client: Client) : ClientDetailsUiState()
    data class Error(val message: String) : ClientDetailsUiState()
    object Saving : ClientDetailsUiState()
    object Saved : ClientDetailsUiState()
} 