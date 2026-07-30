package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CatalogCategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UnitMeasurementEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.UserRole
import com.example.i18n.LocalStrings
import com.example.viewmodel.InventoryViewModel
import com.example.viewmodel.StockAdjustMode
import java.util.Locale

import androidx.compose.material.icons.automirrored.filled.ArrowBack

object InventorySubRoute {
    const val HUB = "inventory_hub"
    const val ITEMS = "inventory_items"
    const val CATALOG = "inventory_catalog"
    const val UNITS = "inventory_units"
}

@Composable
fun InventoryScreen(viewModel: InventoryViewModel, currentUser: UserEntity? = null) {
    val navController = rememberNavController()
    val isAdmin = currentUser == null || currentUser.role == UserRole.ADMIN

    NavHost(
        navController = navController,
        startDestination = InventorySubRoute.HUB,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(InventorySubRoute.HUB) {
            InventoryHubScreen(
                viewModel = viewModel,
                onNavigateToItems = { navController.navigate(InventorySubRoute.ITEMS) },
                onNavigateToCatalog = { navController.navigate(InventorySubRoute.CATALOG) },
                onNavigateToUnits = { navController.navigate(InventorySubRoute.UNITS) }
            )
        }

        composable(InventorySubRoute.ITEMS) {
            InventoryItemsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                isAdmin = isAdmin
            )
        }

        composable(InventorySubRoute.CATALOG) {
            InventoryCatalogScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                isAdmin = isAdmin
            )
        }

        composable(InventorySubRoute.UNITS) {
            InventoryUnitsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                isAdmin = isAdmin
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryHubScreen(
    viewModel: InventoryViewModel,
    onNavigateToItems: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToUnits: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Hero Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = strings.storageInventoryTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.storageSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "STORAGE & INVENTORY SECTIONS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Items & Stock Card
        InventoryCategoryCard(
            title = strings.itemsAndStock,
            subtitle = "View physical stock, filter low stock, and manage details",
            statusBadge = "${state.products.size} Items",
            badgeColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Filled.Inventory2,
            onClick = onNavigateToItems,
            testTag = "nav_card_inventory_items"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Catalog Tree Card
        InventoryCategoryCard(
            title = strings.catalogTree,
            subtitle = "Manage category nesting, catalog hierarchy, and groupings",
            statusBadge = "${state.categories.size} Categories",
            badgeColor = MaterialTheme.colorScheme.secondary,
            icon = Icons.Filled.Category,
            onClick = onNavigateToCatalog,
            testTag = "nav_card_inventory_catalog"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Custom Units Card
        InventoryCategoryCard(
            title = strings.customUnits,
            subtitle = "Define physical/digital units of measurement",
            statusBadge = "${state.units.size} Units",
            badgeColor = MaterialTheme.colorScheme.tertiary,
            icon = Icons.Filled.Straighten,
            onClick = onNavigateToUnits,
            testTag = "nav_card_inventory_units"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InventoryCategoryCard(
    title: String,
    subtitle: String,
    statusBadge: String,
    badgeColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusBadge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryItemsScreen(viewModel: InventoryViewModel, onBack: () -> Unit, isAdmin: Boolean = true) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.itemsAndStock) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp)) {
            ItemsAndStockTab(viewModel = viewModel, isAdmin = isAdmin)
        }
    }

    if (state.isItemDialogOpen) {
        ItemCreateEditDialog(
            product = state.editingProduct,
            categories = state.categories,
            units = state.units,
            onDismiss = { viewModel.closeItemDialog() },
            onSave = { id, sku, name, brand, model, type, unitPrice, costPrice, stock, minStock, catId, unitCode ->
                viewModel.saveProduct(id, sku, name, brand, model, type, unitPrice, costPrice, stock, minStock, catId, unitCode)
            }
        )
    }

    if (state.isStockAdjustDialogOpen && state.selectedProductForStock != null) {
        StockAdjustDialog(
            product = state.selectedProductForStock!!,
            mode = state.stockAdjustMode,
            qtyInput = state.stockAdjustQty,
            noteInput = state.stockAdjustNote,
            onModeChange = { viewModel.updateStockAdjustState(mode = it) },
            onQtyChange = { viewModel.updateStockAdjustState(qty = it) },
            onNoteChange = { viewModel.updateStockAdjustState(note = it) },
            onConfirm = { viewModel.confirmStockAdjust() },
            onDismiss = { viewModel.closeStockAdjustDialog() }
        )
    }

    if (state.deletingProduct != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteItem() },
            title = { Text("Delete Inventory Item?") },
            text = { Text("Are you sure you want to delete '${state.deletingProduct?.brand} ${state.deletingProduct?.name}'? This operation cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeDeleteItem() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteItem() }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryCatalogScreen(viewModel: InventoryViewModel, onBack: () -> Unit, isAdmin: Boolean = true) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.catalogTree) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp)) {
            CatalogTreeTab(viewModel = viewModel, isAdmin = isAdmin)
        }
    }

    if (state.isCategoryDialogOpen) {
        CategoryCreateEditDialog(
            category = state.editingCategory,
            parentForNew = state.parentCategoryForNew,
            allCategories = state.categories,
            onDismiss = { viewModel.closeCategoryDialog() },
            onSave = { id, name, desc, parentId ->
                viewModel.saveCategory(id, name, desc, parentId)
            }
        )
    }

    if (state.deletingCategory != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteCategory() },
            title = { Text("Delete Catalog Category?") },
            text = { Text("Are you sure you want to delete category '${state.deletingCategory?.name}'? Sub-categories or items under it will remain but become root.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeDeleteCategory() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Category")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteCategory() }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryUnitsScreen(viewModel: InventoryViewModel, onBack: () -> Unit, isAdmin: Boolean = true) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.customUnits) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp)) {
            CustomUnitsTab(viewModel = viewModel, isAdmin = isAdmin)
        }
    }

    if (state.isUnitDialogOpen) {
        UnitCreateEditDialog(
            unit = state.editingUnit,
            onDismiss = { viewModel.closeUnitDialog() },
            onSave = { id, name, code, allowDecimal ->
                viewModel.saveUnit(id, name, code, allowDecimal)
            }
        )
    }

    if (state.deletingUnit != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteUnit() },
            title = { Text("Delete Custom Unit?") },
            text = { Text("Are you sure you want to delete custom unit '${state.deletingUnit?.name} (${state.deletingUnit?.code})'?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeDeleteUnit() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Unit")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteUnit() }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// TAB 1: ITEMS & STOCK
// ==========================================
@Composable
private fun ItemsAndStockTab(viewModel: InventoryViewModel, isAdmin: Boolean = true) {
    val state by viewModel.uiState.collectAsState()

    // Filter products
    val filteredProducts = remember(state.products, state.searchQuery, state.selectedCategoryId, state.showLowStockOnly) {
        state.products.filter { prod ->
            val matchesQuery = state.searchQuery.isBlank() ||
                    prod.name.contains(state.searchQuery, ignoreCase = true) ||
                    prod.brand.contains(state.searchQuery, ignoreCase = true) ||
                    prod.sku.contains(state.searchQuery, ignoreCase = true) ||
                    prod.model.contains(state.searchQuery, ignoreCase = true)

            val matchesCat = state.selectedCategoryId == null || prod.categoryId == state.selectedCategoryId
            val matchesLow = !state.showLowStockOnly || prod.stockQuantity <= prod.minStockThreshold

            matchesQuery && matchesCat && matchesLow
        }
    }

    val isCompact = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 600

    var selectedDateFilter by remember { mutableStateOf("All Dates") }
    var selectedNameFilter by remember { mutableStateOf("All Names") }
    var selectedSortOption by remember { mutableStateOf("Newest First") }

    val finalFilteredProducts = remember(filteredProducts, selectedDateFilter, selectedNameFilter, selectedSortOption) {
        var list = filteredProducts
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L

        // Date filter
        list = when (selectedDateFilter) {
            "Today" -> list.filter { it.createdAt >= now - dayMillis }
            "Last 7 Days" -> list.filter { it.createdAt >= now - (7 * dayMillis) }
            "Last 30 Days" -> list.filter { it.createdAt >= now - (30 * dayMillis) }
            else -> list
        }

        // Name filter
        list = when (selectedNameFilter) {
            "A-M" -> list.filter { 
                val firstChar = it.name.trim().firstOrNull()?.uppercaseChar()
                firstChar != null && firstChar in 'A'..'M'
            }
            "N-Z" -> list.filter { 
                val firstChar = it.name.trim().firstOrNull()?.uppercaseChar()
                firstChar != null && firstChar in 'N'..'Z'
            }
            else -> list
        }

        // Sorting
        list = when (selectedSortOption) {
            "Newest First" -> list.sortedByDescending { it.createdAt }
            "Oldest First" -> list.sortedBy { it.createdAt }
            "Name (A - Z)" -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            "Name (Z - A)" -> list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name })
            else -> list
        }

        list
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Controls Row / Column
        if (isCompact) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Search SKU, Name, Brand...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inventory_search_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Filter Dropdown
                var categoryMenuExpanded by remember { mutableStateOf(false) }
                val selectedCatName = remember(state.selectedCategoryId, state.categories) {
                    if (state.selectedCategoryId == null) "All Categories"
                    else state.categories.find { it.id == state.selectedCategoryId }?.name ?: "Category"
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { categoryMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(selectedCatName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                viewModel.selectCategoryFilter(null)
                                categoryMenuExpanded = false
                            }
                        )
                        Divider()
                        state.categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    viewModel.selectCategoryFilter(cat.id)
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Add Item FAB / Button
                if (isAdmin) {
                    Button(
                        onClick = { viewModel.openCreateItemDialog() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_new_item_btn")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Item")
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search SKU, Name, Brand...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("inventory_search_input"),
                    singleLine = true
                )

                // Category Filter Dropdown
                var categoryMenuExpanded by remember { mutableStateOf(false) }
                val selectedCatName = remember(state.selectedCategoryId, state.categories) {
                    if (state.selectedCategoryId == null) "All Categories"
                    else state.categories.find { it.id == state.selectedCategoryId }?.name ?: "Category"
                }

                Box {
                    OutlinedButton(onClick = { categoryMenuExpanded = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(selectedCatName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                viewModel.selectCategoryFilter(null)
                                categoryMenuExpanded = false
                            }
                        )
                        Divider()
                        state.categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    viewModel.selectCategoryFilter(cat.id)
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Add Item FAB / Button
                if (isAdmin) {
                    Button(
                        onClick = { viewModel.openCreateItemDialog() },
                        modifier = Modifier.testTag("add_new_item_btn")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Item")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = state.showLowStockOnly,
                onClick = { viewModel.toggleLowStockFilter() },
                label = { Text("Low Stock Warning Only") },
                leadingIcon = if (state.showLowStockOnly) {
                    { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                } else null
            )

            if (state.selectedCategoryId != null) {
                AssistChip(
                    onClick = { viewModel.selectCategoryFilter(null) },
                    label = { Text("Clear Category Filter X") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter & Sort Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date Filter Dropdown
            var dateMenuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Card(
                    onClick = { dateMenuExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Date: $selectedDateFilter",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = dateMenuExpanded,
                    onDismissRequest = { dateMenuExpanded = false }
                ) {
                    listOf("All Dates", "Today", "Last 7 Days", "Last 30 Days").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                    selectedDateFilter = option
                                    dateMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Name Filter Dropdown
            var nameMenuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Card(
                    onClick = { nameMenuExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Name: $selectedNameFilter",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = nameMenuExpanded,
                    onDismissRequest = { nameMenuExpanded = false }
                ) {
                    listOf("All Names", "A-M", "N-Z").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                    selectedNameFilter = option
                                    nameMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Sort Dropdown
            var sortMenuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Card(
                    onClick = { sortMenuExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sort: $selectedSortOption",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    listOf("Newest First", "Oldest First", "Name (A - Z)", "Name (Z - A)").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                    selectedSortOption = option
                                    sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Item List
        if (finalFilteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No inventory items found", style = MaterialTheme.typography.titleMedium)
                    Text("Click '+ New Item' to add items and stock", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(finalFilteredProducts, key = { it.id }) { product ->
                    ProductItemCard(
                        product = product,
                        categoryName = state.categories.find { it.id == product.categoryId }?.name,
                        isAdmin = isAdmin,
                        onStockAdjust = { viewModel.openStockAdjustDialog(product) },
                        onEdit = { viewModel.openEditItemDialog(product) },
                        onDelete = { viewModel.confirmDeleteItem(product) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductItemCard(
    product: ProductEntity,
    categoryName: String?,
    isAdmin: Boolean = true,
    onStockAdjust: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isLowStock = product.stockQuantity <= product.minStockThreshold

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header Row: Brand + Name & Category Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${product.brand} ${product.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = product.type,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (!categoryName.isNull_or_blank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = categoryName ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Quantity badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer
                ) {
                    val formattedQty = formatStockQuantity(product.stockQuantity)
                    Text(
                        text = "Stock: $formattedQty ${product.unitCode}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub details: SKU, Model
            Text(
                text = "SKU: ${product.sku} | Model: ${product.model.ifEmpty { "N/A" }} | Min Alert: ${formatStockQuantity(product.minStockThreshold)} ${product.unitCode}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Action Row: Pricing & CRUD Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("Retail Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Ks ${String.format(Locale.US, "%.2f", product.unitPrice)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    if (isAdmin) {
                        Column {
                            Text("Cost Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Ks ${String.format(Locale.US, "%.2f", product.costPrice)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (isAdmin) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onStockAdjust,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("stock_adjust_btn_${product.id}")
                        ) {
                            Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stock In/Out", style = MaterialTheme.typography.labelMedium)
                        }

                        IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_product_${product.id}")) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Item", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_product_${product.id}")) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Item", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: UNLIMITED CATALOG CATEGORY TREE
// ==========================================
@Composable
private fun CatalogTreeTab(viewModel: InventoryViewModel, isAdmin: Boolean = true) {
    val state by viewModel.uiState.collectAsState()

    // Build hierarchy tree map
    val rootCategories = remember(state.categories) {
        state.categories.filter { it.parentId == null }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Catalog Categories Hierarchy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Unlimited parent-child nested categories", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (isAdmin) {
                Button(onClick = { viewModel.openCreateCategoryDialog(null) }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Root Category")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No categories found. Click 'Root Category' to create one.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(rootCategories, key = { it.id }) { rootCat ->
                    CategoryTreeNodeItem(
                        category = rootCat,
                        allCategories = state.categories,
                        depth = 0,
                        isAdmin = isAdmin,
                        onAddSubCategory = { parent -> viewModel.openCreateCategoryDialog(parent) },
                        onEdit = { cat -> viewModel.openEditCategoryDialog(cat) },
                        onDelete = { cat -> viewModel.confirmDeleteCategory(cat) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTreeNodeItem(
    category: CatalogCategoryEntity,
    allCategories: List<CatalogCategoryEntity>,
    depth: Int,
    isAdmin: Boolean = true,
    onAddSubCategory: (CatalogCategoryEntity) -> Unit,
    onEdit: (CatalogCategoryEntity) -> Unit,
    onDelete: (CatalogCategoryEntity) -> Unit
) {
    val children = remember(allCategories, category.id) {
        allCategories.filter { it.parentId == category.id }
    }
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 20).dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (depth == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (children.isNotEmpty()) {
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight,
                                contentDescription = "Expand/Collapse"
                            )
                        }
                    } else if (depth > 0) {
                        Icon(
                            imageVector = Icons.Filled.SubdirectoryArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 4.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = category.name,
                            style = if (depth == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                            fontWeight = if (depth == 0) FontWeight.Bold else FontWeight.SemiBold
                        )
                        if (category.description.isNotBlank()) {
                            Text(
                                text = category.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isAdmin) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { onAddSubCategory(category) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Sub-Cat", style = MaterialTheme.typography.labelSmall)
                        }

                        IconButton(onClick = { onEdit(category) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = { onDelete(category) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Render nested children recursively
        if (children.isNotEmpty() && isExpanded) {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                children.forEach { childCat ->
                    CategoryTreeNodeItem(
                        category = childCat,
                        allCategories = allCategories,
                        depth = depth + 1,
                        isAdmin = isAdmin,
                        onAddSubCategory = onAddSubCategory,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 3: CUSTOM MEASUREMENT UNITS
// ==========================================
@Composable
private fun CustomUnitsTab(viewModel: InventoryViewModel, isAdmin: Boolean = true) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Custom Unit Creation & Setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Create custom units like item, kg, device, box, set, meter, etc.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (isAdmin) {
                Button(onClick = { viewModel.openCreateUnitDialog() }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Unit")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.units.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No custom units found. Click 'New Unit' to add one.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.units, key = { it.id }) { unit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = unit.code,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(unit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (unit.allowDecimal) "Supports decimal quantities (e.g. 2.5 ${unit.code})" else "Integer quantity only",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isAdmin) {
                                Row {
                                    IconButton(onClick = { viewModel.openEditUnitDialog(unit) }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.confirmDeleteUnit(unit) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ITEM CREATE / EDIT DIALOG
// ==========================================
@Composable
private fun ItemCreateEditDialog(
    product: ProductEntity?,
    categories: List<CatalogCategoryEntity>,
    units: List<UnitMeasurementEntity>,
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> Unit
) {
    var sku by remember { mutableStateOf(product?.sku ?: "SKU-${System.currentTimeMillis().toString().takeLast(6)}") }
    var name by remember { mutableStateOf(product?.name ?: "") }
    var brand by remember { mutableStateOf(product?.brand ?: "") }
    var model by remember { mutableStateOf(product?.model ?: "") }
    var type by remember { mutableStateOf(product?.type ?: "PART") }
    var unitPriceStr by remember { mutableStateOf(product?.unitPrice?.toString() ?: "0.0") }
    var costPriceStr by remember { mutableStateOf(product?.costPrice?.toString() ?: "0.0") }
    var stockStr by remember { mutableStateOf(product?.stockQuantity?.let { formatStockQuantity(it) } ?: "10") }
    var minStockStr by remember { mutableStateOf(product?.minStockThreshold?.let { formatStockQuantity(it) } ?: "5") }
    var selectedCategoryId by remember { mutableStateOf(product?.categoryId) }
    var selectedUnitCode by remember { mutableStateOf(product?.unitCode ?: "pcs") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Create New Inventory Item" else "Edit Item: ${product.name}") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Item Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("item_dialog_name")
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU / Barcode *") },
                            modifier = Modifier.weight(1f).testTag("item_dialog_sku")
                        )
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand") },
                            modifier = Modifier.weight(1f).testTag("item_dialog_brand")
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it },
                            label = { Text("Model / Spec") },
                            modifier = Modifier.weight(1f).testTag("item_dialog_model")
                        )
                        OutlinedTextField(
                            value = type,
                            onValueChange = { type = it },
                            label = { Text("Type (e.g. PART, HARDWARE)") },
                            modifier = Modifier.weight(1f).testTag("item_dialog_type")
                        )
                    }
                }

                // Category Selector
                item {
                    var catExpanded by remember { mutableStateOf(false) }
                    val currentCatName = categories.find { it.id == selectedCategoryId }?.name ?: "Select Catalog Category"

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = currentCatName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Catalog Category") },
                            trailingIcon = {
                                IconButton(onClick = { catExpanded = true }) {
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { catExpanded = true }
                        )

                        DropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (Uncategorized)") },
                                onClick = {
                                    selectedCategoryId = null
                                    catExpanded = false
                                }
                            )
                            Divider()
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategoryId = cat.id
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Custom Unit Selector
                item {
                    var unitExpanded by remember { mutableStateOf(false) }
                    val currentUnitName = units.find { it.code == selectedUnitCode }?.let { "${it.name} (${it.code})" } ?: selectedUnitCode

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = currentUnitName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Measurement Unit") },
                            trailingIcon = {
                                IconButton(onClick = { unitExpanded = true }) {
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { unitExpanded = true }
                        )

                        DropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.name} (${u.code})") },
                                    onClick = {
                                        selectedUnitCode = u.code
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Prices & Stock
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = unitPriceStr,
                            onValueChange = { unitPriceStr = it },
                            label = { Text("Retail Price (Ks)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("item_dialog_price")
                        )
                        OutlinedTextField(
                            value = costPriceStr,
                            onValueChange = { costPriceStr = it },
                            label = { Text("Cost Price (Ks)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("item_dialog_cost")
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            label = { Text("Current Stock ($selectedUnitCode)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("item_dialog_stock")
                        )
                        OutlinedTextField(
                            value = minStockStr,
                            onValueChange = { minStockStr = it },
                            label = { Text("Min Stock Alert") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("item_dialog_min_stock")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || sku.isBlank()) return@Button
                    val uPrice = unitPriceStr.toDoubleOrNull() ?: 0.0
                    val cPrice = costPriceStr.toDoubleOrNull() ?: 0.0
                    val stock = stockStr.toDoubleOrNull() ?: 0.0
                    val minStock = minStockStr.toDoubleOrNull() ?: 5.0

                    onSave(
                        product?.id ?: 0L,
                        sku.trim(),
                        name.trim(),
                        brand.trim(),
                        model.trim(),
                        type.trim().ifEmpty { "PART" },
                        uPrice,
                        cPrice,
                        stock,
                        minStock,
                        selectedCategoryId,
                        selectedUnitCode
                    )
                },
                modifier = Modifier.testTag("save_item_dialog_btn")
            ) {
                Text(if (product == null) "Create Item" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ==========================================
// STOCK ADJUSTMENT DIALOG
// ==========================================
@Composable
private fun StockAdjustDialog(
    product: ProductEntity,
    mode: StockAdjustMode,
    qtyInput: String,
    noteInput: String,
    onModeChange: (StockAdjustMode) -> Unit,
    onQtyChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stock Management: ${product.brand} ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Current Stock: ${formatStockQuantity(product.stockQuantity)} ${product.unitCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                // Mode Tabs
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = mode == StockAdjustMode.STOCK_IN,
                        onClick = { onModeChange(StockAdjustMode.STOCK_IN) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text("Stock In (+)")
                    }
                    SegmentedButton(
                        selected = mode == StockAdjustMode.STOCK_OUT,
                        onClick = { onModeChange(StockAdjustMode.STOCK_OUT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text("Stock Out (-)")
                    }
                    SegmentedButton(
                        selected = mode == StockAdjustMode.SET_EXACT,
                        onClick = { onModeChange(StockAdjustMode.SET_EXACT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text("Set Exact (=)")
                    }
                }

                OutlinedTextField(
                    value = qtyInput,
                    onValueChange = onQtyChange,
                    label = {
                        Text(
                            when (mode) {
                                StockAdjustMode.STOCK_IN -> "Quantity to Add (${product.unitCode})"
                                StockAdjustMode.STOCK_OUT -> "Quantity to Remove (${product.unitCode})"
                                StockAdjustMode.SET_EXACT -> "New Total Stock (${product.unitCode})"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("stock_adjust_qty_input")
                )

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = onNoteChange,
                    label = { Text("Reason / Reference Note (Optional)") },
                    placeholder = { Text("e.g. Supplier invoice #88, Damaged unit, Inventory recount") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_stock_adjust_btn")
            ) {
                Text("Confirm Stock Adjustment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ==========================================
// CATEGORY CREATE / EDIT DIALOG
// ==========================================
@Composable
private fun CategoryCreateEditDialog(
    category: CatalogCategoryEntity?,
    parentForNew: CatalogCategoryEntity?,
    allCategories: List<CatalogCategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (id: Long, name: String, desc: String, parentId: Long?) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var description by remember { mutableStateOf(category?.description ?: "") }
    var selectedParentId by remember { mutableStateOf(category?.parentId ?: parentForNew?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when {
                    category != null -> "Edit Category: ${category.name}"
                    parentForNew != null -> "Add Sub-Category under '${parentForNew.name}'"
                    else -> "Create Root Catalog Category"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("category_name_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Parent selector
                var parentExpanded by remember { mutableStateOf(false) }
                val currentParentName = allCategories.find { it.id == selectedParentId }?.name ?: "None (Root Category)"

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentParentName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Parent Category") },
                        trailingIcon = {
                            IconButton(onClick = { parentExpanded = true }) {
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { parentExpanded = true }
                    )

                    DropdownMenu(
                        expanded = parentExpanded,
                        onDismissRequest = { parentExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (Root Category)") },
                            onClick = {
                                selectedParentId = null
                                parentExpanded = false
                            }
                        )
                        Divider()
                        allCategories.filter { it.id != category?.id }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedParentId = cat.id
                                    parentExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    onSave(category?.id ?: 0L, name.trim(), description.trim(), selectedParentId)
                },
                modifier = Modifier.testTag("save_category_btn")
            ) {
                Text(if (category == null) "Create Category" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ==========================================
// UNIT CREATE / EDIT DIALOG
// ==========================================
@Composable
private fun UnitCreateEditDialog(
    unit: UnitMeasurementEntity?,
    onDismiss: () -> Unit,
    onSave: (id: Long, name: String, code: String, allowDecimal: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(unit?.name ?: "") }
    var code by remember { mutableStateOf(unit?.code ?: "") }
    var allowDecimal by remember { mutableStateOf(unit?.allowDecimal ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (unit == null) "Create Custom Unit" else "Edit Unit: ${unit.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Unit Full Name * (e.g. Kilogram, Piece)") },
                    modifier = Modifier.fillMaxWidth().testTag("unit_name_input")
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Unit Code / Symbol * (e.g. kg, pcs, device, m)") },
                    modifier = Modifier.fillMaxWidth().testTag("unit_code_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow Decimal Quantities", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Enable for weights or lengths like 1.5 kg, 2.7 m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = allowDecimal,
                        onCheckedChange = { allowDecimal = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || code.isBlank()) return@Button
                    onSave(unit?.id ?: 0L, name.trim(), code.trim().lowercase(), allowDecimal)
                },
                modifier = Modifier.testTag("save_unit_btn")
            ) {
                Text(if (unit == null) "Create Unit" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Utility Helpers
private fun formatStockQuantity(qty: Double): String {
    return if (qty % 1.0 == 0.0) {
        qty.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", qty)
    }
}

private fun String?.isNull_or_blank(): Boolean = this.isNullOrBlank()
