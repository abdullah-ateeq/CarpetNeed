package com.example.carpetneed.model

enum class CarpetOrientation(val label: String, val description: String) {
    WIDTH_ACROSS_ROOM_WIDTH(
        label = "Roll width across room width",
        description = "Carpet width covers the room's width; lengths run along room length."
    ),
    WIDTH_ACROSS_ROOM_LENGTH(
        label = "Roll width across room length",
        description = "Carpet width covers the room's length; lengths run along room width."
    ),
    AUTOMATIC(
        label = "Automatic / Best Fit",
        description = "Compares both orientations and recommends the one minimizing carpet purchase & seams."
    )
}

enum class PurchaseRounding(val label: String, val roundStepMm: Double) {
    EXACT("Exact", 0.0),
    NEAREST_0_1_M("Nearest 0.1 m", 100.0),
    NEAREST_0_5_M("Nearest 0.5 m", 500.0),
    NEAREST_1_M("Nearest 1 m", 1000.0);

    fun roundUp(lengthMm: Double): Double {
        if (roundStepMm <= 0.0) return lengthMm
        return kotlin.math.ceil(lengthMm / roundStepMm) * roundStepMm
    }
}

enum class PileDirection(val label: String, val description: String) {
    SAME_DIRECTION(
        label = "Same direction required",
        description = "All joined pieces maintain the exact same pile and pattern direction. No rotating pieces."
    ),
    ROTATION_ALLOWED(
        label = "Direction can rotate",
        description = "Pieces may be rotated if layout allows."
    )
}

enum class PatternMatch(val label: String) {
    NONE("None"),
    STRAIGHT_MATCH("Straight Match"),
    HALF_DROP("Half Drop"),
    CUSTOM("Custom")
}

enum class AllowanceMethod(val label: String) {
    CARPET_LENGTH("Carpet length"),
    TOTAL_CARPET_AREA("Total carpet area")
}

enum class CalculationMode(val label: String) {
    SINGLE_ROOM("Single Room"),
    MULTIPLE_ROOMS("Multiple Rooms"),
    ROOM_WITH_SECTIONS("Room + Sections")
}

data class CarpetSettings(
    val rollWidthMm: Double = 4000.0,
    val wasteAllowancePercent: Double = 0.0,
    val allowanceMethod: AllowanceMethod = AllowanceMethod.CARPET_LENGTH,
    val seamAllowanceMm: Double = 50.0,
    val patternRepeatMm: Double = 0.0,
    val patternMatch: PatternMatch = PatternMatch.NONE,
    val purchaseRounding: PurchaseRounding = PurchaseRounding.EXACT,
    val pileDirection: PileDirection = PileDirection.SAME_DIRECTION
)
