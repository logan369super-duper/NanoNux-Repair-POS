package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sku: String,
    val name: String,
    val brand: String,
    val model: String,
    val type: String = "PART", // e.g. "PART", "ACCESSORY", "HARDWARE"
    val unitPrice: Double,
    val costPrice: Double,
    val stockQuantity: Double = 0.0,
    val minStockThreshold: Double = 5.0,
    val categoryId: Long? = null,
    val unitCode: String = "pcs",
    val createdAt: Long = System.currentTimeMillis()
)
