package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ServiceOrderEntity
import com.example.data.local.entity.TransactionLogEntity
import com.example.domain.model.RepairTicketStatus
import com.example.viewmodel.CustomersViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomersScreen(viewModel: CustomersViewModel) {
    val state by viewModel.uiState.collectAsState()

    var selectedDateFilter by remember { mutableStateOf("All Dates") }
    var selectedNameFilter by remember { mutableStateOf("All Names") }
    var selectedSortOption by remember { mutableStateOf("Newest First") }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }
    var viewingHistoryCustomer by remember { mutableStateOf<CustomerEntity?>(null) }

    // Dialog state
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    fun openDialog(customer: CustomerEntity? = null) {
        editingCustomer = customer
        if (customer != null) {
            name = customer.name
            phone = customer.phone
            email = customer.email
            address = customer.address
        } else {
            name = ""
            phone = ""
            email = ""
            address = ""
        }
        showEditDialog = true
    }

    // Delete confirmation dialog
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Customer Record") },
            text = { Text("Are you sure you want to remove '${customerToDelete?.name}' from customer directory? Past transactions will remain logged.") },
            confirmButton = {
                Button(
                    onClick = {
                        customerToDelete?.let { viewModel.deleteCustomer(it) }
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Customer Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (editingCustomer == null) "Add New Customer" else "Edit Customer Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name *") },
                        placeholder = { Text("e.g. Alex Morgan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("customer_input_name")
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("e.g. 0912345678") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("customer_input_phone")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address (Optional)") },
                        placeholder = { Text("e.g. alex@example.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("customer_input_email")
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address / Location (Optional)") },
                        placeholder = { Text("e.g. Downtown Branch #12") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("customer_input_address")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.saveCustomer(
                                id = editingCustomer?.id ?: 0L,
                                name = name,
                                phone = phone,
                                email = email,
                                address = address
                            )
                            showEditDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_customer_btn")
                ) {
                    Text(if (editingCustomer == null) "Create Customer" else "Update Customer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Viewing Customer Purchase & Service History Dialog
    if (viewingHistoryCustomer != null) {
        val cust = viewingHistoryCustomer!!

        // Match transactions and orders by customer name or phone
        val custTxns = state.transactions.filter { tx ->
            val matchesName = !tx.customerName.isNullOrBlank() && tx.customerName.contains(cust.name, ignoreCase = true)
            val matchesPhone = cust.phone.isNotBlank() && !tx.customerPhone.isNullOrBlank() && tx.customerPhone == cust.phone
            matchesName || matchesPhone
        }

        val custOrders = state.serviceOrders.filter { ord ->
            val matchesName = ord.customerName.contains(cust.name, ignoreCase = true)
            val matchesPhone = cust.phone.isNotBlank() && ord.customerPhone == cust.phone
            matchesName || matchesPhone
        }

        CustomerHistoryDialog(
            customer = cust,
            transactions = custTxns,
            serviceOrders = custOrders,
            onDismiss = { viewingHistoryCustomer = null }
        )
    }

    // Main Customers Screen Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Customer Directory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track customer profiles, POS purchases & service ticket history",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FloatingActionButton(
                onClick = { openDialog(null) },
                modifier = Modifier.testTag("fab_add_customer")
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = "Add Customer")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Summary Statistics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val totalSpentAll = state.customers.sumOf { it.totalSpent }
            val totalOrdersAll = state.customers.sumOf { it.totalOrdersCount }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Clients", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("${state.customers.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Visits/Orders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Text("$totalOrdersAll", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    Text("Ks ${String.format(Locale.US, "%,.0f", totalSpentAll)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search by name, phone, email or address...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = if (state.searchQuery.isNotEmpty()) {
                { IconButton(onClick = { viewModel.updateSearchQuery("") }) { Icon(Icons.Filled.Close, contentDescription = "Clear") } }
            } else null,
            modifier = Modifier.fillMaxWidth().testTag("search_customer_field"),
            singleLine = true
        )

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
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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

        val finalFilteredCustomers = remember(state.customers, state.searchQuery, selectedDateFilter, selectedNameFilter, selectedSortOption) {
            var list = state.customers.filter { cust ->
                val q = state.searchQuery.trim().lowercase()
                q.isEmpty() ||
                        cust.name.lowercase().contains(q) ||
                        cust.phone.lowercase().contains(q) ||
                        cust.email.lowercase().contains(q) ||
                        cust.address.lowercase().contains(q)
            }
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

        if (finalFilteredCustomers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.PeopleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.customers.isEmpty()) "No customers registered yet. Customers created during service order intake or checkout will automatically appear here!" else "No matching customers found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(finalFilteredCustomers) { customer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                    // Avatar Initial Circle
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = customer.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = customer.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (customer.phone.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Filled.Phone,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = customer.phone,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { openDialog(customer) },
                                        modifier = Modifier.testTag("edit_customer_${customer.id}")
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit Customer", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = { customerToDelete = customer },
                                        modifier = Modifier.testTag("delete_customer_${customer.id}")
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete Customer", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            if (customer.email.isNotBlank() || customer.address.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    if (customer.email.isNotBlank()) {
                                        Text("Email: ${customer.email}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (customer.address.isNotBlank()) {
                                        Text("Address: ${customer.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Badges Row
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Orders: ${customer.totalOrdersCount}", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )

                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Spent: Ks ${String.format(Locale.US, "%,.2f", customer.totalSpent)}", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Filled.Payments, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )

                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Last Visit: ${dateFormat.format(Date(customer.lastVisit))}") },
                                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // History Action Button
                            Button(
                                onClick = { viewingHistoryCustomer = customer },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("view_history_customer_${customer.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Icon(
                                    Icons.Filled.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "View Purchases & Service Orders",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerHistoryDialog(
    customer: CustomerEntity,
    transactions: List<TransactionLogEntity>,
    serviceOrders: List<ServiceOrderEntity>,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Services, 1 = POS Purchases
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (customer.phone.isNotBlank()) {
                    Text("Phone: ${customer.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                // Summary bar
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Services", style = MaterialTheme.typography.labelSmall)
                            Text("${serviceOrders.size}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("POS Sales", style = MaterialTheme.typography.labelSmall)
                            Text("${transactions.size}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Value", style = MaterialTheme.typography.labelSmall)
                            Text("Ks ${String.format(Locale.US, "%,.0f", customer.totalSpent)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Services (${serviceOrders.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("POS Sales (${transactions.size})") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        // Service Orders List
                        if (serviceOrders.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No service orders recorded for this customer.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(serviceOrders) { order ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(order.ticketNumber, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = when (order.status) {
                                                        RepairTicketStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                                                        RepairTicketStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                                    }
                                                ) {
                                                    Text(
                                                        text = order.status.name,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("${order.deviceBrand} ${order.deviceModel}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text("Issue: ${order.issueDescription}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(dateFormat.format(Date(order.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                                Text("Est. Cost: Ks ${String.format(Locale.US, "%,.2f", order.estimatedCost)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // POS Purchases List
                        if (transactions.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No POS checkout sales recorded for this customer.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(transactions) { tx ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(tx.transactionNumber, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                                Text("Method: ${tx.paymentMethod}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Items: ${tx.itemsJson}", style = MaterialTheme.typography.bodySmall)

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(dateFormat.format(Date(tx.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                                Text("Paid: Ks ${String.format(Locale.US, "%,.2f", tx.totalAmount)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
