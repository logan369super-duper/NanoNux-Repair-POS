package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.RepairServiceEntity
import com.example.domain.model.CartItem
import com.example.ui.components.ReceiptPreviewDialog
import com.example.viewmodel.PosViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PosCheckoutScreen(
    viewModel: PosViewModel,
    onCheckoutFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var selectedTab by remember { mutableStateOf(0) } // 0: Products, 1: Services

    if (state.receiptText != null) {
        ReceiptPreviewDialog(
            receiptText = state.receiptText!!,
            title = "Final Sales Receipt Preview (${state.lastTransaction?.transactionNumber})",
            fontSizeSp = (state.shopSettings?.printerFontSize ?: 18).toFloat(),
            onDismiss = {
                viewModel.dismissReceipt()
                onCheckoutFinished()
            },
            onPrint = { /* Thermal printer stream execution */ }
        )
    }

    if (isTablet) {
        // Tablet Split View: Catalog on Left (60%), Cart on Right (40%)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1.2f)) {
                CatalogSection(
                    state = state,
                    selectedTab = selectedTab,
                    onTabChange = { selectedTab = it },
                    onAddProduct = { viewModel.addProductToCart(it) },
                    onAddService = { viewModel.addServiceToCart(it) }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                CartSection(
                    state = state,
                    viewModel = viewModel
                )
            }
        }
    } else {
        // Mobile Stacked View: Catalog & Cart
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Parts & Goods") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Labor Services") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Cart (${state.cartItems.sumOf { it.quantity }})") })
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> CatalogSection(state = state, selectedTab = 0, onTabChange = { selectedTab = it }, onAddProduct = { viewModel.addProductToCart(it) }, onAddService = { viewModel.addServiceToCart(it) })
                1 -> CatalogSection(state = state, selectedTab = 1, onTabChange = { selectedTab = it }, onAddProduct = { viewModel.addProductToCart(it) }, onAddService = { viewModel.addServiceToCart(it) })
                2 -> CartSection(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun CatalogSection(
    state: com.example.viewmodel.PosUiState,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    onAddProduct: (ProductEntity) -> Unit,
    onAddService: (RepairServiceEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Inventory & Service Catalog",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { onTabChange(0) },
                    label = { Text("Parts/Goods") },
                    modifier = Modifier.testTag("pos_tab_parts")
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { onTabChange(1) },
                    label = { Text("Labor Services") },
                    modifier = Modifier.testTag("pos_tab_services")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.availableProducts) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddProduct(product) }
                                .testTag("pos_item_prod_${product.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "${product.brand} ${product.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Model: ${product.model} | Stock: ${product.stockQuantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (product.stockQuantity <= product.minStockThreshold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Ks ${String.format(Locale.US, "%.2f", product.unitPrice)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { onAddProduct(product) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.AddShoppingCart, contentDescription = "Add", modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.availableServices) { service ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddService(service) }
                                .testTag("pos_item_service_${service.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = service.serviceName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Category: ${service.category} (~${service.estimatedTimeMinutes} mins)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Ks ${String.format(Locale.US, "%.2f", service.baseLaborFee)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    IconButton(
                                        onClick = { onAddService(service) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Add Labor", modifier = Modifier.size(20.dp))
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CartSection(
    state: com.example.viewmodel.PosUiState,
    viewModel: PosViewModel
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Cart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (state.cartItems.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearCart() }) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            var showCustomerFields by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showCustomerFields = !showCustomerFields },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.customerName.isNotBlank()) "Customer: ${state.customerName}" else "Link Customer (Optional)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = if (showCustomerFields) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (showCustomerFields) {
                        Spacer(modifier = Modifier.height(8.dp))

                        if (state.availableCustomers.isNotEmpty()) {
                            Text("Quick Select:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(state.availableCustomers.take(5)) { cust ->
                                    AssistChip(
                                        onClick = {
                                            viewModel.setCustomerDetails(cust.name, cust.phone)
                                        },
                                        label = { Text(cust.name, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedTextField(
                            value = state.customerName,
                            onValueChange = { viewModel.setCustomerDetails(it, state.customerPhone) },
                            label = { Text("Customer Name") },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth().testTag("pos_customer_name")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = state.customerPhone,
                            onValueChange = { viewModel.setCustomerDetails(state.customerName, it) },
                            label = { Text("Customer Phone") },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth().testTag("pos_customer_phone")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cart is empty. Click items on the left to add.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.cartItems) { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ks ${String.format(Locale.US, "%.2f", item.unitPrice * item.quantity)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.updateQuantity(item.id, item.quantity - 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = "${item.quantity}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateQuantity(item.id, item.quantity + 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Promotions Picker
            if (state.availablePromotions.isNotEmpty()) {
                Text("Apply Active Promotion / Coupon:", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.availablePromotions.forEach { promo ->
                        FilterChip(
                            selected = state.appliedPromotion?.id == promo.id,
                            onClick = {
                                if (state.appliedPromotion?.id == promo.id) {
                                    viewModel.applyPromotion(null)
                                } else {
                                    viewModel.applyPromotion(promo)
                                }
                            },
                            label = {
                                val desc = if (promo.discountType == "PERCENTAGE") {
                                    "${promo.code} (${promo.effectiveDiscountPercent.toInt()}%)"
                                } else {
                                    "${promo.code} (Ks ${promo.effectiveFixedDiscount.toInt()})"
                                }
                                Text(desc)
                            },
                            modifier = Modifier.testTag("promo_chip_${promo.code.lowercase()}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Payment Method Selector
            Text("Payment Method:", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("CASH", "CARD", "DIGITAL_WALLET").forEach { method ->
                    FilterChip(
                        selected = state.paymentMethod == method,
                        onClick = { viewModel.setPaymentMethod(method) },
                        label = { Text(method.replace("_", " ")) },
                        modifier = Modifier.testTag("payment_method_${method.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calculation Breakdown
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal:", style = MaterialTheme.typography.bodySmall)
                    Text("Ks ${String.format(Locale.US, "%.2f", state.subtotal)}", style = MaterialTheme.typography.bodySmall)
                }
                if (state.discount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        Text("-Ks ${String.format(Locale.US, "%.2f", state.discount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tax (${state.taxRatePercent}%):", style = MaterialTheme.typography.bodySmall)
                    Text("Ks ${String.format(Locale.US, "%.2f", state.taxAmount)}", style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL DUE:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Ks ${String.format(Locale.US, "%.2f", state.grandTotal)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.checkout() },
                enabled = state.cartItems.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("complete_checkout_btn"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.ShoppingCartCheckout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Process Payment & Print Receipt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
