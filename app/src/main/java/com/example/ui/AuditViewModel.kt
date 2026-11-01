package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.SmartContractAuditEngine
import com.example.data.local.AppDatabase
import com.example.data.local.AuditEntity
import com.example.data.model.AuditReport
import com.example.data.model.ChecklistCategory
import com.example.data.sample.SampleContract
import com.example.data.model.VulnerabilityFinding
import com.example.data.model.VulnerabilitySeverity
import com.example.data.repository.AuditRepository
import com.example.data.sample.SampleContracts
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab(val label: String) {
    AUDIT("Audit Scanner"),
    REPORT("Executive Report"),
    CHECKLIST("Audit Checklist"),
    SWC_MATRIX("SWC Registry"),
    HISTORY("Audit History")
}

class AuditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuditRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AuditRepository(database.auditDao())
    }

    val savedAudits = repository.savedAudits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val checklistCategories = repository.checklistState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current navigation tab
    private val _currentTab = MutableStateFlow(AppTab.AUDIT)
    val currentTab = _currentTab.asStateFlow()

    // Smart contract code in editor
    private val defaultSample = SampleContracts.samples.first()
    private val _sourceCode = MutableStateFlow(defaultSample.code)
    val sourceCode = _sourceCode.asStateFlow()

    private val _contractName = MutableStateFlow(defaultSample.title)
    val contractName = _contractName.asStateFlow()

    // Scanning state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    // Active audit report
    private val _currentReport = MutableStateFlow<AuditReport?>(null)
    val currentReport = _currentReport.asStateFlow()

    // Filters
    private val _selectedSeverityFilter = MutableStateFlow<VulnerabilitySeverity?>(null)
    val selectedSeverityFilter = _selectedSeverityFilter.asStateFlow()

    private val _findingSearchQuery = MutableStateFlow("")
    val findingSearchQuery = _findingSearchQuery.asStateFlow()

    // Notification toast / snackbar event
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        // Automatically perform an initial baseline audit so the user sees results immediately
        runAudit(initial = true)
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun updateSourceCode(newCode: String) {
        _sourceCode.value = newCode
    }

    fun updateContractName(name: String) {
        _contractName.value = name
    }

    fun setSeverityFilter(severity: VulnerabilitySeverity?) {
        _selectedSeverityFilter.value = severity
    }

    fun setSearchQuery(query: String) {
        _findingSearchQuery.value = query
    }

    fun loadSample(sample: SampleContract) {
        _sourceCode.value = sample.code
        _contractName.value = sample.title
        runAudit()
    }

    fun clearCode() {
        _sourceCode.value = ""
        _contractName.value = "NewContract"
        _currentReport.value = null
    }

    fun runAudit(initial: Boolean = false) {
        val code = _sourceCode.value
        if (code.isBlank()) {
            _currentReport.value = null
            return
        }

        viewModelScope.launch {
            if (!initial) {
                _isAnalyzing.value = true
                delay(650) // Realistic scanning feedback delay
            }
            val report = SmartContractAuditEngine.analyze(code, _contractName.value)
            _currentReport.value = report
            _isAnalyzing.value = false

            if (!initial) {
                _uiEvent.emit("Audit complete: ${report.findings.size} issues identified (Score: ${report.score}/100)")
            }
        }
    }

    fun saveCurrentReport() {
        val report = _currentReport.value ?: return
        viewModelScope.launch {
            repository.saveAudit(report)
            _uiEvent.emit("Audit report saved to History")
        }
    }

    fun loadFromHistory(audit: AuditEntity) {
        _sourceCode.value = audit.sourceCode
        _contractName.value = audit.contractName
        _currentTab.value = AppTab.AUDIT
        runAudit()
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            repository.deleteAudit(id)
            _uiEvent.emit("Audit record deleted")
        }
    }

    fun toggleChecklist(catName: String, itemId: String) {
        repository.toggleChecklistItem(catName, itemId)
    }

    fun resetChecklist() {
        repository.resetChecklist()
        viewModelScope.launch {
            _uiEvent.emit("Checklist reset to default")
        }
    }

    fun applyFix(finding: VulnerabilityFinding) {
        val current = _sourceCode.value
        if (finding.codeSnippet.isNotEmpty() && finding.patchedSnippet.isNotEmpty() && current.contains(finding.codeSnippet)) {
            val updated = current.replaceFirst(finding.codeSnippet, finding.patchedSnippet)
            _sourceCode.value = updated
            runAudit()
            viewModelScope.launch {
                _uiEvent.emit("Applied security patch for ${finding.swcId}")
            }
        } else {
            viewModelScope.launch {
                _uiEvent.emit("Could not automatically replace snippet. Please apply manually from recommendations.")
            }
        }
    }

    fun shareReport(context: Context) {
        val report = _currentReport.value ?: return
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(report.timestamp))
        val text = buildString {
            appendLine("🛡️ SMART CONTRACT SECURITY AUDIT REPORT")
            appendLine("Contract: ${report.contractName}")
            appendLine("Date: $dateStr")
            appendLine("Security Score: ${report.score}/100 [${report.riskRating}]")
            appendLine("Source Lines: ${report.sloc} | Functions: ${report.functionCount}")
            appendLine("----------------------------------------")
            appendLine("FINDINGS SUMMARY:")
            appendLine("🚨 Critical: ${report.criticalCount}")
            appendLine("⚠️ High: ${report.highCount}")
            appendLine("🟡 Medium: ${report.mediumCount}")
            appendLine("🔵 Low: ${report.lowCount}")
            appendLine("⚡ Gas Optimization: ${report.gasCount}")
            appendLine("----------------------------------------")
            report.findings.forEachIndexed { i, f ->
                appendLine("[#${i + 1}] [${f.severity.displayName}] ${f.swcId}: ${f.title}")
                appendLine("Line ${f.lineNumber}: ${f.codeSnippet}")
                appendLine("Issue: ${f.description}")
                appendLine("Bangla Tip: ${f.bengaliTip}")
                appendLine("Fix: ${f.remediation}")
                appendLine("")
            }
            appendLine("Generated by AuditGuard - Smart Contract Security Auditor")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Smart Contract Audit Report")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
