package com.example.carpetneed.model

data class CarpetPiece(
    val pieceIndex: Int,
    val cutWidthMm: Double,
    val rollWidthMm: Double,
    val cutLengthMm: Double,
    val unusedWidthMm: Double = maxOf(0.0, rollWidthMm - cutWidthMm),
    val startRollMm: Double = 0.0,
    val endRollMm: Double = 0.0,
    val roomXOffsetMm: Double = 0.0,
    val roomYOffsetMm: Double = 0.0,
    val sectionName: String = "Room"
)

data class OrientationOptionResult(
    val orientation: CarpetOrientation,
    val coverageWidthMm: Double,
    val coverageLengthMm: Double,
    val pieceCount: Int,
    val pieceLengthMm: Double,
    val baseRollLengthMm: Double,
    val allowanceLengthMm: Double,
    val purchaseLengthMm: Double,
    val purchaseAreaMm2: Double,
    val excessAreaMm2: Double,
    val wastePercent: Double,
    val estimatedSeams: Int,
    val pieces: List<CarpetPiece>,
    val widthWasteAreaMm2: Double,
    val lengthWasteAreaMm2: Double,
    val allowanceAreaMm2: Double
)

data class CarpetResult(
    val floorAreaMm2: Double,
    val requiredWidths: Int,
    val baseLengthMm: Double,
    val allowanceLengthMm: Double,
    val purchaseLengthMm: Double,
    val purchaseAreaMm2: Double,
    val excessAreaMm2: Double,
    val wastePercent: Double,
    val estimatedSeams: Int,
    val chosenOrientation: CarpetOrientation,
    val optionA: OrientationOptionResult,
    val optionB: OrientationOptionResult,
    val pieces: List<CarpetPiece>,
    val geometryWasteMm2: Double,
    val userAllowanceMm2: Double,
    val totalExcessMm2: Double,
    val calculationSteps: List<String> = emptyList(),
    val notes: List<String> = emptyList()
)

data class MultipleRoomSummary(
    val totalRooms: Int,
    val totalFloorAreaMm2: Double,
    val totalCarpetLengthMm: Double,
    val totalPurchaseAreaMm2: Double,
    val totalExcessAreaMm2: Double,
    val totalWastePercent: Double,
    val rollWidthGroups: Map<Double, Double> // rollWidthMm -> totalLengthMm
)
