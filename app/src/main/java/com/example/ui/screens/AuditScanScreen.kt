package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VulnerabilitySeverity
import com.example.ui.AppTab
import com.example.ui.AuditViewModel
import com.example.ui.components.ScoreGauge
import com.example.ui.components.SolidityCodeView
import com.example.ui.components.VulnerabilityCard
import com.example.ui.theme.*

@Composable
fun AuditScanScreen(
    viewModel: AuditViewModel,
    modifier: Modifier = Modifier
) {
    val sourceCode by viewModel.sourceCode.collectAsState()
    val contractName by viewModel.contractName.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val currentReport by viewModel.currentReport.collectAsState()
    val selectedSeverityFilter by viewModel.selectedSeverityFilter.collectAsState()
    val findingSearchQuery by viewModel.findingSearchQuery.collectAsState()

    val filteredFindings = remember(currentReport, selectedSeverityFilter, findingSearchQuery) {
        currentReport?.findings?.filter { finding ->
            val matchesSeverity = selectedSeverityFilter == null || finding.severity == selectedSeverityFilter
            val matchesQuery = findingSearchQuery.isBlank() ||
                    finding.title.contains(findingSearchQuery, ignoreCase = true) ||
                    finding.swcId.contains(findingSearchQuery, ignoreCase = true) ||
                    finding.description.contains(findingSearchQuery, ignoreCase = true)
            matchesSeverity && matchesQuery
        } ?: emptyList()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Contract Code Editor
        item {
            SolidityCodeView(
                code = sourceCode,
                onCodeChange = { viewModel.updateSourceCode(it) },
                contractName = contractName,
                onContractNameChange = { viewModel.updateContractName(it) },
                onRunAudit = { viewModel.runAudit() },
                onLoadSample = { viewModel.loadSample(it) },
                onClear = { viewModel.clearCode() }
            )
        }

        // Scanning Loading Indicator
        if (isAnalyzing) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = CyanNeon, modifier = Modifier.size(36.dp))
                        Text(
                            text = "Analyzing AST & Checking SWC Security Rules...",
                            fontSize = 13.sp,
                            color = CyanNeon,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Score Gauge
        currentReport?.let { report ->
            item {
                ScoreGauge(
                    score = report.score,
                    riskRating = report.riskRating
                )
            }

            // Quick Actions: Save to History & View Report
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveCurrentReport() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceCardElevated,
                            contentColor = CyanNeon
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("save_audit_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save Audit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.setTab(AppTab.REPORT) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("view_report_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Executive Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Severity Filter Bar & Counts
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "VULNERABILITY FINDINGS (${report.findings.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    // Severity Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SeverityFilterChip(
                            label = "ALL",
                            count = report.findings.size,
                            isSelected = selectedSeverityFilter == null,
                            color = CyanNeon,
                            onClick = { viewModel.setSeverityFilter(null) }
                        )
                        SeverityFilterChip(
                            label = "CRIT",
                            count = report.criticalCount,
                            isSelected = selectedSeverityFilter == VulnerabilitySeverity.CRITICAL,
                            color = SeverityCritical,
                            onClick = { viewModel.setSeverityFilter(VulnerabilitySeverity.CRITICAL) }
                        )
                        SeverityFilterChip(
                            label = "HIGH",
                            count = report.highCount,
                            isSelected = selectedSeverityFilter == VulnerabilitySeverity.HIGH,
                            color = SeverityHigh,
                            onClick = { viewModel.setSeverityFilter(VulnerabilitySeverity.HIGH) }
                        )
                        SeverityFilterChip(
                            label = "MED",
                            count = report.mediumCount,
                            isSelected = selectedSeverityFilter == VulnerabilitySeverity.MEDIUM,
                            color = SeverityMedium,
                            onClick = { viewModel.setSeverityFilter(VulnerabilitySeverity.MEDIUM) }
                        )
                        SeverityFilterChip(
                            label = "GAS",
                            count = report.gasCount,
                            isSelected = selectedSeverityFilter == VulnerabilitySeverity.GAS_OPTIMIZATION,
                            color = SeverityGas,
                            onClick = { viewModel.setSeverityFilter(VulnerabilitySeverity.GAS_OPTIMIZATION) }
                        )
                    }

                    // Search input
                    OutlinedTextField(
                        value = findingSearchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search findings by name or SWC...", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = BorderStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                }
            }

            // Findings List
            if (filteredFindings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard, RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (report.findings.isEmpty()) "🎉 No vulnerabilities detected!" else "No findings match filter",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (report.findings.isEmpty()) SeveritySecure else TextSecondary
                            )
                            Text(
                                text = if (report.findings.isEmpty()) "All automated static analysis rules passed safely." else "Try clearing or changing your severity search filter.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(filteredFindings, key = { it.id }) { finding ->
                    VulnerabilityCard(
                        finding = finding,
                        onApplyFix = { viewModel.applyFix(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SeverityFilterChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) color.copy(alpha = 0.2f) else SurfaceCard,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isSelected) color else BorderStroke,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else TextSecondary
            )
            if (count > 0) {
                Text(
                    text = "$count",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}
