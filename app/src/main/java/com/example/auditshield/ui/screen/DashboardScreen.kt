package com.example.auditshield.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.auditshield.viewmodel.AuditTrend
import com.example.auditshield.viewmodel.DashboardUiState
import com.example.auditshield.viewmodel.DashboardViewModel
import com.example.auditshield.viewmodel.FindingDetail
import com.example.auditshield.viewmodel.SgsiStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard de Cumplimiento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Use a default icon since we don't have access to imports for ArrowBack if not imported.
                        // Assuming Icons.AutoMirrored.Filled.ArrowBack is available from common imports or use text
                        // Let's import Icons
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Estado General SGSI
            item {
                SgsiStatusCard(state.sgsiStatus)
            }

            // 2. Nivel de Cumplimiento Global (Gráfico Circular)
            item {
                ComplianceCard(state)
            }

            // 3. Cumplimiento por Controles (Barras)
            item {
                ControlsComplianceCard(state.complianceByControl)
            }

            // 4. No Conformidades (Comparativo)
            item {
                NonConformitiesCard(state)
            }

            // 5. Tendencia por Auditoría
            item {
                TrendsCard(state.auditTrends)
            }

            // 6. Hallazgos Detallados
            item {
                Text(
                    "Hallazgos Detallados (Respuestas 'No')",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(state.findings) { finding ->
                FindingItem(finding)
            }
        }
    }
}

@Composable
fun SgsiStatusCard(status: SgsiStatus) {
    val (color, text, label) = when (status) {
        SgsiStatus.HIGH -> Triple(Color(0xFF4CAF50), "Alto", "Listo para auditoría")
        SgsiStatus.MEDIUM -> Triple(Color(0xFFFFA000), "Medio", "Requiere mejoras")
        SgsiStatus.LOW -> Triple(Color(0xFFF44336), "Bajo", "Crítico")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Nivel de Preparación", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Text(text, style = MaterialTheme.typography.displayMedium, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ComplianceCard(state: DashboardUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cumplimiento Global", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Simple Pie Chart
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    Canvas(modifier = Modifier.size(120.dp)) {
                        val strokeWidth = 15.dp.toPx()
                        val radius = size.minDimension / 2 - strokeWidth / 2
                        
                        // Background
                        drawCircle(color = Color.LightGray.copy(alpha = 0.2f), radius = radius, style = Stroke(strokeWidth))

                        var startAngle = -90f
                        
                        // Yes (Green)
                        val yesSweep = (state.yesPercentage / 100f) * 360f
                        drawArc(
                            color = Color(0xFF4CAF50),
                            startAngle = startAngle,
                            sweepAngle = yesSweep,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += yesSweep

                        // Partial (Orange)
                        val partialSweep = (state.partialPercentage / 100f) * 360f
                        drawArc(
                            color = Color(0xFFFFA000),
                            startAngle = startAngle,
                            sweepAngle = partialSweep,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += partialSweep

                        // No (Red)
                        val noSweep = (state.noPercentage / 100f) * 360f
                        drawArc(
                            color = Color(0xFFF44336),
                            startAngle = startAngle,
                            sweepAngle = noSweep,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Butt)
                        )
                    }
                    Text(
                        text = "${state.globalCompliance.toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Legend
                Column {
                    LegendItem(Color(0xFF4CAF50), "Sí (${state.yesPercentage.toInt()}%)")
                    LegendItem(Color(0xFFFFA000), "Parcial (${state.partialPercentage.toInt()}%)")
                    LegendItem(Color(0xFFF44336), "No (${state.noPercentage.toInt()}%)")
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ControlsComplianceCard(complianceMap: Map<String, Float>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cumplimiento por Control", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            complianceMap.toSortedMap().forEach { (control, score) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(control, modifier = Modifier.width(40.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { score / 100f },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (score > 85) Color(0xFF4CAF50) else if (score > 60) Color(0xFFFFA000) else Color(0xFFF44336),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${score.toInt()}%", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun NonConformitiesCard(state: DashboardUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("No Conformidades", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatColumn("NC Mayor", state.totalMajorNC, Color(0xFFD32F2F))
                StatColumn("NC Menor", state.totalMinorNC, Color(0xFFF57C00))
                StatColumn("Observ.", state.totalObservations, Color(0xFF1976D2))
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun TrendsCard(trends: List<AuditTrend>) {
    if (trends.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tendencia (Últimas Auditorías)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                trends.takeLast(5).forEach { trend ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height((trend.score).dp) // Scale directly for simplicity
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                        Text(
                            trend.auditName.take(3), // Truncate name
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FindingItem(finding: FindingDetail) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(finding.controlRef, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                SuggestionChip(onClick = {}, label = { Text(finding.type) })
            }
            if (finding.comment.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(finding.comment, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
