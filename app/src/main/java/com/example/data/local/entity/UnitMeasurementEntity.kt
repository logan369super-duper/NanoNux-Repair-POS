package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unit_measurements")
data class UnitMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "Piece", "Kilogram", "Device", "Meter", "Box"
    val code: String, // e.g. "pcs", "kg", "device", "m", "box"
    val allowDecimal: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
