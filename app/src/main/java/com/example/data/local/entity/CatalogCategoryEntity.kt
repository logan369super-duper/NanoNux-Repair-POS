package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_categories")
data class CatalogCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentId: Long? = null, // null indicates a root category; non-null points to parent category ID
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
