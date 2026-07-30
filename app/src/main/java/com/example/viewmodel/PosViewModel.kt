package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromotionEntity
import com.example.data.local.entity.RepairServiceEntity
import com.example.data.local.entity.ShopSettingsEntity
import com.example.data.local.entity.TransactionLogEntity
import com.example.data.repository.PosRepository
import com.example.domain.model.CartItem
import com.example.domain.model.CartItemType
import com.example.service.printer.EscPosPrinterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PosUiState(
    val cartItems: List<CartItem> = emptyList(),
    val availableProducts: List<ProductEntity> = emptyList(),
    val availableServices: List<RepairServiceEntity> = emptyList(),
    val availablePromotions: List<PromotionEntity> = emptyList(),
    val availableCustomers: List<CustomerEntity> = emptyList(),
    val appliedPromotion: PromotionEntity? = null,
    val shopSettings: ShopSettingsEntity? = null,

    val customerName: String = "",
    val customerPhone: String = "",
    val discountAmount: Double = 0.0,
    val paymentMethod: String = "CASH", // CASH, CARD, DIGITAL_WALLET
    val cashierName: String = "Staff",
    val notes: String = "",

    val lastTransaction: TransactionLogEntity? = null,
    val receiptText: String? = null,
    val isCheckoutComplete: Boolean = false,
    val errorMessage: String? = null
) {
    val subtotal: Double get() = cartItems.sumOf { it.unitPrice * it.quantity }
    val taxRatePercent: Double get() = shopSettings?.defaultTaxRatePercent ?: 8.5
    val discount: Double get() {
        val promo = appliedPromotion
        if (promo != null && promo.isActive) {
            if (subtotal < promo.minOrderAmount) return 0.0

            if (promo.appliesToSpecificItems) {
                val targetKeys = promo.targetItemIds.split(",").map { it.trim().uppercase() }.toSet()
                val matchingSubtotal = cartItems.filter { item ->
                    val itemIdKey = item.id.uppercase()
                    val refIdStr = item.referenceId?.toString() ?: ""
                    itemIdKey in targetKeys || (refIdStr.isNotEmpty() && refIdStr in targetKeys)
                }.sumOf { it.unitPrice * it.quantity }

                if (matchingSubtotal <= 0.0) return 0.0

                return if (promo.discountType == "PERCENTAGE") {
                    val pct = promo.effectiveDiscountPercent
                    (matchingSubtotal * (pct / 100.0)).coerceAtMost(matchingSubtotal)
                } else {
                    val fixed = promo.effectiveFixedDiscount
                    fixed.coerceAtMost(matchingSubtotal)
                }
            } else {
                return if (promo.discountType == "PERCENTAGE") {
                    val pct = promo.effectiveDiscountPercent
                    (subtotal * (pct / 100.0)).coerceAtMost(subtotal)
                } else {
                    val fixed = promo.effectiveFixedDiscount
                    fixed.coerceAtMost(subtotal)
                }
            }
        }
        return discountAmount.coerceAtMost(subtotal)
    }
    val taxableBase: Double get() = (subtotal - discount).coerceAtLeast(0.0)
    val taxAmount: Double get() = taxableBase * (taxRatePercent / 100.0)
    val grandTotal: Double get() = taxableBase + taxAmount
}

class PosViewModel(private val repository: PosRepository) : ViewModel() {

    private val printerService = EscPosPrinterService()
    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        viewModelScope.launch {
            repository.getAllProducts().collect { products ->
                _uiState.value = _uiState.value.copy(availableProducts = products)
            }
        }
        viewModelScope.launch {
            repository.getAllActiveServices().collect { services ->
                _uiState.value = _uiState.value.copy(availableServices = services)
            }
        }
        viewModelScope.launch {
            repository.getAllActivePromotions().collect { promos ->
                val posPromos = promos.filter { it.appliesToScope == "BOTH" || it.appliesToScope == "POS" || it.appliesToScope == "ALL" }
                _uiState.value = _uiState.value.copy(availablePromotions = posPromos)
            }
        }
        viewModelScope.launch {
            repository.getShopSettingsFlow().collect { settings ->
                _uiState.value = _uiState.value.copy(shopSettings = settings)
            }
        }
        viewModelScope.launch {
            repository.getAllCustomers().collect { customers ->
                _uiState.value = _uiState.value.copy(availableCustomers = customers)
            }
        }
    }

    fun addProductToCart(product: ProductEntity) {
        val currentCart = _uiState.value.cartItems.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.id == "PROD_${product.id}" }
        if (existingIndex >= 0) {
            val item = currentCart[existingIndex]
            if (item.quantity + 1 <= product.stockQuantity) {
                currentCart[existingIndex] = item.copy(quantity = item.quantity + 1)
            }
        } else {
            currentCart.add(
                CartItem(
                    id = "PROD_${product.id}",
                    name = "${product.brand} ${product.name}",
                    type = CartItemType.PRODUCT_PART,
                    unitPrice = product.unitPrice,
                    costPrice = product.costPrice,
                    quantity = 1,
                    referenceId = product.id
                )
            )
        }
        _uiState.value = _uiState.value.copy(cartItems = currentCart, errorMessage = null)
    }

    fun addServiceToCart(service: RepairServiceEntity) {
        val currentCart = _uiState.value.cartItems.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.id == "SERV_${service.id}" }
        if (existingIndex >= 0) {
            val item = currentCart[existingIndex]
            currentCart[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            currentCart.add(
                CartItem(
                    id = "SERV_${service.id}",
                    name = "Labor: ${service.serviceName}",
                    type = CartItemType.LABOR_SERVICE,
                    unitPrice = service.baseLaborFee,
                    costPrice = 0.0,
                    quantity = 1,
                    referenceId = service.id
                )
            )
        }
        _uiState.value = _uiState.value.copy(cartItems = currentCart, errorMessage = null)
    }

    fun updateQuantity(cartItemId: String, newQty: Int) {
        val currentCart = _uiState.value.cartItems.toMutableList()
        val index = currentCart.indexOfFirst { it.id == cartItemId }
        if (index >= 0) {
            if (newQty <= 0) {
                currentCart.removeAt(index)
            } else {
                currentCart[index] = currentCart[index].copy(quantity = newQty)
            }
            _uiState.value = _uiState.value.copy(cartItems = currentCart)
        }
    }

    fun applyPromotion(promotion: PromotionEntity?) {
        _uiState.value = _uiState.value.copy(appliedPromotion = promotion)
    }

    fun setCustomerDetails(name: String, phone: String) {
        _uiState.value = _uiState.value.copy(customerName = name, customerPhone = phone)
    }

    fun setPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun setCashierName(name: String) {
        _uiState.value = _uiState.value.copy(cashierName = name)
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(
            cartItems = emptyList(),
            appliedPromotion = null,
            discountAmount = 0.0,
            customerName = "",
            customerPhone = "",
            notes = "",
            errorMessage = null
        )
    }

    fun checkout() {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Cart is empty!")
            return
        }

        viewModelScope.launch {
            try {
                val transaction = repository.processPosSale(
                    items = state.cartItems,
                    discount = state.discount,
                    taxPercent = state.taxRatePercent,
                    paymentMethod = state.paymentMethod,
                    cashierName = state.cashierName.ifBlank { "Staff" },
                    customerName = state.customerName.ifBlank { null },
                    customerPhone = state.customerPhone.ifBlank { null },
                    notes = state.notes.ifBlank { null }
                )

                val receipt = printerService.generateFinalSalesReceipt(transaction, state.shopSettings)

                _uiState.value = state.copy(
                    lastTransaction = transaction,
                    receiptText = receipt,
                    isCheckoutComplete = true,
                    cartItems = emptyList(),
                    appliedPromotion = null,
                    discountAmount = 0.0,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(errorMessage = "Checkout failed: ${e.localizedMessage}")
            }
        }
    }

    fun dismissReceipt() {
        _uiState.value = _uiState.value.copy(
            lastTransaction = null,
            receiptText = null,
            isCheckoutComplete = false
        )
    }
}
