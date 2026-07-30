package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ShopSettingsEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.PosRepository
import com.example.domain.model.PrinterConnectionType
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FirstTimeSetupUiState(
    val shopName: String = "",
    val shopAddress: String = "",
    val shopPhone: String = "",
    val defaultTaxRate: String = "0.0",
    val adminName: String = "",
    val adminPin: String = "",
    val adminPinConfirm: String = "",
    val errorMessage: String? = null,
    val isSetupComplete: Boolean = false,
    val createdAdminUser: UserEntity? = null
)

class FirstTimeSetupViewModel(private val repository: PosRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FirstTimeSetupUiState())
    val uiState: StateFlow<FirstTimeSetupUiState> = _uiState.asStateFlow()

    fun updateShopName(value: String) { _uiState.value = _uiState.value.copy(shopName = value, errorMessage = null) }
    fun updateShopAddress(value: String) { _uiState.value = _uiState.value.copy(shopAddress = value) }
    fun updateShopPhone(value: String) { _uiState.value = _uiState.value.copy(shopPhone = value) }
    fun updateTaxRate(value: String) { _uiState.value = _uiState.value.copy(defaultTaxRate = value) }
    fun updateAdminName(value: String) { _uiState.value = _uiState.value.copy(adminName = value, errorMessage = null) }
    fun updateAdminPin(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(adminPin = value, errorMessage = null)
        }
    }
    fun updateAdminPinConfirm(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(adminPinConfirm = value, errorMessage = null)
        }
    }

    fun completeFirstTimeSetup() {
        val state = _uiState.value
        if (state.shopName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter your Repair Shop Name")
            return
        }
        if (state.adminName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter the Admin / Manager Name")
            return
        }
        if (state.adminPin.length != 6) {
            _uiState.value = state.copy(errorMessage = "Admin Security PIN must be exactly 6 digits")
            return
        }
        if (state.adminPin != state.adminPinConfirm) {
            _uiState.value = state.copy(errorMessage = "Security PIN and Confirm PIN do not match")
            return
        }

        viewModelScope.launch {
            val taxRate = state.defaultTaxRate.toDoubleOrNull() ?: 0.0

            val settings = ShopSettingsEntity(
                id = 1,
                shopName = state.shopName.trim(),
                address = state.shopAddress.trim(),
                phone = state.shopPhone.trim(),
                isTaxEnabled = taxRate > 0,
                defaultTaxRatePercent = taxRate,
                printerConnection = PrinterConnectionType.NONE,
                printerAddress = "",
                isConfigured = true
            )
            repository.saveShopSettings(settings)

            val adminUser = UserEntity(
                name = state.adminName.trim(),
                role = UserRole.ADMIN,
                pinCode = state.adminPin
            )
            val userId = repository.insertUser(adminUser)
            val savedAdminUser = adminUser.copy(id = userId)

            _uiState.value = state.copy(
                isSetupComplete = true,
                createdAdminUser = savedAdminUser
            )
        }
    }

    fun resetSetupState() {
        _uiState.value = FirstTimeSetupUiState()
    }
}
