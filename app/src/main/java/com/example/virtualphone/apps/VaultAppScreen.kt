package com.example.virtualphone.apps

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.virtualphone.viewmodel.VirtualPhoneViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VaultAppScreen(
    viewModel: VirtualPhoneViewModel,
    modifier: Modifier = Modifier
) {
    val isUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val vaultNotes by viewModel.vaultNotes.collectAsState()
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    if (!isUnlocked) {
        // PIN Entry Gate
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF070B14))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(ElectricBlue.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, CyanNeon, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(36.dp))
                }

                Text(
                    text = "Encrypted Security Vault",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Enter 4-digit PIN to decrypt your vault (Default: 1234)",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 4) {
                            pinInput = it
                            pinError = false
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = pinError,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    modifier = Modifier.width(180.dp).testTag("vault_pin_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (pinError) {
                    Text("Incorrect PIN. Please try again.", color = SeverityCritical, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (!viewModel.verifyVaultPin(pinInput)) {
                            pinError = true
                        } else {
                            pinInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = BackgroundDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.width(180.dp).height(44.dp).testTag("vault_unlock_btn")
                ) {
                    Text("Unlock Vault", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        // Decrypted Vault Notes
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF070B14))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Secure Vault (Decrypted)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("AES-256 GCM Isolated Storage", fontSize = 11.sp, color = SeveritySecure, fontFamily = FontFamily.Monospace)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showAddNoteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = BackgroundDark),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = { viewModel.lockVault() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF131D31), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Notes List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(vaultNotes, key = { it.id }) { note ->
                    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(note.timestamp))
                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderStroke, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(note.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                                Text(dateStr, fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                            }

                            Text(note.encryptedContent, fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(note.encryptedContent))
                                        Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.deleteVaultNote(note.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SeverityCritical, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Add Encrypted Note", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Confidential Content") },
                        minLines = 3,
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            viewModel.addVaultNote(title, content)
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = BackgroundDark)
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
