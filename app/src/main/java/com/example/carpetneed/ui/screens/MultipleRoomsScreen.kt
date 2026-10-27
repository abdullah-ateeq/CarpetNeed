package com.example.carpetneed.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carpetneed.data.ExportManager
import com.example.carpetneed.model.CarpetOrientation
import com.example.carpetneed.model.CarpetRoom
import com.example.carpetneed.model.UnitType
import com.example.carpetneed.ui.viewmodel.CarpetNeedUiState
import com.example.carpetneed.ui.viewmodel.CarpetNeedViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleRoomsScreen(
    viewModel: CarpetNeedViewModel,
    uiState: CarpetNeedUiState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportType by remember { mutableStateOf("txt") }

    val summary = uiState.multipleRoomSummary
    val unit = uiState.unit
    val decimalPlaces = uiState.appSettings.decimalPlaces

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            if (exportType == "csv") "text/csv" else "text/plain"
        )
    ) { uri ->
        if (uri != null && summary != null) {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                val txt = ExportManager.generateMultipleRoomsTxt(
                    summary = summary,
                    roomsList = uiState.multipleRoomResults,
                    unit = unit,
                    decimalPlaces = decimalPlaces
                )
                ExportManager.writeToOutputStream(os, txt)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multiple Rooms", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddRoomDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Room")
                    }
                    if (summary != null) {
                        IconButton(
                            onClick = {
                                val txt = ExportManager.generateMultipleRoomsTxt(
                                    summary = summary,
                                    roomsList = uiState.multipleRoomResults,
                                    unit = unit,
                                    decimalPlaces = decimalPlaces
                                )
                                ExportManager.shareText(context, txt, "Share Project Summary")
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Settings row: Use same carpet settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.useSameCarpetSettings,
                                onCheckedChange = { viewModel.setUseSameCarpetSettings(it) }
                            )
                            Text("Use same carpet settings", style = MaterialTheme.typography.bodyMedium)
                        }

                        Button(onClick = { showAddRoomDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Room")
                        }
                    }
                }
            }

            // Project Summary Card
            if (summary != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "PROJECT SUMMARY",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total Rooms", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${summary.totalRooms}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text("Total Floor Area", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${String.format(Locale.US, "%.${decimalPlaces}f", summary.totalFloorAreaMm2 / 1_000_000.0)} m²",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Carpet Purchase", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${String.format(Locale.US, "%.${decimalPlaces}f", summary.totalCarpetLengthMm / 1000.0)} linear m",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text("Purchased Area", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${String.format(Locale.US, "%.${decimalPlaces}f", summary.totalPurchaseAreaMm2 / 1_000_000.0)} m²",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text("Total Excess", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${String.format(Locale.US, "%.${decimalPlaces}f", summary.totalExcessAreaMm2 / 1_000_000.0)} m²",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // List of rooms
            item {
                Text(
                    text = "PROJECT ROOMS (${uiState.multipleRooms.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(uiState.multipleRoomResults) { (room, settings, result) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = room.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                IconButton(onClick = { viewModel.duplicateRoom(room) }) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate")
                                }
                                IconButton(onClick = { viewModel.removeRoom(room.id) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Text(
                            text = "${String.format(Locale.US, "%.0f", unit.fromMm(room.lengthMm))} × ${String.format(Locale.US, "%.0f", unit.fromMm(room.widthMm))} ${unit.symbol}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Floor Area", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${String.format(Locale.US, "%.${decimalPlaces}f", result.floorAreaMm2 / 1_000_000.0)} m²",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column {
                                Text("Carpet Required", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${String.format(Locale.US, "%.${decimalPlaces}f", result.purchaseLengthMm / 1000.0)} m",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text("Purchase Area", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${String.format(Locale.US, "%.${decimalPlaces}f", result.purchaseAreaMm2 / 1_000_000.0)} m²",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column {
                                Text("Excess", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${String.format(Locale.US, "%.${decimalPlaces}f", result.excessAreaMm2 / 1_000_000.0)} m²",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Room-specific orientation dropdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Orientation:", style = MaterialTheme.typography.bodySmall)
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { expanded = true }) {
                                    Text(room.orientation.label)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    CarpetOrientation.entries.forEach { orient ->
                                        DropdownMenuItem(
                                            text = { Text(orient.label) },
                                            onClick = {
                                                viewModel.updateRoomOrientation(room.id, orient)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddRoomDialog) {
        AddRoomDialog(
            unit = unit,
            onDismiss = { showAddRoomDialog = false },
            onAdd = { name, len, wid ->
                viewModel.addRoom(name, len, wid)
                showAddRoomDialog = false
            }
        )
    }
}

@Composable
fun AddRoomDialog(
    unit: UnitType,
    onDismiss: () -> Unit,
    onAdd: (name: String, lengthMm: Double, widthMm: Double) -> Unit
) {
    var name by remember { mutableStateOf("New Room") }
    var lenText by remember { mutableStateOf("400") }
    var widText by remember { mutableStateOf("350") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room to Project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Room Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = lenText,
                    onValueChange = { lenText = it },
                    label = { Text("Length (${unit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = widText,
                    onValueChange = { widText = it },
                    label = { Text("Width (${unit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val l = lenText.toDoubleOrNull() ?: 0.0
                    val w = widText.toDoubleOrNull() ?: 0.0
                    if (l > 0 && w > 0) {
                        onAdd(name, unit.toMm(l), unit.toMm(w))
                    }
                }
            ) { Text("Add Room") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
