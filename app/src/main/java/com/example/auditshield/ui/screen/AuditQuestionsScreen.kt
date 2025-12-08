package com.example.auditshield.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.auditshield.viewmodel.AuditQuestionsViewModel
import com.example.auditshield.database.entity.AnswerEntity
import com.example.auditshield.database.entity.QuestionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditQuestionsScreen(
    navController: NavController,
    auditId: Long,
    viewModel: AuditQuestionsViewModel = hiltViewModel(),
    onFinishAudit: () -> Unit = {}
) {
    LaunchedEffect(auditId) {
        viewModel.loadQuestions(auditId)
    }

    val qa by viewModel.qa.collectAsState()
    val index by viewModel.currentIndex.collectAsState()
    var showValidationErrors by remember { mutableStateOf(false) }

    // Reset validation errors when changing question
    LaunchedEffect(index) {
        showValidationErrors = false
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Auditoría en Curso") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (qa.isNotEmpty()) {
                    LinearProgressIndicator(
                        progress = { (index + 1).toFloat() / qa.size },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (index > 0) {
                        OutlinedButton(onClick = { viewModel.previousQuestion() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Anterior")
                        }
                    } else {
                        Spacer(Modifier.width(8.dp)) // Placeholder
                    }

                    // Validation logic for current question
                    val currentQuestion = qa.getOrNull(index)
                    val currentAnswer = currentQuestion?.second
                    val isNoSelected = (currentAnswer?.value ?: 0) == 0
                    val isCommentValid = !currentAnswer?.comment.isNullOrBlank()
                    val isLevelValid = !currentAnswer?.nonComplianceLevel.isNullOrBlank()
                    val isValid = !isNoSelected || (isCommentValid && isLevelValid)

                    if (index < qa.size - 1) {
                        Button(
                            onClick = {
                                if (isValid) {
                                    viewModel.nextQuestion()
                                } else {
                                    showValidationErrors = true
                                }
                            }
                        ) {
                            Text("Siguiente")
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (isValid) {
                                    viewModel.finishAudit(auditId) {
                                        onFinishAudit()
                                        navController.popBackStack()
                                    }
                                } else {
                                    showValidationErrors = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Finalizar")
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (qa.isNotEmpty()) {
                val (question, answer) = qa[index]

                // Animated Content for smooth transitions between questions
                AnimatedContent(
                    targetState = question,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "questionTransition"
                ) { targetQuestion ->
                    // We need to find the answer for the target question because 'answer' in the outer scope might be stale during animation
                    // However, for simplicity in this structure, we can just use the current one as the index updates. 
                    // A better approach for AnimatedContent is to pass the pair (question, answer) as targetState.
                    // Let's stick to simple recomposition for now to avoid complexity with finding the answer again.
                    
                    QuestionCard(
                        question = question, // Use current question
                        answer = answer,     // Use current answer
                        auditId = auditId,
                        viewModel = viewModel,
                        questionNumber = index + 1,
                        totalQuestions = qa.size,
                        showValidationErrors = showValidationErrors
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}


@Composable
fun QuestionCard(
    question: QuestionEntity,
    answer: AnswerEntity?,
    auditId: Long,
    viewModel: AuditQuestionsViewModel,
    questionNumber: Int,
    totalQuestions: Int,
    showValidationErrors: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Pregunta $questionNumber de $totalQuestions",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(32.dp))

            // Custom Selection Buttons
            val selectedValue = answer?.value ?: 0
            
            SelectionOption(
                label = "Sí",
                isSelected = selectedValue == 2,
                color = Color(0xFF4CAF50),
                onClick = { updateAnswer(viewModel, answer, auditId, question.id, 2, answer?.comment, answer?.nonComplianceLevel) }
            )
            Spacer(Modifier.height(12.dp))
            SelectionOption(
                label = "Parcial",
                isSelected = selectedValue == 1,
                color = Color(0xFFFFA000),
                onClick = { updateAnswer(viewModel, answer, auditId, question.id, 1, answer?.comment, answer?.nonComplianceLevel) }
            )
            Spacer(Modifier.height(12.dp))
            SelectionOption(
                label = "No",
                isSelected = selectedValue == 0,
                color = MaterialTheme.colorScheme.error,
                onClick = { updateAnswer(viewModel, answer, auditId, question.id, 0, answer?.comment, answer?.nonComplianceLevel) }
            )

            // Non-Compliance Level Selection (Only if "No" is selected)
            if (selectedValue == 0) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Tipo de No Conformidad",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val currentLevel = answer?.nonComplianceLevel
                    
                    // Minor
                    FilterChip(
                        selected = currentLevel == "MENOR",
                        onClick = { updateAnswer(viewModel, answer, auditId, question.id, 0, answer?.comment, "MENOR") },
                        label = { Text("Menor") },
                        leadingIcon = if (currentLevel == "MENOR") {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Major
                    FilterChip(
                        selected = currentLevel == "MAYOR",
                        onClick = { updateAnswer(viewModel, answer, auditId, question.id, 0, answer?.comment, "MAYOR") },
                        label = { Text("Mayor") },
                        leadingIcon = if (currentLevel == "MAYOR") {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.error,
                            selectedLabelColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (showValidationErrors && answer?.nonComplianceLevel == null) {
                   Text(
                       text = "Selecciona un tipo",
                       style = MaterialTheme.typography.bodySmall,
                       color = MaterialTheme.colorScheme.error,
                       modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                   )
                }
            }

            Spacer(Modifier.height(32.dp))

            var comment by remember(answer?.comment) { mutableStateOf(answer?.comment ?: "") }
            val isError = showValidationErrors && selectedValue == 0 && comment.isBlank()

            OutlinedTextField(
                value = comment,
                onValueChange = {
                    comment = it
                    updateAnswer(viewModel, answer, auditId, question.id, selectedValue, it, answer?.nonComplianceLevel)
                },
                label = { Text(if (selectedValue == 0) "Comentarios adicionales *" else "Comentarios adicionales") },
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text("Requerido si la respuesta es 'No'", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

@Composable
fun SelectionOption(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = color)
            }
        }
    }
}

fun updateAnswer(
    viewModel: AuditQuestionsViewModel,
    currentAnswer: AnswerEntity?,
    auditId: Long,
    questionId: Long,
    value: Int,
    comment: String?,
    nonComplianceLevel: String?
) {
    // If value is not NO (0), clear nonComplianceLevel
    val finalLevel = if (value == 0) nonComplianceLevel else null

    val newAnswer = currentAnswer?.copy(value = value, comment = comment, nonComplianceLevel = finalLevel)
        ?: AnswerEntity(
            auditId = auditId,
            questionId = questionId,
            value = value,
            comment = comment,
            nonComplianceLevel = finalLevel
        )
    viewModel.saveAnswer(newAnswer)
}


