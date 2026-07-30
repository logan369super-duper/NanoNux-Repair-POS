package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.config.IdentifierConfig
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromotionEntity
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.ReceiptPreviewDialog
import com.example.viewmodel.ReplacementPartOption
import com.example.viewmodel.ServiceOrderViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServiceOrderIntakeScreen(
    viewModel: ServiceOrderViewModel,
    onViewTicketList: () -> Unit,
    onNavigateToBrands: () -> Unit = {},
    onNavigateToRepairSpecs: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showBarcodeScanner by remember { mutableStateOf(false) }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onDismissRequest = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                viewModel.updateSerialImei(scannedCode)
                showBarcodeScanner = false
            }
        )
    }

    if (state.claimTicketText != null) {
        ReceiptPreviewDialog(
            receiptText = state.claimTicketText!!,
            title = "Claim Ticket Preview (${state.newlyCreatedOrder?.ticketNumber})",
            fontSizeSp = (state.shopSettings?.printerFontSize ?: 18).toFloat(),
            onDismiss = { viewModel.dismissSuccess() },
            onPrint = { /* Simulated printing */ }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val isCompact = maxWidth < 560.dp

        Card(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(if (isCompact) 16.dp else 24.dp)
                    .fillMaxWidth()
            ) {
                // Top Header Bar - Fully Responsive
                if (isCompact) {
                    Column {
                        Text(
                            text = "INTAKE MANAGEMENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "New Service Order",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Build,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "INTAKE MANAGEMENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "New Service Order Intake",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!state.errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 1: Customer Details
                Text(
                    text = "1. Customer Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (state.availableCustomers.isNotEmpty()) {
                    Text(
                        text = "Quick Select Existing Customer:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.availableCustomers.take(10)) { cust ->
                            AssistChip(
                                onClick = {
                                    viewModel.updateCustomerName(cust.name)
                                    if (cust.phone.isNotBlank()) viewModel.updateCustomerPhone(cust.phone)
                                },
                                label = { Text(cust.name, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isCompact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.customerName,
                            onValueChange = { viewModel.updateCustomerName(it) },
                            label = { Text("Customer Name *") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().testTag("intake_customer_name"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.customerPhone,
                            onValueChange = { viewModel.updateCustomerPhone(it) },
                            label = { Text("Phone Number *") },
                            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("intake_customer_phone"),
                            singleLine = true
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.customerName,
                            onValueChange = { viewModel.updateCustomerName(it) },
                            label = { Text("Customer Name *") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            modifier = Modifier.weight(1f).testTag("intake_customer_name"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.customerPhone,
                            onValueChange = { viewModel.updateCustomerPhone(it) },
                            label = { Text("Phone Number *") },
                            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f).testTag("intake_customer_phone"),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Item & Issue Details
                Text(
                    text = "2. Repair Item & Fault Specification",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                val brands = state.brandsList
                var expandedBrand by remember { mutableStateOf(false) }

                Text(
                    text = "Brand / Manufacturer Selection",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (isCompact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedBrand,
                            onExpandedChange = { expandedBrand = !expandedBrand },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = state.deviceBrand,
                                onValueChange = { viewModel.updateDeviceBrand(it) },
                                label = { Text("Brand / Manufacturer *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBrand) },
                                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("intake_device_brand"),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = expandedBrand,
                                onDismissRequest = { expandedBrand = false }
                            ) {
                                brands.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b) },
                                        onClick = {
                                            viewModel.updateDeviceBrand(b)
                                            expandedBrand = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = state.deviceModel,
                            onValueChange = { viewModel.updateDeviceModel(it) },
                            label = { Text("Item / Model Name * (e.g. Laptop XPS, Bike, Washer)") },
                            leadingIcon = { Icon(Icons.Filled.Build, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().testTag("intake_device_model"),
                            singleLine = true
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedBrand,
                            onExpandedChange = { expandedBrand = !expandedBrand },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = state.deviceBrand,
                                onValueChange = { viewModel.updateDeviceBrand(it) },
                                label = { Text("Brand / Manufacturer *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBrand) },
                                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("intake_device_brand"),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = expandedBrand,
                                onDismissRequest = { expandedBrand = false }
                            ) {
                                brands.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b) },
                                        onClick = {
                                            viewModel.updateDeviceBrand(b)
                                            expandedBrand = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = state.deviceModel,
                            onValueChange = { viewModel.updateDeviceModel(it) },
                            label = { Text("Item / Model Name * (e.g. Laptop XPS, Bike, Washer)") },
                            leadingIcon = { Icon(Icons.Filled.Build, contentDescription = null) },
                            modifier = Modifier.weight(1f).testTag("intake_device_model"),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.serialImei,
                    onValueChange = { viewModel.updateSerialImei(it) },
                    label = { Text(IdentifierConfig.LABEL_OPTIONAL) },
                    leadingIcon = { Icon(Icons.Filled.QrCode, contentDescription = null) },
                    trailingIcon = {
                        IconButton(
                            onClick = { showBarcodeScanner = true },
                            modifier = Modifier.testTag("scan_serial_barcode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Scan Barcode with Camera",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("intake_serial_imei"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Common Issue Preset:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                val issueTypes = state.issueTypesList
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    issueTypes.forEach { issue ->
                        FilterChip(
                            selected = state.issueType == issue,
                            onClick = { viewModel.updateIssueType(issue) },
                            label = { Text(issue, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("issue_chip_${issue.lowercase().take(6)}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.issueDescription,
                    onValueChange = { viewModel.updateIssueDescription(it) },
                    label = { Text("Detailed Issue Description & Physical Condition") },
                    modifier = Modifier.fillMaxWidth().testTag("intake_description"),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Costing & Part Allocation
                Text(
                    text = "3. Pricing & Part Allocation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (isCompact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.laborFee,
                            onValueChange = { viewModel.updateLaborFee(it) },
                            label = { Text("Labor Fee (Ks)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("intake_labor_fee"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.additionalCost,
                            onValueChange = { viewModel.updateAdditionalCost(it) },
                            label = { Text("Additional Cost (Ks)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("intake_additional_cost"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.depositPaid,
                            onValueChange = { viewModel.updateDepositPaid(it) },
                            label = { Text("Deposit Paid (Ks)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("intake_deposit_paid"),
                            singleLine = true
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.laborFee,
                            onValueChange = { viewModel.updateLaborFee(it) },
                            label = { Text("Labor Fee (Ks)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("intake_labor_fee"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.additionalCost,
                            onValueChange = { viewModel.updateAdditionalCost(it) },
                            label = { Text("Additional Cost (Ks)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("intake_additional_cost"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.depositPaid,
                            onValueChange = { viewModel.updateDepositPaid(it) },
                            label = { Text("Deposit Paid (Ks)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("intake_deposit_paid"),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Replacement Part Source Option
                Text(
                    text = "Add Item / Replacement Part Source:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = state.replacementPartOption == ReplacementPartOption.NONE,
                        onClick = { viewModel.updateReplacementPartOption(ReplacementPartOption.NONE) },
                        label = { Text("None (Service Only)", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("part_opt_none")
                    )
                    FilterChip(
                        selected = state.replacementPartOption == ReplacementPartOption.SHOP_INVENTORY,
                        onClick = { viewModel.updateReplacementPartOption(ReplacementPartOption.SHOP_INVENTORY) },
                        label = { Text("Add Item from Inventory", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("part_opt_shop")
                    )
                    FilterChip(
                        selected = state.replacementPartOption == ReplacementPartOption.CUSTOMER_PROVIDED,
                        onClick = { viewModel.updateReplacementPartOption(ReplacementPartOption.CUSTOMER_PROVIDED) },
                        label = { Text("Customer Provided Part", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("part_opt_customer")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (state.replacementPartOption) {
                    ReplacementPartOption.SHOP_INVENTORY -> {
                        var expandedParts by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedParts,
                            onExpandedChange = { expandedParts = !expandedParts },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = state.selectedPart?.let { "${it.brand} ${it.name} - Ks ${String.format(Locale.US, "%,.2f", it.unitPrice)}" } ?: "Select Part from Inventory",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Inventory Part *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedParts) },
                                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("intake_allocated_part")
                            )
                            ExposedDropdownMenu(
                                expanded = expandedParts,
                                onDismissRequest = { expandedParts = false }
                            ) {
                                state.availableParts.forEach { part ->
                                    DropdownMenuItem(
                                        text = { Text("${part.brand} ${part.name} - Ks ${String.format(Locale.US, "%,.2f", part.unitPrice)} (Stock: ${part.stockQuantity})") },
                                        onClick = {
                                            viewModel.selectPart(part)
                                            expandedParts = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    ReplacementPartOption.CUSTOMER_PROVIDED -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.customerProvidedPartName,
                                onValueChange = { viewModel.updateCustomerProvidedPartName(it) },
                                label = { Text("Customer Provided Part Name / Notes (e.g., Own Display/Screen)") },
                                modifier = Modifier.fillMaxWidth().testTag("customer_provided_part_input"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ℹ️ Customer provided their own replacement item. No inventory stock charge applied (Ks 0.00).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                    ReplacementPartOption.NONE -> { /* No extra fields */ }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PROMOTION SELECTION FOR SERVICE ORDERS
                if (state.availablePromotions.isNotEmpty()) {
                    Text(
                        text = "Apply Active Promotion / Coupon:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        state.availablePromotions.forEach { promo ->
                            val isSelected = state.appliedPromotion?.id == promo.id
                            val labelText = if (promo.discountType == "PERCENTAGE") {
                                "${promo.code} (${promo.effectiveDiscountPercent.toInt()}%)"
                            } else {
                                "${promo.code} (Ks ${promo.effectiveFixedDiscount.toInt()})"
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        viewModel.applyPromotion(null)
                                    } else {
                                        viewModel.applyPromotion(promo)
                                    }
                                },
                                label = { Text(labelText, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("service_promo_chip_${promo.code.lowercase()}")
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // TOTAL PRICE & PAYMENT BREAKDOWN CARD
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ORDER COST SUMMARY",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val laborVal = state.laborFee.toDoubleOrNull() ?: 0.0
                        val addVal = state.additionalCost.toDoubleOrNull() ?: 0.0
                        val partVal = state.calculatedPartCost
                        val discountVal = state.discountAmount
                        val totalVal = state.calculatedTotalPrice
                        val depositVal = state.depositPaid.toDoubleOrNull() ?: 0.0
                        val balanceVal = state.balanceDue

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Labor Fee:", style = MaterialTheme.typography.bodyMedium)
                            Text("Ks ${String.format(Locale.US, "%,.2f", laborVal)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        if (addVal > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Additional Cost:", style = MaterialTheme.typography.bodyMedium)
                                Text("Ks ${String.format(Locale.US, "%,.2f", addVal)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (state.replacementPartOption != ReplacementPartOption.NONE) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Replacement Item:", style = MaterialTheme.typography.bodyMedium)
                                val itemText = if (state.replacementPartOption == ReplacementPartOption.SHOP_INVENTORY) {
                                    "Ks ${String.format(Locale.US, "%,.2f", partVal)}"
                                } else {
                                    "Customer Provided (Ks 0.00)"
                                }
                                Text(itemText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (discountVal > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Promotion Discount (${state.appliedPromotion?.code}):", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                Text("- Ks ${String.format(Locale.US, "%,.2f", discountVal)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL PRICE:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                            Text(
                                "Ks ${String.format(Locale.US, "%,.2f", totalVal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (depositVal > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Deposit Paid:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                                Text("- Ks ${String.format(Locale.US, "%,.2f", depositVal)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Balance Due on Pickup:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Ks ${String.format(Locale.US, "%,.2f", balanceVal)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { viewModel.createIntakeTicket() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("create_intake_ticket_button"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Claim Ticket & Save Intake",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

