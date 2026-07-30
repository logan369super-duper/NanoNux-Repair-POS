package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.config.IdentifierConfig
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ServiceOrderEntity
import com.example.domain.model.RepairTicketStatus
import com.example.i18n.LocalStrings
import com.example.ui.components.StatusBadge
import com.example.viewmodel.ServiceOrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceOrderListScreen(
    viewModel: ServiceOrderViewModel,
    onNavigateToIntake: () -> Unit,
    onNavigateToBrands: () -> Unit = {},
    onNavigateToRepairSpecs: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTicketForDetail by remember { mutableStateOf<ServiceOrderEntity?>(null) }

    var selectedDateFilter by remember { mutableStateOf("All Dates") }
    var selectedNameFilter by remember { mutableStateOf("All Names") }
    var selectedSortOption by remember { mutableStateOf("Newest First") }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US) }

    selectedTicketForDetail?.let { ticket ->
        val currentTicket = state.orders.find { it.id == ticket.id } ?: ticket

        AlertDialog(
            onDismissRequest = { selectedTicketForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentTicket.ticketNumber,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        StatusBadge(status = currentTicket.status)
                    }
                    IconButton(
                        onClick = { selectedTicketForDetail = null },
                        modifier = Modifier.testTag("close_ticket_detail_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Change Section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TICKET STATUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentTicket.status.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            var expandedDetailStatus by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { expandedDetailStatus = true },
                                    modifier = Modifier.testTag("detail_status_dropdown_${currentTicket.id}")
                                ) {
                                    Text("Change Status")
                                }
                                DropdownMenu(
                                    expanded = expandedDetailStatus,
                                    onDismissRequest = { expandedDetailStatus = false }
                                ) {
                                    RepairTicketStatus.values().forEach { st ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    StatusBadge(status = st)
                                                    if (st == currentTicket.status) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("✓", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            },
                                            onClick = {
                                                viewModel.updateOrderStatus(currentTicket.id, st)
                                                expandedDetailStatus = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Timestamps Section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "TIMESTAMPS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Created: ${dateFormat.format(Date(currentTicket.createdAt))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Last Updated: ${dateFormat.format(Date(currentTicket.updatedAt))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Customer Details
                    Column {
                        Text(
                            text = "Customer Details",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Name: ${currentTicket.customerName}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Phone: ${currentTicket.customerPhone}", style = MaterialTheme.typography.bodyMedium)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Device Details
                    Column {
                        Text(
                            text = "Device Information",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Brand: ${currentTicket.deviceBrand}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Model: ${currentTicket.deviceModel}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "${IdentifierConfig.LABEL}: ${currentTicket.serialImei}", style = MaterialTheme.typography.bodyMedium)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Fault & Description
                    Column {
                        Text(
                            text = "Issue & Description",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Issue Category: ${currentTicket.issueType}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentTicket.issueDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Financial Overview
                    Column {
                        Text(
                            text = "Financial Summary",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimated Cost:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Ks ${String.format(Locale.US, "%.2f", currentTicket.estimatedCost)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Labor Fee:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Ks ${String.format(Locale.US, "%.2f", currentTicket.laborFee)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Deposit Paid:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Ks ${String.format(Locale.US, "%.2f", currentTicket.depositPaid)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { selectedTicketForDetail = null },
                    modifier = Modifier.testTag("dismiss_ticket_detail_dialog")
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add New Repair Ticket",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.customerName,
                        onValueChange = { viewModel.updateCustomerName(it) },
                        label = { Text("Customer Name *") },
                        placeholder = { Text("e.g. John Doe") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_ticket_customer_name"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.deviceModel,
                        onValueChange = { viewModel.updateDeviceModel(it) },
                        label = { Text("Device Model *") },
                        placeholder = { Text("e.g. iPhone 13, Galaxy S21") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_ticket_device_model"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.issueDescription,
                        onValueChange = { viewModel.updateIssueDescription(it) },
                        label = { Text("Issue Description") },
                        placeholder = { Text("e.g. Cracked screen, won't charge") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_ticket_issue_description"),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = state.customerPhone,
                        onValueChange = { viewModel.updateCustomerPhone(it) },
                        label = { Text("Customer Phone (Optional)") },
                        placeholder = { Text("e.g. (555) 019-2834") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_ticket_customer_phone"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (state.customerName.isNotBlank() && state.deviceModel.isNotBlank()) {
                            viewModel.createIntakeTicket()
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_ticket_button")
                ) {
                    Text("Create Ticket")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false },
                    modifier = Modifier.testTag("cancel_add_ticket_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_ticket")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Repair Ticket")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.repairTickets,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                var expandedMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { expandedMenu = true },
                        modifier = Modifier.testTag("repair_options_button")
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Repair Options")
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Manage Brands") },
                            leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null) },
                            onClick = {
                                expandedMenu = false
                                onNavigateToBrands()
                            },
                            modifier = Modifier.testTag("menu_manage_brands")
                        )
                        DropdownMenuItem(
                            text = { Text("Manage Fault Presets") },
                            leadingIcon = { Icon(Icons.Filled.Build, contentDescription = null) },
                            onClick = {
                                expandedMenu = false
                                onNavigateToRepairSpecs()
                            },
                            modifier = Modifier.testTag("menu_manage_repair_specs")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Search by Customer, Phone, IMEI or Ticket #") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("ticket_search_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("status_filter_bar")
            ) {
                item {
                    FilterChip(
                        selected = state.selectedStatusFilter == null,
                        onClick = { viewModel.onStatusFilterChange(null) },
                        label = { Text("All (${state.orders.size})") },
                        modifier = Modifier.testTag("filter_chip_all")
                    )
                }
                items(RepairTicketStatus.values()) { status ->
                    val count = state.orders.count { it.status == status }
                    FilterChip(
                        selected = state.selectedStatusFilter == status,
                        onClick = {
                            if (state.selectedStatusFilter == status) {
                                viewModel.onStatusFilterChange(null)
                            } else {
                                viewModel.onStatusFilterChange(status)
                            }
                        },
                        label = { Text("${status.displayName} ($count)") },
                        modifier = Modifier.testTag("filter_chip_${status.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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

            val finalFilteredOrders = remember(state.filteredOrders, selectedDateFilter, selectedNameFilter, selectedSortOption) {
                var list = state.filteredOrders
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
                        val firstChar = it.customerName.trim().firstOrNull()?.uppercaseChar()
                        firstChar != null && firstChar in 'A'..'M'
                    }
                    "N-Z" -> list.filter { 
                        val firstChar = it.customerName.trim().firstOrNull()?.uppercaseChar()
                        firstChar != null && firstChar in 'N'..'Z'
                    }
                    else -> list
                }

                // Sorting
                list = when (selectedSortOption) {
                    "Newest First" -> list.sortedByDescending { it.createdAt }
                    "Oldest First" -> list.sortedBy { it.createdAt }
                    "Name (A - Z)" -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.customerName })
                    "Name (Z - A)" -> list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.customerName })
                    else -> list
                }

                list
            }

            if (finalFilteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No repair tickets match your filters.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(finalFilteredOrders) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("repair_ticket_item_${order.id}")
                                .clickable { selectedTicketForDetail = order },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = order.ticketNumber,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Ks ${String.format(Locale.US, "%.2f", order.estimatedCost)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                StatusBadge(status = order.status)

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Customer: ${order.customerName} (${order.customerPhone})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Item: ${order.deviceBrand} ${order.deviceModel} | ${IdentifierConfig.LABEL}: ${order.serialImei}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Fault: ${order.issueType} - ${order.issueDescription}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Status Update Menu
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Update Status:", style = MaterialTheme.typography.labelSmall)

                                    var expandedStatus by remember { mutableStateOf(false) }
                                    Box {
                                        OutlinedButton(
                                            onClick = { expandedStatus = true },
                                            modifier = Modifier.testTag("status_dropdown_${order.id}")
                                        ) {
                                            Text(order.status.displayName)
                                        }
                                        DropdownMenu(
                                            expanded = expandedStatus,
                                            onDismissRequest = { expandedStatus = false }
                                        ) {
                                            RepairTicketStatus.values().forEach { st ->
                                                DropdownMenuItem(
                                                    text = { Text(st.displayName) },
                                                    onClick = {
                                                        viewModel.updateOrderStatus(order.id, st)
                                                        expandedStatus = false
                                                    }
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
        }
    }
}
