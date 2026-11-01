package com.example.virtualphone.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PhoneDao {
    // Contacts
    @Query("SELECT * FROM phone_contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("DELETE FROM phone_contacts WHERE id = :id")
    suspend fun deleteContact(id: String)

    // Vault Notes
    @Query("SELECT * FROM phone_vault_notes ORDER BY timestamp DESC")
    fun getAllVaultNotes(): Flow<List<VaultNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultNote(note: VaultNoteEntity)

    @Query("DELETE FROM phone_vault_notes WHERE id = :id")
    suspend fun deleteVaultNote(id: String)

    // Photos
    @Query("SELECT * FROM phone_gallery_photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Query("DELETE FROM phone_gallery_photos WHERE id = :id")
    suspend fun deletePhoto(id: String)

    // Installed Web Apps
    @Query("SELECT * FROM phone_installed_apps")
    fun getAllInstalledApps(): Flow<List<InstalledAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun installApp(app: InstalledAppEntity)

    @Query("DELETE FROM phone_installed_apps WHERE id = :id")
    suspend fun uninstallApp(id: String)
}
