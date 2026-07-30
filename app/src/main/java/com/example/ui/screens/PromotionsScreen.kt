package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.PromotionEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.UserRole
import com.example.viewmodel.PromotionsViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PromotionsScreen(viewModel: PromotionsViewModel, currentUser: UserEntity? = null) {
    val state by viewModel.uiState.collectAsState()
    val isAdmin = currentUser == null || currentUser.role == UserRole.ADMIN

    var showDialog by remember { mutableStateOf(false) }
    var editingPromo by remember { mutableStateOf<PromotionEntity?>(null) }
    var promoToDelete by remember { mutableStateOf<PromotionEntity?>(null) }

    // Dialog state variables
    var code by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("PERCENTAGE") } // PERCENTAGE or FIXED
    var discountValue by remember { mutableStateOf("10.0") }
    var appliesToScope by remember { mutableStateOf("BOTH") } // BOTH, POS, SERVICE
    var minOrderAmount by remember { mutableStateOf("0.0") }
    var appliesToSpecificItems by remember { mutableStateOf(false) }
    var selectedTargetKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isActive by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }

    // Function to open create/edit dialog
    fun openDialog(promo: PromotionEntity? = null) {
        editingPromo = promo
        if (promo != null) {
            code = promo.code
            title = promo.title
            description = promo.description
            discountType = promo.discountType
            discountValue = (if (promo.discountType == "PERCENTAGE") promo.effectiveDiscountPercent else promo.effectiveFixedDiscount).toString()
            appliesToScope = promo.appliesToScope
            minOrderAmount = promo.minOrderAmount.toString()
            appliesToSpecificItems = promo.appliesToSpecificItems
            selectedTargetKeys = promo.targetItemIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            isActive = promo.isActive
        } else {
            code = ""
            title = ""
            description = ""
            discountType = "PERCENTAGE"
            discountValue = "10.0"
            appliesToScope = "BOTH"
            minOrderAmount = "0.0"
            appliesToSpecificItems = false
            selectedTargetKeys = emptySet()
            isActive = true
        }
        showDialog = true
    }

    // Delete confirmation dialog
    if (promoToDelete != null) {
        AlertDialog(
            onDismissRequest = { promoToDelete = null },
            title = { Text("Delete Promotion") },
            text = { Text("Are you sure you want to delete the promotion '${promoToDelete?.code}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        promoToDelete?.let { viewModel.deletePromotion(it) }
                        promoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { promoToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Promotion Create/Edit Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (editingPromo == null) "Create Promotion Coupon" else "Edit Promotion Coupon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Coupon Code * (e.g. SAVE10)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_promo_code"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title * (e.g. 10% Discount)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_promo_title"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().testTag("add_promo_desc"),
                        minLines = 2
                    )

                    // Discount Type Choice
                    Text("Discount Type:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = discountType == "PERCENTAGE",
                            onClick = {
                                discountType = "PERCENTAGE"
                                if (discountValue.toDoubleOrNull() ?: 0.0 > 100) discountValue = "10.0"
                            },
                            label = { Text("Percentage (%)") },
                            leadingIcon = { Icon(Icons.Filled.Percent, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f).testTag("promo_type_pct")
                        )
                        FilterChip(
                            selected = discountType == "FIXED",
                            onClick = { discountType = "FIXED" },
                            label = { Text("Fixed Flat (Ks / $)") },
                            leadingIcon = { Icon(Icons.Filled.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f).testTag("promo_type_fixed")
                        )
                    }

                    OutlinedTextField(
                        value = discountValue,
                        onValueChange = { discountValue = it },
                        label = {
                            Text(if (discountType == "PERCENTAGE") "Discount Percentage (%) *" else "Fixed Discount Amount (Ks) *")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("add_promo_percent"),
                        singleLine = true
                    )

                    // Applicable Channel Scope
                    Text("Applicable Scope:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = appliesToScope == "BOTH",
                            onClick = { appliesToScope = "BOTH" },
                            label = { Text("POS & Service") }
                        )
                        FilterChip(
                            selected = appliesToScope == "POS",
                            onClick = { appliesToScope = "POS" },
                            label = { Text("POS Checkout Only") }
                        )
                        FilterChip(
                            selected = appliesToScope == "SERVICE",
                            onClick = { appliesToScope = "SERVICE" },
                            label = { Text("Service Orders Only") }
                        )
                    }

                    OutlinedTextField(
                        value = minOrderAmount,
                        onValueChange = { minOrderAmount = it },
                        label = { Text("Minimum Order Subtotal (Ks, Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Option: "Applied to specific items only"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { appliesToSpecificItems = !appliesToSpecificItems }
                    ) {
                        Checkbox(
                            checked = appliesToSpecificItems,
                            onCheckedChange = { appliesToSpecificItems = it },
                            modifier = Modifier.testTag("applies_to_specific_items_checkbox")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Applied to specific items only",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Item Selector List if "Applied to specific items only" is checked
                    if (appliesToSpecificItems) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Select Applicable Items (${selectedTargetKeys.size} selected):",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (selectedTargetKeys.isNotEmpty()) {
                                        TextButton(
                                            onClick = { selectedTargetKeys = emptySet() },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Clear All", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Column(
                                    modifier = Modifier
                                        .height(180.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Products Section
                                    if (state.products.isNotEmpty()) {
                                        Text(
                                            "Inventory Products / Parts:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                        state.products.forEach { prod ->
                                            val key = "PROD_${prod.id}"
                                            val isSelected = selectedTargetKeys.contains(key)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                                                    .clickable {
                                                        selectedTargetKeys = if (isSelected) {
                                                            selectedTargetKeys - key
                                                        } else {
                                                            selectedTargetKeys + key
                                                        }
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = { checked ->
                                                        selectedTargetKeys = if (checked) selectedTargetKeys + key else selectedTargetKeys - key
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(prod.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    Text("${prod.brand} • Ks ${String.format(Locale.US, "%,.2f", prod.unitPrice)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }

                                    // Repair Services Section
                                    if (state.services.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Repair Services:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                        state.services.forEach { srv ->
                                            val key = "SRV_${srv.id}"
                                            val isSelected = selectedTargetKeys.contains(key)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                                                    .clickable {
                                                        selectedTargetKeys = if (isSelected) {
                                                            selectedTargetKeys - key
                                                        } else {
                                                            selectedTargetKeys + key
                                                        }
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = { checked ->
                                                        selectedTargetKeys = if (checked) selectedTargetKeys + key else selectedTargetKeys - key
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(srv.serviceName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    Text("${srv.category} • Base Labor: Ks ${String.format(Locale.US, "%,.2f", srv.baseLaborFee)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Active Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Status:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valDouble = discountValue.toDoubleOrNull() ?: 0.0
                        val minDouble = minOrderAmount.toDoubleOrNull() ?: 0.0
                        if (code.isNotBlank() && title.isNotBlank() && valDouble > 0) {
                            val targetStr = selectedTargetKeys.joinToString(",")
                            viewModel.savePromotion(
                                id = editingPromo?.id ?: 0L,
                                code = code,
                                title = title,
                                description = description,
                                discountType = discountType,
                                discountValue = valDouble,
                                appliesToSpecificItems = appliesToSpecificItems,
                                targetItemIds = targetStr,
                                appliesToScope = appliesToScope,
                                minOrderAmount = minDouble,
                                isActive = isActive
                            )
                            showDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_promo_confirm_btn")
                ) {
                    Text(if (editingPromo == null) "Save Promotion" else "Update Promotion")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Main Promotions List View
    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { openDialog(null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_promo")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Promotion")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Promotions & Coupons",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configure discounts for POS checkout and service orders",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search promotion code or title...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Filled.Close, contentDescription = "Clear") } }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        val filteredPromos = state.promotions.filter { promo ->
            searchQuery.isBlank() || promo.code.contains(searchQuery, ignoreCase = true) || promo.title.contains(searchQuery, ignoreCase = true)
        }

        if (filteredPromos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.promotions.isEmpty()) "No promotions created yet. Click '+' to add your first coupon!" else "No matching promotions found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredPromos) { promo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (promo.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (promo.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = promo.code,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (promo.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = promo.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (promo.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                }

                                if (isAdmin) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { openDialog(promo) },
                                            modifier = Modifier.testTag("edit_promo_${promo.code.lowercase()}")
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit Promotion", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = { promoToDelete = promo },
                                            modifier = Modifier.testTag("delete_promo_${promo.code.lowercase()}")
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete Promotion", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }

                            if (promo.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = promo.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Badges Row
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Discount Value Badge
                                val discLabel = if (promo.discountType == "PERCENTAGE") {
                                    "${promo.effectiveDiscountPercent.toInt()}% OFF"
                                } else {
                                    "Ks ${String.format(Locale.US, "%,.0f", promo.effectiveFixedDiscount)} OFF"
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(discLabel, fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Filled.LocalOffer, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )

                                // Scope Badge
                                val scopeText = when (promo.appliesToScope) {
                                    "POS" -> "POS Only"
                                    "SERVICE" -> "Service Only"
                                    else -> "POS & Service"
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(scopeText) },
                                    icon = { Icon(Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )

                                // Target Level Badge
                                val targetText = if (promo.appliesToSpecificItems) {
                                    val count = promo.targetItemIds.split(",").filter { it.isNotBlank() }.size
                                    "Specific Items Only ($count selected)"
                                } else {
                                    "All Items (Order Level)"
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(targetText) },
                                    icon = { Icon(Icons.Filled.Category, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Active Switch Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (promo.isActive) "Status: Active" else "Status: Inactive",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (promo.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Switch(
                                    checked = promo.isActive,
                                    onCheckedChange = { viewModel.togglePromotionActive(promo) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

