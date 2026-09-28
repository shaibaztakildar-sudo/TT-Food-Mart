package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val title: String = "Home", // "Home", "Work", "Other"
    val receiverName: String,
    val receiverPhone: String,
    val streetAddress: String,
    val landmark: String = "",
    val area: String,
    val city: String = "Ajara",
    val state: String = "Maharashtra",
    val pincode: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
