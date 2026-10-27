package com.example.carpetneed.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carpetneed.data.ExportManager
import com.example.carpetneed.model.CarpetRoom
import com.example.carpetneed.model.CarpetSettings
import com.example.carpetneed.model.PileDirection
import com.example.carpetneed.ui.components.CuttingPlanCanvas
import com.example.carpetneed.ui.viewmodel.CarpetNeedUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuttingPlanScreen(
    uiState: CarpetNeedUiState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val result = uiState.result
    val unit = uiState.unit
    val decimalPlaces = uiState.appSettings.decimalPlaces

    val settings = CarpetSettings(
        rollWidthMm = uiState.rollWidthMm,
        wasteAllowancePercent = uiState.wasteAllowancePercent,
        allowanceMethod = uiState.allowanceMethod,
        seamAllowanceMm = uiState.seamAllowanceMm,
        patternRepeatMm = uiState.patternRepeatMm,
        patternMatch = uiState.patternMatch,
        purchaseRounding = uiState.purchaseRounding,
        pileDirection = uiState.pileDirection
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cutting Plan — ${uiState.roomName}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (result != null) {
                        IconButton(
                            onClick = {
                                val room = CarpetRoom(
                                    id = "room",
                                    name = uiState.roomName,
                                    lengthMm = uiState.roomLengthMm,
                                    widthMm = uiState.roomWidthMm,
                                    orientation = uiState.orientation
                                )
                                val text = ExportManager.generateTxt(room, settings, result, unit, decimalPlaces)
                                ExportManager.shareText(context, text, "Share Cutting Plan")
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (result == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No calculation available", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Header overview
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ROLL CUTTING SPECIFICATIONS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Roll Width", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${String.format(Locale.US, "%.${decimalPlaces}f", unit.fromMm(settings.rollWidthMm))} ${unit.symbol}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text("Required Widths", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${result.requiredWidths}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text("Total Roll Length", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${String.format(Locale.US, "%.${decimalPlaces}f", result.purchaseLengthMm / 1000.0)} m",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Visual Roll Cutting Plan Diagram
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CONTINUOUS ROLL CUTTING DIAGRAM",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (settings.pileDirection == PileDirection.SAME_DIRECTION) {
                                Text(
                                    text = "→ Fixed Pile Direction",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        CuttingPlanCanvas(
                            result = result,
                            settings = settings,
                            unit = unit,
                            decimalPlaces = decimalPlaces
                        )
                    }
                }

                // Useful room coverage vs Additional planning margin
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CUTTING PLAN BREAKDOWN",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            PlanRow(
                                "Useful Room Coverage Length",
                                "${String.format(Locale.US, "%.${decimalPlaces}f", result.baseLengthMm / 1000.0)} m"
                            )

                            if (result.allowanceLengthMm > 0.0) {
                                PlanRow(
                                    "Additional Planning Margin (${String.format(Locale.US, "%.1f", settings.wasteAllowancePercent)}%)",
                                    "+${String.format(Locale.US, "%.${decimalPlaces}f", result.allowanceLengthMm / 1000.0)} m"
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            PlanRow(
                                "Recommended Total Purchase",
                                "${String.format(Locale.US, "%.${decimalPlaces}f", result.purchaseLengthMm / 1000.0)} linear m",
                                isBold = true
                            )
                        }
                    }
                }

                // Individual Pieces Table
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "INDIVIDUAL PIECES TO CUT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            for (piece in result.pieces) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Piece #${piece.pieceIndex} (${piece.sectionName})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Cut: ${String.format(Locale.US, "%.0f", unit.fromMm(piece.cutWidthMm))} × ${String.format(Locale.US, "%.0f", unit.fromMm(piece.cutLengthMm))} ${unit.symbol}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            if (piece.unusedWidthMm > 1.0) {
                                                Text(
                                                    text = "Unused width: ${String.format(Locale.US, "%.0f", unit.fromMm(piece.unusedWidthMm))} ${unit.symbol}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            } else {
                                                Text(
                                                    text = "Full roll width used",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            Text(
                                                text = "Roll pos: ${String.format(Locale.US, "%.1f", piece.startRollMm / 1000.0)}–${String.format(Locale.US, "%.1f", piece.endRollMm / 1000.0)} m",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Cutting summary table
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CUTTING SUMMARY",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            PlanRow("Room", uiState.roomName)
                            PlanRow("Floor Area", "${String.format(Locale.US, "%.${decimalPlaces}f", result.floorAreaMm2 / 1_000_000.0)} m²")
                            PlanRow("Carpet Roll Width", "${String.format(Locale.US, "%.${decimalPlaces}f", settings.rollWidthMm / 1000.0)} m")
                            PlanRow("Widths Required", "${result.requiredWidths}")
                            PlanRow("Length Required", "${String.format(Locale.US, "%.${decimalPlaces}f", result.baseLengthMm / 1000.0)} m")
                            PlanRow("Waste Allowance", "${String.format(Locale.US, "%.1f", settings.wasteAllowancePercent)}%")
                            PlanRow("Purchase Length", "${String.format(Locale.US, "%.${decimalPlaces}f", result.purchaseLengthMm / 1000.0)} linear m", isBold = true)
                            PlanRow("Purchase Area", "${String.format(Locale.US, "%.${decimalPlaces}f", result.purchaseAreaMm2 / 1_000_000.0)} m²", isBold = true)
                            PlanRow("Estimated Waste", "${String.format(Locale.US, "%.${decimalPlaces}f", result.excessAreaMm2 / 1_000_000.0)} m²", isBold = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
