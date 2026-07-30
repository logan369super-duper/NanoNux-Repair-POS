package com.example.data.repository

import com.example.data.local.entity.*
import com.example.domain.model.CartItem
import com.example.domain.model.FinancialReportSummary
import com.example.domain.model.RepairTicketStatus
import kotlinx.coroutines.flow.Flow

interface PosRepository {
    // Users & Auth
    fun getAllActiveUsers(): Flow<List<UserEntity>>
    suspend fun getUserByPin(pin: String): UserEntity?
    suspend fun insertUser(user: UserEntity): Long
    suspend fun updateUser(user: UserEntity)
    suspend fun getAdminUserCount(): Int
    fun getAdminUserCountFlow(): Flow<Int>

    // Products & Inventory
    fun getAllProducts(): Flow<List<ProductEntity>>
    fun getLowStockProducts(): Flow<List<ProductEntity>>
    fun searchProducts(query: String): Flow<List<ProductEntity>>
    suspend fun insertProduct(product: ProductEntity): Long
    suspend fun updateProduct(product: ProductEntity)
    suspend fun updateStock(productId: Long, quantityDelta: Double)
    suspend fun deleteProduct(product: ProductEntity)

    // Catalog Categories (Unlimited Parent-Child)
    fun getAllCategories(): Flow<List<CatalogCategoryEntity>>
    suspend fun insertCategory(category: CatalogCategoryEntity): Long
    suspend fun updateCategory(category: CatalogCategoryEntity)
    suspend fun deleteCategory(category: CatalogCategoryEntity)

    // Custom Unit Measurements
    fun getAllUnits(): Flow<List<UnitMeasurementEntity>>
    suspend fun insertUnit(unit: UnitMeasurementEntity): Long
    suspend fun updateUnit(unit: UnitMeasurementEntity)
    suspend fun deleteUnit(unit: UnitMeasurementEntity)

    // Service Orders (Repair Tickets)
    fun getAllServiceOrders(): Flow<List<ServiceOrderEntity>>
    fun getActiveServiceOrders(): Flow<List<ServiceOrderEntity>>
    fun searchServiceOrders(query: String): Flow<List<ServiceOrderEntity>>
    suspend fun getServiceOrderById(id: Long): ServiceOrderEntity?
    suspend fun createServiceOrder(order: ServiceOrderEntity): Long
    suspend fun updateServiceOrder(order: ServiceOrderEntity)
    suspend fun updateOrderStatus(orderId: Long, status: RepairTicketStatus)
    fun getOpenTicketCount(): Flow<Int>

    // POS & Checkout
    suspend fun processPosSale(
        items: List<CartItem>,
        discount: Double,
        taxPercent: Double,
        paymentMethod: String,
        cashierName: String,
        customerName: String?,
        customerPhone: String?,
        notes: String?
    ): TransactionLogEntity

    // Transaction Logs & Audit
    fun getAllTransactions(): Flow<List<TransactionLogEntity>>
    fun filterTransactions(brand: String?, customer: String?, issue: String?): Flow<List<TransactionLogEntity>>

    // Services & Pricing
    fun getAllActiveServices(): Flow<List<RepairServiceEntity>>
    suspend fun insertService(service: RepairServiceEntity): Long
    suspend fun updateService(service: RepairServiceEntity)
    suspend fun deleteService(service: RepairServiceEntity)

    // Promotions
    fun getAllPromotions(): Flow<List<PromotionEntity>>
    fun getAllActivePromotions(): Flow<List<PromotionEntity>>
    suspend fun getPromotionByCode(code: String): PromotionEntity?
    suspend fun insertPromotion(promotion: PromotionEntity): Long
    suspend fun updatePromotion(promotion: PromotionEntity)
    suspend fun deletePromotion(promotion: PromotionEntity)

    // Shop Settings
    fun getShopSettingsFlow(): Flow<ShopSettingsEntity?>
    suspend fun getShopSettings(): ShopSettingsEntity?
    suspend fun saveShopSettings(settings: ShopSettingsEntity)

    // Repair Item Brands & Fault Types
    fun getAllRepairItemBrands(): Flow<List<RepairItemBrandEntity>>
    suspend fun insertRepairItemBrand(brand: RepairItemBrandEntity): Long
    suspend fun updateRepairItemBrand(brand: RepairItemBrandEntity)
    suspend fun deleteRepairItemBrand(brand: RepairItemBrandEntity)

    fun getAllRepairIssueTypes(): Flow<List<RepairIssueTypeEntity>>
    suspend fun insertRepairIssueType(issueType: RepairIssueTypeEntity): Long
    suspend fun updateRepairIssueType(issueType: RepairIssueTypeEntity)
    suspend fun deleteRepairIssueType(issueType: RepairIssueTypeEntity)

    // Data Management
    suspend fun clearAllData()

    // Financial Reports
    fun getFinancialReportSummary(): Flow<FinancialReportSummary>

    // Customers
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    suspend fun getCustomerById(id: Long): CustomerEntity?
    suspend fun insertCustomer(customer: CustomerEntity): Long
    suspend fun updateCustomer(customer: CustomerEntity)
    suspend fun deleteCustomer(customer: CustomerEntity)
    suspend fun recordOrUpdateCustomer(name: String, phone: String?, amountSpent: Double = 0.0)
}
