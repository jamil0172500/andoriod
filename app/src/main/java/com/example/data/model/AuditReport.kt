package com.example.data.model

data class AuditReport(
    val id: String,
    val contractName: String,
    val timestamp: Long,
    val sourceCode: String,
    val score: Int,
    val sloc: Int,
    val functionCount: Int,
    val stateVarCount: Int,
    val compilerVersion: String,
    val findings: List<VulnerabilityFinding>
) {
    val criticalCount: Int get() = findings.count { it.severity == VulnerabilitySeverity.CRITICAL }
    val highCount: Int get() = findings.count { it.severity == VulnerabilitySeverity.HIGH }
    val mediumCount: Int get() = findings.count { it.severity == VulnerabilitySeverity.MEDIUM }
    val lowCount: Int get() = findings.count { it.severity == VulnerabilitySeverity.LOW }
    val gasCount: Int get() = findings.count { it.severity == VulnerabilitySeverity.GAS_OPTIMIZATION }

    val riskRating: String
        get() = when {
            criticalCount > 0 -> "CRITICAL RISK"
            highCount > 0 -> "HIGH RISK"
            mediumCount > 0 -> "MEDIUM RISK"
            lowCount > 0 -> "LOW RISK"
            findings.isEmpty() -> "SECURE / AUDIT PASSED"
            else -> "OPTIMIZATION NEEDED"
        }
}
