package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val title: String,
    val description: String,
    val discountType: String = "PERCENTAGE", // "PERCENTAGE" or "FIXED"
    val discountValue: Double = 0.0, // Value for % or fixed amount
    val discountPercent: Double = 0.0, // Kept for backwards compatibility
    val fixedDiscountAmount: Double = 0.0, // Kept for backwards compatibility
    val appliesToSpecificItems: Boolean = false, // Checkbox option: applied to specific items only
    val targetItemIds: String = "", // Comma-separated list of item keys e.g. "PROD_1,PROD_2,SRV_5"
    val appliesToScope: String = "BOTH", // "BOTH", "POS", "SERVICE"
    val minOrderAmount: Double = 0.0,
    val isActive: Boolean = true
) {
    /**
     * Helper property returning normalized discount percent or value depending on discountType
     */
    val effectiveDiscountPercent: Double
        get() = if (discountType == "PERCENTAGE") {
            if (discountValue > 0) discountValue else discountPercent
        } else 0.0

    val effectiveFixedDiscount: Double
        get() = if (discountType == "FIXED") {
            if (discountValue > 0) discountValue else fixedDiscountAmount
        } else 0.0
}

