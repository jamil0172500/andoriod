package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_audits")
data class AuditEntity(
    @PrimaryKey
    val id: String,
    val contractName: String,
    val timestamp: Long,
    val score: Int,
    val sloc: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val gasCount: Int,
    val sourceCode: String,
    val findingsSummary: String
)
