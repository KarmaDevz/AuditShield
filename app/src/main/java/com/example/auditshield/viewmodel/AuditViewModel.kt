package com.example.auditshield.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auditshield.database.entity.AnswerEntity
import com.example.auditshield.database.entity.AuditEntity
import com.example.auditshield.database.entity.AuditStatus
import com.example.auditshield.database.entity.QuestionEntity
import com.example.auditshield.database.entity.RiskLevel
import com.example.auditshield.repository.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AuditViewModel @Inject constructor(
    private val repository: AuditRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _audits = MutableStateFlow<List<AuditEntity>>(emptyList())
    val audits = _audits.asStateFlow()

    private val _selectedAudit = MutableStateFlow<AuditEntity?>(null)
    val selectedAudit = _selectedAudit.asStateFlow()

    private val _answers = MutableStateFlow<List<AnswerEntity>>(emptyList())
    val answers = _answers.asStateFlow()

    private val _questions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val questions = _questions.asStateFlow()

    init {
        loadAllAudits()
        loadQuestions()   // <-- cargar preguntas al iniciar el ViewModel
    }

    fun loadQuestions() {
        viewModelScope.launch {
            repository.getQuestions().collectLatest { list ->
                _questions.value = list
            }
        }
    }

    fun loadAllAudits() {
        viewModelScope.launch {
            repository.getAllAudits().collectLatest { list ->
                _audits.value = list
            }
        }
    }

    fun loadAudit(auditId: Long) {
        viewModelScope.launch {
            repository.getAuditById(auditId).collectLatest { audit ->
                _selectedAudit.value = audit
            }
        }
        viewModelScope.launch {
            repository.getAnswersForAudit(auditId).collectLatest { list ->
                _answers.value = list
            }
        }
    }

    fun updateAnswer(answer: AnswerEntity) {
        viewModelScope.launch {
            repository.updateAnswer(answer)
        }
    }

    fun startNewAudit(title: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.createAudit(title)
            loadAllAudits()
            onCreated(id)
        }
    }

    fun openAudit(auditId: Long) {
        loadAudit(auditId)
    }

    fun finishAudit(auditId: Long, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.finishAudit(auditId)
            repository.getAuditById(auditId).collectLatest { a ->
                _selectedAudit.value = a
            }
            onFinished()
        }
    }

    fun getAuditStats(): Map<String, Any> {
        val list = _audits.value
        return mapOf(
            "total" to list.size,
            "completed" to list.count { it.status == AuditStatus.COMPLETED },
            "averageScore" to if (list.isNotEmpty()) list.map { it.score }.average().toInt() else 0,
            "highRiskCount" to list.count {
                it.riskLevel == RiskLevel.HIGH || it.riskLevel == RiskLevel.CRITICAL
            }
        )
    }
}
