package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val targetUserId: String, // Specific user ID, or "ADMIN", "SELLER_<id>", "RIDER_<id>", "ALL"
    val title: String,
    val message: String,
    val orderId: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
