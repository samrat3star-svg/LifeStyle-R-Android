package com.lifestyler.android.presentation.main.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifestyler.android.domain.entity.PendingClient
import com.lifestyler.android.domain.usecase.GetPendingClientsUseCase
import com.lifestyler.android.presentation.main.home.HomeContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPendingClientsUseCase: GetPendingClientsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeContract.State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeContract.Effect>()
    val effect = _effect.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("HomeViewModel", "Caught Exception:", throwable)
        _state.update { it.copy(isLoading = false, error = throwable.message) }
        viewModelScope.launch {
            _effect.emit(HomeContract.Effect.ShowErrorToast)
        }
    }

    init {
        Log.d("HomeViewModel", "ViewModel Initialized")
        onEvent(HomeContract.Event.OnFetchPendingClients)
    }

    fun onEvent(event: HomeContract.Event) {
        when (event) {
            is HomeContract.Event.OnFetchPendingClients -> fetchPendingClients()
        }
    }

    private fun fetchPendingClients() {
        Log.d("HomeViewModel", "fetchPendingClients() called")
        viewModelScope.launch(exceptionHandler) {
            _state.update { it.copy(isLoading = true, error = null) }
            val clients = getPendingClientsUseCase()
            val pendingClients = clients.map { 
                PendingClient(name = it.name, status = it.status)
            }
            _state.update { it.copy(isLoading = false, clients = pendingClients) }
        }
    }
} 