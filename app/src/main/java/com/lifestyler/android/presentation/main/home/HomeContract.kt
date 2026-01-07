package com.lifestyler.android.presentation.main.home

import com.lifestyler.android.domain.entity.PendingClient

class HomeContract {
    data class State(
        val isLoading: Boolean = false,
        val clients: List<PendingClient> = emptyList(),
        val error: String? = null
    )

    sealed class Event {
        object OnFetchPendingClients : Event()
    }

    sealed class Effect {
        object ShowErrorToast : Effect()
    }
} 