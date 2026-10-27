package com.example.carpetneed.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carpetneed.model.AppSettings
import com.example.carpetneed.model.AppThemeMode
import com.example.carpetneed.model.CarpetOrientation
import com.example.carpetneed.model.UnitType
import com.example.carpetneed.ui.viewmodel.CarpetNeedUiState
import com.example.carpetneed.ui.viewmodel.CarpetNeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CarpetNeedViewModel,
    uiState: CarpetNeedUiState,
    onBack: () -> Unit
) {
    val settings = uiState.appSettings
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Settings")
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
            // Appearance
            item {
                SettingsSectionHeader("APPEARANCE")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = settings.themeMode == mode,
                                    onClick = { viewModel.updateSettings(settings.copy(themeMode = mode)) },
                                    label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Dynamic Color", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Use Material You dynamic palette (Android 12+)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Switch(
                                checked = settings.dynamicColor,
                                onCheckedChange = { viewModel.updateSettings(settings.copy(dynamicColor = it)) }
                            )
                        }
                    }
                }
            }

            // Calculator Defaults
            item {
                SettingsSectionHeader("CALCULATOR DEFAULTS")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Default Unit
                        Text("Default Unit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            UnitType.entries.forEach { unit ->
                                FilterChip(
                                    selected = settings.defaultUnit == unit,
                                    onClick = { viewModel.updateSettings(settings.copy(defaultUnit = unit)) },
                                    label = { Text(unit.symbol) }
                                )
                            }
                        }

                        HorizontalDivider()

                        // Default Roll Width
                        Text("Default Carpet Roll Width", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(4000.0, 5000.0, 6000.0).forEach { widthMm ->
                                FilterChip(
                                    selected = settings.defaultRollWidthMm == widthMm,
                                    onClick = { viewModel.updateSettings(settings.copy(defaultRollWidthMm = widthMm)) },
                                    label = { Text("${(widthMm / 10.0).toInt()} cm") }
                                )
                            }
                        }

                        HorizontalDivider()

                        // Default Orientation
                        Text("Default Laying Direction", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Column {
                            CarpetOrientation.entries.forEach { orient ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.updateSettings(settings.copy(defaultOrientation = orient)) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = settings.defaultOrientation == orient,
                                        onClick = { viewModel.updateSettings(settings.copy(defaultOrientation = orient)) }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(orient.label, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // Display Settings
            item {
                SettingsSectionHeader("DISPLAY SETTINGS")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsToggleRow(
                            title = "Show Room Diagram",
                            subtitle = "Interactive floor plan canvas with proportional scaling",
                            checked = settings.showRoomDiagram,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(showRoomDiagram = it)) }
                        )
                        SettingsToggleRow(
                            title = "Show Cutting Plan",
                            subtitle = "Visual continuous roll diagram with piece divisions",
                            checked = settings.showCuttingPlan,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(showCuttingPlan = it)) }
                        )
                        SettingsToggleRow(
                            title = "Show Calculation Breakdown",
                            subtitle = "Expandable 'How was this calculated?' card",
                            checked = settings.showCalculationBreakdown,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(showCalculationBreakdown = it)) }
                        )
                        SettingsToggleRow(
                            title = "Show Waste Breakdown",
                            subtitle = "Unavoidable roll geometry waste vs user allowance",
                            checked = settings.showWasteBreakdown,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(showWasteBreakdown = it)) }
                        )
                        SettingsToggleRow(
                            title = "Show Unit Conversions",
                            subtitle = "Show square meters & square feet concurrently",
                            checked = settings.showUnitConversions,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(showUnitConversions = it)) }
                        )
                    }
                }
            }

            // Precision
            item {
                SettingsSectionHeader("PRECISION")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Decimal Places", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Number of decimal places displayed for area and length calculations.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0, 1, 2, 3, 4).forEach { decimals ->
                                FilterChip(
                                    selected = settings.decimalPlaces == decimals,
                                    onClick = { viewModel.updateSettings(settings.copy(decimalPlaces = decimals)) },
                                    label = { Text("$decimals") }
                                )
                            }
                        }
                    }
                }
            }

            // Behavior
            item {
                SettingsSectionHeader("BEHAVIOR")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsToggleRow(
                            title = "Live Calculation",
                            subtitle = "Update results immediately as dimensions change",
                            checked = settings.liveCalculation,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(liveCalculation = it)) }
                        )
                        SettingsToggleRow(
                            title = "Haptic Feedback",
                            subtitle = "Provide subtle tactile response on actions",
                            checked = settings.haptics,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(haptics = it)) }
                        )
                        SettingsToggleRow(
                            title = "Animations",
                            subtitle = "Smooth UI and diagram transitions",
                            checked = settings.animations,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(animations = it)) }
                        )
                        SettingsToggleRow(
                            title = "Confirm Delete",
                            subtitle = "Prompt before deleting rooms or history items",
                            checked = settings.confirmDelete,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDelete = it)) }
                        )
                        SettingsToggleRow(
                            title = "Restore Last Calculation",
                            subtitle = "Load previous room dimensions when app restarts",
                            checked = settings.restoreLastCalculation,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(restoreLastCalculation = it)) }
                        )
                    }
                }
            }

            // Reset Button
            item {
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset All Settings to Defaults")
                }
                Text(
                    text = "Note: Resetting settings will NOT delete your saved calculation history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings?") },
            text = { Text("This will restore all default preferences. Your history will be preserved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSettings()
                        showResetDialog = false
                    }
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
