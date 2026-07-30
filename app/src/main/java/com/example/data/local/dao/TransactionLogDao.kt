package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.TransactionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionLogDao {
    @Query("SELECT * FROM transaction_logs ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionLogEntity>>

    @Query("SELECT * FROM transaction_logs WHERE (:brand IS NULL OR deviceBrand LIKE '%' || :brand || '%') AND (:customer IS NULL OR customerName LIKE '%' || :customer || '%') AND (:issue IS NULL OR issueType LIKE '%' || :issue || '%') ORDER BY timestamp DESC")
    fun filterTransactions(brand: String?, customer: String?, issue: String?): Flow<List<TransactionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionLogEntity): Long

    @Query("SELECT SUM(totalAmount) FROM transaction_logs")
    fun getTotalRevenue(): Flow<Double?>

    @Query("SELECT SUM(cogs) FROM transaction_logs")
    fun getTotalCOGS(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transaction_logs")
    fun getTransactionCount(): Flow<Int>
}
