package com.example.auditshield.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.yml.charts.common.utils.DataUtils
import co.yml.charts.ui.piechart.charts.DonutPieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.auditshield.database.entity.AuditEntity
import com.example.auditshield.viewmodel.AuditViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import co.yml.charts.common.model.PlotType
import com.example.auditshield.database.entity.RiskLevel
import com.example.auditshield.database.entity.AuditStatus


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditListScreen(
    viewModel: AuditViewModel,
    onAuditClick: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val audits by viewModel.audits.collectAsState()
    val stats by remember { derivedStateOf { viewModel.getAuditStats() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Auditorías") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen de Auditorías",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            title = "Total",
                            value = stats["total"].toString()
                        )
                        StatItem(
                            title = "Puntuación Media",
                            value = "${stats["averageScore"]}%"
                        )
                        StatItem(
                            title = "Alto Riesgo",
                            value = stats["highRiskCount"].toString()
                        )
                    }

                    // Pie Chart
                    if (audits.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DonutPieChart(
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth(),
                            pieChartData = createPieChartData(audits),
                            pieChartConfig = PieChartConfig(
                                isAnimationEnable = true,
                                showSliceLabels = true
                            )
                        )
                    }
                }
            }

            // Audit List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(audits) { audit ->
                    AuditListItem(
                        audit = audit,
                        onClick = { onAuditClick(audit.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun AuditListItem(audit: AuditEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = audit.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                RiskChip(riskLevel = audit.riskLevel)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = audit.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Control: ${audit.isoControl}",
                    style = MaterialTheme.typography.bodySmall
                )
                LinearProgressIndicator(
                    progress = audit.score.toFloat() / audit.maxScore,
                    modifier = Modifier.width(100.dp)
                )
                Text(
                    text = "${audit.score}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun RiskChip(riskLevel: RiskLevel) {
    val (text, color) = when (riskLevel) {
        RiskLevel.LOW -> Pair("BAJO", Color.Green)
        RiskLevel.MEDIUM -> Pair("MEDIO", Color.Yellow)
        RiskLevel.HIGH -> Pair("ALTO", Color.Red)
        RiskLevel.CRITICAL -> Pair("CRÍTICO", Color(0xFF8B0000))
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f),
        contentColor = color
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun createPieChartData(audits: List<AuditEntity>): PieChartData {
    val riskCounts = audits.groupingBy { it.riskLevel }.eachCount()

    val slices = listOf(
        PieChartData.Slice(
            "Bajo",
            riskCounts.getOrDefault(RiskLevel.LOW, 0).toFloat(),
            Color.Green
        ),
        PieChartData.Slice(
            "Medio",
            riskCounts.getOrDefault(RiskLevel.MEDIUM, 0).toFloat(),
            Color.Yellow

        ),
        PieChartData.Slice(
            "Alto",
            riskCounts.getOrDefault(RiskLevel.HIGH, 0).toFloat(),
            Color.Red

        ),
        PieChartData.Slice(
            "Crítico",
            riskCounts.getOrDefault(RiskLevel.CRITICAL, 0).toFloat(),
            Color(0xFF8B0000),

        )
    )

    return PieChartData(slices = slices, plotType = PlotType.Donut)
}
