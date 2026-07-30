package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.TransactionLogEntity
import com.example.data.repository.PosRepository
import com.example.domain.model.FinancialReportSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyReportItem(
    val dateKey: String,          // e.g. "2026-07-29"
    val dateFormatted: String,    // e.g. "Wed, Jul 29, 2026"
    val dateTimestamp: Long,
    val transactionCount: Int,
    val totalRevenue: Double,
    val totalCogs: Double,
    val grossProfit: Double,
    val avgTransactionValue: Double
)

data class WeeklyReportItem(
    val weekKey: String,          // e.g. "2026-07-27"
    val weekLabel: String,        // e.g. "Jul 27 - Aug 02, 2026"
    val startTimestamp: Long,
    val transactionCount: Int,
    val totalRevenue: Double,
    val totalCogs: Double,
    val grossProfit: Double,
    val avgTransactionValue: Double
)

data class MonthlyReportItem(
    val monthKey: String,         // e.g. "2026-07"
    val monthLabel: String,       // e.g. "July 2026"
    val startTimestamp: Long,
    val transactionCount: Int,
    val totalRevenue: Double,
    val totalCogs: Double,
    val grossProfit: Double,
    val avgTransactionValue: Double
)

data class ReportsUiState(
    val summary: FinancialReportSummary = FinancialReportSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0),
    val transactions: List<TransactionLogEntity> = emptyList(),
    val filteredTransactions: List<TransactionLogEntity> = emptyList(),
    val dailyReports: List<DailyReportItem> = emptyList(),
    val weeklyReports: List<WeeklyReportItem> = emptyList(),
    val monthlyReports: List<MonthlyReportItem> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: Int = 0 // 0: Overview, 1: Per Txn, 2: Per Day, 3: Per Week, 4: Per Month
)

class ReportsViewModel(private val repository: PosRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getFinancialReportSummary(),
                repository.getAllTransactions()
            ) { summary, transactions ->
                val daily = computeDailyReports(transactions)
                val weekly = computeWeeklyReports(transactions)
                val monthly = computeMonthlyReports(transactions)
                val filtered = filterTxns(transactions, _uiState.value.searchQuery)

                _uiState.value.copy(
                    summary = summary,
                    transactions = transactions,
                    filteredTransactions = filtered,
                    dailyReports = daily,
                    weeklyReports = weekly,
                    monthlyReports = monthly
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val currentTxns = _uiState.value.transactions
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredTransactions = filterTxns(currentTxns, query)
        )
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    private fun filterTxns(txns: List<TransactionLogEntity>, query: String): List<TransactionLogEntity> {
        if (query.isBlank()) return txns
        val q = query.trim().lowercase(Locale.ROOT)
        return txns.filter { txn ->
            txn.transactionNumber.lowercase(Locale.ROOT).contains(q) ||
            txn.type.lowercase(Locale.ROOT).contains(q) ||
            (txn.customerName?.lowercase(Locale.ROOT)?.contains(q) == true) ||
            txn.cashierName.lowercase(Locale.ROOT).contains(q) ||
            txn.paymentMethod.lowercase(Locale.ROOT).contains(q)
        }
    }

    private fun computeDailyReports(txns: List<TransactionLogEntity>): List<DailyReportItem> {
        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayLabelFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)

        val grouped = txns.groupBy { dayKeyFormat.format(Date(it.timestamp)) }
        return grouped.map { (dateKey, list) ->
            val firstTime = list.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()
            val formattedLabel = try {
                val parsed = dayKeyFormat.parse(dateKey)
                if (parsed != null) dayLabelFormat.format(parsed) else dateKey
            } catch (e: Exception) {
                dateKey
            }
            val count = list.size
            val revenue = list.sumOf { it.totalAmount }
            val cogs = list.sumOf { it.cogs }
            val profit = revenue - cogs
            val avg = if (count > 0) revenue / count else 0.0

            DailyReportItem(
                dateKey = dateKey,
                dateFormatted = formattedLabel,
                dateTimestamp = firstTime,
                transactionCount = count,
                totalRevenue = revenue,
                totalCogs = cogs,
                grossProfit = profit,
                avgTransactionValue = avg
            )
        }.sortedByDescending { it.dateKey }
    }

    private fun computeWeeklyReports(txns: List<TransactionLogEntity>): List<WeeklyReportItem> {
        val dayLabelFormat = SimpleDateFormat("MMM dd", Locale.US)
        val yearFormat = SimpleDateFormat("yyyy", Locale.US)

        val map = mutableMapOf<String, MutableList<TransactionLogEntity>>()
        val labelMap = mutableMapOf<String, Pair<String, Long>>()

        val cal = Calendar.getInstance()
        for (txn in txns) {
            cal.timeInMillis = txn.timestamp
            cal.firstDayOfWeek = Calendar.MONDAY
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val weekStartMs = cal.timeInMillis
            val weekStartStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

            // Compute week end (Sunday)
            cal.add(Calendar.DAY_OF_WEEK, 6)
            val weekEndStr = dayLabelFormat.format(cal.time)
            val weekStartFormatted = dayLabelFormat.format(Date(weekStartMs))
            val yearStr = yearFormat.format(Date(weekStartMs))

            val label = "$weekStartFormatted - $weekEndStr, $yearStr"

            map.getOrPut(weekStartStr) { mutableListOf() }.add(txn)
            labelMap[weekStartStr] = Pair(label, weekStartMs)
        }

        return map.map { (weekKey, list) ->
            val (label, startMs) = labelMap[weekKey] ?: Pair(weekKey, 0L)
            val count = list.size
            val revenue = list.sumOf { it.totalAmount }
            val cogs = list.sumOf { it.cogs }
            val profit = revenue - cogs
            val avg = if (count > 0) revenue / count else 0.0

            WeeklyReportItem(
                weekKey = weekKey,
                weekLabel = label,
                startTimestamp = startMs,
                transactionCount = count,
                totalRevenue = revenue,
                totalCogs = cogs,
                grossProfit = profit,
                avgTransactionValue = avg
            )
        }.sortedByDescending { it.weekKey }
    }

    private fun computeMonthlyReports(txns: List<TransactionLogEntity>): List<MonthlyReportItem> {
        val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
        val monthLabelFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

        val grouped = txns.groupBy { monthKeyFormat.format(Date(it.timestamp)) }
        return grouped.map { (monthKey, list) ->
            val firstTime = list.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()
            val formattedLabel = try {
                val parsed = monthKeyFormat.parse(monthKey)
                if (parsed != null) monthLabelFormat.format(parsed) else monthKey
            } catch (e: Exception) {
                monthKey
            }
            val count = list.size
            val revenue = list.sumOf { it.totalAmount }
            val cogs = list.sumOf { it.cogs }
            val profit = revenue - cogs
            val avg = if (count > 0) revenue / count else 0.0

            MonthlyReportItem(
                monthKey = monthKey,
                monthLabel = formattedLabel,
                startTimestamp = firstTime,
                transactionCount = count,
                totalRevenue = revenue,
                totalCogs = cogs,
                grossProfit = profit,
                avgTransactionValue = avg
            )
        }.sortedByDescending { it.monthKey }
    }
}

