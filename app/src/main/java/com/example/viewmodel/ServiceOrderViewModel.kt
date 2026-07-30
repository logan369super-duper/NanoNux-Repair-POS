package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromotionEntity
import com.example.data.local.entity.RepairServiceEntity
import com.example.data.local.entity.ServiceOrderEntity
import com.example.data.local.entity.ShopSettingsEntity
import com.example.data.repository.PosRepository
import com.example.domain.model.RepairTicketStatus
import com.example.service.printer.EscPosPrinterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ReplacementPartOption {
    NONE,                   // No item needed
    SHOP_INVENTORY,         // Add item from shop inventory
    CUSTOMER_PROVIDED       // Customer provided own item
}

data class ServiceOrderUiState(
    val orders: List<ServiceOrderEntity> = emptyList(),
    val filteredOrders: List<ServiceOrderEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: RepairTicketStatus? = null,
    val activeServices: List<RepairServiceEntity> = emptyList(),
    val availableParts: List<ProductEntity> = emptyList(),
    val availablePromotions: List<PromotionEntity> = emptyList(),
    val availableCustomers: List<CustomerEntity> = emptyList(),
    val appliedPromotion: PromotionEntity? = null,
    val shopSettings: ShopSettingsEntity? = null,
    val brandsList: List<String> = listOf("Generic / Custom"),
    val issueTypesList: List<String> = listOf("General Maintenance"),

    // Intake Form State
    val customerName: String = "",
    val customerPhone: String = "",
    val deviceBrand: String = "Generic / Custom",
    val deviceModel: String = "",
    val serialImei: String = "",
    val issueType: String = "General Repair & Maintenance",
    val issueDescription: String = "",
    val laborFee: String = "45.00",
    val additionalCost: String = "0.00",
    val depositPaid: String = "0.00",
    val replacementPartOption: ReplacementPartOption = ReplacementPartOption.NONE,
    val selectedPart: ProductEntity? = null,
    val customerProvidedPartName: String = "",

    // Created Ticket Modal or Claim Ticket Print
    val newlyCreatedOrder: ServiceOrderEntity? = null,
    val claimTicketText: String? = null,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val calculatedPartCost: Double
        get() = if (replacementPartOption == ReplacementPartOption.SHOP_INVENTORY) (selectedPart?.unitPrice ?: 0.0) else 0.0

    val subtotalBeforeDiscount: Double
        get() {
            val labor = laborFee.toDoubleOrNull() ?: 0.0
            val addCost = additionalCost.toDoubleOrNull() ?: 0.0
            return labor + addCost + calculatedPartCost
        }

    val discountAmount: Double
        get() {
            val promo = appliedPromotion ?: return 0.0
            if (!promo.isActive) return 0.0
            val baseTotal = subtotalBeforeDiscount
            if (baseTotal < promo.minOrderAmount) return 0.0

            if (promo.appliesToSpecificItems) {
                val targetKeys = promo.targetItemIds.split(",").map { it.trim().uppercase() }.toSet()
                var matchingTotal = 0.0

                // Check selected part
                if (selectedPart != null) {
                    val partKey = "PROD_${selectedPart.id}"
                    val partIdOnly = selectedPart.id.toString()
                    val partSku = selectedPart.sku.uppercase()
                    if (partKey in targetKeys || partIdOnly in targetKeys || partSku in targetKeys) {
                        matchingTotal += selectedPart.unitPrice
                    }
                }

                // Check selected service
                val selectedService = activeServices.find { it.serviceName == issueType }
                if (selectedService != null) {
                    val srvKey = "SRV_${selectedService.id}"
                    val srvIdOnly = selectedService.id.toString()
                    if (srvKey in targetKeys || srvIdOnly in targetKeys) {
                        matchingTotal += (laborFee.toDoubleOrNull() ?: selectedService.baseLaborFee)
                    }
                }

                if (matchingTotal <= 0.0) return 0.0

                return if (promo.discountType == "PERCENTAGE") {
                    (matchingTotal * (promo.effectiveDiscountPercent / 100.0)).coerceAtMost(matchingTotal)
                } else {
                    promo.effectiveFixedDiscount.coerceAtMost(matchingTotal)
                }
            } else {
                return if (promo.discountType == "PERCENTAGE") {
                    (baseTotal * (promo.effectiveDiscountPercent / 100.0)).coerceAtMost(baseTotal)
                } else {
                    promo.effectiveFixedDiscount.coerceAtMost(baseTotal)
                }
            }
        }

    val calculatedTotalPrice: Double
        get() = (subtotalBeforeDiscount - discountAmount).coerceAtLeast(0.0)

    val balanceDue: Double
        get() {
            val deposit = depositPaid.toDoubleOrNull() ?: 0.0
            return (calculatedTotalPrice - deposit).coerceAtLeast(0.0)
        }
}

class ServiceOrderViewModel(private val repository: PosRepository) : ViewModel() {

    private val printerService = EscPosPrinterService()
    private val _uiState = MutableStateFlow(ServiceOrderUiState())
    val uiState: StateFlow<ServiceOrderUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllServiceOrders().collect { orders ->
                _uiState.value = _uiState.value.copy(
                    orders = orders,
                    filteredOrders = filterOrders(orders, _uiState.value.searchQuery, _uiState.value.selectedStatusFilter)
                )
            }
        }
        viewModelScope.launch {
            repository.getAllActiveServices().collect { services ->
                _uiState.value = _uiState.value.copy(activeServices = services)
            }
        }
        viewModelScope.launch {
            repository.getAllActivePromotions().collect { promos ->
                val servicePromos = promos.filter { it.appliesToScope == "BOTH" || it.appliesToScope == "SERVICE" || it.appliesToScope == "ALL" }
                _uiState.value = _uiState.value.copy(availablePromotions = servicePromos)
            }
        }
        viewModelScope.launch {
            repository.getAllProducts().collect { products ->
                _uiState.value = _uiState.value.copy(availableParts = products)
            }
        }
        viewModelScope.launch {
            repository.getShopSettingsFlow().collect { settings ->
                _uiState.value = _uiState.value.copy(shopSettings = settings)
            }
        }
        viewModelScope.launch {
            repository.getAllRepairItemBrands().collect { brands ->
                val names = brands.map { it.name }.toMutableList()
                if (!names.contains("Generic / Custom")) {
                    names.add(0, "Generic / Custom")
                } else {
                    names.remove("Generic / Custom")
                    names.add(0, "Generic / Custom")
                }
                _uiState.value = _uiState.value.copy(brandsList = names)
            }
        }
        viewModelScope.launch {
            repository.getAllRepairIssueTypes().collect { issues ->
                val names = issues.map { it.name }
                _uiState.value = _uiState.value.copy(issueTypesList = names)
            }
        }
        viewModelScope.launch {
            repository.getAllCustomers().collect { customers ->
                _uiState.value = _uiState.value.copy(availableCustomers = customers)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredOrders = filterOrders(_uiState.value.orders, query, _uiState.value.selectedStatusFilter)
        )
    }

    fun onStatusFilterChange(status: RepairTicketStatus?) {
        _uiState.value = _uiState.value.copy(
            selectedStatusFilter = status,
            filteredOrders = filterOrders(_uiState.value.orders, _uiState.value.searchQuery, status)
        )
    }

    private fun filterOrders(
        list: List<ServiceOrderEntity>,
        query: String,
        statusFilter: RepairTicketStatus?
    ): List<ServiceOrderEntity> {
        var filtered = list
        if (statusFilter != null) {
            filtered = filtered.filter { it.status == statusFilter }
        }
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.customerName.contains(query, ignoreCase = true) ||
                it.customerPhone.contains(query, ignoreCase = true) ||
                it.ticketNumber.contains(query, ignoreCase = true) ||
                it.deviceBrand.contains(query, ignoreCase = true) ||
                it.deviceModel.contains(query, ignoreCase = true) ||
                it.serialImei.contains(query, ignoreCase = true)
            }
        }
        return filtered
    }

    // Intake Form Setters
    fun updateCustomerName(v: String) { _uiState.value = _uiState.value.copy(customerName = v) }
    fun updateCustomerPhone(v: String) { _uiState.value = _uiState.value.copy(customerPhone = v) }
    fun updateDeviceBrand(v: String) { _uiState.value = _uiState.value.copy(deviceBrand = v) }
    fun updateDeviceModel(v: String) { _uiState.value = _uiState.value.copy(deviceModel = v) }
    fun updateSerialImei(v: String) { _uiState.value = _uiState.value.copy(serialImei = v) }
    fun updateIssueType(v: String) {
        val presetService = _uiState.value.activeServices.find { it.serviceName == v }
        val labor = presetService?.baseLaborFee ?: 45.0
        _uiState.value = _uiState.value.copy(
            issueType = v,
            laborFee = labor.toString()
        )
    }
    fun updateIssueDescription(v: String) { _uiState.value = _uiState.value.copy(issueDescription = v) }
    fun updateLaborFee(v: String) { _uiState.value = _uiState.value.copy(laborFee = v) }
    fun updateAdditionalCost(v: String) { _uiState.value = _uiState.value.copy(additionalCost = v) }
    fun updateDepositPaid(v: String) { _uiState.value = _uiState.value.copy(depositPaid = v) }
    fun updateReplacementPartOption(option: ReplacementPartOption) { _uiState.value = _uiState.value.copy(replacementPartOption = option) }
    fun selectPart(part: ProductEntity?) { _uiState.value = _uiState.value.copy(selectedPart = part) }
    fun updateCustomerProvidedPartName(v: String) { _uiState.value = _uiState.value.copy(customerProvidedPartName = v) }
    fun applyPromotion(promotion: PromotionEntity?) { _uiState.value = _uiState.value.copy(appliedPromotion = promotion) }

    fun createIntakeTicket() {
        val state = _uiState.value
        val phone = state.customerPhone.ifBlank { "N/A" }
        if (state.customerName.isBlank() || state.deviceModel.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please fill in customer name and device model.")
            return
        }

        viewModelScope.launch {
            val ticketNum = "TCK-${(1000..9999).random()}"
            val labor = state.laborFee.toDoubleOrNull() ?: 45.0
            val addCost = state.additionalCost.toDoubleOrNull() ?: 0.0
            val totalPrice = state.calculatedTotalPrice
            val deposit = state.depositPaid.toDoubleOrNull() ?: 0.0

            val allocatedPartsJson = when (state.replacementPartOption) {
                ReplacementPartOption.SHOP_INVENTORY -> {
                    if (state.selectedPart != null) {
                        "[{\"id\":${state.selectedPart.id},\"name\":\"${state.selectedPart.name}\",\"cost\":${state.selectedPart.unitPrice}}]"
                    } else "[]"
                }
                ReplacementPartOption.CUSTOMER_PROVIDED -> {
                    val name = state.customerProvidedPartName.ifBlank { "Customer Provided Replacement Item" }
                    "[{\"id\":-1,\"name\":\"$name ($0.00 - Customer Provided)\",\"cost\":0.0}]"
                }
                ReplacementPartOption.NONE -> "[]"
            }

            val fullDescription = if (addCost > 0) {
                "${state.issueDescription.ifBlank { "Standard repair intake" }} | Add. Cost: Ks $addCost"
            } else {
                state.issueDescription.ifBlank { "Standard repair intake" }
            }

            val order = ServiceOrderEntity(
                ticketNumber = ticketNum,
                customerName = state.customerName,
                customerPhone = phone,
                deviceBrand = state.deviceBrand.ifBlank { "Generic / Custom" },
                deviceModel = state.deviceModel,
                serialImei = state.serialImei.ifBlank { "N/A" },
                issueType = state.issueType.ifBlank { "General Repair & Maintenance" },
                issueDescription = fullDescription,
                estimatedCost = totalPrice,
                laborFee = labor,
                allocatedPartsJson = allocatedPartsJson,
                status = RepairTicketStatus.RECEIVED,
                depositPaid = deposit,
                createdAt = System.currentTimeMillis()
            )

            val newId = repository.createServiceOrder(order)
            if (state.customerName.isNotBlank()) {
                repository.recordOrUpdateCustomer(state.customerName, state.customerPhone, deposit)
            }
            val savedOrder = order.copy(id = newId)

            val receiptText = printerService.generateClaimTicketReceipt(savedOrder, state.shopSettings)

            _uiState.value = state.copy(
                newlyCreatedOrder = savedOrder,
                claimTicketText = receiptText,
                isSuccess = true,
                errorMessage = null,
                // Reset form
                customerName = "",
                customerPhone = "",
                deviceModel = "",
                serialImei = "",
                issueDescription = "",
                additionalCost = "0.00",
                depositPaid = "0.00",
                replacementPartOption = ReplacementPartOption.NONE,
                selectedPart = null,
                customerProvidedPartName = ""
            )
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: RepairTicketStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun dismissSuccess() {
        _uiState.value = _uiState.value.copy(newlyCreatedOrder = null, claimTicketText = null, isSuccess = false)
    }
}
