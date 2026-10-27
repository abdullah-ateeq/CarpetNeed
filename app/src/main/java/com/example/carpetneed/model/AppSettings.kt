package com.example.carpetneed.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultUnit: UnitType = UnitType.CM,
    val defaultRollWidthMm: Double = 4000.0,
    val defaultWasteAllowancePercent: Double = 0.0,
    val defaultSeamAllowanceMm: Double = 50.0,
    val defaultPatternRepeatMm: Double = 0.0,
    val defaultOrientation: CarpetOrientation = CarpetOrientation.AUTOMATIC,
    val showRoomDiagram: Boolean = true,
    val showCuttingPlan: Boolean = true,
    val showCalculationBreakdown: Boolean = true,
    val showWasteBreakdown: Boolean = true,
    val showUnitConversions: Boolean = false,
    val decimalPlaces: Int = 2,
    val liveCalculation: Boolean = true,
    val haptics: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val restoreLastCalculation: Boolean = true
)
