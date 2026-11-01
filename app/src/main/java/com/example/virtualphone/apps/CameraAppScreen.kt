package com.example.virtualphone.apps

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.virtualphone.data.PhotoEntity
import com.example.virtualphone.viewmodel.VirtualPhoneViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraAppScreen(
    viewModel: VirtualPhoneViewModel,
    modifier: Modifier = Modifier
) {
    val photos by viewModel.photos.collectAsState()
    var selectedPhoto by remember { mutableStateOf<PhotoEntity?>(null) }
    var currentTab by remember { mutableIntStateOf(0) } // 0: Shutter, 1: Gallery

    // Real Camera Launcher using Android Activity Result
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.saveCapturedPhoto(bitmap)
            currentTab = 1 // Go to gallery to view result
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
    ) {
        // Mode Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentTab == 0) "Camera Viewfinder" else "Captured Photos (${photos.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { currentTab = 0 },
                    modifier = Modifier
                        .size(38.dp)
                        .background(if (currentTab == 0) CyanNeon.copy(alpha = 0.2f) else Color(0xFF131D31), CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = if (currentTab == 0) CyanNeon else TextMuted, modifier = Modifier.size(20.dp))
                }
                IconButton(
                    onClick = { currentTab = 1 },
                    modifier = Modifier
                        .size(38.dp)
                        .background(if (currentTab == 1) CyanNeon.copy(alpha = 0.2f) else Color(0xFF131D31), CircleShape)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = if (currentTab == 1) CyanNeon else TextMuted, modifier = Modifier.size(20.dp))
                }
            }
        }

        if (currentTab == 0) {
            // Viewfinder & Shutter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF10192A))
                    .border(1.dp, BorderStroke, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = CyanNeon.copy(alpha = 0.6f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Real Device Camera Hardware",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tap the shutter below to take real photos using your device camera.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Big Shutter Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .border(3.dp, CyanNeon, CircleShape)
                        .padding(6.dp)
                        .background(Color.White, CircleShape)
                        .clickable { takePictureLauncher.launch(null) }
                        .testTag("camera_shutter_btn")
                )
            }
        } else {
            // Gallery Grid
            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Text("No photos captured yet", color = TextSecondary, fontSize = 14.sp)
                        Button(
                            onClick = { currentTab = 0 },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = BackgroundDark)
                        ) {
                            Text("Take a Photo")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(photos, key = { it.id }) { photo ->
                        val bytes = remember(photo.imageBase64) {
                            try { Base64.decode(photo.imageBase64, Base64.DEFAULT) } catch (_: Exception) { null }
                        }
                        val bitmap = remember(bytes) {
                            bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF131D31))
                                .clickable { selectedPhoto = photo }
                        ) {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Photo Viewer Modal
    selectedPhoto?.let { photo ->
        val bytes = remember(photo.imageBase64) {
            try { Base64.decode(photo.imageBase64, Base64.DEFAULT) } catch (_: Exception) { null }
        }
        val bitmap = remember(bytes) {
            bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        }
        val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(photo.timestamp))

        AlertDialog(
            onDismissRequest = { selectedPhoto = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dateStr, fontSize = 13.sp, color = TextPrimary)
                    IconButton(
                        onClick = {
                            viewModel.deletePhoto(photo.id)
                            selectedPhoto = null
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SeverityCritical)
                    }
                }
            },
            text = {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Full Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPhoto = null }) {
                    Text("Close", color = CyanNeon)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
