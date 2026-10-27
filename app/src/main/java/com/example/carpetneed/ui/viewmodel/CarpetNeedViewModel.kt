package com.example.carpetneed.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpetneed.calculator.CarpetCalculator
import com.example.carpetneed.data.ExportManager
import com.example.carpetneed.data.HistoryRepository
import com.example.carpetneed.data.PreferencesManager
import com.example.carpetneed.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CarpetNeedViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)
    private val historyRepo = HistoryRepository(application)

    private val _uiState = MutableStateFlow(CarpetNeedUiState())
    val uiState: StateFlow<CarpetNeedUiState> = _uiState.asStateFlow()

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        val settings = prefsManager.getSettings()
        val history = historyRepo.getHistory()
        val lastCalc = if (settings.restoreLastCalculation) prefsManager.getLastCalculation() else null

        val initialUnit = lastCalc?.unit ?: settings.defaultUnit
        val initialRoomName = lastCalc?.roomName ?: "Room 1"
        val initialLengthMm = lastCalc?.lengthMm ?: 5000.0
        val initialWidthMm = lastCalc?.widthMm ?: 4000.0
        val initialRollWidthMm = lastCalc?.rollWidthMm ?: settings.defaultRollWidthMm
        val initialOrientation = lastCalc?.orientation ?: settings.defaultOrientation
        val initialWastePercent = lastCalc?.wastePercent ?: settings.defaultWasteAllowancePercent
        val initialPatternRepeatMm = lastCalc?.patternRepeatMm ?: settings.defaultPatternRepeatMm
        val initialSeamAllowanceMm = lastCalc?.seamAllowanceMm ?: settings.defaultSeamAllowanceMm
        val initialPileDirection = lastCalc?.pileDirection ?: PileDirection.SAME_DIRECTION

        val defaultMultiRooms = listOf(
            CarpetRoom(
                id = UUID.randomUUID().toString(),
                name = "Living Room",
                lengthMm = 5000.0,
                widthMm = 4000.0,
                orientation = CarpetOrientation.AUTOMATIC
            ),
            CarpetRoom(
                id = UUID.randomUUID().toString(),
                name = "Bedroom 1",
                lengthMm = 4000.0,
                widthMm = 3500.0,
                orientation = CarpetOrientation.AUTOMATIC
            ),
            CarpetRoom(
                id = UUID.randomUUID().toString(),
                name = "Bedroom 2",
                lengthMm = 3500.0,
                widthMm = 3000.0,
                orientation = CarpetOrientation.AUTOMATIC
            )
        )

        _uiState.update {
            it.copy(
                unit = initialUnit,
                roomName = initialRoomName,
                roomLengthMm = initialLengthMm,
                roomWidthMm = initialWidthMm,
                rollWidthMm = initialRollWidthMm,
                orientation = initialOrientation,
                wasteAllowancePercent = initialWastePercent,
                patternRepeatMm = initialPatternRepeatMm,
                seamAllowanceMm = initialSeamAllowanceMm,
                pileDirection = initialPileDirection,
                appSettings = settings,
                historyList = history,
                multipleRooms = defaultMultiRooms
            )
        }

        recalculate()
    }

    fun setUnit(newUnit: UnitType) {
        _uiState.update { it.copy(unit = newUnit) }
    }

    fun setCalculationMode(mode: CalculationMode) {
        _uiState.update { it.copy(calculationMode = mode) }
        recalculate()
    }

    fun setRoomName(name: String) {
        _uiState.update { it.copy(roomName = name) }
    }

    fun setRoomDimensions(lengthMm: Double, widthMm: Double) {
        _uiState.update {
            it.copy(
                roomLengthMm = lengthMm,
                roomWidthMm = widthMm
            )
        }
        recalculate()
    }

    fun setRoomShape(shape: RoomShape) {
        _uiState.update { it.copy(shape = shape) }
        recalculate()
    }

    fun setLShapeDimensions(
        overallLengthMm: Double,
        overallWidthMm: Double,
        cutoutLengthMm: Double,
        cutoutWidthMm: Double
    ) {
        _uiState.update {
            it.copy(
                lShapeOverallLengthMm = overallLengthMm,
                lShapeOverallWidthMm = overallWidthMm,
                lShapeCutoutLengthMm = cutoutLengthMm,
                lShapeCutoutWidthMm = cutoutWidthMm
            )
        }
        recalculate()
    }

    fun setRollWidth(rollWidthMm: Double) {
        _uiState.update { it.copy(rollWidthMm = rollWidthMm) }
        recalculate()
    }

    fun setOrientation(orientation: CarpetOrientation) {
        _uiState.update { it.copy(orientation = orientation) }
        recalculate()
    }

    fun setWasteAllowance(percent: Double) {
        _uiState.update { it.copy(wasteAllowancePercent = percent) }
        recalculate()
    }

    fun setAllowanceMethod(method: AllowanceMethod) {
        _uiState.update { it.copy(allowanceMethod = method) }
        recalculate()
    }

    fun setPurchaseRounding(rounding: PurchaseRounding) {
        _uiState.update { it.copy(purchaseRounding = rounding) }
        recalculate()
    }

    fun setPatternRepeat(repeatMm: Double) {
        _uiState.update { it.copy(patternRepeatMm = repeatMm) }
        recalculate()
    }

    fun setPatternMatch(match: PatternMatch) {
        _uiState.update { it.copy(patternMatch = match) }
        recalculate()
    }

    fun setSeamAllowance(allowanceMm: Double) {
        _uiState.update { it.copy(seamAllowanceMm = allowanceMm) }
        recalculate()
    }

    fun setPileDirection(direction: PileDirection) {
        _uiState.update { it.copy(pileDirection = direction) }
        recalculate()
    }

    fun addSection(name: String, lengthMm: Double, widthMm: Double) {
        val newSection = RoomSection(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Section ${_uiState.value.sections.size + 1}" },
            lengthMm = lengthMm,
            widthMm = widthMm
        )
        _uiState.update { it.copy(sections = it.sections + newSection) }
        recalculate()
    }

    fun removeSection(sectionId: String) {
        _uiState.update { it.copy(sections = it.sections.filter { s -> s.id != sectionId }) }
        recalculate()
    }

    fun addExclusion(name: String, widthMm: Double, depthMm: Double) {
        val newExclusion = RoomExclusion(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Exclusion" },
            widthMm = widthMm,
            depthMm = depthMm
        )
        _uiState.update { it.copy(exclusions = it.exclusions + newExclusion) }
        recalculate()
    }

    fun removeExclusion(exclusionId: String) {
        _uiState.update { it.copy(exclusions = it.exclusions.filter { e -> e.id != exclusionId }) }
        recalculate()
    }

    fun loadQuickExample() {
        _uiState.update {
            it.copy(
                roomName = "Living Room",
                roomLengthMm = 5000.0,
                roomWidthMm = 4000.0,
                rollWidthMm = 4000.0,
                orientation = CarpetOrientation.AUTOMATIC,
                wasteAllowancePercent = 0.0,
                patternRepeatMm = 0.0,
                seamAllowanceMm = 50.0,
                pileDirection = PileDirection.SAME_DIRECTION,
                purchaseRounding = PurchaseRounding.EXACT,
                shape = RoomShape.RECTANGLE,
                sections = emptyList(),
                exclusions = emptyList()
            )
        }
        recalculate()
    }

    // Multiple Rooms management
    fun addRoom(name: String, lengthMm: Double, widthMm: Double) {
        val newRoom = CarpetRoom(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Room ${_uiState.value.multipleRooms.size + 1}" },
            lengthMm = lengthMm,
            widthMm = widthMm,
            orientation = _uiState.value.orientation
        )
        _uiState.update { it.copy(multipleRooms = it.multipleRooms + newRoom) }
        recalculate()
    }

    fun removeRoom(roomId: String) {
        _uiState.update { it.copy(multipleRooms = it.multipleRooms.filter { r -> r.id != roomId }) }
        recalculate()
    }

    fun duplicateRoom(room: CarpetRoom) {
        val duplicated = room.copy(
            id = UUID.randomUUID().toString(),
            name = "${room.name} (Copy)"
        )
        _uiState.update { it.copy(multipleRooms = it.multipleRooms + duplicated) }
        recalculate()
    }

    fun updateRoomOrientation(roomId: String, orientation: CarpetOrientation) {
        _uiState.update {
            it.copy(
                multipleRooms = it.multipleRooms.map { r ->
                    if (r.id == roomId) r.copy(orientation = orientation) else r
                }
            )
        }
        recalculate()
    }

    fun setUseSameCarpetSettings(useSame: Boolean) {
        _uiState.update { it.copy(useSameCarpetSettings = useSame) }
        recalculate()
    }

    // History
    fun saveCurrentCalculationToHistory(): String {
        val state = _uiState.value
        val result = state.result ?: return "No valid calculation to save"

        val historyItem = HistoryItem(
            id = UUID.randomUUID().toString(),
            roomName = state.roomName,
            timestamp = System.currentTimeMillis(),
            lengthMm = state.roomLengthMm,
            widthMm = state.roomWidthMm,
            floorAreaM2 = result.floorAreaMm2 / 1_000_000.0,
            rollWidthMm = state.rollWidthMm,
            purchaseLengthM = result.purchaseLengthMm / 1000.0,
            purchaseAreaM2 = result.purchaseAreaMm2 / 1_000_000.0,
            excessM2 = result.excessAreaMm2 / 1_000_000.0,
            wastePercent = result.wastePercent,
            piecesCount = result.requiredWidths,
            seamsCount = result.estimatedSeams,
            orientationName = result.chosenOrientation.label,
            unitSymbol = state.unit.symbol
        )

        val updated = historyRepo.saveCalculation(historyItem)
        _uiState.update { it.copy(historyList = updated, userNotice = "Saved to History") }
        return "Saved to History"
    }

    fun loadHistoryItem(item: HistoryItem) {
        _uiState.update {
            it.copy(
                roomName = item.roomName,
                roomLengthMm = item.lengthMm,
                roomWidthMm = item.widthMm,
                rollWidthMm = item.rollWidthMm,
                calculationMode = CalculationMode.SINGLE_ROOM,
                shape = RoomShape.RECTANGLE,
                userNotice = "Loaded ${item.roomName} from History"
            )
        }
        recalculate()
    }

    fun duplicateHistoryItem(item: HistoryItem) {
        val duplicated = item.copy(
            id = UUID.randomUUID().toString(),
            roomName = "${item.roomName} (Copy)",
            timestamp = System.currentTimeMillis()
        )
        val updated = historyRepo.saveCalculation(duplicated)
        _uiState.update { it.copy(historyList = updated) }
    }

    fun deleteHistoryItem(id: String) {
        val updated = historyRepo.deleteItem(id)
        _uiState.update { it.copy(historyList = updated) }
    }

    fun clearHistory() {
        historyRepo.clearHistory()
        _uiState.update { it.copy(historyList = emptyList()) }
    }

    fun clearUserNotice() {
        _uiState.update { it.copy(userNotice = null) }
    }

    // Settings
    fun updateSettings(newSettings: AppSettings) {
        prefsManager.saveSettings(newSettings)
        _uiState.update { it.copy(appSettings = newSettings) }
        recalculate()
    }

    fun resetSettings() {
        val defaultSettings = prefsManager.resetSettings()
        _uiState.update { it.copy(appSettings = defaultSettings) }
        recalculate()
    }

    private fun getCurrentSettings(): CarpetSettings {
        val state = _uiState.value
        return CarpetSettings(
            rollWidthMm = state.rollWidthMm,
            wasteAllowancePercent = state.wasteAllowancePercent,
            allowanceMethod = state.allowanceMethod,
            seamAllowanceMm = state.seamAllowanceMm,
            patternRepeatMm = state.patternRepeatMm,
            patternMatch = state.patternMatch,
            purchaseRounding = state.purchaseRounding,
            pileDirection = state.pileDirection
        )
    }

    private fun getCurrentRoom(): CarpetRoom {
        val state = _uiState.value
        val lShape = if (state.shape == RoomShape.L_SHAPE) {
            try {
                LShapeDimensions(
                    overallLengthMm = state.lShapeOverallLengthMm,
                    overallWidthMm = state.lShapeOverallWidthMm,
                    cutoutLengthMm = state.lShapeCutoutLengthMm,
                    cutoutWidthMm = state.lShapeCutoutWidthMm
                )
            } catch (e: Exception) {
                null
            }
        } else null

        return CarpetRoom(
            id = "current-room",
            name = state.roomName,
            shape = state.shape,
            lengthMm = state.roomLengthMm,
            widthMm = state.roomWidthMm,
            lShape = lShape,
            sections = if (state.calculationMode == CalculationMode.ROOM_WITH_SECTIONS) state.sections else emptyList(),
            exclusions = state.exclusions,
            orientation = state.orientation
        )
    }

    fun recalculate() {
        val state = _uiState.value
        val settings = getCurrentSettings()

        try {
            val room = getCurrentRoom()
            val result = CarpetCalculator.calculateRoom(room, settings)

            // Recalculate multiple rooms if needed
            val multiResults = mutableListOf<Triple<CarpetRoom, CarpetSettings, CarpetResult>>()
            for (mRoom in state.multipleRooms) {
                val rSettings = if (state.useSameCarpetSettings) settings else (mRoom.customSettings ?: settings)
                val mResult = CarpetCalculator.calculateRoom(mRoom, rSettings)
                multiResults.add(Triple(mRoom, rSettings, mResult))
            }

            val multiSummary = CarpetCalculator.calculateMultipleRooms(
                multiResults.map { it.first to it.second }
            )

            _uiState.update {
                it.copy(
                    result = result,
                    multipleRoomSummary = multiSummary,
                    multipleRoomResults = multiResults,
                    errorMessage = null
                )
            }

            // Persist last calculation if enabled
            if (state.appSettings.restoreLastCalculation) {
                prefsManager.saveLastCalculation(
                    roomName = state.roomName,
                    lengthMm = state.roomLengthMm,
                    widthMm = state.roomWidthMm,
                    rollWidthMm = state.rollWidthMm,
                    orientation = state.orientation,
                    wastePercent = state.wasteAllowancePercent,
                    patternRepeatMm = state.patternRepeatMm,
                    seamAllowanceMm = state.seamAllowanceMm,
                    pileDirection = state.pileDirection,
                    unit = state.unit
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    result = null,
                    errorMessage = e.message ?: "Invalid dimensions or settings"
                )
            }
        }
    }
}
