package com.lifestyler.android.presentation.clients.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.domain.usecase.CreateClientUseCase
import com.lifestyler.android.domain.usecase.DeleteClientUseCase
import com.lifestyler.android.domain.usecase.GetAllClientsUseCase
import com.lifestyler.android.domain.usecase.SearchClientsUseCase
import com.lifestyler.android.domain.usecase.UpdateClientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val getAllClientsUseCase: GetAllClientsUseCase,
    private val createClientUseCase: CreateClientUseCase,
    private val updateClientUseCase: UpdateClientUseCase,
    private val deleteClientUseCase: DeleteClientUseCase,
    private val searchClientsUseCase: SearchClientsUseCase
) : ViewModel() {
    
    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<Client>>(emptyList())
    val searchResults: StateFlow<List<Client>> = _searchResults.asStateFlow()
    
    init {
        loadClients()
    }
    
    fun loadClients() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            runCatching {
                getAllClientsUseCase()
            }.onSuccess { clientList ->
                _clients.value = clientList
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load clients"
            }
            
            _isLoading.value = false
        }
    }
    
    fun createClient(client: Client) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            createClientUseCase(client).onSuccess { clientId ->
                // Reload clients to show the new client
                loadClients()
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to create client"
            }
            
            _isLoading.value = false
        }
    }
    
    fun updateClient(clientId: String, client: Client) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            updateClientUseCase(clientId, client).onSuccess { success ->
                if (success) {
                    // Reload clients to show the updated client
                    loadClients()
                } else {
                    _error.value = "Failed to update client"
                }
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to update client"
            }
            
            _isLoading.value = false
        }
    }
    
    fun deleteClient(clientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            deleteClientUseCase(clientId).onSuccess { success ->
                if (success) {
                    // Reload clients to show the updated list
                    loadClients()
                } else {
                    _error.value = "Failed to delete client"
                }
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to delete client"
            }
            
            _isLoading.value = false
        }
    }
    
    fun searchClientsByName(name: String) {
        if (name.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            searchClientsUseCase.searchByName(name).onSuccess { results ->
                _searchResults.value = results
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to search clients"
                _searchResults.value = emptyList()
            }
            
            _isLoading.value = false
        }
    }
    
    fun searchClientsByPhone(phone: String) {
        if (phone.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            searchClientsUseCase.searchByPhone(phone).onSuccess { results ->
                _searchResults.value = results
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to search clients"
                _searchResults.value = emptyList()
            }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
} 