package com.example.domain.model

enum class UserRole {
    ADMIN,
    STAFF
}

enum class RepairTicketStatus(val displayName: String) {
    RECEIVED("Received"),
    DIAGNOSING("Diagnosing"),
    IN_PROGRESS("In Progress"),
    READY("Ready for Pickup"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class PrinterConnectionType {
    NONE,
    BLUETOOTH,
    WIFI
}

data class CartItem(
    val id: String, // product_id or service_id
    val name: String,
    val type: CartItemType,
    val unitPrice: Double,
    val costPrice: Double,
    var quantity: Int,
    val referenceId: Long? = null
)

enum class CartItemType {
    PRODUCT_PART,
    LABOR_SERVICE
}

data class FinancialReportSummary(
    val totalRevenue: Double,
    val totalCostOfGoods: Double,
    val totalLaborRevenue: Double,
    val grossProfit: Double,
    val netProfit: Double,
    val totalSalesCount: Int,
    val totalRepairsCompleted: Int
)
