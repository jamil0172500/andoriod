package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sample.SampleContract
import com.example.data.sample.SampleContracts
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolidityCodeView(
    code: String,
    onCodeChange: (String) -> Unit,
    contractName: String,
    onContractNameChange: (String) -> Unit,
    onRunAudit: () -> Unit,
    onLoadSample: (SampleContract) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSampleMenu by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val lines = code.lines()
    val lineCount = lines.size.coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(14.dp))
            .border(1.dp, BorderStroke, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Toolbar: Contract title and action chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TARGET CONTRACT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    letterSpacing = 1.sp
                )
                BasicTextField(
                    value = contractName,
                    onValueChange = onContractNameChange,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(CyanNeon),
                    modifier = Modifier.fillMaxWidth().testTag("contract_name_input")
                )
            }

            // Sample Contract Picker
            Box {
                OutlinedButton(
                    onClick = { showSampleMenu = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(BorderStrokeHover)
                    ),
                    modifier = Modifier.testTag("sample_picker_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Load Sample",
                        tint = CyanNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Samples",
                        fontSize = 12.sp,
                        color = CyanNeon
                    )
                }

                DropdownMenu(
                    expanded = showSampleMenu,
                    onDismissRequest = { showSampleMenu = false },
                    modifier = Modifier.background(SurfaceCardElevated)
                ) {
                    SampleContracts.samples.forEach { sample ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = sample.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = sample.difficulty,
                                        fontSize = 11.sp,
                                        color = when {
                                            sample.difficulty.contains("Critical") -> SeverityCritical
                                            sample.difficulty.contains("High") -> SeverityHigh
                                            else -> SeveritySecure
                                        }
                                    )
                                }
                            },
                            onClick = {
                                onLoadSample(sample)
                                showSampleMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Code Editor Box with Line Numbers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = 280.dp)
                .background(CodeBackground, RoundedCornerShape(8.dp))
                .border(1.dp, BorderStroke, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
            ) {
                // Line Numbers Gutter
                Column(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .widthIn(min = 28.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lineCount) {
                        Text(
                            text = "$i",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(BorderStroke)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Editable Solidity Code
                BasicTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(CyanNeon),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contract_code_input")
                )
            }
        }

        // Action Buttons Row: Paste, Clear, Run Audit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Paste button
                IconButton(
                    onClick = {
                        val clip = clipboardManager.getText()?.text
                        if (!clip.isNullOrEmpty()) {
                            onCodeChange(clip)
                            Toast.makeText(context, "Code pasted from clipboard", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste Code",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Clear button
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .size(38.dp)
                        .background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Code",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // SLOC Counter Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .background(SurfaceVariantDark, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$lineCount SLOC",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
            }

            // Big Audit Scanner Button
            Button(
                onClick = onRunAudit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanNeon,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(40.dp)
                    .testTag("run_audit_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Scan Code",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
