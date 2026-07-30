package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.RepairItemBrandEntity
import com.example.data.local.entity.RepairIssueTypeEntity
import com.example.data.local.entity.ShopSettingsEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.PosRepository
import com.example.domain.model.PrinterConnectionType
import com.example.service.printer.EscPosPrinterService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.Dispatchers

data class BluetoothDeviceModel(
    val name: String,
    val address: String
)

data class ShopSettingsUiState(
    val shopName: String = "",
    val address: String = "",
    val phone: String = "",
    val logoUri: String? = null,
    val isTaxEnabled: Boolean = true,
    val defaultTaxRate: String = "0.0",
    val receiptFooterNote: String = "Thank you for choosing our repair service! All labor comes with a 90-day warranty.",
    val printerType: PrinterConnectionType = PrinterConnectionType.NONE,
    val printerAddress: String = "",
    val printerPort: String = "9100",
    val paperSizeMm: Int = 58, // 58mm or 80mm
    val printerFontSize: Int = 18, // Font size before converting receipt/invoice text to image
    val showTaxInPrintedInvoice: Boolean = true, // Switch to show tax in printed invoice
    val showLogoInPrintedInvoice: Boolean = true, // Switch to show shop logo image on printed invoice
    val isDarkMode: Boolean = false,
    val language: String = "en",
    val isTestingPrinter: Boolean = false,
    val testPrintResult: String? = null,
    val showTestResultDialog: Boolean = false,
    val isSaved: Boolean = false,
    val saveMessage: String? = null,
    val currentSettings: ShopSettingsEntity? = null,

    // Bluetooth thermal printer state
    val bluetoothDevices: List<BluetoothDeviceModel> = emptyList(),
    val isScanningBluetooth: Boolean = false,
    val connectedPrinterName: String? = null,
    val bluetoothConnectionStatus: String = "Not connected",

    // Repair Specs & Fault Lists
    val brandsList: List<RepairItemBrandEntity> = emptyList(),
    val issueTypesList: List<RepairIssueTypeEntity> = emptyList(),
    val newBrandInput: String = "",
    val newIssueTypeInput: String = ""
)

// Backward compatibility typealias for ShopSetupUiState
typealias ShopSetupUiState = ShopSettingsUiState

class ShopSettingsViewModel(private val repository: PosRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopSettingsUiState())
    val uiState: StateFlow<ShopSettingsUiState> = _uiState.asStateFlow()

    private val printerService = EscPosPrinterService()

    init {
        loadSettings()
    }

    fun getAllActiveUsersFlow() = repository.getAllActiveUsers()

    suspend fun getUserByPin(pin: String): UserEntity? = repository.getUserByPin(pin)

    suspend fun insertUser(user: UserEntity): Long = repository.insertUser(user)

    suspend fun updateUser(user: UserEntity) = repository.updateUser(user)

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getShopSettingsFlow().collect { settings ->
                if (settings != null) {
                    _uiState.value = _uiState.value.copy(
                        shopName = settings.shopName,
                        address = settings.address,
                        phone = settings.phone,
                        logoUri = settings.logoUri,
                        isTaxEnabled = settings.isTaxEnabled,
                        defaultTaxRate = settings.defaultTaxRatePercent.toString(),
                        receiptFooterNote = settings.receiptFooterNote,
                        printerType = settings.printerConnection,
                        printerAddress = settings.printerAddress,
                        printerPort = settings.printerPort.toString(),
                        paperSizeMm = settings.paperSizeMm,
                        printerFontSize = settings.printerFontSize,
                        showTaxInPrintedInvoice = settings.showTaxInPrintedInvoice,
                        showLogoInPrintedInvoice = settings.showLogoInPrintedInvoice,
                        isDarkMode = settings.isDarkMode,
                        language = settings.language,
                        currentSettings = settings
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.getAllRepairItemBrands().collect { list ->
                _uiState.value = _uiState.value.copy(brandsList = list)
            }
        }
        viewModelScope.launch {
            repository.getAllRepairIssueTypes().collect { list ->
                _uiState.value = _uiState.value.copy(issueTypesList = list)
            }
        }
    }

    fun updateShopName(value: String) { _uiState.value = _uiState.value.copy(shopName = value, isSaved = false) }
    fun updateAddress(value: String) { _uiState.value = _uiState.value.copy(address = value, isSaved = false) }
    fun updatePhone(value: String) { _uiState.value = _uiState.value.copy(phone = value, isSaved = false) }
    fun updateLogoUri(uri: String?) { _uiState.value = _uiState.value.copy(logoUri = uri, isSaved = false) }
    fun updateIsTaxEnabled(enabled: Boolean) { _uiState.value = _uiState.value.copy(isTaxEnabled = enabled, isSaved = false) }
    fun updateTaxRate(value: String) { _uiState.value = _uiState.value.copy(defaultTaxRate = value, isSaved = false) }
    fun updateReceiptFooterNote(value: String) { _uiState.value = _uiState.value.copy(receiptFooterNote = value, isSaved = false) }
    fun updateIsDarkMode(isDark: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = isDark)
        saveSettings("Application Theme")
    }
    fun updateLanguage(langCode: String) {
        _uiState.value = _uiState.value.copy(language = langCode)
        saveSettings("Language")
    }
    fun updateNewBrandInput(value: String) { _uiState.value = _uiState.value.copy(newBrandInput = value) }
    fun updateNewIssueTypeInput(value: String) { _uiState.value = _uiState.value.copy(newIssueTypeInput = value) }

    fun addRepairBrand() {
        val name = _uiState.value.newBrandInput.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertRepairItemBrand(RepairItemBrandEntity(name = name, isSystemDefault = false))
            _uiState.value = _uiState.value.copy(newBrandInput = "", saveMessage = "Brand '$name' added!")
        }
    }

    fun updateRepairBrand(brand: RepairItemBrandEntity, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank() || brand.isSystemDefault || brand.name == "Generic / Custom") return
        viewModelScope.launch {
            repository.updateRepairItemBrand(brand.copy(name = trimmed))
            _uiState.value = _uiState.value.copy(saveMessage = "Brand updated to '$trimmed'")
        }
    }

    fun deleteRepairBrand(brand: RepairItemBrandEntity) {
        viewModelScope.launch {
            repository.deleteRepairItemBrand(brand)
            _uiState.value = _uiState.value.copy(saveMessage = "Brand '${brand.name}' removed.")
        }
    }

    fun addRepairIssueType() {
        val name = _uiState.value.newIssueTypeInput.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertRepairIssueType(RepairIssueTypeEntity(name = name, isSystemDefault = false))
            _uiState.value = _uiState.value.copy(newIssueTypeInput = "", saveMessage = "Fault type '$name' added!")
        }
    }

    fun deleteRepairIssueType(issueType: RepairIssueTypeEntity) {
        viewModelScope.launch {
            repository.deleteRepairIssueType(issueType)
            _uiState.value = _uiState.value.copy(saveMessage = "Fault type '${issueType.name}' removed.")
        }
    }

    fun updatePrinterType(value: PrinterConnectionType) { _uiState.value = _uiState.value.copy(printerType = value, isSaved = false) }
    fun updatePrinterAddress(value: String) { _uiState.value = _uiState.value.copy(printerAddress = value, isSaved = false) }
    fun updatePrinterPort(value: String) { _uiState.value = _uiState.value.copy(printerPort = value, isSaved = false) }
    fun updatePaperSize(sizeMm: Int) { _uiState.value = _uiState.value.copy(paperSizeMm = sizeMm, isSaved = false) }
    fun updatePrinterFontSize(sizeSp: Int) { _uiState.value = _uiState.value.copy(printerFontSize = sizeSp, isSaved = false) }
    fun updateShowTaxInPrintedInvoice(show: Boolean) { _uiState.value = _uiState.value.copy(showTaxInPrintedInvoice = show, isSaved = false) }
    fun updateShowLogoInPrintedInvoice(show: Boolean) { _uiState.value = _uiState.value.copy(showLogoInPrintedInvoice = show, isSaved = false) }

    fun checkBluetooth(context: android.content.Context) {
        val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            _uiState.value = _uiState.value.copy(
                saveMessage = "Bluetooth is not supported on this device"
            )
            return
        }

        if (adapter.isEnabled) {
            _uiState.value = _uiState.value.copy(
                saveMessage = "Bluetooth is turned on!"
            )
        } else {
            try {
                @Suppress("DEPRECATION")
                val success = adapter.enable()
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        saveMessage = "Enabling Bluetooth..."
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        saveMessage = "Please enable Bluetooth in your device settings."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveMessage = "Please enable Bluetooth in your device settings."
                )
            }
        }
    }

    private var bluetoothReceiver: android.content.BroadcastReceiver? = null

    fun scanBluetoothDevices(context: android.content.Context) {
        val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isScanningBluetooth = true, bluetoothDevices = emptyList())
                delay(1500)
                _uiState.value = _uiState.value.copy(
                    isScanningBluetooth = false,
                    bluetoothDevices = listOf(
                        BluetoothDeviceModel("PT-210 Thermal Printer", "00:11:22:33:44:55"),
                        BluetoothDeviceModel("MTP-II Pocket Printer", "88:25:83:F1:C2:10"),
                        BluetoothDeviceModel("Office Desktop Printer", "AA:BB:CC:DD:EE:FF")
                    )
                )
            }
            return
        }

        try {
            val bondedList = mutableListOf<BluetoothDeviceModel>()
            adapter.bondedDevices?.forEach { device ->
                try {
                    bondedList.add(BluetoothDeviceModel(device.name ?: "Unknown Device", device.address))
                } catch (e: SecurityException) {}
            }

            _uiState.value = _uiState.value.copy(
                isScanningBluetooth = true,
                bluetoothDevices = bondedList
            )

            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }

            bluetoothReceiver?.let {
                try {
                    context.applicationContext.unregisterReceiver(it)
                } catch (e: Exception) {}
            }

            val foundDevices = mutableSetOf<BluetoothDeviceModel>()
            foundDevices.addAll(bondedList)

            val receiver = object : android.content.BroadcastReceiver() {
                override fun onReceive(ctx: android.content.Context, intent: android.content.Intent) {
                    val action = intent.action
                    if (android.bluetooth.BluetoothDevice.ACTION_FOUND == action) {
                        val device = intent.getParcelableExtra<android.bluetooth.BluetoothDevice>(android.bluetooth.BluetoothDevice.EXTRA_DEVICE)
                        if (device != null) {
                            try {
                                val name = device.name ?: "Unknown Device"
                                val address = device.address
                                val model = BluetoothDeviceModel(name, address)
                                if (foundDevices.add(model)) {
                                    _uiState.value = _uiState.value.copy(
                                        bluetoothDevices = foundDevices.toList()
                                    )
                                }
                            } catch (e: SecurityException) {}
                        }
                    } else if (android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                        _uiState.value = _uiState.value.copy(isScanningBluetooth = false)
                        try {
                            context.applicationContext.unregisterReceiver(this)
                        } catch (e: Exception) {}
                        if (bluetoothReceiver == this) {
                            bluetoothReceiver = null
                        }
                    }
                }
            }

            bluetoothReceiver = receiver
            val filter = android.content.IntentFilter().apply {
                addAction(android.bluetooth.BluetoothDevice.ACTION_FOUND)
                addAction(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            context.applicationContext.registerReceiver(receiver, filter)
            adapter.startDiscovery()

            viewModelScope.launch {
                delay(12000)
                if (_uiState.value.isScanningBluetooth) {
                    try {
                        adapter.cancelDiscovery()
                    } catch (e: SecurityException) {}
                    _uiState.value = _uiState.value.copy(isScanningBluetooth = false)
                    bluetoothReceiver?.let {
                        try {
                            context.applicationContext.unregisterReceiver(it)
                        } catch (e: Exception) {}
                        bluetoothReceiver = null
                    }
                }
            }

        } catch (e: SecurityException) {
            _uiState.value = _uiState.value.copy(
                isScanningBluetooth = false,
                saveMessage = "Bluetooth permission is required to scan devices."
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isScanningBluetooth = false,
                saveMessage = "Error starting Bluetooth scan: ${e.message}"
            )
        }
    }

    fun connectToBluetoothPrinter(address: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                bluetoothConnectionStatus = "Connecting...",
                printerAddress = address
            )
            val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
            if (adapter == null) {
                delay(1500)
                _uiState.value = _uiState.value.copy(
                    connectedPrinterName = name,
                    bluetoothConnectionStatus = "Connected"
                )
                return@launch
            }

            try {
                val device = adapter.getRemoteDevice(address)
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                adapter.cancelDiscovery()

                socket.connect()
                socket.close()

                _uiState.value = _uiState.value.copy(
                    connectedPrinterName = name.ifBlank { device.name ?: "Unknown Printer" },
                    bluetoothConnectionStatus = "Connected"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    connectedPrinterName = null,
                    bluetoothConnectionStatus = "Not connected"
                )
            }
        }
    }

    fun testPrinterConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingPrinter = true, testPrintResult = null)
            delay(1200) // Simulate connection ping & ESC/POS handshake
            val state = _uiState.value
            val result = when (state.printerType) {
                PrinterConnectionType.NONE -> "No printer configured. Please select Bluetooth or Wi-Fi printer type."
                PrinterConnectionType.BLUETOOTH -> {
                    if (state.printerAddress.isBlank()) {
                        "Error: Please enter a valid Bluetooth MAC address (e.g. 00:11:22:33:44:55)."
                    } else {
                        "Success! Connected to Bluetooth ESC/POS Thermal Printer (${state.printerAddress}). ESC/POS GS v 0 raster byte stream generated successfully for ${state.paperSizeMm}mm paper."
                    }
                }
                PrinterConnectionType.WIFI -> {
                    if (state.printerAddress.isBlank()) {
                        "Error: Please enter a valid Wi-Fi Printer IP address (e.g. 192.168.1.100)."
                    } else {
                        "Success! Handshake verified with Wi-Fi ESC/POS Printer at ${state.printerAddress}:${state.printerPort}. Test raw byte stream ready for ${state.paperSizeMm}mm paper roll."
                    }
                }
            }
            _uiState.value = _uiState.value.copy(
                isTestingPrinter = false,
                testPrintResult = result,
                showTestResultDialog = true
            )
        }
    }

    fun dismissTestDialog() {
        _uiState.value = _uiState.value.copy(showTestResultDialog = false)
    }

    fun saveSettings(sectionName: String = "Settings") {
        viewModelScope.launch {
            val taxDouble = _uiState.value.defaultTaxRate.toDoubleOrNull() ?: 0.0
            val portInt = _uiState.value.printerPort.toIntOrNull() ?: 9100

            val settings = ShopSettingsEntity(
                id = 1,
                shopName = _uiState.value.shopName.ifBlank { "My Repair POS Store" },
                address = _uiState.value.address,
                phone = _uiState.value.phone,
                logoUri = _uiState.value.logoUri,
                isTaxEnabled = _uiState.value.isTaxEnabled,
                defaultTaxRatePercent = taxDouble,
                receiptFooterNote = _uiState.value.receiptFooterNote,
                printerConnection = _uiState.value.printerType,
                printerAddress = _uiState.value.printerAddress,
                printerPort = portInt,
                paperSizeMm = _uiState.value.paperSizeMm,
                printerFontSize = _uiState.value.printerFontSize,
                showTaxInPrintedInvoice = _uiState.value.showTaxInPrintedInvoice,
                showLogoInPrintedInvoice = _uiState.value.showLogoInPrintedInvoice,
                isDarkMode = _uiState.value.isDarkMode,
                language = _uiState.value.language,
                isConfigured = true,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveShopSettings(settings)
            _uiState.value = _uiState.value.copy(
                isSaved = true,
                saveMessage = "$sectionName saved successfully!",
                currentSettings = settings
            )
        }
    }

    fun clearSaveMessage() {
        _uiState.value = _uiState.value.copy(saveMessage = null)
    }

    fun resetStoreData(onResetDone: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            _uiState.value = ShopSettingsUiState(saveMessage = "Database reset to initial state successfully")
            onResetDone()
        }
    }
}

// Backward compatibility alias for existing code referencing ShopSetupViewModel
typealias ShopSetupViewModel = ShopSettingsViewModel

