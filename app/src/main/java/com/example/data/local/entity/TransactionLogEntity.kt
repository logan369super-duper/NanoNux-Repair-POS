package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_logs")
data class TransactionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionNumber: String,
    val type: String, // e.g. "POS_SALE", "REPAIR_COMPLETED", "STOCK_IN"
    val customerName: String? = null,
    val customerPhone: String? = null,
    val deviceBrand: String? = null,
    val issueType: String? = null,
    val itemsJson: String = "[]",
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val totalAmount: Double,
    val cogs: Double, // Cost of goods sold
    val paymentMethod: String, // e.g. "CASH", "CARD", "DIGITAL_WALLET"
    val cashierName: String,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
