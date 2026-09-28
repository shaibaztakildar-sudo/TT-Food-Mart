package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "riders")
data class RiderProfileEntity(
    @PrimaryKey val riderId: String, // corresponds to UserEntity id
    val name: String,
    val phone: String,
    val email: String,
    val vehicleNumber: String,
    val licenseUri: String? = null,
    val status: String = "PENDING", // "PENDING", "APPROVED", "ACTIVE", "INACTIVE", "REJECTED"
    val totalDeliveries: Int = 0,
    val earnings: Double = 0.0,
    val pendingPayout: Double = 0.0,
    val paidPayout: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
