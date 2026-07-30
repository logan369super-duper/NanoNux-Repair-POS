package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromotionEntity
import com.example.data.local.entity.RepairServiceEntity
import com.example.data.repository.PosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class PromotionsUiState(
    val promotions: List<PromotionEntity> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val services: List<RepairServiceEntity> = emptyList(),
    val isLoading: Boolean = false
)

class PromotionsViewModel(private val repository: PosRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PromotionsUiState())
    val uiState: StateFlow<PromotionsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getAllPromotions(),
                repository.getAllProducts(),
                repository.getAllActiveServices()
            ) { promos, prods, srvs ->
                PromotionsUiState(
                    promotions = promos,
                    products = prods,
                    services = srvs,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun savePromotion(
        id: Long = 0,
        code: String,
        title: String,
        description: String,
        discountType: String,
        discountValue: Double,
        appliesToSpecificItems: Boolean,
        targetItemIds: String,
        appliesToScope: String,
        minOrderAmount: Double = 0.0,
        isActive: Boolean = true
    ) {
        viewModelScope.launch {
            val isPct = discountType == "PERCENTAGE"
            val promo = PromotionEntity(
                id = id,
                code = code.uppercase().trim(),
                title = title.trim(),
                description = description.trim(),
                discountType = discountType,
                discountValue = discountValue,
                discountPercent = if (isPct) discountValue else 0.0,
                fixedDiscountAmount = if (!isPct) discountValue else 0.0,
                appliesToSpecificItems = appliesToSpecificItems,
                targetItemIds = targetItemIds,
                appliesToScope = appliesToScope,
                minOrderAmount = minOrderAmount,
                isActive = isActive
            )
            if (id == 0L) {
                repository.insertPromotion(promo)
            } else {
                repository.updatePromotion(promo)
            }
        }
    }

    fun togglePromotionActive(promotion: PromotionEntity) {
        viewModelScope.launch {
            repository.updatePromotion(promotion.copy(isActive = !promotion.isActive))
        }
    }

    fun deletePromotion(promotion: PromotionEntity) {
        viewModelScope.launch {
            repository.deletePromotion(promotion)
        }
    }
}

