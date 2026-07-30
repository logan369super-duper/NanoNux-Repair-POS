package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ServiceOrderEntity
import com.example.data.local.entity.TransactionLogEntity
import com.example.data.repository.PosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CustomersUiState(
    val customers: List<CustomerEntity> = emptyList(),
    val transactions: List<TransactionLogEntity> = emptyList(),
    val serviceOrders: List<ServiceOrderEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCustomer: CustomerEntity? = null,
    val isLoading: Boolean = false
)

class CustomersViewModel(private val repository: PosRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomersUiState(isLoading = true))
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getAllCustomers(),
                repository.getAllTransactions(),
                repository.getAllServiceOrders()
            ) { customers, txns, orders ->
                _uiState.value.copy(
                    customers = customers,
                    transactions = txns,
                    serviceOrders = orders,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectCustomer(customer: CustomerEntity?) {
        _uiState.value = _uiState.value.copy(selectedCustomer = customer)
    }

    fun saveCustomer(
        id: Long = 0,
        name: String,
        phone: String,
        email: String,
        address: String
    ) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            val existing = if (id != 0L) _uiState.value.customers.find { it.id == id } else null

            val customer = CustomerEntity(
                id = id,
                name = name.trim(),
                phone = phone.trim(),
                email = email.trim(),
                address = address.trim(),
                totalOrdersCount = existing?.totalOrdersCount ?: 0,
                totalSpent = existing?.totalSpent ?: 0.0,
                lastVisit = existing?.lastVisit ?: System.currentTimeMillis(),
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )

            if (id == 0L) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
            }
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            if (_uiState.value.selectedCustomer?.id == customer.id) {
                _uiState.value = _uiState.value.copy(selectedCustomer = null)
            }
        }
    }
}
