package com.example.carpetneed.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carpetneed.data.ExportManager
import com.example.carpetneed.model.*
import com.example.carpetneed.ui.components.*
import com.example.carpetneed.ui.viewmodel.CarpetNeedUiState
import com.example.carpetneed.ui.viewmodel.CarpetNeedViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CarpetNeedViewModel,
    uiState: CarpetNeedUiState,
    onNavigateToCuttingPlan: () -> Unit,
    onNavigateToMultipleRooms: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddSectionDialog by remember { mutableStateOf(false) }
    var showAddExclusionDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportType by remember { mutableStateOf("txt") }

    // Storage Access Framework launcher
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            when (exportType) {
                "csv" -> "text/csv"
                "json" -> "application/json"
                else -> "text/plain"
            }
        )
    ) { uri ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            contentResolver.openOutputStream(uri)?.use { os ->
                val result = uiState.result
                if (result != null) {
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
                    val room = CarpetRoom(
                        id = "room",
                        name = uiState.roomName,
                        shape = uiState.shape,
                        lengthMm = uiState.roomLengthMm,
                        widthMm = uiState.roomWidthMm,
                        orientation = uiState.orientation
                    )
                    val content = when (exportType) {
                        "csv" -> ExportManager.generateCsv(room, settings, result, uiState.unit, uiState.appSettings.decimalPlaces)
                        "json" -> ExportManager.generateJson(room, settings, result, uiState.unit)
                        else -> ExportManager.generateTxt(room, settings, result, uiState.unit, uiState.appSettings.decimalPlaces)
                    }
                    ExportManager.writeToOutputStream(os, content)
                }
            }
        }
    }

    LaunchedEffect(uiState.userNotice) {
        uiState.userNotice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserNotice()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CarpetNeed",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Calculate carpet quantity with roll width & waste",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "About")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Mode Selector & Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                        CalculationMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = uiState.calculationMode == mode,
                                onClick = {
                                    if (mode == CalculationMode.MULTIPLE_ROOMS) {
                                        onNavigateToMultipleRooms()
                                    } else {
                                        viewModel.setCalculationMode(mode)
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = CalculationMode.entries.size)
                            ) {
                                Text(
                                    text = when (mode) {
                                        CalculationMode.SINGLE_ROOM -> "Single Room"
                                        CalculationMode.MULTIPLE_ROOMS -> "Multi-Room"
                                        CalculationMode.ROOM_WITH_SECTIONS -> "+ Sections"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.loadQuickExample() },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Example", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Unit Selector & Room Name
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.roomName,
                            onValueChange = { viewModel.setRoomName(it) },
                            label = { Text("Room Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        UnitDropdownSelector(
                            selectedUnit = uiState.unit,
                            onUnitSelected = { viewModel.setUnit(it) },
                            modifier = Modifier.width(130.dp)
                        )
                    }
                }
            }

            // Room Dimensions & Shape
            item {
                RoomDimensionsSection(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // Room Diagram
            if (uiState.appSettings.showRoomDiagram) {
                item {
                    val currentRoom = CarpetRoom(
                        id = "preview",
                        name = uiState.roomName,
                        shape = uiState.shape,
                        lengthMm = uiState.roomLengthMm,
                        widthMm = uiState.roomWidthMm,
                        lShape = if (uiState.shape == RoomShape.L_SHAPE) {
                            try {
                                LShapeDimensions(
                                    overallLengthMm = uiState.lShapeOverallLengthMm,
                                    overallWidthMm = uiState.lShapeOverallWidthMm,
                                    cutoutLengthMm = uiState.lShapeCutoutLengthMm,
                                    cutoutWidthMm = uiState.lShapeCutoutWidthMm
                                )
                            } catch (e: Exception) { null }
                        } else null,
                        orientation = uiState.orientation
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ROOM DIAGRAM",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Real Aspect Ratio",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        RoomDiagramCanvas(
                            room = currentRoom,
                            result = uiState.result,
                            unit = uiState.unit,
                            decimalPlaces = uiState.appSettings.decimalPlaces
                        )
                    }
                }
            }

            // Carpet Roll Width
            item {
                CarpetRollWidthSection(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // Laying Direction & Best Fit
            item {
                CarpetOrientationSection(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // Waste Allowance & Rounding
            item {
                WasteAllowanceSection(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // Carpet Pile & Pattern Settings
            item {
                CarpetPileAndPatternSection(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // Sections management (if ROOM_WITH_SECTIONS)
            if (uiState.calculationMode == CalculationMode.ROOM_WITH_SECTIONS) {
                item {
                    RoomSectionsManager(
                        sections = uiState.sections,
                        unit = uiState.unit,
                        decimalPlaces = uiState.appSettings.decimalPlaces,
                        onAddSectionClick = { showAddSectionDialog = true },
                        onRemoveSection = { viewModel.removeSection(it) }
                    )
                }
            }

            // Exclusions Section
            item {
                ExclusionsManager(
                    exclusions = uiState.exclusions,
                    unit = uiState.unit,
                    decimalPlaces = uiState.appSettings.decimalPlaces,
                    onAddExclusionClick = { showAddExclusionDialog = true },
                    onRemoveExclusion = { viewModel.removeExclusion(it) }
                )
            }

            // Results & Breakdown
            item {
                val result = uiState.result
                if (result != null) {
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

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ResultCard(
                            result = result,
                            settings = settings,
                            unit = uiState.unit,
                            decimalPlaces = uiState.appSettings.decimalPlaces
                        )

                        if (uiState.appSettings.showWasteBreakdown) {
                            WasteBreakdownCard(
                                result = result,
                                decimalPlaces = uiState.appSettings.decimalPlaces
                            )
                        }

                        if (uiState.appSettings.showCalculationBreakdown) {
                            CalculationBreakdownCard(result = result)
                        }

                        // Cutting Plan Action Card
                        Button(
                            onClick = onNavigateToCuttingPlan,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ViewCompact, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Dedicated Cutting Plan", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }

                        // Save, Share, Export Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.saveCurrentCalculationToHistory() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save")
                            }

                            OutlinedButton(
                                onClick = {
                                    val room = CarpetRoom(
                                        id = "room",
                                        name = uiState.roomName,
                                        lengthMm = uiState.roomLengthMm,
                                        widthMm = uiState.roomWidthMm,
                                        orientation = uiState.orientation
                                    )
                                    val text = ExportManager.generateShareText(
                                        room, settings, result, uiState.unit, uiState.appSettings.decimalPlaces
                                    )
                                    ExportManager.shareText(context, text)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }

                            OutlinedButton(
                                onClick = { showExportDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export")
                            }
                        }
                    }
                } else if (uiState.errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // Export format dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Calculation") },
            text = {
                Column {
                    Text("Select export format using Android Storage Access Framework:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { exportType = "txt" },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = exportType == "txt", onClick = { exportType = "txt" })
                        Text("Plain Text (.txt)")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { exportType = "csv" },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = exportType == "csv", onClick = { exportType = "csv" })
                        Text("Spreadsheet (.csv)")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { exportType = "json" },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = exportType == "json", onClick = { exportType = "json" })
                        Text("Structured (.json)")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        val filename = "${uiState.roomName.replace(" ", "_")}_CarpetNeed.$exportType"
                        createDocLauncher.launch(filename)
                    }
                ) {
                    Text("Save to File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Section Dialog
    if (showAddSectionDialog) {
        AddSectionDialog(
            unit = uiState.unit,
            onDismiss = { showAddSectionDialog = false },
            onAdd = { name, len, wid ->
                viewModel.addSection(name, len, wid)
                showAddSectionDialog = false
            }
        )
    }

    // Add Exclusion Dialog
    if (showAddExclusionDialog) {
        AddExclusionDialog(
            unit = uiState.unit,
            onDismiss = { showAddExclusionDialog = false },
            onAdd = { name, wid, dep ->
                viewModel.addExclusion(name, wid, dep)
                showAddExclusionDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdownSelector(
    selectedUnit: UnitType,
    onUnitSelected: (UnitType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedUnit.label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            label = { Text("Unit") },
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            UnitType.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text("${unit.label} (${unit.symbol})") },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RoomDimensionsSection(
    uiState: CarpetNeedUiState,
    viewModel: CarpetNeedViewModel
) {
    val unit = uiState.unit

    var lengthText by remember(uiState.roomLengthMm, unit) {
        mutableStateOf(String.format(Locale.US, "%.1f", unit.fromMm(uiState.roomLengthMm)))
    }
    var widthText by remember(uiState.roomWidthMm, unit) {
        mutableStateOf(String.format(Locale.US, "%.1f", unit.fromMm(uiState.roomWidthMm)))
    }

    var lengthError by remember { mutableStateOf<String?>(null) }
    var widthError by remember { mutableStateOf<String?>(null) }

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
                    text = "ROOM DIMENSIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Shape tabs: Rectangle vs L-Shape
                Row {
                    FilterChip(
                        selected = uiState.shape == RoomShape.RECTANGLE,
                        onClick = { viewModel.setRoomShape(RoomShape.RECTANGLE) },
                        label = { Text("Rectangle") }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = uiState.shape == RoomShape.L_SHAPE,
                        onClick = { viewModel.setRoomShape(RoomShape.L_SHAPE) },
                        label = { Text("L-Shape") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.shape == RoomShape.RECTANGLE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = lengthText,
                        onValueChange = { input ->
                            lengthText = input
                            val num = input.toDoubleOrNull()
                            if (num == null || num <= 0) {
                                lengthError = "Enter valid positive number"
                            } else {
                                lengthError = null
                                viewModel.setRoomDimensions(unit.toMm(num), uiState.roomWidthMm)
                            }
                        },
                        label = { Text("Length (${unit.symbol})") },
                        isError = lengthError != null,
                        supportingText = lengthError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = widthText,
                        onValueChange = { input ->
                            widthText = input
                            val num = input.toDoubleOrNull()
                            if (num == null || num <= 0) {
                                widthError = "Enter valid positive number"
                            } else {
                                widthError = null
                                viewModel.setRoomDimensions(uiState.roomLengthMm, unit.toMm(num))
                            }
                        },
                        label = { Text("Width (${unit.symbol})") },
                        isError = widthError != null,
                        supportingText = widthError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            } else {
                // L-Shape inputs
                LShapeInputs(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LShapeInputs(
    uiState: CarpetNeedUiState,
    viewModel: CarpetNeedViewModel
) {
    val unit = uiState.unit

    var overallLenText by remember(uiState.lShapeOverallLengthMm, unit) {
        mutableStateOf(String.format(Locale.US, "%.1f", unit.fromMm(uiState.lShapeOverallLengthMm)))
    }
    var overallWidText by remember(uiState.lShapeOverallWidthMm, unit) {
        mutableStateOf(String.format(Locale.US, "%.1f", unit.fromMm(uiState.lShapeOverallWidthMm)))
    }
    var cutoutLenText by remember(uiState.lShapeCutoutLengthMm, unit) {
        mutableStateOf(String.format(Locale.US, "%.1f", unit.fromMm(uiState.lShapeCutoutLengthMm)))
    }
    var cutoutWidText by remember(uiState.lShapeCutoutWidthMm, unit) {
        mutableStateOf(String.format(Locale.US, "%.1f", unit.fromMm(uiState.lShapeCutoutWidthMm)))
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = overallLenText,
                onValueChange = { input ->
                    overallLenText = input
                    val num = input.toDoubleOrNull()
                    if (num != null && num > 0) {
                        viewModel.setLShapeDimensions(
                            overallLengthMm = unit.toMm(num),
                            overallWidthMm = uiState.lShapeOverallWidthMm,
                            cutoutLengthMm = uiState.lShapeCutoutLengthMm,
                            cutoutWidthMm = uiState.lShapeCutoutWidthMm
                        )
                    }
                },
                label = { Text("Overall Length (${unit.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = overallWidText,
                onValueChange = { input ->
                    overallWidText = input
                    val num = input.toDoubleOrNull()
                    if (num != null && num > 0) {
                        viewModel.setLShapeDimensions(
                            overallLengthMm = uiState.lShapeOverallLengthMm,
                            overallWidthMm = unit.toMm(num),
                            cutoutLengthMm = uiState.lShapeCutoutLengthMm,
                            cutoutWidthMm = uiState.lShapeCutoutWidthMm
                        )
                    }
                },
                label = { Text("Overall Width (${unit.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = cutoutLenText,
                onValueChange = { input ->
                    cutoutLenText = input
                    val num = input.toDoubleOrNull()
                    if (num != null && num >= 0) {
                        viewModel.setLShapeDimensions(
                            overallLengthMm = uiState.lShapeOverallLengthMm,
                            overallWidthMm = uiState.lShapeOverallWidthMm,
                            cutoutLengthMm = unit.toMm(num),
                            cutoutWidthMm = uiState.lShapeCutoutWidthMm
                        )
                    }
                },
                label = { Text("Cutout Length (${unit.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = cutoutWidText,
                onValueChange = { input ->
                    cutoutWidText = input
                    val num = input.toDoubleOrNull()
                    if (num != null && num >= 0) {
                        viewModel.setLShapeDimensions(
                            overallLengthMm = uiState.lShapeOverallLengthMm,
                            overallWidthMm = uiState.lShapeOverallWidthMm,
                            cutoutLengthMm = uiState.lShapeCutoutLengthMm,
                            cutoutWidthMm = unit.toMm(num)
                        )
                    }
                },
                label = { Text("Cutout Width (${unit.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}

@Composable
fun CarpetRollWidthSection(
    uiState: CarpetNeedUiState,
    viewModel: CarpetNeedViewModel
) {
    val unit = uiState.unit
    val rollWidthMm = uiState.rollWidthMm

    val presets = listOf(4000.0, 5000.0, 6000.0)
    var isCustom by remember(rollWidthMm) {
        mutableStateOf(!presets.contains(rollWidthMm))
    }
    var customText by remember(rollWidthMm, unit) {
        mutableStateOf(String.format(Locale.US, "%.1f", unit.fromMm(rollWidthMm)))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CARPET ROLL WIDTH",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Usable carpet roll width before cutting.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { presetMm ->
                    val isSelected = !isCustom && rollWidthMm == presetMm
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            isCustom = false
                            viewModel.setRollWidth(presetMm)
                        },
                        label = {
                            Text(String.format(Locale.US, "%.0f %s", unit.fromMm(presetMm), unit.symbol))
                        }
                    )
                }

                FilterChip(
                    selected = isCustom,
                    onClick = { isCustom = true },
                    label = { Text("Custom") }
                )
            }

            if (isCustom) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customText,
                    onValueChange = { input ->
                        customText = input
                        val num = input.toDoubleOrNull()
                        if (num != null && num > 0) {
                            viewModel.setRollWidth(unit.toMm(num))
                        }
                    },
                    label = { Text("Custom Roll Width (${unit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun CarpetOrientationSection(
    uiState: CarpetNeedUiState,
    viewModel: CarpetNeedViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "LAYING DIRECTION",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "The roll width determines how many strips are needed to cover the opposite room dimension.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                CarpetOrientation.entries.forEach { orientation ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setOrientation(orientation) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.orientation == orientation,
                            onClick = { viewModel.setOrientation(orientation) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = orientation.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(text = orientation.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            if (uiState.result != null) {
                Spacer(modifier = Modifier.height(12.dp))
                BestFitCard(
                    result = uiState.result,
                    decimalPlaces = uiState.appSettings.decimalPlaces
                )
            }
        }
    }
}

@Composable
fun WasteAllowanceSection(
    uiState: CarpetNeedUiState,
    viewModel: CarpetNeedViewModel
) {
    val presets = listOf(0.0, 5.0, 7.5, 10.0, 15.0)
    var isCustom by remember(uiState.wasteAllowancePercent) {
        mutableStateOf(!presets.contains(uiState.wasteAllowancePercent))
    }
    var customText by remember(uiState.wasteAllowancePercent) {
        mutableStateOf(String.format(Locale.US, "%.1f", uiState.wasteAllowancePercent))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "WASTE ALLOWANCE & ROUNDING",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Waste allowance is an additional planning margin. Unavoidable geometric waste from fixed roll width is calculated separately.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { pct ->
                    FilterChip(
                        selected = !isCustom && uiState.wasteAllowancePercent == pct,
                        onClick = {
                            isCustom = false
                            viewModel.setWasteAllowance(pct)
                        },
                        label = { Text("${String.format(Locale.US, "%.1f", pct)}%") }
                    )
                }
                FilterChip(
                    selected = isCustom,
                    onClick = { isCustom = true },
                    label = { Text("Custom") }
                )
            }

            if (isCustom) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = customText,
                    onValueChange = { input ->
                        customText = input
                        val num = input.toDoubleOrNull()
                        if (num != null && num >= 0) {
                            viewModel.setWasteAllowance(num)
                        }
                    },
                    label = { Text("Custom Allowance (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Allowance applies to
            Text(text = "Allowance applies to:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AllowanceMethod.entries.forEach { method ->
                    Row(
                        modifier = Modifier.clickable { viewModel.setAllowanceMethod(method) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = uiState.allowanceMethod == method, onClick = { viewModel.setAllowanceMethod(method) })
                        Text(method.label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Purchase Rounding
            Text(text = "Purchase Rounding:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PurchaseRounding.entries.forEach { rounding ->
                    FilterChip(
                        selected = uiState.purchaseRounding == rounding,
                        onClick = { viewModel.setPurchaseRounding(rounding) },
                        label = { Text(rounding.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

@Composable
fun CarpetPileAndPatternSection(
    uiState: CarpetNeedUiState,
    viewModel: CarpetNeedViewModel
) {
    val unit = uiState.unit
    var patternRepeatChecked by remember(uiState.patternRepeatMm) {
        mutableStateOf(uiState.patternRepeatMm > 0.0)
    }
    var repeatText by remember(uiState.patternRepeatMm, unit) {
        mutableStateOf(String.format(Locale.US, "%.1f", unit.fromMm(uiState.patternRepeatMm)))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PILE & PATTERN SETTINGS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Pile Direction:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PileDirection.entries.forEach { dir ->
                    Row(
                        modifier = Modifier.clickable { viewModel.setPileDirection(dir) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = uiState.pileDirection == dir, onClick = { viewModel.setPileDirection(dir) })
                        Text(dir.label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (uiState.pileDirection == PileDirection.SAME_DIRECTION) {
                Text(
                    text = "⚠ Directional carpet: All joined pieces maintain identical pile direction. No 90° piece rotations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Pattern Repeat
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = patternRepeatChecked,
                    onCheckedChange = { checked ->
                        patternRepeatChecked = checked
                        if (!checked) viewModel.setPatternRepeat(0.0)
                    }
                )
                Text("Patterned carpet (Pattern repeat)", style = MaterialTheme.typography.bodyMedium)
            }

            if (patternRepeatChecked) {
                OutlinedTextField(
                    value = repeatText,
                    onValueChange = { input ->
                        repeatText = input
                        val num = input.toDoubleOrNull()
                        if (num != null && num > 0) {
                            viewModel.setPatternRepeat(unit.toMm(num))
                        }
                    },
                    label = { Text("Pattern Repeat (${unit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(
                    text = "Cuts are adjusted upward to the next repeat multiple.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Seam Allowance
            Text(
                text = "Seam Allowance: ${String.format(Locale.US, "%.1f %s", unit.fromMm(uiState.seamAllowanceMm), unit.symbol)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun RoomSectionsManager(
    sections: List<RoomSection>,
    unit: UnitType,
    decimalPlaces: Int,
    onAddSectionClick: () -> Unit,
    onRemoveSection: (String) -> Unit
) {
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
                    text = "ROOM SECTIONS (Alcoves, Closets, Bays)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onAddSectionClick) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Section")
                }
            }

            if (sections.isEmpty()) {
                Text(
                    text = "No additional sections added.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                for (sec in sections) {
                    val secAreaM2 = (sec.lengthMm * sec.widthMm) / 1_000_000.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = sec.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${String.format(Locale.US, "%.${decimalPlaces}f", unit.fromMm(sec.lengthMm))} × ${String.format(Locale.US, "%.${decimalPlaces}f", unit.fromMm(sec.widthMm))} ${unit.symbol} (${String.format(Locale.US, "%.${decimalPlaces}f m²", secAreaM2)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(onClick = { onRemoveSection(sec.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExclusionsManager(
    exclusions: List<RoomExclusion>,
    unit: UnitType,
    decimalPlaces: Int,
    onAddExclusionClick: () -> Unit,
    onRemoveExclusion: (String) -> Unit
) {
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
                    text = "DOORWAYS & EXCLUSIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onAddExclusionClick) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            Text(
                text = "⚠ For carpet ordering, subtracting a small area does not necessarily reduce the number of full carpet widths required.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(6.dp))

            for (ex in exclusions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${ex.name}: ${String.format(Locale.US, "%.${decimalPlaces}f", unit.fromMm(ex.widthMm))} × ${String.format(Locale.US, "%.${decimalPlaces}f", unit.fromMm(ex.depthMm))} ${unit.symbol}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    IconButton(onClick = { onRemoveExclusion(ex.id) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddSectionDialog(
    unit: UnitType,
    onDismiss: () -> Unit,
    onAdd: (name: String, lengthMm: Double, widthMm: Double) -> Unit
) {
    var name by remember { mutableStateOf("Alcove") }
    var lenText by remember { mutableStateOf("150") }
    var widText by remember { mutableStateOf("120") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room Section") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Section Name") },
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
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddExclusionDialog(
    unit: UnitType,
    onDismiss: () -> Unit,
    onAdd: (name: String, widthMm: Double, depthMm: Double) -> Unit
) {
    var name by remember { mutableStateOf("Door Opening") }
    var widText by remember { mutableStateOf("90") }
    var depText by remember { mutableStateOf("15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exclusion") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exclusion Type") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = widText,
                    onValueChange = { widText = it },
                    label = { Text("Width (${unit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = depText,
                    onValueChange = { depText = it },
                    label = { Text("Depth (${unit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val w = widText.toDoubleOrNull() ?: 0.0
                    val d = depText.toDoubleOrNull() ?: 0.0
                    if (w > 0 && d > 0) {
                        onAdd(name, unit.toMm(w), unit.toMm(d))
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
