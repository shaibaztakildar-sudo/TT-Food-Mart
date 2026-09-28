package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payouts")
data class PayoutEntity(
    @PrimaryKey val id: String,
    val recipientId: String,
    val recipientType: String, // "SELLER", "RIDER"
    val recipientName: String,
    val amount: Double,
    val referenceId: String,
    val status: String = "COMPLETED",
    val paymentMode: String = "BANK_TRANSFER", // or UPI
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
