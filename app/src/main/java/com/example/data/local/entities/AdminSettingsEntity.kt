package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_settings")
data class AdminSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val deliveryChargeType: String = "FIXED", // "FIXED" or "DISTANCE"
    val fixedDeliveryFee: Double = 30.0,
    val baseDistanceKm: Double = 3.0,
    val baseDistanceFee: Double = 25.0,
    val perKmFee: Double = 10.0,
    val freeDeliveryThreshold: Double = 499.0,
    val isCodEnabled: Boolean = true,
    val isOnlinePaymentEnabled: Boolean = true,
    val platformCommissionPercent: Double = 10.0,
    val serviceAreaName: String = "Ajara, Maharashtra",
    val servicePincodes: String = "416505,416506,416502", // comma-separated
    val isSmsGatewayConfigured: Boolean = true,
    val isPaymentGatewayConfigured: Boolean = true
)
