package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AuditViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditReportScreen(
    viewModel: AuditViewModel,
    modifier: Modifier = Modifier
) {
    val currentReport by viewModel.currentReport.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    if (currentReport == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No audit report generated yet.\nPlease scan a contract in the Audit tab first.",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
        return
    }

    val report = currentReport!!
    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(report.timestamp))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Report Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderStroke, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SMART CONTRACT AUDIT CERTIFICATE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = dateStr,
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = report.contractName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Score and Risk Rating Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Score: ${report.score}/100",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    report.score >= 80 -> SeveritySecure
                                    report.score >= 60 -> SeverityMedium
                                    else -> SeverityCritical
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    when {
                                        report.score >= 80 -> SeveritySecureBg
                                        report.score >= 60 -> SeverityMediumBg
                                        else -> SeverityCriticalBg
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = report.riskRating,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    report.score >= 80 -> SeveritySecure
                                    report.score >= 60 -> SeverityMedium
                                    else -> SeverityCritical
                                }
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons: Share & Copy Markdown
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.shareReport(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = BackgroundDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp).testTag("share_report_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val md = generateMarkdownReport(report)
                        clipboardManager.setText(AnnotatedString(md))
                        Toast.makeText(context, "Markdown report copied", Toast.LENGTH_SHORT).show()
                    },
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderStrokeHover)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp).testTag("copy_markdown_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Markdown", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        // Metrics Grid (4 Stats)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(label = "SLOC", value = "${report.sloc}", modifier = Modifier.weight(1f))
                MetricCard(label = "Functions", value = "${report.functionCount}", modifier = Modifier.weight(1f))
                MetricCard(label = "State Vars", value = "${report.stateVarCount}", modifier = Modifier.weight(1f))
                MetricCard(label = "Solidity", value = report.compilerVersion.take(8), modifier = Modifier.weight(1f))
            }
        }

        // Severity Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderStroke, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "VULNERABILITY SEVERITY BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SeverityCountItem(label = "Critical", count = report.criticalCount, color = SeverityCritical)
                        SeverityCountItem(label = "High", count = report.highCount, color = SeverityHigh)
                        SeverityCountItem(label = "Medium", count = report.mediumCount, color = SeverityMedium)
                        SeverityCountItem(label = "Low", count = report.lowCount, color = SeverityLow)
                        SeverityCountItem(label = "Gas", count = report.gasCount, color = SeverityGas)
                    }
                }
            }
        }

        // Executive Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderStroke, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "EXECUTIVE SUMMARY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (report.findings.isEmpty()) {
                            "The automated security inspection completed with zero identified vulnerabilities. The smart contract adheres to standard security patterns and compiler recommendations. Comprehensive dynamic fuzz testing is still advised prior to mainnet deployment."
                        } else {
                            "The automated static analyzer detected ${report.findings.size} issues across the contract codebase. Immediate remediation is mandatory for all Critical and High severity findings to prevent potential funds drainage, reentrancy attacks, or access control exploits."
                        },
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Detailed Findings Summary List
        item {
            Text(
                text = "DETAILED FINDINGS (${report.findings.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        report.findings.forEachIndexed { index, finding ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(10.dp))
                        .border(1.dp, BorderStroke, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${index + 1} [${finding.severity.displayName}] ${finding.swcId}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = finding.severity.badgeColor,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Line ${finding.lineNumber}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = finding.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = finding.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(SurfaceCard, RoundedCornerShape(10.dp))
            .border(1.dp, BorderStroke, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = label, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
fun SeverityCountItem(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = TextMuted)
    }
}

fun generateMarkdownReport(report: com.example.data.model.AuditReport): String {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(report.timestamp))
    return buildString {
        appendLine("# Smart Contract Security Audit: ${report.contractName}")
        appendLine("**Date:** $dateStr | **Security Score:** ${report.score}/100 [${report.riskRating}]")
        appendLine("")
        appendLine("### Summary")
        appendLine("- **SLOC:** ${report.sloc}")
        appendLine("- **Functions:** ${report.functionCount}")
        appendLine("- **Critical Issues:** ${report.criticalCount}")
        appendLine("- **High Issues:** ${report.highCount}")
        appendLine("- **Medium Issues:** ${report.mediumCount}")
        appendLine("- **Low Issues:** ${report.lowCount}")
        appendLine("- **Gas Optimizations:** ${report.gasCount}")
        appendLine("")
        appendLine("### Findings")
        report.findings.forEachIndexed { i, f ->
            appendLine("#### ${i + 1}. [${f.severity.displayName}] ${f.swcId}: ${f.title}")
            appendLine("- **Line:** ${f.lineNumber}")
            appendLine("- **Vulnerable Code:** `${f.codeSnippet}`")
            appendLine("- **Description:** ${f.description}")
            appendLine("- **Remediation:** ${f.remediation}")
            appendLine("")
        }
    }
}
