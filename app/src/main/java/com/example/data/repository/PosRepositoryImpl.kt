package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.domain.model.CartItem
import com.example.domain.model.CartItemType
import com.example.domain.model.FinancialReportSummary
import com.example.domain.model.RepairTicketStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class PosRepositoryImpl(private val database: AppDatabase) : PosRepository {

    private val userDao = database.userDao()
    private val productDao = database.productDao()
    private val serviceOrderDao = database.serviceOrderDao()
    private val transactionLogDao = database.transactionLogDao()
    private val shopSettingsDao = database.shopSettingsDao()
    private val repairServiceDao = database.repairServiceDao()
    private val promotionDao = database.promotionDao()
    private val repairItemBrandDao = database.repairItemBrandDao()
    private val repairIssueTypeDao = database.repairIssueTypeDao()

    private val catalogCategoryDao = database.catalogCategoryDao()
    private val unitMeasurementDao = database.unitMeasurementDao()
    private val customerDao = database.customerDao()

    override fun getAllActiveUsers(): Flow<List<UserEntity>> = userDao.getAllActiveUsers()

    override suspend fun getUserByPin(pin: String): UserEntity? = userDao.getUserByPin(pin)

    override suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)

    override suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    override suspend fun getAdminUserCount(): Int = userDao.getAdminUserCount()

    override fun getAdminUserCountFlow(): Flow<Int> = userDao.getAdminUserCountFlow()

    override fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    override fun getLowStockProducts(): Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    override fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    override suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)

    override suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    override suspend fun updateStock(productId: Long, quantityDelta: Double) = productDao.updateStock(productId, quantityDelta)

    override suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    // Catalog Categories
    override fun getAllCategories(): Flow<List<CatalogCategoryEntity>> = catalogCategoryDao.getAllCategories()

    override suspend fun insertCategory(category: CatalogCategoryEntity): Long = catalogCategoryDao.insertCategory(category)

    override suspend fun updateCategory(category: CatalogCategoryEntity) = catalogCategoryDao.updateCategory(category)

    override suspend fun deleteCategory(category: CatalogCategoryEntity) = catalogCategoryDao.deleteCategory(category)

    // Custom Units
    override fun getAllUnits(): Flow<List<UnitMeasurementEntity>> = unitMeasurementDao.getAllUnits()

    override suspend fun insertUnit(unit: UnitMeasurementEntity): Long = unitMeasurementDao.insertUnit(unit)

    override suspend fun updateUnit(unit: UnitMeasurementEntity) = unitMeasurementDao.updateUnit(unit)

    override suspend fun deleteUnit(unit: UnitMeasurementEntity) = unitMeasurementDao.deleteUnit(unit)

    override fun getAllServiceOrders(): Flow<List<ServiceOrderEntity>> = serviceOrderDao.getAllServiceOrders()

    override fun getActiveServiceOrders(): Flow<List<ServiceOrderEntity>> = serviceOrderDao.getActiveServiceOrders()

    override fun searchServiceOrders(query: String): Flow<List<ServiceOrderEntity>> = serviceOrderDao.searchServiceOrders(query)

    override suspend fun getServiceOrderById(id: Long): ServiceOrderEntity? = serviceOrderDao.getServiceOrderById(id)

    override suspend fun createServiceOrder(order: ServiceOrderEntity): Long = serviceOrderDao.insertServiceOrder(order)

    override suspend fun updateServiceOrder(order: ServiceOrderEntity) = serviceOrderDao.updateServiceOrder(order)

    override suspend fun updateOrderStatus(orderId: Long, status: RepairTicketStatus) = serviceOrderDao.updateOrderStatus(orderId, status)

    override fun getOpenTicketCount(): Flow<Int> = serviceOrderDao.getOpenTicketCount()

    override suspend fun processPosSale(
        items: List<CartItem>,
        discount: Double,
        taxPercent: Double,
        paymentMethod: String,
        cashierName: String,
        customerName: String?,
        customerPhone: String?,
        notes: String?
    ): TransactionLogEntity {
        var subtotal = 0.0
        var totalCogs = 0.0

        val itemsSummaryList = mutableListOf<String>()

        items.forEach { cartItem ->
            val lineTotal = cartItem.unitPrice * cartItem.quantity
            subtotal += lineTotal
            totalCogs += (cartItem.costPrice * cartItem.quantity)
            itemsSummaryList.add("${cartItem.name} x${cartItem.quantity} (Ks $lineTotal)")

            // Deduct stock if product item
            if (cartItem.type == CartItemType.PRODUCT_PART && cartItem.referenceId != null) {
                productDao.updateStock(cartItem.referenceId, -cartItem.quantity.toDouble())
            }
        }

        val taxableBase = (subtotal - discount).coerceAtLeast(0.0)
        val taxAmount = taxableBase * (taxPercent / 100.0)
        val finalTotal = taxableBase + taxAmount

        val txNumber = "TX-${System.currentTimeMillis().toString().takeLast(8)}"

        val transaction = TransactionLogEntity(
            transactionNumber = txNumber,
            type = "POS_SALE",
            customerName = customerName,
            customerPhone = customerPhone,
            itemsJson = itemsSummaryList.joinToString(", "),
            subtotal = subtotal,
            discount = discount,
            tax = taxAmount,
            totalAmount = finalTotal,
            cogs = totalCogs,
            paymentMethod = paymentMethod,
            cashierName = cashierName,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )

        val id = transactionLogDao.insertTransaction(transaction)
        if (!customerName.isNullOrBlank()) {
            recordOrUpdateCustomer(customerName, customerPhone, finalTotal)
        }
        return transaction.copy(id = id)
    }

    override fun getAllTransactions(): Flow<List<TransactionLogEntity>> = transactionLogDao.getAllTransactions()

    override fun filterTransactions(
        brand: String?,
        customer: String?,
        issue: String?
    ): Flow<List<TransactionLogEntity>> {
        val cleanBrand = if (brand.isNull_or_blank()) null else brand
        val cleanCustomer = if (customer.isNull_or_blank()) null else customer
        val cleanIssue = if (issue.isNull_or_blank()) null else issue
        return transactionLogDao.filterTransactions(cleanBrand, cleanCustomer, cleanIssue)
    }

    private fun String?.isNull_or_blank(): Boolean = this.isNullOrBlank()

    override fun getAllActiveServices(): Flow<List<RepairServiceEntity>> = repairServiceDao.getAllActiveServices()

    override suspend fun insertService(service: RepairServiceEntity): Long = repairServiceDao.insertService(service)

    override suspend fun updateService(service: RepairServiceEntity) = repairServiceDao.updateService(service)

    override suspend fun deleteService(service: RepairServiceEntity) = repairServiceDao.deleteService(service)

    override fun getAllPromotions(): Flow<List<PromotionEntity>> = promotionDao.getAllPromotions()

    override fun getAllActivePromotions(): Flow<List<PromotionEntity>> = promotionDao.getAllActivePromotions()

    override suspend fun getPromotionByCode(code: String): PromotionEntity? = promotionDao.getPromotionByCode(code)

    override suspend fun insertPromotion(promotion: PromotionEntity): Long = promotionDao.insertPromotion(promotion)

    override suspend fun updatePromotion(promotion: PromotionEntity) = promotionDao.updatePromotion(promotion)

    override suspend fun deletePromotion(promotion: PromotionEntity) = promotionDao.deletePromotion(promotion)

    override fun getShopSettingsFlow(): Flow<ShopSettingsEntity?> = shopSettingsDao.getShopSettingsFlow()

    override suspend fun getShopSettings(): ShopSettingsEntity? = shopSettingsDao.getShopSettings()

    override suspend fun saveShopSettings(settings: ShopSettingsEntity) = shopSettingsDao.insertOrUpdateShopSettings(settings)

    override fun getAllRepairItemBrands(): Flow<List<RepairItemBrandEntity>> = repairItemBrandDao.getAllBrands()

    override suspend fun insertRepairItemBrand(brand: RepairItemBrandEntity): Long = repairItemBrandDao.insertBrand(brand)

    override suspend fun updateRepairItemBrand(brand: RepairItemBrandEntity) = repairItemBrandDao.updateBrand(brand)

    override suspend fun deleteRepairItemBrand(brand: RepairItemBrandEntity) = repairItemBrandDao.deleteBrand(brand)

    override fun getAllRepairIssueTypes(): Flow<List<RepairIssueTypeEntity>> = repairIssueTypeDao.getAllIssueTypes()

    override suspend fun insertRepairIssueType(issueType: RepairIssueTypeEntity): Long = repairIssueTypeDao.insertIssueType(issueType)

    override suspend fun updateRepairIssueType(issueType: RepairIssueTypeEntity) = repairIssueTypeDao.updateIssueType(issueType)

    override suspend fun deleteRepairIssueType(issueType: RepairIssueTypeEntity) = repairIssueTypeDao.deleteIssueType(issueType)

    override suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }

    override fun getFinancialReportSummary(): Flow<FinancialReportSummary> {
        return combine(
            transactionLogDao.getTotalRevenue(),
            transactionLogDao.getTotalCOGS(),
            transactionLogDao.getTransactionCount(),
            serviceOrderDao.getAllServiceOrders()
        ) { rev, cogs, count, orders ->
            val totalRev = rev ?: 0.0
            val totalCogsVal = cogs ?: 0.0
            val completedRepairs = orders.count { it.status == RepairTicketStatus.COMPLETED }
            val totalLaborRev = orders.sumOf { it.laborFee }
            val grossProfit = totalRev - totalCogsVal
            val estimatedOperatingExpenses = totalRev * 0.15 // 15% estimated shop overhead
            val netProfit = grossProfit - estimatedOperatingExpenses

            FinancialReportSummary(
                totalRevenue = totalRev,
                totalCostOfGoods = totalCogsVal,
                totalLaborRevenue = totalLaborRev,
                grossProfit = grossProfit,
                netProfit = netProfit,
                totalSalesCount = count ?: 0,
                totalRepairsCompleted = completedRepairs
            )
        }
    }

    override fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    override suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)

    override suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)

    override suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)

    override suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    override suspend fun recordOrUpdateCustomer(name: String, phone: String?, amountSpent: Double) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        val cleanPhone = phone?.trim() ?: ""

        val existing = if (cleanPhone.isNotEmpty()) {
            customerDao.getCustomerByPhone(cleanPhone) ?: customerDao.getCustomerByName(cleanName)
        } else {
            customerDao.getCustomerByName(cleanName)
        }

        if (existing != null) {
            val updated = existing.copy(
                name = cleanName.ifBlank { existing.name },
                phone = if (cleanPhone.isNotEmpty()) cleanPhone else existing.phone,
                totalOrdersCount = existing.totalOrdersCount + 1,
                totalSpent = existing.totalSpent + amountSpent,
                lastVisit = System.currentTimeMillis()
            )
            customerDao.updateCustomer(updated)
        } else {
            val newCustomer = CustomerEntity(
                name = cleanName,
                phone = cleanPhone,
                totalOrdersCount = 1,
                totalSpent = amountSpent,
                lastVisit = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
            customerDao.insertCustomer(newCustomer)
        }
    }
}
