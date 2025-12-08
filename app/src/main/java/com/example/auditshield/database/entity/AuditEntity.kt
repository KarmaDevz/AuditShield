package com.example.auditshield.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audits")
data class AuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val description: String = "",
    val isoControl: String = "",
    val
    ompletedDate: Long? = null,
    val status: AuditStatus = AuditStatus.IN_PROGRESS,
    val score: Int = 0,        // Puntuación inicial
    val maxScore: Int = 100,   // Para el progress bar
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val recommendations: String = ""
)
