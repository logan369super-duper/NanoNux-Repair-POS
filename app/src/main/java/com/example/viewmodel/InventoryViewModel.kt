package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CatalogCategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UnitMeasurementEntity
import com.example.data.repository.PosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StockAdjustMode { STOCK_IN, STOCK_OUT, SET_EXACT }

data class InventoryUiState(
    val selectedTab: Int = 0, // 0: Items & Stock, 1: Catalog Categories, 2: Custom Units
    val products: List<ProductEntity> = emptyList(),
    val lowStockProducts: List<ProductEntity> = emptyList(),
    val categories: List<CatalogCategoryEntity> = emptyList(),
    val units: List<UnitMeasurementEntity> = emptyList(),
    
    // Filters
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val showLowStockOnly: Boolean = false,

    // Item Dialogs
    val isItemDialogOpen: Boolean = false,
    val editingProduct: ProductEntity? = null,
    val deletingProduct: ProductEntity? = null,

    // Stock Adjust Dialog
    val isStockAdjustDialogOpen: Boolean = false,
    val selectedProductForStock: ProductEntity? = null,
    val stockAdjustMode: StockAdjustMode = StockAdjustMode.STOCK_IN,
    val stockAdjustQty: String = "10",
    val stockAdjustNote: String = "",

    // Category Dialogs
    val isCategoryDialogOpen: Boolean = false,
    val editingCategory: CatalogCategoryEntity? = null,
    val parentCategoryForNew: CatalogCategoryEntity? = null,
    val deletingCategory: CatalogCategoryEntity? = null,

    // Unit Dialogs
    val isUnitDialogOpen: Boolean = false,
    val editingUnit: UnitMeasurementEntity? = null,
    val deletingUnit: UnitMeasurementEntity? = null,

    val errorMessage: String? = null
)

class InventoryViewModel(private val repository: PosRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Load Products
        viewModelScope.launch {
            repository.getAllProducts().collect { list ->
                _uiState.value = _uiState.value.copy(products = list)
            }
        }
        viewModelScope.launch {
            repository.getLowStockProducts().collect { lowStock ->
                _uiState.value = _uiState.value.copy(lowStockProducts = lowStock)
            }
        }

        // Load Categories
        viewModelScope.launch {
            repository.getAllCategories().collect { catList ->
                if (catList.isEmpty()) {
                    seedDefaultCategories()
                } else {
                    _uiState.value = _uiState.value.copy(categories = catList)
                }
            }
        }

        // Load Custom Units
        viewModelScope.launch {
            repository.getAllUnits().collect { unitList ->
                if (unitList.isEmpty()) {
                    seedDefaultUnits()
                } else {
                    _uiState.value = _uiState.value.copy(units = unitList)
                }
            }
        }
    }

    private suspend fun seedDefaultCategories() {
        val rootElec = repository.insertCategory(
            CatalogCategoryEntity(name = "Electronics & Parts", description = "Electronic components and phone/laptop spares")
        )
        repository.insertCategory(
            CatalogCategoryEntity(parentId = rootElec, name = "Display & Touch Screens", description = "LCD, OLED and Digitizers")
        )
        repository.insertCategory(
            CatalogCategoryEntity(parentId = rootElec, name = "Batteries & Power", description = "Internal batteries & power ICs")
        )

        val rootAcc = repository.insertCategory(
            CatalogCategoryEntity(name = "Accessories & Consumables", description = "Cables, chargers, thermal paste, wire")
        )
        repository.insertCategory(
            CatalogCategoryEntity(parentId = rootAcc, name = "Cables & Adapters", description = "USB-C, HDMI, power cords")
        )
        repository.insertCategory(
            CatalogCategoryEntity(parentId = rootAcc, name = "Soldering & Chemicals", description = "Flux, solder wire, isopropyl alcohol")
        )
    }

    private suspend fun seedDefaultUnits() {
        val defaultUnits = listOf(
            UnitMeasurementEntity(name = "Piece", code = "pcs", allowDecimal = false),
            UnitMeasurementEntity(name = "Item", code = "item", allowDecimal = false),
            UnitMeasurementEntity(name = "Kilogram", code = "kg", allowDecimal = true),
            UnitMeasurementEntity(name = "Device / Unit", code = "device", allowDecimal = false),
            UnitMeasurementEntity(name = "Meter", code = "m", allowDecimal = true),
            UnitMeasurementEntity(name = "Box", code = "box", allowDecimal = false),
            UnitMeasurementEntity(name = "Set", code = "set", allowDecimal = false),
            UnitMeasurementEntity(name = "Liter", code = "L", allowDecimal = true)
        )
        defaultUnits.forEach { repository.insertUnit(it) }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectCategoryFilter(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun toggleLowStockFilter() {
        _uiState.value = _uiState.value.copy(showLowStockOnly = !_uiState.value.showLowStockOnly)
    }

    // --- ITEM CRUD ---
    fun openCreateItemDialog() {
        _uiState.value = _uiState.value.copy(
            isItemDialogOpen = true,
            editingProduct = null
        )
    }

    fun openEditItemDialog(product: ProductEntity) {
        _uiState.value = _uiState.value.copy(
            isItemDialogOpen = true,
            editingProduct = product
        )
    }

    fun closeItemDialog() {
        _uiState.value = _uiState.value.copy(
            isItemDialogOpen = false,
            editingProduct = null
        )
    }

    fun saveProduct(
        id: Long,
        sku: String,
        name: String,
        brand: String,
        model: String,
        type: String,
        unitPrice: Double,
        costPrice: Double,
        stockQuantity: Double,
        minStockThreshold: Double,
        categoryId: Long?,
        unitCode: String
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                id = id,
                sku = sku,
                name = name,
                brand = brand,
                model = model,
                type = type,
                unitPrice = unitPrice,
                costPrice = costPrice,
                stockQuantity = stockQuantity,
                minStockThreshold = minStockThreshold,
                categoryId = categoryId,
                unitCode = unitCode
            )
            if (id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
            closeItemDialog()
        }
    }

    fun confirmDeleteItem(product: ProductEntity) {
        _uiState.value = _uiState.value.copy(deletingProduct = product)
    }

    fun cancelDeleteItem() {
        _uiState.value = _uiState.value.copy(deletingProduct = null)
    }

    fun executeDeleteItem() {
        val prod = _uiState.value.deletingProduct ?: return
        viewModelScope.launch {
            repository.deleteProduct(prod)
            _uiState.value = _uiState.value.copy(deletingProduct = null)
        }
    }

    // --- STOCK ADJUSTMENT ---
    fun openStockAdjustDialog(product: ProductEntity, mode: StockAdjustMode = StockAdjustMode.STOCK_IN) {
        _uiState.value = _uiState.value.copy(
            isStockAdjustDialogOpen = true,
            selectedProductForStock = product,
            stockAdjustMode = mode,
            stockAdjustQty = if (mode == StockAdjustMode.SET_EXACT) product.stockQuantity.toString() else "10",
            stockAdjustNote = ""
        )
    }

    fun closeStockAdjustDialog() {
        _uiState.value = _uiState.value.copy(
            isStockAdjustDialogOpen = false,
            selectedProductForStock = null
        )
    }

    fun updateStockAdjustState(mode: StockAdjustMode? = null, qty: String? = null, note: String? = null) {
        _uiState.value = _uiState.value.copy(
            stockAdjustMode = mode ?: _uiState.value.stockAdjustMode,
            stockAdjustQty = qty ?: _uiState.value.stockAdjustQty,
            stockAdjustNote = note ?: _uiState.value.stockAdjustNote
        )
    }

    fun confirmStockAdjust() {
        val product = _uiState.value.selectedProductForStock ?: return
        val qtyInput = _uiState.value.stockAdjustQty.toDoubleOrNull() ?: return
        val mode = _uiState.value.stockAdjustMode

        viewModelScope.launch {
            val delta = when (mode) {
                StockAdjustMode.STOCK_IN -> qtyInput
                StockAdjustMode.STOCK_OUT -> -qtyInput
                StockAdjustMode.SET_EXACT -> qtyInput - product.stockQuantity
            }
            repository.updateStock(product.id, delta)
            closeStockAdjustDialog()
        }
    }

    // --- CATALOG CATEGORY CRUD ---
    fun openCreateCategoryDialog(parent: CatalogCategoryEntity? = null) {
        _uiState.value = _uiState.value.copy(
            isCategoryDialogOpen = true,
            editingCategory = null,
            parentCategoryForNew = parent
        )
    }

    fun openEditCategoryDialog(category: CatalogCategoryEntity) {
        _uiState.value = _uiState.value.copy(
            isCategoryDialogOpen = true,
            editingCategory = category,
            parentCategoryForNew = null
        )
    }

    fun closeCategoryDialog() {
        _uiState.value = _uiState.value.copy(
            isCategoryDialogOpen = false,
            editingCategory = null,
            parentCategoryForNew = null
        )
    }

    fun saveCategory(id: Long, name: String, description: String, parentId: Long?) {
        viewModelScope.launch {
            val category = CatalogCategoryEntity(
                id = id,
                parentId = parentId,
                name = name,
                description = description
            )
            if (id == 0L) {
                repository.insertCategory(category)
            } else {
                repository.updateCategory(category)
            }
            closeCategoryDialog()
        }
    }

    fun confirmDeleteCategory(category: CatalogCategoryEntity) {
        _uiState.value = _uiState.value.copy(deletingCategory = category)
    }

    fun cancelDeleteCategory() {
        _uiState.value = _uiState.value.copy(deletingCategory = null)
    }

    fun executeDeleteCategory() {
        val cat = _uiState.value.deletingCategory ?: return
        viewModelScope.launch {
            repository.deleteCategory(cat)
            _uiState.value = _uiState.value.copy(deletingCategory = null)
        }
    }

    // --- CUSTOM UNIT CRUD ---
    fun openCreateUnitDialog() {
        _uiState.value = _uiState.value.copy(
            isUnitDialogOpen = true,
            editingUnit = null
        )
    }

    fun openEditUnitDialog(unit: UnitMeasurementEntity) {
        _uiState.value = _uiState.value.copy(
            isUnitDialogOpen = true,
            editingUnit = unit
        )
    }

    fun closeUnitDialog() {
        _uiState.value = _uiState.value.copy(
            isUnitDialogOpen = false,
            editingUnit = null
        )
    }

    fun saveUnit(id: Long, name: String, code: String, allowDecimal: Boolean) {
        viewModelScope.launch {
            val unit = UnitMeasurementEntity(
                id = id,
                name = name,
                code = code,
                allowDecimal = allowDecimal
            )
            if (id == 0L) {
                repository.insertUnit(unit)
            } else {
                repository.updateUnit(unit)
            }
            closeUnitDialog()
        }
    }

    fun confirmDeleteUnit(unit: UnitMeasurementEntity) {
        _uiState.value = _uiState.value.copy(deletingUnit = unit)
    }

    fun cancelDeleteUnit() {
        _uiState.value = _uiState.value.copy(deletingUnit = null)
    }

    fun executeDeleteUnit() {
        val u = _uiState.value.deletingUnit ?: return
        viewModelScope.launch {
            repository.deleteUnit(u)
            _uiState.value = _uiState.value.copy(deletingUnit = null)
        }
    }
}
