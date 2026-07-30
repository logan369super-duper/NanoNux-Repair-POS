package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.RepairServiceEntity
import com.example.data.repository.PosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServiceCatalogUiState(
    val services: List<RepairServiceEntity> = emptyList()
)

class ServiceCatalogViewModel(private val repository: PosRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceCatalogUiState())
    val uiState: StateFlow<ServiceCatalogUiState> = _uiState.asStateFlow()

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            repository.getAllActiveServices().collect { list ->
                _uiState.value = _uiState.value.copy(services = list)
            }
        }
    }

    fun saveService(id: Long = 0, name: String, category: String, fee: Double, estMinutes: Int) {
        viewModelScope.launch {
            val service = RepairServiceEntity(
                id = id,
                serviceName = name,
                category = category,
                baseLaborFee = fee,
                estimatedTimeMinutes = estMinutes
            )
            if (id == 0L) {
                repository.insertService(service)
            } else {
                repository.updateService(service)
            }
        }
    }

    fun deleteService(service: RepairServiceEntity) {
        viewModelScope.launch {
            repository.deleteService(service)
        }
    }
}
