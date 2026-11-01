package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AuditViewModel
import com.example.ui.theme.*

@Composable
fun ChecklistScreen(
    viewModel: AuditViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.checklistCategories.collectAsState()

    val totalItems = categories.sumOf { it.items.size }
    val checkedItems = categories.sumOf { cat -> cat.items.count { it.isChecked } }
    val progressPercent = if (totalItems > 0) (checkedItems * 100) / totalItems else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderStroke, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AUDIT VERIFICATION PROGRESS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$checkedItems of $totalItems checks passed ($progressPercent%)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.resetChecklist() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Checklist",
                                tint = TextSecondary
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (progressPercent >= 100) SeveritySecure else CyanNeon,
                        trackColor = SurfaceVariantDark,
                    )
                }
            }
        }

        // Categories & Checklist Items
        categories.forEach { category ->
            item {
                Text(
                    text = category.name.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(category.items, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (item.isChecked) SeveritySecure.copy(alpha = 0.5f) else BorderStroke,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.toggleChecklist(category.name, item.id) }
                        .testTag("checklist_item_${item.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (item.isChecked) "Checked" else "Unchecked",
                            tint = if (item.isChecked) SeveritySecure else TextMuted,
                            modifier = Modifier.size(24.dp).padding(top = 2.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.isChecked) TextSecondary else TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .background(SurfaceVariantDark, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.swcReference,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyanNeon
                                    )
                                }
                            }

                            Text(
                                text = item.description,
                                fontSize = 12.sp,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )

                            // Bengali Guidance
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyanNeon.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = "💡 ${item.bengaliExplanation}",
                                    fontSize = 11.sp,
                                    color = CyanGlow,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
