package com.botanik.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocalUserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: LocalUserEntity)

    @Query("SELECT * FROM local_users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): LocalUserEntity?

    @Query("SELECT * FROM local_users WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): LocalUserEntity?

    @Query("SELECT COUNT(*) FROM local_users WHERE email = :email")
    suspend fun countByEmail(email: String): Int
}
