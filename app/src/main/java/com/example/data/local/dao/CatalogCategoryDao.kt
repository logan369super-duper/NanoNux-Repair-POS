package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CatalogCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogCategoryDao {
    @Query("SELECT * FROM catalog_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CatalogCategoryEntity>>

    @Query("SELECT * FROM catalog_categories WHERE parentId IS NULL ORDER BY name ASC")
    fun getRootCategories(): Flow<List<CatalogCategoryEntity>>

    @Query("SELECT * FROM catalog_categories WHERE parentId = :parentId ORDER BY name ASC")
    fun getChildCategories(parentId: Long): Flow<List<CatalogCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CatalogCategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CatalogCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CatalogCategoryEntity)

    @Query("DELETE FROM catalog_categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)
}
