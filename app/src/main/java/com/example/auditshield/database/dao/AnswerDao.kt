package com.example.auditshield.database.dao

import androidx.room.*
import com.example.auditshield.database.entity.AnswerEntity
import com.example.auditshield.database.entity.RiskLevel
import kotlinx.coroutines.flow.Flow

@Dao
interface AnswerDao {
    @Query("SELECT * FROM answers WHERE auditId = :auditId")
    suspend fun getAnswersForAuditOnce(auditId: Long): List<AnswerEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: AnswerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<AnswerEntity>)

    @Update
    suspend fun updateAnswer(answer: AnswerEntity)

    @Query("SELECT * FROM answers WHERE auditId = :auditId")
    fun getAnswersForAudit(auditId: Long): Flow<List<AnswerEntity>>

    @Query("SELECT * FROM answers WHERE id = :id")
    suspend fun getAnswerById(id: Long): AnswerEntity?

    @Query("SELECT * FROM answers")
    fun getAllAnswers(): Flow<List<AnswerEntity>>
}
