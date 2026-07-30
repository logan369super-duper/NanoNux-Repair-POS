package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.RepairServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairServiceDao {
    @Query("SELECT * FROM repair_services WHERE isActive = 1 ORDER BY serviceName ASC")
    fun getAllActiveServices(): Flow<List<RepairServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: RepairServiceEntity): Long

    @Update
    suspend fun updateService(service: RepairServiceEntity)

    @Delete
    suspend fun deleteService(service: RepairServiceEntity)
}
