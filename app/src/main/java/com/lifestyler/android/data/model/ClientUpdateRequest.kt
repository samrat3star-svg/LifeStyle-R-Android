package com.lifestyler.android.data.model

data class ClientUpdateRequest(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val action: String = "updateClient"
)