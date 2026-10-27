package com.example.carpetneed.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carpetneed.data.ExportManager
import com.example.carpetneed.model.HistoryItem
import com.example.carpetneed.ui.viewmodel.CarpetNeedUiState
import com.example.carpetneed.ui.viewmodel.CarpetNeedViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: CarpetNeedViewModel,
    uiState: CarpetNeedUiState,
    onBack: () -> Unit,
    onOpenCalculation: () -> Unit
) {
    val context = LocalContext.current
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculation History (${uiState.historyList.size}/10)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.historyList.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear History")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No saved calculations yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Save calculations from the Home screen to view them here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(uiState.historyList, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        decimalPlaces = uiState.appSettings.decimalPlaces,
                        onOpen = {
                            viewModel.loadHistoryItem(item)
                            onOpenCalculation()
                        },
                        onDuplicate = { viewModel.duplicateHistoryItem(item) },
                        onCopy = {
                            val summary = "${item.roomName}\nRoom: ${String.format(Locale.US, "%.0f", item.lengthMm / 10.0)} × ${String.format(Locale.US, "%.0f", item.widthMm / 10.0)} cm\nFloor: ${String.format(Locale.US, "%.2f", item.floorAreaM2)} m²\nCarpet: ${String.format(Locale.US, "%.2f", item.purchaseLengthM)} m\nPurchase Area: ${String.format(Locale.US, "%.2f", item.purchaseAreaM2)} m²"
                            val clipboardService = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("CarpetNeed Calculation", summary)
                            clipboardService?.setPrimaryClip(clip)
                        },
                        onDelete = { viewModel.deleteHistoryItem(item.id) }
                    )
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All History?") },
            text = { Text("This will permanently remove all saved calculations.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryItem,
    decimalPlaces: Int,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val relativeTime = DateUtils.getRelativeTimeSpanString(
        item.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    )

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
                    text = item.roomName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = relativeTime.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${String.format(Locale.US, "%.0f", item.lengthMm / 10.0)} × ${String.format(Locale.US, "%.0f", item.widthMm / 10.0)} cm",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Floor Area", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${String.format(Locale.US, "%.${decimalPlaces}f", item.floorAreaM2)} m²",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text("Carpet Required", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${String.format(Locale.US, "%.${decimalPlaces}f", item.purchaseLengthM)} m",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text("Purchase Area", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${String.format(Locale.US, "%.${decimalPlaces}f", item.purchaseAreaM2)} m²",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text("Excess", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${String.format(Locale.US, "%.${decimalPlaces}f", item.excessM2)} m²",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Action row: Open, Duplicate, Copy, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOpen) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open")
                }
                TextButton(onClick = onDuplicate) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duplicate")
                }
                IconButton(onClick = onCopy) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Copy")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
