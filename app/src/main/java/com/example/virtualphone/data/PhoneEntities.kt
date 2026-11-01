package com.example.virtualphone.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_contacts")
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String = ""
)

@Entity(tableName = "phone_vault_notes")
data class VaultNoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val encryptedContent: String,
    val timestamp: Long
)

@Entity(tableName = "phone_gallery_photos")
data class PhotoEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val imageBase64: String
)

@Entity(tableName = "phone_installed_apps")
data class InstalledAppEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val url: String,
    val category: String
)
