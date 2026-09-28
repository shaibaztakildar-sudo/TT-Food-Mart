package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val code: String, // e.g. "TTFIRST", "AJARA50"
    val title: String,
    val discountPercent: Int,
    val maxDiscountAmount: Double,
    val minOrderAmount: Double,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
