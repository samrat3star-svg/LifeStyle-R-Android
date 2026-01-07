package com.lifestyler.android.domain.entity

data class User(
    val userId: String = "",
    val registrationCode: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val status: String = "active", // active, inactive, pending
    
    // Authentication System
    val password: String = "", // Initial password = mobile number
    val isFirstLogin: Boolean = true, // Flag for first login
    val passwordChanged: Boolean = false, // Track if password was changed
    val passwordChangeRequired: Boolean = true, // Force password change on first login
    
    // Timestamps
    val dateCreated: String = "",
    val lastLogin: String = "",
    val passwordChangedAt: String? = null,
    
    val profile: UserProfile = UserProfile(),
    val settings: UserSettings = UserSettings()
)

data class UserProfile(
    val age: Int = 0,
    val gender: String = "",
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val goal: String = "",
    val medicalConditions: String = "",
    val medications: String = "",
    val allergies: String = "",
    val emergencyContact: String = "",
    val emergencyPhone: String = ""
)

data class UserSettings(
    val notifications: Boolean = true,
    val whatsappAlerts: Boolean = true,
    val timezone: String = "Asia/Kolkata"
) 