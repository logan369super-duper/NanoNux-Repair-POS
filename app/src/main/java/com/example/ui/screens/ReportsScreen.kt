package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionLogEntity
import com.example.i18n.LocalStrings
import com.example.ui.components.StatCard
import com.example.viewmodel.DailyReportItem
import com.example.viewmodel.MonthlyReportItem
import com.example.viewmodel.ReportsViewModel
import com.example.viewmodel.WeeklyReportItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    val tabs = listOf(
        "Overview" to Icons.Filled.Assessment,
        "Per Transaction" to Icons.Filled.Receipt,
        "Per Day" to Icons.Filled.Today,
        "Per Week" to Icons.Filled.DateRange,
        "Per Month" to Icons.Filled.CalendarMonth
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.Assessment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = strings.reports,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Financial performance & breakdown analysis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tab Navigation Bar
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            divider = {}
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.onTabSelected(index) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = title, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        }

        // Tab Content Display
        Box(modifier = Modifier.fillMaxSize()) {
            when (state.selectedTab) {
                0 -> OverviewTabContent(state = state)
                1 -> PerTransactionTabContent(
                    state = state,
                    onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) }
                )
                2 -> PerDayTabContent(dailyReports = state.dailyReports)
                3 -> PerWeekTabContent(weeklyReports = state.weeklyReports)
                4 -> PerMonthTabContent(monthlyReports = state.monthlyReports)
            }
        }
    }
}

@Composable
private fun OverviewTabContent(state: com.example.viewmodel.ReportsUiState) {
    val summary = state.summary

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isCompact) {
                StatCard(
                    title = "Gross Revenue",
                    value = "Ks ${String.format(Locale.US, "%.2f", summary.totalRevenue)}",
                    subtitle = "Total Sales Income",
                    icon = Icons.Filled.AttachMoney,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    title = "Net Profit",
                    value = "Ks ${String.format(Locale.US, "%.2f", summary.netProfit)}",
                    subtitle = "After COGS & Overhead",
                    icon = Icons.Filled.TrendingUp,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    title = "Cost of Goods (COGS)",
                    value = "Ks ${String.format(Locale.US, "%.2f", summary.totalCostOfGoods)}",
                    subtitle = "Parts & Materials Cost",
                    icon = Icons.Filled.ShoppingCart,
                    iconColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    title = "Labor Income",
                    value = "Ks ${String.format(Locale.US, "%.2f", summary.totalLaborRevenue)}",
                    subtitle = "Service Charges",
                    icon = Icons.Filled.Build,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Gross Revenue",
                        value = "Ks ${String.format(Locale.US, "%.2f", summary.totalRevenue)}",
                        subtitle = "Total Sales Income",
                        icon = Icons.Filled.AttachMoney,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Net Profit",
                        value = "Ks ${String.format(Locale.US, "%.2f", summary.netProfit)}",
                        subtitle = "After COGS & Overhead",
                        icon = Icons.Filled.TrendingUp,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Cost of Goods (COGS)",
                        value = "Ks ${String.format(Locale.US, "%.2f", summary.totalCostOfGoods)}",
                        subtitle = "Parts & Materials Cost",
                        icon = Icons.Filled.ShoppingCart,
                        iconColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Labor Income",
                        value = "Ks ${String.format(Locale.US, "%.2f", summary.totalLaborRevenue)}",
                        subtitle = "Service Charges",
                        icon = Icons.Filled.Build,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Financial Breakdown Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total POS Sales Count:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${summary.totalSalesCount}", fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Repairs Completed:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${summary.totalRepairsCompleted}", fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gross Profit (Revenue - COGS):", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "Ks ${String.format(Locale.US, "%.2f", summary.grossProfit)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerTransactionTabContent(
    state: com.example.viewmodel.ReportsUiState,
    onSearchQueryChanged: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }
    val txns = state.filteredTransactions

    val totalRev = txns.sumOf { it.totalAmount }
    val totalCogs = txns.sumOf { it.cogs }
    val totalProfit = totalRev - totalCogs

    val isCompact = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 600

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Stats bar
        if (isCompact) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search by Txn #, Customer, Payment...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Txns", style = MaterialTheme.typography.labelSmall)
                        Text("${txns.size}", fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Filtered Rev", style = MaterialTheme.typography.labelSmall)
                        Text("Ks ${String.format(Locale.US, "%.2f", totalRev)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Search by Txn #, Customer, Payment...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text("Txns", style = MaterialTheme.typography.labelSmall)
                            Text("${txns.size}", fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Filtered Rev", style = MaterialTheme.typography.labelSmall)
                            Text("Ks ${String.format(Locale.US, "%.2f", totalRev)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        if (txns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No transactions found matching criteria.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Responsive Scrollable Table
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                val scrollState = rememberScrollState()

                Column(modifier = Modifier.fillMaxSize()) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("Date & Time", width = 140.dp)
                        TableHeaderCell("Txn Ref #", width = 110.dp)
                        TableHeaderCell("Type", width = 110.dp)
                        TableHeaderCell("Customer", width = 130.dp)
                        TableHeaderCell("Cashier", width = 110.dp)
                        TableHeaderCell("Payment", width = 100.dp)
                        TableHeaderCell("Revenue", width = 110.dp, textAlign = TextAlign.End)
                        TableHeaderCell("COGS", width = 100.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Gross Profit", width = 110.dp, textAlign = TextAlign.End)
                    }

                    HorizontalDivider()

                    // Scrollable Rows
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(scrollState)
                    ) {
                        itemsIndexed(txns) { index, txn ->
                            val isEven = index % 2 == 0
                            val bgColor = if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)

                            Row(
                                modifier = Modifier
                                    .background(bgColor)
                                    .padding(vertical = 10.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableDataCell(dateFormat.format(Date(txn.timestamp)), width = 140.dp)
                                TableDataCell(txn.transactionNumber, width = 110.dp, fontWeight = FontWeight.Bold)
                                Box(modifier = Modifier.width(110.dp)) {
                                    TypeBadge(txn.type)
                                }
                                TableDataCell(txn.customerName ?: "-", width = 130.dp)
                                TableDataCell(txn.cashierName, width = 110.dp)
                                Box(modifier = Modifier.width(100.dp)) {
                                    PaymentBadge(txn.paymentMethod)
                                }
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", txn.totalAmount)}", width = 110.dp, textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", txn.cogs)}", width = 100.dp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.error)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", txn.totalAmount - txn.cogs)}", width = 110.dp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                    }

                    HorizontalDivider(thickness = 2.dp)

                    // Footer Total Row
                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableDataCell("Total (${txns.size} txns)", width = 140.dp, fontWeight = FontWeight.Bold)
                        TableDataCell("", width = 110.dp)
                        TableDataCell("", width = 110.dp)
                        TableDataCell("", width = 130.dp)
                        TableDataCell("", width = 110.dp)
                        TableDataCell("", width = 100.dp)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalRev)}", width = 110.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalCogs)}", width = 100.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalProfit)}", width = 110.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun PerDayTabContent(dailyReports: List<DailyReportItem>) {
    val totalRev = dailyReports.sumOf { it.totalRevenue }
    val totalCogs = dailyReports.sumOf { it.totalCogs }
    val totalProfit = dailyReports.sumOf { it.grossProfit }
    val totalTxns = dailyReports.sumOf { it.transactionCount }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryHeaderBanner(
            periodLabel = "Daily Summary",
            itemCountLabel = "Days: ${dailyReports.size}",
            totalTxns = totalTxns,
            totalRevenue = totalRev,
            totalProfit = totalProfit
        )

        if (dailyReports.isEmpty()) {
            EmptyReportState("No daily transaction data available yet.")
        } else {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("Date", width = 180.dp)
                        TableHeaderCell("Txn Count", width = 110.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Gross Revenue", width = 140.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Total COGS", width = 130.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Gross Profit", width = 140.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Avg Order Value", width = 140.dp, textAlign = TextAlign.End)
                    }

                    HorizontalDivider()

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(scrollState)
                    ) {
                        itemsIndexed(dailyReports) { index, item ->
                            val isEven = index % 2 == 0
                            val bgColor = if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)

                            Row(
                                modifier = Modifier
                                    .background(bgColor)
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableDataCell(item.dateFormatted, width = 180.dp, fontWeight = FontWeight.SemiBold)
                                TableDataCell("${item.transactionCount}", width = 110.dp, textAlign = TextAlign.End)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.totalRevenue)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.totalCogs)}", width = 130.dp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.error)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.grossProfit)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.avgTransactionValue)}", width = 140.dp, textAlign = TextAlign.End)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                    }

                    HorizontalDivider(thickness = 2.dp)

                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableDataCell("Total (${dailyReports.size} Days)", width = 180.dp, fontWeight = FontWeight.Bold)
                        TableDataCell("$totalTxns", width = 110.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalRev)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalCogs)}", width = 130.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalProfit)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        val avgAll = if (totalTxns > 0) totalRev / totalTxns else 0.0
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", avgAll)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PerWeekTabContent(weeklyReports: List<WeeklyReportItem>) {
    val totalRev = weeklyReports.sumOf { it.totalRevenue }
    val totalCogs = weeklyReports.sumOf { it.totalCogs }
    val totalProfit = weeklyReports.sumOf { it.grossProfit }
    val totalTxns = weeklyReports.sumOf { it.transactionCount }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryHeaderBanner(
            periodLabel = "Weekly Summary",
            itemCountLabel = "Weeks: ${weeklyReports.size}",
            totalTxns = totalTxns,
            totalRevenue = totalRev,
            totalProfit = totalProfit
        )

        if (weeklyReports.isEmpty()) {
            EmptyReportState("No weekly transaction data available yet.")
        } else {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("Week Period", width = 210.dp)
                        TableHeaderCell("Txn Count", width = 110.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Gross Revenue", width = 140.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Total COGS", width = 130.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Gross Profit", width = 140.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Avg Order Value", width = 140.dp, textAlign = TextAlign.End)
                    }

                    HorizontalDivider()

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(scrollState)
                    ) {
                        itemsIndexed(weeklyReports) { index, item ->
                            val isEven = index % 2 == 0
                            val bgColor = if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)

                            Row(
                                modifier = Modifier
                                    .background(bgColor)
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableDataCell(item.weekLabel, width = 210.dp, fontWeight = FontWeight.SemiBold)
                                TableDataCell("${item.transactionCount}", width = 110.dp, textAlign = TextAlign.End)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.totalRevenue)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.totalCogs)}", width = 130.dp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.error)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.grossProfit)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.avgTransactionValue)}", width = 140.dp, textAlign = TextAlign.End)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                    }

                    HorizontalDivider(thickness = 2.dp)

                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableDataCell("Total (${weeklyReports.size} Weeks)", width = 210.dp, fontWeight = FontWeight.Bold)
                        TableDataCell("$totalTxns", width = 110.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalRev)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalCogs)}", width = 130.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalProfit)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        val avgAll = if (totalTxns > 0) totalRev / totalTxns else 0.0
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", avgAll)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PerMonthTabContent(monthlyReports: List<MonthlyReportItem>) {
    val totalRev = monthlyReports.sumOf { it.totalRevenue }
    val totalCogs = monthlyReports.sumOf { it.totalCogs }
    val totalProfit = monthlyReports.sumOf { it.grossProfit }
    val totalTxns = monthlyReports.sumOf { it.transactionCount }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryHeaderBanner(
            periodLabel = "Monthly Summary",
            itemCountLabel = "Months: ${monthlyReports.size}",
            totalTxns = totalTxns,
            totalRevenue = totalRev,
            totalProfit = totalProfit
        )

        if (monthlyReports.isEmpty()) {
            EmptyReportState("No monthly transaction data available yet.")
        } else {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("Month", width = 180.dp)
                        TableHeaderCell("Txn Count", width = 110.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Gross Revenue", width = 140.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Total COGS", width = 130.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Gross Profit", width = 140.dp, textAlign = TextAlign.End)
                        TableHeaderCell("Avg Order Value", width = 140.dp, textAlign = TextAlign.End)
                    }

                    HorizontalDivider()

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(scrollState)
                    ) {
                        itemsIndexed(monthlyReports) { index, item ->
                            val isEven = index % 2 == 0
                            val bgColor = if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)

                            Row(
                                modifier = Modifier
                                    .background(bgColor)
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableDataCell(item.monthLabel, width = 180.dp, fontWeight = FontWeight.SemiBold)
                                TableDataCell("${item.transactionCount}", width = 110.dp, textAlign = TextAlign.End)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.totalRevenue)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.totalCogs)}", width = 130.dp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.error)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.grossProfit)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                TableDataCell("Ks ${String.format(Locale.US, "%.2f", item.avgTransactionValue)}", width = 140.dp, textAlign = TextAlign.End)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                    }

                    HorizontalDivider(thickness = 2.dp)

                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableDataCell("Total (${monthlyReports.size} Months)", width = 180.dp, fontWeight = FontWeight.Bold)
                        TableDataCell("$totalTxns", width = 110.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalRev)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalCogs)}", width = 130.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", totalProfit)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        val avgAll = if (totalTxns > 0) totalRev / totalTxns else 0.0
                        TableDataCell("Ks ${String.format(Locale.US, "%.2f", avgAll)}", width = 140.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryHeaderBanner(
    periodLabel: String,
    itemCountLabel: String,
    totalTxns: Int,
    totalRevenue: Double,
    totalProfit: Double
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(periodLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(itemCountLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Orders", style = MaterialTheme.typography.labelSmall)
                    Text("$totalTxns", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Revenue", style = MaterialTheme.typography.labelSmall)
                    Text("Ks ${String.format(Locale.US, "%.2f", totalRevenue)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Profit", style = MaterialTheme.typography.labelSmall)
                    Text("Ks ${String.format(Locale.US, "%.2f", totalProfit)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        textAlign = textAlign
    )
}

@Composable
private fun TableDataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TypeBadge(type: String) {
    val (label, bg, fg) = when (type) {
        "POS_SALE" -> Triple("POS Sale", Color(0xFFE3F2FD), Color(0xFF1565C0))
        "REPAIR_COMPLETED" -> Triple("Repair", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        else -> Triple(type, Color(0xFFFFF3E0), Color(0xFFE65100))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}

@Composable
private fun PaymentBadge(method: String) {
    val (bg, fg) = when (method.uppercase(Locale.ROOT)) {
        "CASH" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "CARD" -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
        "DIGITAL_WALLET", "KBZ PAY", "WAVE PAY" -> Pair(Color(0xFFF3E5F5), Color(0xFF6A1B9A))
        else -> Pair(Color(0xFFECEFF1), Color(0xFF37474F))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = method,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}

@Composable
private fun EmptyReportState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Assessment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

