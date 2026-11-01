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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import com.example.data.model.SwcDatabase
import com.example.data.model.SwcItem
import com.example.ui.theme.*

@Composable
fun SwcMatrixScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(searchQuery) {
        if (searchQuery.isBlank()) SwcDatabase.items
        else {
            SwcDatabase.items.filter { item ->
                item.swcId.contains(searchQuery, ignoreCase = true) ||
                        item.title.contains(searchQuery, ignoreCase = true) ||
                        item.summary.contains(searchQuery, ignoreCase = true) ||
                        item.bengaliExplanation.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search SWC registry (e.g. SWC-107, reentrancy)...", fontSize = 12.sp, color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
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
                    .height(50.dp)
                    .testTag("swc_search_input")
            )
        }

        item {
            Text(
                text = "SWC REGISTRY & VULNERABILITY MATRIX (${filteredItems.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        items(filteredItems, key = { it.swcId }) { item ->
            SwcDetailCard(item = item)
        }
    }
}

@Composable
fun SwcDetailCard(item: SwcItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderStroke, RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .testTag("swc_card_${item.swcId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(item.severity.badgeBgColor, RoundedCornerShape(6.dp))
                            .border(1.dp, item.severity.badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.severity.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = item.severity.badgeColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = item.swcId,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Text(
                text = item.summary,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            // Bengali Help box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyanNeon.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "💡 বাংলা টিপ: ${item.bengaliExplanation}",
                    fontSize = 11.sp,
                    color = TextPrimary,
                    lineHeight = 15.sp
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Vulnerable Pattern Box
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "❌ VULNERABLE CODE PATTERN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SeverityCritical,
                            letterSpacing = 0.8.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CodeBackground, RoundedCornerShape(6.dp))
                                .border(1.dp, SeverityCritical.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = item.vulnerablePattern,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = SeverityCritical
                            )
                        }
                    }

                    // Secure Pattern Box
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "✅ SECURE REMEDIATION PATTERN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SeveritySecure,
                            letterSpacing = 0.8.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CodeBackground, RoundedCornerShape(6.dp))
                                .border(1.dp, SeveritySecure.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = item.securePattern,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = SeveritySecure
                            )
                        }
                    }
                }
            }
        }
    }
}
