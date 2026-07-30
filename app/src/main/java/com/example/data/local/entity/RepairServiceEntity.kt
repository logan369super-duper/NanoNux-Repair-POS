package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repair_services")
data class RepairServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceName: String,
    val category: String, // e.g. "Screen", "Battery", "Motherboard", "Software"
    val baseLaborFee: Double,
    val estimatedTimeMinutes: Int = 45,
    val isActive: Boolean = true
)
