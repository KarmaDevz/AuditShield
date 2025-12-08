package com.example.auditshield.database.dao

import androidx.room.*
import com.example.auditshield.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Insert
    suspend fun insertAudit(audit: AuditEntity): Long

    @Query("SELECT * FROM audits ORDER BY createdAt DESC")
    fun getAllAudits(): Flow<List<AuditEntity>>

    @Query("UPDATE audits SET score = :score, riskLevel = :risk WHERE id = :auditId")
    suspend fun updateScoreAndRisk(auditId: Long, score: Int, risk: RiskLevel)


    @Query("SELECT * FROM audits WHERE id = :id")
    fun getAuditById(id: Long): Flow<AuditEntity?>

    @Update
    suspend fun updateAudit(audit: AuditEntity)

    @Delete
    suspend fun deleteAudit(audit: AuditEntity)
}
