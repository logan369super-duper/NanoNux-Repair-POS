package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.PromotionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromotionDao {
    @Query("SELECT * FROM promotions ORDER BY id DESC")
    fun getAllPromotions(): Flow<List<PromotionEntity>>

    @Query("SELECT * FROM promotions WHERE isActive = 1 ORDER BY id DESC")
    fun getAllActivePromotions(): Flow<List<PromotionEntity>>

    @Query("SELECT * FROM promotions WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getPromotionByCode(code: String): PromotionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotion(promotion: PromotionEntity): Long

    @Update
    suspend fun updatePromotion(promotion: PromotionEntity)

    @Delete
    suspend fun deletePromotion(promotion: PromotionEntity)
}
