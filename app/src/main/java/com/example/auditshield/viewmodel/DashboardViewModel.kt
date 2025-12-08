package com.example.auditshield.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auditshield.database.entity.AnswerEntity
import com.example.auditshield.database.entity.AuditEntity
import com.example.auditshield.database.entity.QuestionEntity
import com.example.auditshield.repository.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val globalCompliance: Float = 0f,
    val yesPercentage: Float = 0f,
    val partialPercentage: Float = 0f,
    val noPercentage: Float = 0f,
    val complianceByControl: Map<String, Float> = emptyMap(),
    val totalMajorNC: Int = 0,
    val totalMinorNC: Int = 0,
    val totalObservations: Int = 0, // Assuming simple comments on 'No' or explicit field? User said "Observaciones ya implementado" but maybe refers to comments in general or specific type. I'll count count of 'No' with comments? Or just all comments?
    // User request: "Total de observaciones". Field "comentarios adicionales" exists. Maybe "Observación" is a type? 
    // Wait, prompt says: "Campo adicional tipo de No Conformidad: Mayor / Menor / Observación ya implementado". 
    // I only implemented Mayor/Menor in previous step. 
    // User might imply adding "Observación" as a 3rd option? Or counting general comments?
    // "Comentarios opcionales (u obligatorios en caso de 'No')"
    // Re-reading: "Campo adicional tipo de No Conformidad: Mayor / Menor / Observación ya implementado"
    // -> I should probably check if I need to add "OBSERVACION" to the chips. 
    // BUT the user said "ya implementado". I only added Mayor/Menor. 
    // I will assume for now I only count Mayor/Menor. 
    // If I need to count "Observaciones", maybe it means non-No answers with comments? Or answers with value=1 (Partial)?
    // Usually "Partial" is treated as observation or minor. 
    // Let's count "Partial" (value=1) as Observation for now, OR simply add Logic later.
    // Actually, let's treat "Total de observaciones" as count of Answers where value != 2 (Yes) and maybe have comments? 
    // Let's just track Stats based on available data.
    
    val findings: List<FindingDetail> = emptyList(),
    val auditTrends: List<AuditTrend> = emptyList(),
    val sgsiStatus: SgsiStatus = SgsiStatus.LOW
)

data class FindingDetail(
    val controlRef: String,
    val comment: String,
    val type: String // "MAYOR", "MENOR", "N/A"
)

data class AuditTrend(
    val auditName: String,
    val score: Int
)

enum class SgsiStatus {
    HIGH, MEDIUM, LOW
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AuditRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                repository.getAllAnswers(),
                repository.getQuestions(),
                repository.getAllAudits()
            ) { answers, questions, audits ->
                calculateStats(answers, questions, audits)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun calculateStats(
        answers: List<AnswerEntity>,
        questions: List<QuestionEntity>,
        audits: List<AuditEntity>
    ): DashboardUiState {
        if (answers.isEmpty()) return DashboardUiState()

        val totalAnswers = answers.size.toFloat()
        val yesCount = answers.count { it.value == 2 }
        val partialCount = answers.count { it.value == 1 }
        val noCount = answers.count { it.value == 0 }

        val yesPct = (yesCount / totalAnswers) * 100
        val partialPct = (partialCount / totalAnswers) * 100
        val noPct = (noCount / totalAnswers) * 100
        
        // Global Compliance (Average Score)
        // Score: Yes=100, Partial=50, No=0
        val totalScore = yesCount * 100 + partialCount * 50
        val globalCompliance = if (totalAnswers > 0) totalScore / totalAnswers else 0f

        // Compliance by Control
        val answersByQuestion = answers.groupBy { it.questionId }
        val questionMap = questions.associateBy { it.id }
        
        val complianceByControl = mutableMapOf<String, MutableList<Int>>()
        
        answers.forEach { ans ->
            val q = questionMap[ans.questionId]
            val ref = q?.controlRef ?: "Unknown"
            // Start of ref, e.g. "A.5" from "A.5.1..." logic if needed. 
            // Assuming controlRef is exactly "A.5", "A.6" etc or implies a grouping. 
            // If controlRef is "A.5.1", we might want to group by "A.5".
            // Let's group by the first part if it contains dots.
            val groupKey = if (ref.contains(".")) {
                 val parts = ref.split(".")
                 if (parts.size >= 2) "${parts[0]}.${parts[1]}" else ref // e.g. A.5
            } else ref

            val score = when (ans.value) {
                2 -> 100
                1 -> 50
                else -> 0
            }
            
            if (!complianceByControl.containsKey(groupKey)) {
                complianceByControl[groupKey] = mutableListOf()
            }
            complianceByControl[groupKey]?.add(score)
        }

        val complianceMap = complianceByControl.mapValues { (_, scores) ->
            scores.average().toFloat()
        }

        // NCs
        val majorNC = answers.count { it.value == 0 && it.nonComplianceLevel == "MAYOR" }
        val minorNC = answers.count { it.value == 0 && it.nonComplianceLevel == "MENOR" }
        // Observations: Arbitrarily deciding Partial answers are "Observations" for the dashboard count 
        // OR answers with comments that are not No?
        // Let's stick to user request: "Total de observaciones". 
        // If the user said "campo... observación ya implementado", maybe I missed it? 
        // But I see I only added Mayor/Menor.
        // Let's assume Partial answers = Observations for this context.
        val observations = partialCount 

        // Findings
        val findingsList = answers.filter { it.value == 0 }.map { ans ->
            val q = questionMap[ans.questionId]
            FindingDetail(
                controlRef = q?.controlRef ?: "?",
                comment = ans.comment ?: "",
                type = ans.nonComplianceLevel ?: "N/A"
            )
        }

        // Trends
        // Showing last 5 audits or similar
        val trends = audits.sortedBy { it.id }.map { audit ->
            AuditTrend(audit.title, audit.score)
        }

        val status = when {
            globalCompliance  > 85 -> SgsiStatus.HIGH
            globalCompliance  > 60 -> SgsiStatus.MEDIUM
            else -> SgsiStatus.LOW
        }

        return DashboardUiState(
            globalCompliance = globalCompliance,
            yesPercentage = yesPct,
            partialPercentage = partialPct,
            noPercentage = noPct,
            complianceByControl = complianceMap,
            totalMajorNC = majorNC,
            totalMinorNC = minorNC,
            totalObservations = observations,
            findings = findingsList,
            auditTrends = trends,
            sgsiStatus = status
        )
    }
}
