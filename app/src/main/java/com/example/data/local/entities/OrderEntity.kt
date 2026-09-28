package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val sellerId: String,
    val restaurantName: String,
    val restaurantAddress: String,
    val riderId: String? = null,
    val riderName: String? = null,
    val riderPhone: String? = null,
    val deliveryAddress: String,
    val customerInstructions: String = "",
    val subtotal: Double,
    val deliveryCharge: Double,
    val discount: Double = 0.0,
    val totalAmount: Double,
    val couponCode: String? = null,
    val paymentMethod: String, // "ONLINE", "COD"
    val paymentStatus: String, // "PAID", "PENDING", "FAILED"
    val paymentTransactionId: String? = null,
    val orderStatus: String = "PLACED",
    // Possible statuses: "PLACED", "ACCEPTED", "PREPARING", "READY", "RIDER_ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
    val cancellationReason: String? = null,
    val deliveryOtp: String,
    val isOtpVerified: Boolean = false,
    val rating: Int? = null,
    val reviewText: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
