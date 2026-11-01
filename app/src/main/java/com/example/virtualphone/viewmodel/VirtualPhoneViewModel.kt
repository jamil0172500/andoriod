package com.example.virtualphone.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.telephony.TelephonyManager
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.virtualphone.data.ContactEntity
import com.example.virtualphone.data.InstalledAppEntity
import com.example.virtualphone.data.PhotoEntity
import com.example.virtualphone.data.VaultNoteEntity
import com.example.virtualphone.model.PhoneAppId
import com.example.virtualphone.model.WebAppItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class PhoneWallpaper(val title: String, val bgStart: Long, val bgEnd: Long) {
    CYBER_NEON("Cyber Neon", 0xFF0A0F1D, 0xFF0D1B2A),
    DEEP_SPACE("Deep Space", 0xFF0B091A, 0xFF1B1438),
    SUNSET_GLOW("Sunset Glow", 0xFF1A0B1A, 0xFF351226),
    AMOLED_VOID("AMOLED Black", 0xFF000000, 0xFF0A0A0A),
    EMERALD_MATRIX("Matrix Emerald", 0xFF05140F, 0xFF0A291E)
}

class VirtualPhoneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val phoneDao = db.phoneDao()

    // Navigation & Window Management
    private val _currentApp = MutableStateFlow(PhoneAppId.HOME)
    val currentApp = _currentApp.asStateFlow()

    private val _appStack = mutableListOf<PhoneAppId>(PhoneAppId.HOME)

    private val _recentApps = MutableStateFlow<List<PhoneAppId>>(emptyList())
    val recentApps = _recentApps.asStateFlow()

    // UI overlays
    private val _isQuickSettingsOpen = MutableStateFlow(false)
    val isQuickSettingsOpen = _isQuickSettingsOpen.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked = _isLocked.asStateFlow()

    private val _vaultPin = MutableStateFlow("1234")
    val vaultPin = _vaultPin.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked = _isVaultUnlocked.asStateFlow()

    // Settings
    private val _wallpaper = MutableStateFlow(PhoneWallpaper.CYBER_NEON)
    val wallpaper = _wallpaper.asStateFlow()

    // Quick Settings Toggles
    private val _isWifiEnabled = MutableStateFlow(true)
    val isWifiEnabled = _isWifiEnabled.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(true)
    val isBluetoothEnabled = _isBluetoothEnabled.asStateFlow()

    private val _isMobileDataEnabled = MutableStateFlow(true)
    val isMobileDataEnabled = _isMobileDataEnabled.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn = _isFlashlightOn.asStateFlow()

    private val _brightness = MutableStateFlow(0.85f)
    val brightness = _brightness.asStateFlow()

    // Telemetry: Real Battery & Network
    private val _batteryLevel = MutableStateFlow(88)
    val batteryLevel = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging = _isCharging.asStateFlow()

    private val _networkType = MutableStateFlow("5G WiFi")
    val networkType = _networkType.asStateFlow()

    private val _simCarrier = MutableStateFlow("Cellular Active")
    val simCarrier = _simCarrier.asStateFlow()

    // Real Browser State
    private val _browserUrl = MutableStateFlow("https://www.google.com")
    val browserUrl = _browserUrl.asStateFlow()

    // Dialer State
    private val _dialedNumber = MutableStateFlow("")
    val dialedNumber = _dialedNumber.asStateFlow()

    // Calculator State
    private val _calcDisplay = MutableStateFlow("0")
    val calcDisplay = _calcDisplay.asStateFlow()
    private var calcFirstOperand = 0.0
    private var calcOperation = ""
    private var calcIsNewNumber = true

    // Room DB Observables
    val contacts = phoneDao.getAllContacts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val vaultNotes = phoneDao.getAllVaultNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val photos = phoneDao.getAllPhotos().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val installedApps = phoneDao.getAllInstalledApps().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Default App Store Catalog
    val availableWebApps = listOf(
        WebAppItem("youtube", "YouTube", "https://m.youtube.com", "Media", "Watch videos, tutorials, and music stream."),
        WebAppItem("google", "Google Search", "https://www.google.com", "Productivity", "The world's leading search engine."),
        WebAppItem("wiki", "Wikipedia", "https://en.m.wikipedia.org", "Knowledge", "The free encyclopedia."),
        WebAppItem("reddit", "Reddit", "https://www.reddit.com", "Social", "Community forums and discussions."),
        WebAppItem("duckduckgo", "DuckDuckGo", "https://duckduckgo.com", "Privacy", "Privacy-focused search engine without tracking."),
        WebAppItem("github", "GitHub", "https://github.com", "Developer", "Code hosting and open source collaboration."),
        WebAppItem("spotify", "Spotify Web", "https://open.spotify.com", "Music", "Stream music and podcasts.")
    )

    init {
        fetchRealDeviceTelemetry()
        seedInitialData()
    }

    private fun fetchRealDeviceTelemetry() {
        val context = getApplication<Application>()
        try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 88
            _batteryLevel.value = if (level in 1..100) level else 88

            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNet = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(activeNet)
            when {
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> _networkType.value = "WiFi 6"
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> _networkType.value = "5G LTE"
                else -> _networkType.value = "Online"
            }

            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val opName = tm?.networkOperatorName
            if (!opName.isNullOrBlank()) {
                _simCarrier.value = opName
            }
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            // Seed sample contacts if empty
            if (phoneDao.getAllContacts().first().isEmpty()) {
                phoneDao.insertContact(ContactEntity(UUID.randomUUID().toString(), "Emergency Helpline", "999", "helpline@gov"))
                phoneDao.insertContact(ContactEntity(UUID.randomUUID().toString(), "Telecom Customer Care", "121", "care@telecom"))
                phoneDao.insertContact(ContactEntity(UUID.randomUUID().toString(), "SecOps Emergency", "+18005550199", "security@aegis"))
            }
            // Seed a welcome note in the encrypted vault
            if (phoneDao.getAllVaultNotes().first().isEmpty()) {
                phoneDao.insertVaultNote(
                    VaultNoteEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Welcome to VirtuDroid Secure OS",
                        encryptedContent = "Your files and notes here are isolated in this secure virtual container. Use your PIN (default: 1234) to unlock sensitive spaces.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // App Navigation
    fun openApp(appId: PhoneAppId, customUrl: String? = null) {
        if (customUrl != null) {
            _browserUrl.value = customUrl
        }
        if (_currentApp.value != appId) {
            _appStack.add(appId)
            _currentApp.value = appId

            // Update recents
            val currentRecents = _recentApps.value.toMutableList()
            currentRecents.remove(appId)
            currentRecents.add(0, appId)
            _recentApps.value = currentRecents.take(6)
        }
        _isQuickSettingsOpen.value = false
    }

    fun openBrowserWithUrl(url: String) {
        _browserUrl.value = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://www.google.com/search?q=${Uri.encode(url)}"
        openApp(PhoneAppId.BROWSER)
    }

    fun pressHome() {
        _currentApp.value = PhoneAppId.HOME
        _isQuickSettingsOpen.value = false
    }

    fun pressBack(): Boolean {
        if (_isQuickSettingsOpen.value) {
            _isQuickSettingsOpen.value = false
            return true
        }
        if (_currentApp.value != PhoneAppId.HOME) {
            _currentApp.value = PhoneAppId.HOME
            return true
        }
        return false
    }

    fun toggleQuickSettings() {
        _isQuickSettingsOpen.value = !_isQuickSettingsOpen.value
    }

    fun closeQuickSettings() {
        _isQuickSettingsOpen.value = false
    }

    fun toggleLockScreen() {
        _isLocked.value = !_isLocked.value
    }

    fun unlockPhone() {
        _isLocked.value = false
    }

    fun setWallpaper(w: PhoneWallpaper) {
        _wallpaper.value = w
    }

    fun setBrightness(b: Float) {
        _brightness.value = b.coerceIn(0.1f, 1f)
    }

    fun toggleWifi() { _isWifiEnabled.value = !_isWifiEnabled.value }
    fun toggleBluetooth() { _isBluetoothEnabled.value = !_isBluetoothEnabled.value }
    fun toggleMobileData() { _isMobileDataEnabled.value = !_isMobileDataEnabled.value }
    fun toggleFlashlight() { _isFlashlightOn.value = !_isFlashlightOn.value }

    // Dialer Logic
    fun dialDigit(digit: String) {
        _dialedNumber.value += digit
    }

    fun dialBackspace() {
        if (_dialedNumber.value.isNotEmpty()) {
            _dialedNumber.value = _dialedNumber.value.dropLast(1)
        }
    }

    fun clearDialer() {
        _dialedNumber.value = ""
    }

    fun makeRealPhoneCall(context: Context, number: String) {
        val num = if (number.isNotBlank()) number else _dialedNumber.value
        if (num.isNotBlank()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(num)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun addContact(name: String, phone: String, email: String) {
        viewModelScope.launch {
            phoneDao.insertContact(ContactEntity(UUID.randomUUID().toString(), name, phone, email))
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch {
            phoneDao.deleteContact(id)
        }
    }

    // Photo Capture Logic
    fun saveCapturedPhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val base64 = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
            phoneDao.insertPhoto(PhotoEntity(UUID.randomUUID().toString(), System.currentTimeMillis(), base64))
        }
    }

    fun deletePhoto(id: String) {
        viewModelScope.launch {
            phoneDao.deletePhoto(id)
        }
    }

    // Calculator Logic
    fun calcPress(btn: String) {
        when (btn) {
            "C" -> {
                _calcDisplay.value = "0"
                calcFirstOperand = 0.0
                calcOperation = ""
                calcIsNewNumber = true
            }
            "±" -> {
                val cur = _calcDisplay.value.toDoubleOrNull() ?: 0.0
                _calcDisplay.value = if (cur % 1 == 0.0) (-cur.toLong()).toString() else (-cur).toString()
            }
            "%" -> {
                val cur = _calcDisplay.value.toDoubleOrNull() ?: 0.0
                _calcDisplay.value = (cur / 100.0).toString()
            }
            "+", "-", "×", "÷" -> {
                calcFirstOperand = _calcDisplay.value.toDoubleOrNull() ?: 0.0
                calcOperation = btn
                calcIsNewNumber = true
            }
            "=" -> {
                val secondOperand = _calcDisplay.value.toDoubleOrNull() ?: 0.0
                val result = when (calcOperation) {
                    "+" -> calcFirstOperand + secondOperand
                    "-" -> calcFirstOperand - secondOperand
                    "×" -> calcFirstOperand * secondOperand
                    "÷" -> if (secondOperand != 0.0) calcFirstOperand / secondOperand else Double.NaN
                    else -> secondOperand
                }
                _calcDisplay.value = if (result.isNaN()) "Error" else if (result % 1 == 0.0) result.toLong().toString() else "%.4f".format(result).trimEnd('0').trimEnd('.')
                calcIsNewNumber = true
                calcOperation = ""
            }
            "." -> {
                if (!_calcDisplay.value.contains(".")) {
                    _calcDisplay.value += "."
                    calcIsNewNumber = false
                }
            }
            else -> {
                if (calcIsNewNumber || _calcDisplay.value == "0" || _calcDisplay.value == "Error") {
                    _calcDisplay.value = btn
                    calcIsNewNumber = false
                } else {
                    _calcDisplay.value += btn
                }
            }
        }
    }

    // Vault Logic
    fun verifyVaultPin(enteredPin: String): Boolean {
        return if (enteredPin == _vaultPin.value) {
            _isVaultUnlocked.value = true
            true
        } else false
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun addVaultNote(title: String, content: String) {
        viewModelScope.launch {
            phoneDao.insertVaultNote(
                VaultNoteEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    encryptedContent = content,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteVaultNote(id: String) {
        viewModelScope.launch {
            phoneDao.deleteVaultNote(id)
        }
    }

    // App Store
    fun installWebApp(item: WebAppItem) {
        viewModelScope.launch {
            phoneDao.installApp(InstalledAppEntity(item.id, item.name, item.url, item.category))
        }
    }

    fun uninstallWebApp(id: String) {
        viewModelScope.launch {
            phoneDao.uninstallApp(id)
        }
    }
}
