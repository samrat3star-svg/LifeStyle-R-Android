package com.lifestyler.android.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("regCode") val regCode: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("action") val action: String = "login"
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("sheetName") val sheetName: String?,
    @SerializedName("userName") val userName: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("token") val token: String?
)

data class FastingSettingsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("fastingHours") val fastingHours: String?, 
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("userName") val userName: String?,
    @SerializedName("isLoggedToday") val isLoggedToday: Boolean,
    @SerializedName("message") val message: String?
)

data class LogFastingRequest(
    @SerializedName("sheetName") val sheetName: String,
    @SerializedName("duration") val duration: String, // e.g., "16 hours"
    @SerializedName("action") val action: String = "logFasting"
)

data class LogFastingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)

data class MeasurementsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("userName") val userName: String?,
    @SerializedName("joiningStatus") val joiningStatus: JoiningStatus?,
    @SerializedName("history") val history: List<MeasurementEntry>?,
    @SerializedName("message") val message: String?
)

data class JoiningStatus(
    @SerializedName("weight") val weight: String?,
    @SerializedName("height") val height: String?,
    @SerializedName("chest") val chest: String?,
    @SerializedName("tummy") val tummy: String?,
    @SerializedName("hips") val hips: String?,
    @SerializedName("targetWeight") val targetWeight: String?
)

data class MeasurementEntry(
    @SerializedName("date") val date: String?,
    @SerializedName("weight") val weight: String?,
    @SerializedName("chest") val chest: String?,
    @SerializedName("waist") val waist: String?,
    @SerializedName("hips") val hips: String?
)

data class BreaksResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("breaks") val breaks: List<BreakEntry>?,
    @SerializedName("message") val message: String?
)

data class BreakEntry(
    @SerializedName("date") val date: String?,
    @SerializedName("reason") val reason: String?
)
