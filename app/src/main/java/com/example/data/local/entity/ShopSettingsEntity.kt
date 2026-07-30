package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.PrinterConnectionType

@Entity(tableName = "shop_settings")
data class ShopSettingsEntity(
    @PrimaryKey val id: Int = 1, // Single row config
    val shopName: String,
    val address: String,
    val phone: String,
    val logoUri: String? = null,
    val isTaxEnabled: Boolean = true,
    val defaultTaxRatePercent: Double = 0.0,
    val receiptFooterNote: String = "Thank you for choosing our repair service! All labor comes with a 90-day warranty.",
    val printerConnection: PrinterConnectionType = PrinterConnectionType.NONE,
    val printerAddress: String = "",
    val printerPort: Int = 9100,
    val paperSizeMm: Int = 58, // 58 or 80
    val printerFontSize: Int = 18, // Font size in SP before image conversion
    val showTaxInPrintedInvoice: Boolean = true, // Switch to show/hide tax line in printed invoice
    val showLogoInPrintedInvoice: Boolean = true, // Switch to show/hide logo header in printed invoice
    val isDarkMode: Boolean = false,
    val language: String = "en", // "en", "my", "th"
    val isConfigured: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

