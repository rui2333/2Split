package com.split.android.screens

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.split.android.ocr.ReceiptOCRProcessor
import com.split.shared.models.Item
import com.split.shared.models.Person
import com.split.shared.models.Split
import kotlinx.coroutines.launch
import java.util.UUID

data class ReceiptUpload(
    val id: String,
    val name: String,
    val bitmap: Bitmap,
    val isProcessing: Boolean = false,
    val items: List<Item> = emptyList()
)

@Composable
fun HomeScreen(navController: NavController) {
    var splitName by remember { mutableStateOf("Split a bill") }
    var people by remember { mutableStateOf(listOf(
        Person(UUID.randomUUID().toString(), "You", "👤"),
        Person(UUID.randomUUID().toString(), "Sam", "👤")
    )) }

    var isUploadMode by remember { mutableStateOf(false) }
    var receipts = remember { mutableStateListOf<ReceiptUpload>() }
    var currentSplitId by remember { mutableStateOf("") }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val ocrProcessor = remember { ReceiptOCRProcessor() }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val receiptId = UUID.randomUUID().toString()
            val receipt = ReceiptUpload(
                id = receiptId,
                name = "Receipt ${receipts.size + 1}",
                bitmap = bitmap,
                isProcessing = true
            )
            receipts.add(receipt)

            scope.launch {
                val items = ocrProcessor.processReceipt(bitmap)
                val index = receipts.indexOfFirst { it.id == receiptId }
                if (index >= 0) {
                    receipts[index] = receipts[index].copy(
                        isProcessing = false,
                        items = items
                    )
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            showPermissionDialog = true
        }
    }

    fun requestCameraAccess() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Camera Permission Required") },
            text = { Text("This app needs camera access to capture receipt photos. Please enable camera permission in app settings.") },
            confirmButton = {
                Button(onClick = { showPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!isUploadMode) {
            // Initial screen
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = splitName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Two people, one bill.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // People avatars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    people.forEachIndexed { index, person ->
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = person.name.firstOrNull()?.toString() ?: "?",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (index < people.size - 1) {
                            Text(
                                text = "&",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val newSplit = Split(
                            id = UUID.randomUUID().toString(),
                            name = splitName,
                            people = people
                        )
                        currentSplitId = newSplit.id
                        isUploadMode = true
                        receipts.clear()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("+ Add receipts", fontSize = 16.sp)
                }
                Text(
                    text = "You can add more than one.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            // Upload mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { isUploadMode = false },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("< back", color = Color.Black)
                }
                Text("Receipts (${receipts.size})", fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.weight(1f))
            }

            // Receipt list
            if (receipts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("📸", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No receipts yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add receipts to get started",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(receipts) { receipt ->
                        ReceiptItemView(receipt)
                        Divider()
                    }
                }
            }

            // Camera and Scan buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { requestCameraAccess() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("📷 Camera")
                    }

                    Button(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("📁 Library")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (receipts.isNotEmpty()) {
                    Button(
                        onClick = {
                            navController.navigate("review_items/$currentSplitId")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Scan receipts →", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptItemView(receipt: ReceiptUpload) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = receipt.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (receipt.isProcessing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Reading your receipts...",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                } else if (receipt.items.isNotEmpty()) {
                    Text(
                        text = "${receipt.items.size} items found",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Text(
                text = "✓",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
