package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.UnitMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitMeasurementDao {
    @Query("SELECT * FROM unit_measurements ORDER BY name ASC")
    fun getAllUnits(): Flow<List<UnitMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: UnitMeasurementEntity): Long

    @Update
    suspend fun updateUnit(unit: UnitMeasurementEntity)

    @Delete
    suspend fun deleteUnit(unit: UnitMeasurementEntity)
}
