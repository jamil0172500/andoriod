package com.example.data.repository

import com.example.data.checklist.DefaultChecklistData
import com.example.data.local.AuditDao
import com.example.data.local.AuditEntity
import com.example.data.model.AuditReport
import com.example.data.model.ChecklistCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuditRepository(private val auditDao: AuditDao) {

    val savedAudits: Flow<List<AuditEntity>> = auditDao.getAllAudits()

    private val _checklistState = MutableStateFlow(DefaultChecklistData.getCategories())
    val checklistState = _checklistState.asStateFlow()

    suspend fun saveAudit(report: AuditReport) {
        val summary = buildString {
            append("Critical: ${report.criticalCount}, ")
            append("High: ${report.highCount}, ")
            append("Medium: ${report.mediumCount}, ")
            append("Low: ${report.lowCount}, ")
            append("Gas: ${report.gasCount}")
        }
        val entity = AuditEntity(
            id = report.id,
            contractName = report.contractName,
            timestamp = report.timestamp,
            score = report.score,
            sloc = report.sloc,
            criticalCount = report.criticalCount,
            highCount = report.highCount,
            mediumCount = report.mediumCount,
            lowCount = report.lowCount,
            gasCount = report.gasCount,
            sourceCode = report.sourceCode,
            findingsSummary = summary
        )
        auditDao.insertAudit(entity)
    }

    suspend fun deleteAudit(id: String) {
        auditDao.deleteAuditById(id)
    }

    suspend fun clearHistory() {
        auditDao.clearAll()
    }

    fun toggleChecklistItem(categoryId: String, itemId: String) {
        _checklistState.update { categories ->
            categories.map { category ->
                if (category.name == categoryId) {
                    category.copy(
                        items = category.items.map { item ->
                            if (item.id == itemId) item.copy(isChecked = !item.isChecked)
                            else item
                        }
                    )
                } else {
                    category
                }
            }
        }
    }

    fun resetChecklist() {
        _checklistState.value = DefaultChecklistData.getCategories()
    }
}
