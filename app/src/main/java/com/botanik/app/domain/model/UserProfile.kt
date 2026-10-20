package com.botanik.app.domain.model

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?,
    val ageRange: String? = null,
    val userType: String? = null,
    val notificationsEnabled: Boolean = true,
    val createdAt: Long
)
