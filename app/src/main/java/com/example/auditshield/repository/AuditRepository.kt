package com.example.auditshield.repository

import com.example.auditshield.database.dao.AnswerDao
import com.example.auditshield.database.dao.AuditDao
import com.example.auditshield.database.dao.QuestionDao
import com.example.auditshield.database.entity.AnswerEntity
import com.example.auditshield.database.entity.AuditEntity
import com.example.auditshield.database.entity.AuditStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.combine
import com.example.auditshield.database.entity.QuestionEntity
import com.example.auditshield.database.entity.RiskLevel

import javax.inject.Inject

class AuditRepository @Inject constructor(
    private val auditDao: AuditDao,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao
) {
    fun getAllAudits() = auditDao.getAllAudits()
    fun getAuditById(id: Long) = auditDao.getAuditById(id)
    fun getAllAnswers() = answerDao.getAllAnswers()
    suspend fun createAudit(title: String): Long {
        val id = auditDao.insertAudit(AuditEntity(title = title, isoControl = "ISO/IEC 27001:2022"))
        val questions = questionDao.getAllQuestions().firstOrNull() ?: emptyList()
        val answers = questions.map { q ->
            AnswerEntity(auditId = id, questionId = q.id, value = 0)
        }
        if (answers.isNotEmpty()) answerDao.insertAnswers(answers)
        return id
    }

    fun getQuestions() = questionDao.getAllQuestions()
    fun getAnswersForAudit(auditId: Long) = answerDao.getAnswersForAudit(auditId)
    

    fun getQuestionsWithAnswers(auditId: Long): Flow<List<Pair<QuestionEntity, AnswerEntity?>>> {
        val questionsFlow = questionDao.getAllQuestions()
        val answersFlow = answerDao.getAnswersForAudit(auditId)

        return combine(questionsFlow, answersFlow) { questions, answers ->
            val answersByQuestion = answers.associateBy { it.questionId }
            questions.map { q -> q to answersByQuestion[q.id] }
        }
    }

    suspend fun updateAnswer(answer: AnswerEntity) = answerDao.updateAnswer(answer)

    suspend fun finishAudit(auditId: Long) {
        val answers = answerDao.getAnswersForAudit(auditId).firstOrNull() ?: emptyList()
        val score = calculateScoreFromAnswers(answers)
        val audit = auditDao.getAuditById(auditId).firstOrNull() ?: return
        
        val riskLevel = when {
            score >= 80 -> com.example.auditshield.database.entity.RiskLevel.LOW
            score >= 60 -> com.example.auditshield.database.entity.RiskLevel.MEDIUM
            score >= 40 -> com.example.auditshield.database.entity.RiskLevel.HIGH
            else -> com.example.auditshield.database.entity.RiskLevel.CRITICAL
        }
        
        auditDao.updateAudit(audit.copy(score = score, status = AuditStatus.COMPLETED, riskLevel = riskLevel))
    }
    suspend fun getAnswersForAuditOnce(auditId: Long): List<AnswerEntity> {
        return answerDao.getAnswersForAuditOnce(auditId)
    }

    suspend fun updateAuditScoreAndRisk(id: Long, score: Int, risk: RiskLevel) {
        auditDao.updateScoreAndRisk(id, score, risk)
    }

    private fun calculateScoreFromAnswers(answers: List<AnswerEntity>): Int {
        if (answers.isEmpty()) return 0
        val total = answers.sumOf { mapValueToPercent(it.value) }
        return (total / answers.size).coerceIn(0, 100)
    }

    private fun mapValueToPercent(value: Int): Int {
        return when (value) {
            0 -> 0
            1 -> 50
            2 -> 100
            else -> 0
        }
    }
}
