package com.example.virtualphone.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.virtualphone.apps.*
import com.example.virtualphone.model.PhoneAppId
import com.example.virtualphone.viewmodel.VirtualPhoneViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VirtualPhoneShell(
    viewModel: VirtualPhoneViewModel,
    modifier: Modifier = Modifier
) {
    val currentApp by viewModel.currentApp.collectAsState()
    val isQuickSettingsOpen by viewModel.isQuickSettingsOpen.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val wallpaper by viewModel.wallpaper.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val networkType by viewModel.networkType.collectAsState()
    val isWifiEnabled by viewModel.isWifiEnabled.collectAsState()
    val isBluetoothEnabled by viewModel.isBluetoothEnabled.collectAsState()
    val isMobileDataEnabled by viewModel.isMobileDataEnabled.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val brightness by viewModel.brightness.collectAsState()

    var showRecentsModal by remember { mutableStateOf(false) }

    // Intercept hardware Android back to behave like virtual phone back
    BackHandler(enabled = currentApp != PhoneAppId.HOME || isQuickSettingsOpen || showRecentsModal) {
        if (showRecentsModal) {
            showRecentsModal = false
        } else {
            viewModel.pressBack()
        }
    }

    val currentTime = remember {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date())
    }
    val currentDate = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.format(Date())
    }

    // Outer Phone Shell Frame
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(wallpaper.bgStart), Color(wallpaper.bgEnd))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Live Virtual Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { viewModel.toggleQuickSettings() }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time & Notification Indicators
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentTime.take(5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Secure OS",
                        tint = SeveritySecure,
                        modifier = Modifier.size(12.dp)
                    )
                }

                // Battery, WiFi, Cellular Icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isWifiEnabled) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "WiFi",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    if (isMobileDataEnabled) {
                        Icon(
                            imageVector = Icons.Default.NetworkCell,
                            contentDescription = "5G",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "$batteryLevel%",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Icon(
                        imageVector = Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = SeveritySecure,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // 2. Main Phone Viewport (Home Screen or Active App)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLocked) {
                    // Lock Screen View
                    LockScreenView(
                        time = currentTime,
                        date = currentDate,
                        onUnlock = { viewModel.unlockPhone() }
                    )
                } else {
                    when (currentApp) {
                        PhoneAppId.HOME -> HomeScreenView(
                            time = currentTime,
                            date = currentDate,
                            viewModel = viewModel
                        )
                        PhoneAppId.BROWSER -> BrowserAppScreen(viewModel = viewModel)
                        PhoneAppId.YOUTUBE -> BrowserAppScreen(viewModel = viewModel)
                        PhoneAppId.DIALER -> DialerAppScreen(viewModel = viewModel)
                        PhoneAppId.MESSAGES -> DialerAppScreen(viewModel = viewModel)
                        PhoneAppId.CAMERA -> CameraAppScreen(viewModel = viewModel)
                        PhoneAppId.GALLERY -> CameraAppScreen(viewModel = viewModel)
                        PhoneAppId.CALCULATOR -> CalculatorAppScreen(viewModel = viewModel)
                        PhoneAppId.VAULT -> VaultAppScreen(viewModel = viewModel)
                        PhoneAppId.APP_STORE -> AppStoreScreen(viewModel = viewModel)
                        PhoneAppId.SETTINGS -> SettingsAppScreen(viewModel = viewModel)
                        PhoneAppId.CLOCK -> CalculatorAppScreen(viewModel = viewModel)
                    }
                }

                // Quick Settings Dropdown Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = isQuickSettingsOpen,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    QuickSettingsPanel(
                        isWifi = isWifiEnabled,
                        isBluetooth = isBluetoothEnabled,
                        isData = isMobileDataEnabled,
                        isFlash = isFlashlightOn,
                        brightness = brightness,
                        onToggleWifi = { viewModel.toggleWifi() },
                        onToggleBt = { viewModel.toggleBluetooth() },
                        onToggleData = { viewModel.toggleMobileData() },
                        onToggleFlash = { viewModel.toggleFlashlight() },
                        onBrightnessChange = { viewModel.setBrightness(it) },
                        onClose = { viewModel.closeQuickSettings() },
                        onLock = {
                            viewModel.closeQuickSettings()
                            viewModel.toggleLockScreen()
                        }
                    )
                }
            }

            // 3. Virtual 3-Button Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (◀)
                IconButton(
                    onClick = { viewModel.pressBack() },
                    modifier = Modifier.size(44.dp).testTag("virtual_nav_back")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Home Button (⚪)
                IconButton(
                    onClick = { viewModel.pressHome() },
                    modifier = Modifier.size(44.dp).testTag("virtual_nav_home")
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }

                // Recents Button (◽)
                IconButton(
                    onClick = { showRecentsModal = true },
                    modifier = Modifier.size(44.dp).testTag("virtual_nav_recents")
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(2.dp, Color.White, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }

    // Recent Apps Dialog
    if (showRecentsModal) {
        AlertDialog(
            onDismissRequest = { showRecentsModal = false },
            title = { Text("Recent Applications", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val appList = listOf(
                        PhoneAppId.BROWSER to "Browser (Chrome)",
                        PhoneAppId.YOUTUBE to "YouTube",
                        PhoneAppId.DIALER to "Phone & Contacts",
                        PhoneAppId.CAMERA to "Camera & Photos",
                        PhoneAppId.CALCULATOR to "Calculator",
                        PhoneAppId.VAULT to "Secure Vault",
                        PhoneAppId.APP_STORE to "App Market",
                        PhoneAppId.SETTINGS to "Settings & Telemetry"
                    )

                    appList.forEach { (id, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.openApp(id)
                                    showRecentsModal = false
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, color = TextPrimary, fontSize = 14.sp)
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.pressHome()
                    showRecentsModal = false
                }) {
                    Text("Clear All / Home", color = CyanNeon)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun HomeScreenView(
    time: String,
    date: String,
    viewModel: VirtualPhoneViewModel
) {
    var searchInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Widget: Clock & Date
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = time,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = date,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Google Search Widget
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .clickable { viewModel.openBrowserWithUrl("https://www.google.com") }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("G", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                        Text("Search Google or type URL...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }

            // Grid of Installed Virtual Apps
            val gridApps = listOf(
                Triple(PhoneAppId.BROWSER, "Chrome", Icons.Default.Language),
                Triple(PhoneAppId.YOUTUBE, "YouTube", Icons.Default.SmartDisplay),
                Triple(PhoneAppId.DIALER, "Phone", Icons.Default.Call),
                Triple(PhoneAppId.CAMERA, "Camera", Icons.Default.CameraAlt),
                Triple(PhoneAppId.CALCULATOR, "Calculator", Icons.Default.Calculate),
                Triple(PhoneAppId.VAULT, "Secure Vault", Icons.Default.Lock),
                Triple(PhoneAppId.APP_STORE, "App Market", Icons.Default.Shop),
                Triple(PhoneAppId.SETTINGS, "Settings", Icons.Default.Settings)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(gridApps) { (id, name, icon) ->
                    AppIconItem(
                        title = name,
                        icon = icon,
                        color = when (id) {
                            PhoneAppId.YOUTUBE -> Color(0xFFFF0000)
                            PhoneAppId.BROWSER -> CyanNeon
                            PhoneAppId.DIALER -> SeveritySecure
                            PhoneAppId.CAMERA -> Color(0xFFFFA000)
                            PhoneAppId.VAULT -> PurpleAccent
                            PhoneAppId.APP_STORE -> ElectricBlue
                            PhoneAppId.SETTINGS -> Color(0xFF90A4AE)
                            else -> Color(0xFF26A69A)
                        },
                        onClick = {
                            if (id == PhoneAppId.YOUTUBE) {
                                viewModel.openBrowserWithUrl("https://m.youtube.com")
                            } else {
                                viewModel.openApp(id)
                            }
                        }
                    )
                }
            }
        }

        // Pinned Bottom Dock (4 Core Apps)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(26.dp))
                .padding(vertical = 10.dp, horizontal = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockIcon(Icons.Default.Call, SeveritySecure) { viewModel.openApp(PhoneAppId.DIALER) }
                DockIcon(Icons.Default.Language, CyanNeon) { viewModel.openApp(PhoneAppId.BROWSER) }
                DockIcon(Icons.Default.SmartDisplay, Color(0xFFFF0000)) { viewModel.openBrowserWithUrl("https://m.youtube.com") }
                DockIcon(Icons.Default.CameraAlt, Color(0xFFFFA000)) { viewModel.openApp(PhoneAppId.CAMERA) }
            }
        }
    }
}

@Composable
fun AppIconItem(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("app_icon_$title")
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun DockIcon(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(color.copy(alpha = 0.25f), CircleShape)
            .border(1.dp, color, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
    }
}

@Composable
fun QuickSettingsPanel(
    isWifi: Boolean,
    isBluetooth: Boolean,
    isData: Boolean,
    isFlash: Boolean,
    brightness: Float,
    onToggleWifi: () -> Unit,
    onToggleBt: () -> Unit,
    onToggleData: () -> Unit,
    onToggleFlash: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onClose: () -> Unit,
    onLock: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0F172A).copy(alpha = 0.96f),
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quick Settings & Toggles", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onLock) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock", tint = TextSecondary)
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }
            }

            // Quick Toggle Grid (4 items)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickTogglePill("Wi-Fi", Icons.Default.Wifi, isWifi, onToggleWifi)
                QuickTogglePill("Bluetooth", Icons.Default.Bluetooth, isBluetooth, onToggleBt)
                QuickTogglePill("Mobile Data", Icons.Default.NetworkCell, isData, onToggleData)
                QuickTogglePill("Flashlight", Icons.Default.FlashlightOn, isFlash, onToggleFlash)
            }

            // Brightness Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Brightness6, contentDescription = null, tint = TextMuted)
                Slider(
                    value = brightness,
                    onValueChange = onBrightnessChange,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon)
                )
            }
        }
    }
}

@Composable
fun QuickTogglePill(title: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(if (active) CyanNeon else Color(0xFF1E293B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (active) BackgroundDark else TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
fun LockScreenView(time: String, date: String, onUnlock: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onUnlock() }
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(42.dp))
            Text(time, fontSize = 48.sp, fontWeight = FontWeight.Light, color = Color.White)
            Text(date, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onUnlock,
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = BackgroundDark),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Tap to Unlock VirtuDroid", fontWeight = FontWeight.Bold)
            }
        }
    }
}
