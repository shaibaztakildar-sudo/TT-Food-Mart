package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: String, // "CUSTOMER", "SELLER", "RIDER", "ADMIN"
    val profilePhotoUri: String? = null,
    val status: String = "ACTIVE", // "ACTIVE", "PENDING", "REJECTED", "BLOCKED"
    val createdAt: Long = System.currentTimeMillis()
)
