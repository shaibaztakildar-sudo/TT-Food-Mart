package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val sellerId: String,
    val restaurantName: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String, // e.g., "Biryani", "Thali", "Snacks", "Beverages", "Dessert", "Curry"
    val isVeg: Boolean,
    val isAvailable: Boolean = true,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
