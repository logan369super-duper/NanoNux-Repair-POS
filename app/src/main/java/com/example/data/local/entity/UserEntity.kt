package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.UserRole

@Entity(
    tableName = "users",
    indices = [Index(value = ["pinCode"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: UserRole,
    val pinCode: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
