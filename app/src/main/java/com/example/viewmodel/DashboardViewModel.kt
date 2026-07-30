package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ServiceOrderEntity
import com.example.data.repository.PosRepository
import com.example.domain.model.FinancialReportSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val financialSummary: FinancialReportSummary = FinancialReportSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0),
    val lowStockItems: List<ProductEntity> = emptyList(),
    val openRepairOrders: List<ServiceOrderEntity> = emptyList(),
    val openTicketCount: Int = 0,
    val isLoading: Boolean = false
)

class DashboardViewModel(private val repository: PosRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            repository.getFinancialReportSummary().collect { summary ->
                _uiState.value = _uiState.value.copy(financialSummary = summary)
            }
        }
        viewModelScope.launch {
            repository.getLowStockProducts().collect { lowStock ->
                _uiState.value = _uiState.value.copy(lowStockItems = lowStock)
            }
        }
        viewModelScope.launch {
            repository.getActiveServiceOrders().collect { activeOrders ->
                _uiState.value = _uiState.value.copy(openRepairOrders = activeOrders)
            }
        }
        viewModelScope.launch {
            repository.getOpenTicketCount().collect { count ->
                _uiState.value = _uiState.value.copy(openTicketCount = count)
            }
        }
    }
}
