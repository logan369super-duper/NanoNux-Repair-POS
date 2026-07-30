package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.TransactionLogsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionLogsScreen(viewModel: TransactionLogsViewModel) {
    val state by viewModel.uiState.collectAsState()
    var selectedDateFilter by remember { mutableStateOf("All Dates") }
    var selectedNameFilter by remember { mutableStateOf("All Names") }
    var selectedSortOption by remember { mutableStateOf("Newest First") }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val isCompact = maxWidth < 560.dp

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction Logs & Audit History",
                    style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (state.brandFilter.isNotEmpty() || state.customerFilter.isNotEmpty() || state.issueFilter.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearFilters() }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear Filters")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Responsive Filters
            if (isCompact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.brandFilter,
                        onValueChange = { viewModel.updateBrandFilter(it) },
                        label = { Text("Item Brand / Make") },
                        modifier = Modifier.fillMaxWidth().testTag("filter_brand"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.customerFilter,
                        onValueChange = { viewModel.updateCustomerFilter(it) },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth().testTag("filter_customer"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.issueFilter,
                        onValueChange = { viewModel.updateIssueFilter(it) },
                        label = { Text("Issue Type") },
                        modifier = Modifier.fillMaxWidth().testTag("filter_issue"),
                        singleLine = true
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.brandFilter,
                        onValueChange = { viewModel.updateBrandFilter(it) },
                        label = { Text("Item Brand / Make") },
                        modifier = Modifier.fillMaxWidth().testTag("filter_brand"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.customerFilter,
                        onValueChange = { viewModel.updateCustomerFilter(it) },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth().testTag("filter_customer"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.issueFilter,
                        onValueChange = { viewModel.updateIssueFilter(it) },
                        label = { Text("Issue Type") },
                        modifier = Modifier.fillMaxWidth().testTag("filter_issue"),
                        singleLine = true
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

            val finalFilteredTransactions = remember(state.transactions, selectedDateFilter, selectedNameFilter, selectedSortOption) {
                var list = state.transactions
                val now = System.currentTimeMillis()
                val dayMillis = 24 * 60 * 60 * 1000L

                // Date filter
                list = when (selectedDateFilter) {
                    "Today" -> list.filter { it.timestamp >= now - dayMillis }
                    "Last 7 Days" -> list.filter { it.timestamp >= now - (7 * dayMillis) }
                    "Last 30 Days" -> list.filter { it.timestamp >= now - (30 * dayMillis) }
                    else -> list
                }

                // Name filter
                list = when (selectedNameFilter) {
                    "A-M" -> list.filter { 
                        val name = it.customerName ?: ""
                        val firstChar = name.trim().firstOrNull()?.uppercaseChar()
                        firstChar != null && firstChar in 'A'..'M'
                    }
                    "N-Z" -> list.filter { 
                        val name = it.customerName ?: ""
                        val firstChar = name.trim().firstOrNull()?.uppercaseChar()
                        firstChar != null && firstChar in 'N'..'Z'
                    }
                    else -> list
                }

                // Sorting
                list = when (selectedSortOption) {
                    "Newest First" -> list.sortedByDescending { it.timestamp }
                    "Oldest First" -> list.sortedBy { it.timestamp }
                    "Name (A - Z)" -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.customerName ?: "" })
                    "Name (Z - A)" -> list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.customerName ?: "" })
                    else -> list
                }

                list
            }

            if (finalFilteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transaction audit records match your filters.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(finalFilteredTransactions) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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
                                    text = tx.transactionNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Ks ${String.format(Locale.US, "%.2f", tx.totalAmount)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = tx.type,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Date: ${dateFormat.format(Date(tx.timestamp))} | Cashier: ${tx.cashierName} | Method: ${tx.paymentMethod}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (!tx.customerName.isNullOrBlank()) {
                                Text(
                                    text = "Customer: ${tx.customerName} (${tx.customerPhone ?: ""})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Items: ${tx.itemsJson}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
}
