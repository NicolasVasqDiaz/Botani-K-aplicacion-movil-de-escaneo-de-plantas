package com.botanik.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_users")
data class LocalUserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long
)
