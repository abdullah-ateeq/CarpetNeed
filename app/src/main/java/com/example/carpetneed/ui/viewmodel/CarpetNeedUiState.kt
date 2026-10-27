package com.example.carpetneed.ui.viewmodel

import com.example.carpetneed.model.*

data class CarpetNeedUiState(
    val unit: UnitType = UnitType.CM,
    val roomName: String = "Room 1",
    val roomLengthMm: Double = 5000.0,
    val roomWidthMm: Double = 4000.0,
    val rollWidthMm: Double = 4000.0,
    val orientation: CarpetOrientation = CarpetOrientation.AUTOMATIC,
    val wasteAllowancePercent: Double = 0.0,
    val patternRepeatMm: Double = 0.0,
    val seamAllowanceMm: Double = 50.0,
    val pileDirection: PileDirection = PileDirection.SAME_DIRECTION,
    val allowanceMethod: AllowanceMethod = AllowanceMethod.CARPET_LENGTH,
    val purchaseRounding: PurchaseRounding = PurchaseRounding.EXACT,
    val patternMatch: PatternMatch = PatternMatch.NONE,
    val shape: RoomShape = RoomShape.RECTANGLE,
    val lShapeOverallLengthMm: Double = 6000.0,
    val lShapeOverallWidthMm: Double = 5000.0,
    val lShapeCutoutLengthMm: Double = 2000.0,
    val lShapeCutoutWidthMm: Double = 1500.0,
    val sections: List<RoomSection> = emptyList(),
    val exclusions: List<RoomExclusion> = emptyList(),
    val calculationMode: CalculationMode = CalculationMode.SINGLE_ROOM,
    val multipleRooms: List<CarpetRoom> = emptyList(),
    val useSameCarpetSettings: Boolean = true,
    val result: CarpetResult? = null,
    val multipleRoomSummary: MultipleRoomSummary? = null,
    val multipleRoomResults: List<Triple<CarpetRoom, CarpetSettings, CarpetResult>> = emptyList(),
    val historyList: List<HistoryItem> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val errorMessage: String? = null,
    val userNotice: String? = null
)
