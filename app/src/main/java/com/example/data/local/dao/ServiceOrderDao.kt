package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ServiceOrderEntity
import com.example.domain.model.RepairTicketStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceOrderDao {
    @Query("SELECT * FROM service_orders ORDER BY updatedAt DESC")
    fun getAllServiceOrders(): Flow<List<ServiceOrderEntity>>

    @Query("SELECT * FROM service_orders WHERE status IN ('RECEIVED', 'DIAGNOSING', 'IN_PROGRESS', 'READY') ORDER BY createdAt DESC")
    fun getActiveServiceOrders(): Flow<List<ServiceOrderEntity>>

    @Query("SELECT * FROM service_orders WHERE id = :id LIMIT 1")
    suspend fun getServiceOrderById(id: Long): ServiceOrderEntity?

    @Query("SELECT * FROM service_orders WHERE ticketNumber = :ticketNumber LIMIT 1")
    suspend fun getServiceOrderByTicketNumber(ticketNumber: String): ServiceOrderEntity?

    @Query("SELECT * FROM service_orders WHERE customerName LIKE '%' || :query || '%' OR customerPhone LIKE '%' || :query || '%' OR serialImei LIKE '%' || :query || '%' OR ticketNumber LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchServiceOrders(query: String): Flow<List<ServiceOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceOrder(order: ServiceOrderEntity): Long

    @Update
    suspend fun updateServiceOrder(order: ServiceOrderEntity)

    @Query("UPDATE service_orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: RepairTicketStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM service_orders WHERE status IN ('RECEIVED', 'DIAGNOSING', 'IN_PROGRESS')")
    fun getOpenTicketCount(): Flow<Int>
}
