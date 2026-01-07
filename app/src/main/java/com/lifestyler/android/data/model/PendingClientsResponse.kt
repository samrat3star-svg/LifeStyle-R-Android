package com.lifestyler.android.data.model

import com.google.gson.annotations.SerializedName
import com.lifestyler.android.domain.entity.PendingClient

data class PendingClientsResponse(
    @SerializedName("data")
    val data: List<PendingClient>
) 