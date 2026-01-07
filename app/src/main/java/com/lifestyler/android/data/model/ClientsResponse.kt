package com.lifestyler.android.data.model

import com.google.gson.annotations.SerializedName
import com.lifestyler.android.domain.entity.Client

data class ClientsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<Client>,
    @SerializedName("message")
    val message: String
) 