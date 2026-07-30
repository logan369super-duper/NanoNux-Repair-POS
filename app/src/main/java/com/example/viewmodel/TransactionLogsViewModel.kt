package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.TransactionLogEntity
import com.example.data.repository.PosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionLogsUiState(
    val transactions: List<TransactionLogEntity> = emptyList(),
    val brandFilter: String = "",
    val customerFilter: String = "",
    val issueFilter: String = "",
    val selectedTransaction: TransactionLogEntity? = null
)

class TransactionLogsViewModel(private val repository: PosRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionLogsUiState())
    val uiState: StateFlow<TransactionLogsUiState> = _uiState.asStateFlow()

    init {
        applyFilters()
    }

    fun updateBrandFilter(brand: String) {
        _uiState.value = _uiState.value.copy(brandFilter = brand)
        applyFilters()
    }

    fun updateCustomerFilter(customer: String) {
        _uiState.value = _uiState.value.copy(customerFilter = customer)
        applyFilters()
    }

    fun updateIssueFilter(issue: String) {
        _uiState.value = _uiState.value.copy(issueFilter = issue)
        applyFilters()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            brandFilter = "",
            customerFilter = "",
            issueFilter = ""
        )
        applyFilters()
    }

    fun selectTransaction(tx: TransactionLogEntity?) {
        _uiState.value = _uiState.value.copy(selectedTransaction = tx)
    }

    private fun applyFilters() {
        viewModelScope.launch {
            val brand = _uiState.value.brandFilter.ifBlank { null }
            val customer = _uiState.value.customerFilter.ifBlank { null }
            val issue = _uiState.value.issueFilter.ifBlank { null }

            repository.filterTransactions(brand, customer, issue).collect { list ->
                _uiState.value = _uiState.value.copy(transactions = list)
            }
        }
    }
}
