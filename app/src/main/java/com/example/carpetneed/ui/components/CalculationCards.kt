package com.example.carpetneed.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carpetneed.model.CarpetOrientation
import com.example.carpetneed.model.CarpetResult
import com.example.carpetneed.model.CarpetSettings
import com.example.carpetneed.model.UnitType
import java.util.Locale

@Composable
fun ResultCard(
    result: CarpetResult,
    settings: CarpetSettings,
    unit: UnitType,
    decimalPlaces: Int = 2,
    modifier: Modifier = Modifier
) {
    val purchaseLengthM = result.purchaseLengthMm / 1000.0
    val purchaseLengthFt = purchaseLengthM * 3.28084
    val purchaseAreaM2 = result.purchaseAreaMm2 / 1_000_000.0
    val rollWidthM = settings.rollWidthMm / 1000.0
    val excessM2 = result.excessAreaMm2 / 1_000_000.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CARPET REQUIRED",
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = String.format(Locale.US, "%.${decimalPlaces}f m", purchaseLengthM),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = String.format(Locale.US, "(%.${decimalPlaces}f linear ft)", purchaseLengthFt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Roll Width",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.${decimalPlaces}f m", rollWidthM),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Widths / Pieces",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${result.requiredWidths}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Purchase Area",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.${decimalPlaces}f m²", purchaseAreaM2),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Excess / Waste:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = String.format(Locale.US, "%.${decimalPlaces}f m² (%.1f%%)", excessM2, result.wastePercent),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WasteBreakdownCard(
    result: CarpetResult,
    decimalPlaces: Int = 2,
    modifier: Modifier = Modifier
) {
    val floorAreaM2 = result.floorAreaMm2 / 1_000_000.0
    val purchaseAreaM2 = result.purchaseAreaMm2 / 1_000_000.0
    val geometryWasteM2 = result.geometryWasteMm2 / 1_000_000.0
    val userAllowanceM2 = result.userAllowanceMm2 / 1_000_000.0
    val totalExcessM2 = result.totalExcessMm2 / 1_000_000.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "WASTE & EXCESS BREAKDOWN",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            BreakdownRow("Floor Area", String.format(Locale.US, "%.${decimalPlaces}f m²", floorAreaM2))
            BreakdownRow("Carpet Purchase Area", String.format(Locale.US, "%.${decimalPlaces}f m²", purchaseAreaM2))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            BreakdownRow(
                "Roll Layout Waste (Unavoidable)",
                String.format(Locale.US, "%.${decimalPlaces}f m²", geometryWasteM2),
                isSubItem = true
            )
            BreakdownRow(
                "Additional User Allowance",
                String.format(Locale.US, "%.${decimalPlaces}f m²", userAllowanceM2),
                isSubItem = true
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Excess / Waste",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(Locale.US, "%.${decimalPlaces}f m² (%.1f%%)", totalExcessM2, result.wastePercent),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (totalExcessM2 > 0.0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String, isSubItem: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isSubItem) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = if (isSubItem) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isSubItem) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSubItem) FontWeight.Normal else FontWeight.SemiBold
        )
    }
}

@Composable
fun BestFitCard(
    result: CarpetResult,
    decimalPlaces: Int = 2,
    modifier: Modifier = Modifier
) {
    val optA = result.optionA
    val optB = result.optionB
    val chosen = result.chosenOrientation

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BEST FIT RECOMMENDATION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (chosen == CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH) {
                    "Orientation A: Roll width across room width is recommended."
                } else {
                    "Orientation B: Roll width across room length is recommended."
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Compare Option A and Option B side-by-side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Option A Card
                OrientationOptionSummary(
                    title = "OPTION A",
                    sub = "Across Width",
                    lengthM = optA.purchaseLengthMm / 1000.0,
                    areaM2 = optA.purchaseAreaMm2 / 1_000_000.0,
                    excessM2 = optA.excessAreaMm2 / 1_000_000.0,
                    pieces = optA.pieceCount,
                    seams = optA.estimatedSeams,
                    isSelected = chosen == CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH,
                    decimalPlaces = decimalPlaces,
                    modifier = Modifier.weight(1f)
                )

                // Option B Card
                OrientationOptionSummary(
                    title = "OPTION B",
                    sub = "Across Length",
                    lengthM = optB.purchaseLengthMm / 1000.0,
                    areaM2 = optB.purchaseAreaMm2 / 1_000_000.0,
                    excessM2 = optB.excessAreaMm2 / 1_000_000.0,
                    pieces = optB.pieceCount,
                    seams = optB.estimatedSeams,
                    isSelected = chosen == CarpetOrientation.WIDTH_ACROSS_ROOM_LENGTH,
                    decimalPlaces = decimalPlaces,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OrientationOptionSummary(
    title: String,
    sub: String,
    lengthM: Double,
    areaM2: Double,
    excessM2: Double,
    pieces: Int,
    seams: Int,
    isSelected: Boolean,
    decimalPlaces: Int,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                if (isSelected) {
                    Text(
                        text = "BEST",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(text = sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = String.format(Locale.US, "%.${decimalPlaces}f m", lengthM),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = String.format(Locale.US, "Area: %.${decimalPlaces}f m²", areaM2),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = String.format(Locale.US, "Excess: %.${decimalPlaces}f m²", excessM2),
                style = MaterialTheme.typography.bodySmall,
                color = if (excessM2 > 0.0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Pieces: $pieces | Seams: $seams",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun CalculationBreakdownCard(
    result: CarpetResult,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "How was this calculated?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    for (step in result.calculationSteps) {
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 3.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (result.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        for (note in result.notes) {
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
