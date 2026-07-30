package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.RepairTicketStatus

@Entity(tableName = "service_orders")
data class ServiceOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketNumber: String,
    val customerName: String,
    val customerPhone: String,
    val deviceBrand: String,
    val deviceModel: String,
    val serialImei: String,
    val issueType: String,
    val issueDescription: String,
    val estimatedCost: Double,
    val laborFee: Double,
    val allocatedPartsJson: String = "[]", // Serialized JSON list of allocated part IDs and costs
    val status: RepairTicketStatus = RepairTicketStatus.RECEIVED,
    val depositPaid: Double = 0.0,
    val assignedStaffId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
