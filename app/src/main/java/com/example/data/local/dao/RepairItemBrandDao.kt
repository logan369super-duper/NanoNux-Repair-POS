package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.RepairItemBrandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairItemBrandDao {
    @Query("SELECT * FROM repair_item_brands ORDER BY isSystemDefault DESC, name ASC")
    fun getAllBrands(): Flow<List<RepairItemBrandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(brand: RepairItemBrandEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrands(brands: List<RepairItemBrandEntity>)

    @Update
    suspend fun updateBrand(brand: RepairItemBrandEntity)

    @Delete
    suspend fun deleteBrand(brand: RepairItemBrandEntity)

    @Query("SELECT COUNT(*) FROM repair_item_brands")
    suspend fun getBrandCount(): Int
}
