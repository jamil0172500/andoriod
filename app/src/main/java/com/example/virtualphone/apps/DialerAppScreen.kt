package com.example.virtualphone.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.virtualphone.viewmodel.VirtualPhoneViewModel

@Composable
fun DialerAppScreen(
    viewModel: VirtualPhoneViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dialedNumber by viewModel.dialedNumber.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Keypad, 1: Contacts
    var showAddContactDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tab Selector (Keypad / Contacts)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF131D31),
            contentColor = CyanNeon,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Keypad", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Contacts (${contacts.size})", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // Keypad Screen
            Spacer(modifier = Modifier.height(8.dp))

            // Dialed Number Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF131D31), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (dialedNumber.isEmpty()) "Enter phone number..." else dialedNumber,
                    fontSize = if (dialedNumber.length > 12) 18.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dialedNumber.isEmpty()) TextMuted else TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3x4 Keypad Grid
            val keys = listOf(
                listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                listOf("*" to "", "0" to "+", "#" to "")
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { (digit, sub) ->
                            DialKeyButton(digit = digit, sub = sub) {
                                viewModel.dialDigit(digit)
                            }
                        }
                    }
                }
            }

            // Bottom Actions: Backspace, Call, Clear
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear / Add to contacts
                IconButton(
                    onClick = {
                        if (dialedNumber.isNotEmpty()) {
                            showAddContactDialog = true
                        }
                    },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Contact", tint = CyanNeon, modifier = Modifier.size(24.dp))
                }

                // Big Green Call Button
                FloatingActionButton(
                    onClick = {
                        viewModel.makeRealPhoneCall(context, dialedNumber)
                    },
                    containerColor = SeveritySecure,
                    contentColor = BackgroundDark,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("dialer_call_btn")
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(30.dp))
                }

                // Backspace
                IconButton(
                    onClick = { viewModel.dialBackspace() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(24.dp))
                }
            }
        } else {
            // Contacts Tab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saved Contacts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Button(
                    onClick = { showAddContactDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = BackgroundDark),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Contact", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts, key = { it.id }) { contact ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(ElectricBlue.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(22.dp))
                                }
                                Column {
                                    Text(contact.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text(contact.phoneNumber, fontSize = 12.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { viewModel.makeRealPhoneCall(context, contact.phoneNumber) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = SeveritySecure, modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.deleteContact(contact.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddContactDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf(dialedNumber) }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Add Contact", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            viewModel.addContact(name, phone, "")
                            showAddContactDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = BackgroundDark)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun DialKeyButton(
    digit: String,
    sub: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .background(Color(0xFF162238), CircleShape)
            .border(1.dp, Color(0xFF273859), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (sub.isNotEmpty()) {
                Text(
                    text = sub,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
            }
        }
    }
}
