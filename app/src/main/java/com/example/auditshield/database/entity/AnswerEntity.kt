package com.example.auditshield.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "answers",
    foreignKeys = [
        ForeignKey(entity = AuditEntity::class, parentColumns = ["id"], childColumns = ["auditId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = QuestionEntity::class, parentColumns = ["id"], childColumns = ["questionId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class AnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val auditId: Long,
    val questionId: Long,
    val value: Int = 0,
    val comment: String? = null,
    val nonComplianceLevel: String? = null
)
