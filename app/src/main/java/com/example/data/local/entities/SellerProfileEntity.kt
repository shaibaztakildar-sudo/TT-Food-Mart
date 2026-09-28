package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sellers")
data class SellerProfileEntity(
    @PrimaryKey val sellerId: String, // corresponds to UserEntity id
    val restaurantName: String,
    val ownerName: String,
    val phone: String,
    val email: String,
    val address: String,
    val panNumber: String,
    val bankAccount: String,
    val bankIfsc: String,
    val bankName: String,
    val documentProofUri: String? = null,
    val imageUri: String? = null,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED", "SUSPENDED"
    val rating: Float = 0f,
    val totalOrders: Int = 0,
    val earnings: Double = 0.0,
    val pendingPayout: Double = 0.0,
    val paidPayout: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
