package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.RepairIssueTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairIssueTypeDao {
    @Query("SELECT * FROM repair_issue_types ORDER BY isSystemDefault DESC, name ASC")
    fun getAllIssueTypes(): Flow<List<RepairIssueTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueType(issueType: RepairIssueTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueTypes(issueTypes: List<RepairIssueTypeEntity>)

    @Update
    suspend fun updateIssueType(issueType: RepairIssueTypeEntity)

    @Delete
    suspend fun deleteIssueType(issueType: RepairIssueTypeEntity)

    @Query("SELECT COUNT(*) FROM repair_issue_types")
    suspend fun getIssueTypeCount(): Int
}
