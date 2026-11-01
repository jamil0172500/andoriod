package com.example.virtualphone.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class PhoneAppId(val title: String) {
    HOME("Home"),
    BROWSER("Browser"),
    YOUTUBE("YouTube"),
    DIALER("Phone"),
    MESSAGES("Messages"),
    CAMERA("Camera"),
    GALLERY("Photos"),
    CALCULATOR("Calculator"),
    VAULT("Secure Vault"),
    APP_STORE("App Store"),
    SETTINGS("Settings"),
    CLOCK("Clock")
}

data class VirtualApp(
    val id: PhoneAppId,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val isSystemApp: Boolean = true
)

data class ContactItem(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String = ""
)

data class VaultNote(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

data class GalleryPhoto(
    val id: String,
    val timestamp: Long,
    val bitmapBase64: String
)

data class WebAppItem(
    val id: String,
    val name: String,
    val url: String,
    val category: String,
    val description: String,
    val isInstalled: Boolean = true
)
