package com.example.auditshield.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auditshield.database.entity.AnswerEntity
import com.example.auditshield.database.entity.QuestionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditReportScreen(
    questions: List<QuestionEntity>,
    answers: List<AnswerEntity>,
    totalScore: Int,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporte de Auditoría") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score Card
            ScoreSummaryCard(totalScore = totalScore)

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Detalle por Pregunta",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))

            // Detailed List
            if (questions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay preguntas disponibles.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(questions) { index, q ->
                        val ans = answers.firstOrNull { it.questionId == q.id }
                        ReportQuestionRow(index + 1, q.text, ans)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreSummaryCard(totalScore: Int) {
    val (recommendation, color) = getRecommendationAndColor(totalScore)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Puntaje General",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(24.dp))
            
            CircularScoreIndicator(
                score = totalScore,
                color = color,
                size = 150.dp,
                strokeWidth = 14.dp
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = recommendation,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CircularScoreIndicator(
    score: Int,
    color: Color,
    size: Dp,
    strokeWidth: Dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background circle
            drawCircle(
                color = color.copy(alpha = 0.2f),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ReportQuestionRow(index: Int, questionText: String, answer: AnswerEntity?) {
    val label = when (answer?.value) {
        0 -> "No"
        1 -> "Parcial"
        2 -> "Sí"
        else -> "Sin responder"
    }
    
    val (statusColor, statusIcon) = when (answer?.value) {
        0 -> MaterialTheme.colorScheme.error to "❌"
        1 -> Color(0xFFFFA000) to "⚠️" // Amber
        2 -> Color(0xFF4CAF50) to "✅" // Green
        else -> MaterialTheme.colorScheme.onSurfaceVariant to "❓"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "$index.",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.width(28.dp),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = questionText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = statusIcon, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (!answer?.comment.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Comentario: ${answer?.comment}",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun getRecommendationAndColor(score: Int): Pair<String, Color> {
    return when (score) {
        in 0..25 -> "⚠️ Nivel Crítico: Se requiere atención inmediata. Implemente controles básicos urgentemente." to Color(0xFFD32F2F) // Red
        in 26..50 -> "🟠 Riesgo Alto: Existen brechas significativas. Priorice las áreas de mayor impacto." to Color(0xFFF57C00) // Orange
        in 51..75 -> "🟡 Riesgo Medio: Buen progreso, pero se necesitan mejoras para cumplir con los estándares." to Color(0xFFFBC02D) // Yellow
        in 76..99 -> "🟢 Buen Estado: La mayoría de los controles están en su lugar. Revise detalles menores." to Color(0xFF388E3C) // Green
        100 -> "🎉 ¡Excelente! Cumplimiento total. Mantenga el monitoreo continuo." to Color(0xFF1B5E20) // Dark Green
        else -> "Sin datos" to Color.Gray
    }
}

