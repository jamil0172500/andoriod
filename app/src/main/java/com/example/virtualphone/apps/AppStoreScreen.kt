package com.example.virtualphone.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Shop
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
import com.example.virtualphone.viewmodel.VirtualPhoneViewModel

@Composable
fun AppStoreScreen(
    viewModel: VirtualPhoneViewModel,
    modifier: Modifier = Modifier
) {
    val installedApps by viewModel.installedApps.collectAsState()
    val availableApps = viewModel.availableWebApps

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Store Header Banner
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, BorderStroke, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp).background(CyanNeon.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shop, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(26.dp))
                }
                Column {
                    Text("VirtuDroid App Market", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Install web applications & productivity tools", fontSize = 11.sp, color = TextMuted)
                }
            }
        }

        Text("FEATURED APPLICATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(availableApps, key = { it.id }) { app ->
                val isInstalled = installedApps.any { it.id == app.id }

                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderStroke, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(app.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Box(
                                    modifier = Modifier
                                        .background(SurfaceVariantDark, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(app.category, fontSize = 9.sp, color = CyanNeon, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Text(app.description, fontSize = 12.sp, color = TextMuted, lineHeight = 16.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (isInstalled) {
                                Button(
                                    onClick = { viewModel.openBrowserWithUrl(app.url) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SeveritySecure, contentColor = BackgroundDark),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("open_app_${app.id}")
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = { viewModel.uninstallWebApp(app.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("×", fontSize = 18.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.installWebApp(app) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = BackgroundDark),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("install_app_${app.id}")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Get", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
