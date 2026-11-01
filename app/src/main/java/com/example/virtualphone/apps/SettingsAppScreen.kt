package com.example.virtualphone.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.virtualphone.viewmodel.PhoneWallpaper
import com.example.virtualphone.viewmodel.VirtualPhoneViewModel

@Composable
fun SettingsAppScreen(
    viewModel: VirtualPhoneViewModel,
    modifier: Modifier = Modifier
) {
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val networkType by viewModel.networkType.collectAsState()
    val simCarrier by viewModel.simCarrier.collectAsState()
    val currentWallpaper by viewModel.wallpaper.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("DEVICE SETTINGS & TELEMETRY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
        }

        // Hardware & Cellular Telemetry Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderStroke, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("HARDWARE & SIM STATUS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanNeon, letterSpacing = 0.8.sp)

                    // Battery
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.BatteryFull, contentDescription = null, tint = SeveritySecure, modifier = Modifier.size(20.dp))
                            Text("Battery Level", fontSize = 14.sp, color = TextPrimary)
                        }
                        Text("$batteryLevel% ${if (isCharging) "(Charging)" else ""}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SeveritySecure, fontFamily = FontFamily.Monospace)
                    }

                    // Network & WiFi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(20.dp))
                            Text("Active Connection", fontSize = 14.sp, color = TextPrimary)
                        }
                        Text(networkType, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanNeon, fontFamily = FontFamily.Monospace)
                    }

                    // Cellular & SIM / eSIM Telemetry
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.NetworkCell, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                            Text("Cellular Carrier (SIM)", fontSize = 14.sp, color = TextPrimary)
                        }
                        Text(simCarrier, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Wallpaper Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderStroke, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Wallpaper, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                        Text("VIRTUAL PHONE WALLPAPER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PhoneWallpaper.entries.forEach { wp ->
                            val isSelected = currentWallpaper == wp
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(100.dp)
                                    .background(Color(wp.bgStart), RoundedCornerShape(10.dp))
                                    .border(
                                        2.dp,
                                        if (isSelected) CyanNeon else BorderStroke,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setWallpaper(wp) }
                                    .padding(6.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = wp.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CyanNeon else TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // GrapheneOS Hardening & Real World Secure OS Guide
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderStroke, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = SeveritySecure, modifier = Modifier.size(20.dp))
                        Text("WORLD'S MOST SECURE OS GUIDE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SeveritySecure)
                    }

                    Text(
                        text = "GrapheneOS & Hardened Mobile Setup (বাংলা ও ইংরেজি গাইড):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Text(
                        text = "১. GrapheneOS হলো বিশ্বের শীর্ষস্থানীয় হার্ডেনড অ্যান্ড্রয়েড ওএস (Google Pixel ডিভাইসের জন্য তৈরি)। এতে টাইটান এম২ হার্ডওয়্যার সিকিউরিটি চিপ ব্যবহৃত হয়।\n" +
                                "২. ইনস্টলেশন: ফোনের Settings > About Phone-এ গিয়ে Build Number-এ ৭ বার ট্যাপ করে Developer Options আনলক করুন, এরপর 'OEM Unlocking' ও 'USB Debugging' অন করুন।\n" +
                                "৩. ওয়েব ব্রাউজারের অফিসিয়াল WebUSB ইনস্টলার দিয়ে বুটলোডার আনলক করে রম ফ্ল্যাশ করার পর অবশ্যই বুটলোডার পুনরায় Lock করতে হবে (Verified Boot সিকিউরিটি বজায় রাখার জন্য)।\n" +
                                "৪. কোনো Google ট্র্যাকিং থাকে না; প্রয়োজন হলে সম্পূর্ণ স্যান্ডবক্সড গুগল প্লে সার্ভিসেস ইনস্টল করা যায়, যা সিস্টেম পারমিশন অ্যাক্সেস করতে পারে না।",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
