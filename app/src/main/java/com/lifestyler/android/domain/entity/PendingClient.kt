package com.lifestyler.android.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PendingClient(
    val name: String,
    val status: String
) : Parcelable