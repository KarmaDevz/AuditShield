package com.example.auditshield.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auditshield.database.entity.AnswerEntity
import com.example.auditshield.database.entity.QuestionEntity
import com.example.auditshield.database.entity.RiskLevel
import com.example.auditshield.repository.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditQuestionsViewModel @Inject constructor(
    private val repository: AuditRepository
) : ViewModel() {

    // PARES (Pregunta, Respuesta existente o null)
    private val _qa = MutableStateFlow<List<Pair<QuestionEntity, AnswerEntity?>>>(emptyList())
    val qa = _qa.asStateFlow()
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    fun nextQuestion() {
        val max = _qa.value.size - 1
        if (_currentIndex.value < max) _currentIndex.value++
    }

    fun previousQuestion() {
        if (_currentIndex.value > 0) _currentIndex.value--
    }
    // Carga preguntas + respuestas para una auditoría
    fun loadQuestions(auditId: Long) {
        viewModelScope.launch {
            repository.getQuestionsWithAnswers(auditId).collectLatest { list ->
                _qa.value = list
            }
        }
    }

    // Guardar o actualizar respuesta
    fun saveAnswer(answer: AnswerEntity) {
        viewModelScope.launch {
            repository.updateAnswer(answer)
            recalculateAuditScore(answer.auditId)
        }
    }

    private fun calculateRiskLevel(compliance: Float): RiskLevel {
        return when {
            compliance >= 85 -> RiskLevel.LOW
            compliance >= 60 -> RiskLevel.MEDIUM
            compliance >= 40 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }
    }

    private suspend fun recalculateAuditScore(auditId: Long) {
        val answers = repository.getAnswersForAuditOnce(auditId) // Debemos agregar esta función en el repo
        if (answers.isEmpty()) return

        val total = answers.size
        val yes = answers.count { it.value == 2 }
        val partial = answers.count { it.value == 1 }

        val score = ((yes * 100) + (partial * 50)) / total
        val compliance = score.toFloat()

        val risk = calculateRiskLevel(compliance)

        repository.updateAuditScoreAndRisk(auditId, score, risk)
    }

    // Finalizar auditoría
    fun finishAudit(auditId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            recalculateAuditScore(auditId)
            repository.finishAudit(auditId)
            onDone()
        }
    }

}

