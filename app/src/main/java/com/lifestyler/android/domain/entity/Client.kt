package com.lifestyler.android.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Client(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val age: Int,
    val gender: String,
    val weight: Double,
    val height: Double,
    val goal: String,
    val medicalConditions: String,
    val medications: String,
    val allergies: String,
    val emergencyContact: String,
    val emergencyPhone: String,
    val status: String,
    val registrationDate: String,
    val notes: String
) : Parcelable 