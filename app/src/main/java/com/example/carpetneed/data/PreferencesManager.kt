package com.example.carpetneed.data

import android.content.Context
import android.content.SharedPreferences
import com.example.carpetneed.model.*

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("carpetneed_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "pref_theme"
        private const val KEY_DYNAMIC_COLOR = "pref_dynamic_color"
        private const val KEY_DEFAULT_UNIT = "pref_default_unit"
        private const val KEY_DEFAULT_ROLL_WIDTH = "pref_default_roll_width"
        private const val KEY_DEFAULT_WASTE_ALLOWANCE = "pref_default_waste_allowance"
        private const val KEY_DEFAULT_SEAM_ALLOWANCE = "pref_default_seam_allowance"
        private const val KEY_DEFAULT_PATTERN_REPEAT = "pref_default_pattern_repeat"
        private const val KEY_DEFAULT_ORIENTATION = "pref_default_orientation"
        private const val KEY_SHOW_ROOM_DIAGRAM = "pref_show_room_diagram"
        private const val KEY_SHOW_CUTTING_PLAN = "pref_show_cutting_plan"
        private const val KEY_SHOW_CALCULATION_BREAKDOWN = "pref_show_calc_breakdown"
        private const val KEY_SHOW_WASTE_BREAKDOWN = "pref_show_waste_breakdown"
        private const val KEY_SHOW_UNIT_CONVERSIONS = "pref_show_unit_conversions"
        private const val KEY_DECIMAL_PLACES = "pref_decimal_places"
        private const val KEY_LIVE_CALCULATION = "pref_live_calculation"
        private const val KEY_HAPTICS = "pref_haptics"
        private const val KEY_ANIMATIONS = "pref_animations"
        private const val KEY_CONFIRM_DELETE = "pref_confirm_delete"
        private const val KEY_RESTORE_LAST = "pref_restore_last"

        // Last calculation state
        private const val KEY_LAST_ROOM_NAME = "last_room_name"
        private const val KEY_LAST_LENGTH_MM = "last_length_mm"
        private const val KEY_LAST_WIDTH_MM = "last_width_mm"
        private const val KEY_LAST_ROLL_WIDTH_MM = "last_roll_width_mm"
        private const val KEY_LAST_ORIENTATION = "last_orientation"
        private const val KEY_LAST_WASTE_PERCENT = "last_waste_percent"
        private const val KEY_LAST_PATTERN_REPEAT_MM = "last_pattern_repeat_mm"
        private const val KEY_LAST_SEAM_ALLOWANCE_MM = "last_seam_allowance_mm"
        private const val KEY_LAST_PILE_DIRECTION = "last_pile_direction"
        private const val KEY_LAST_UNIT = "last_unit"
    }

    fun getSettings(): AppSettings {
        val themeStr = prefs.getString(KEY_THEME, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val unitStr = prefs.getString(KEY_DEFAULT_UNIT, UnitType.CM.name) ?: UnitType.CM.name
        val orientStr = prefs.getString(KEY_DEFAULT_ORIENTATION, CarpetOrientation.AUTOMATIC.name) ?: CarpetOrientation.AUTOMATIC.name

        return AppSettings(
            themeMode = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM),
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
            defaultUnit = runCatching { UnitType.valueOf(unitStr) }.getOrDefault(UnitType.CM),
            defaultRollWidthMm = prefs.getFloat(KEY_DEFAULT_ROLL_WIDTH, 4000f).toDouble(),
            defaultWasteAllowancePercent = prefs.getFloat(KEY_DEFAULT_WASTE_ALLOWANCE, 0f).toDouble(),
            defaultSeamAllowanceMm = prefs.getFloat(KEY_DEFAULT_SEAM_ALLOWANCE, 50f).toDouble(),
            defaultPatternRepeatMm = prefs.getFloat(KEY_DEFAULT_PATTERN_REPEAT, 0f).toDouble(),
            defaultOrientation = runCatching { CarpetOrientation.valueOf(orientStr) }.getOrDefault(CarpetOrientation.AUTOMATIC),
            showRoomDiagram = prefs.getBoolean(KEY_SHOW_ROOM_DIAGRAM, true),
            showCuttingPlan = prefs.getBoolean(KEY_SHOW_CUTTING_PLAN, true),
            showCalculationBreakdown = prefs.getBoolean(KEY_SHOW_CALCULATION_BREAKDOWN, true),
            showWasteBreakdown = prefs.getBoolean(KEY_SHOW_WASTE_BREAKDOWN, true),
            showUnitConversions = prefs.getBoolean(KEY_SHOW_UNIT_CONVERSIONS, false),
            decimalPlaces = prefs.getInt(KEY_DECIMAL_PLACES, 2),
            liveCalculation = prefs.getBoolean(KEY_LIVE_CALCULATION, true),
            haptics = prefs.getBoolean(KEY_HAPTICS, true),
            animations = prefs.getBoolean(KEY_ANIMATIONS, true),
            confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true),
            restoreLastCalculation = prefs.getBoolean(KEY_RESTORE_LAST, true)
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.themeMode.name)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putString(KEY_DEFAULT_UNIT, settings.defaultUnit.name)
            .putFloat(KEY_DEFAULT_ROLL_WIDTH, settings.defaultRollWidthMm.toFloat())
            .putFloat(KEY_DEFAULT_WASTE_ALLOWANCE, settings.defaultWasteAllowancePercent.toFloat())
            .putFloat(KEY_DEFAULT_SEAM_ALLOWANCE, settings.defaultSeamAllowanceMm.toFloat())
            .putFloat(KEY_DEFAULT_PATTERN_REPEAT, settings.defaultPatternRepeatMm.toFloat())
            .putString(KEY_DEFAULT_ORIENTATION, settings.defaultOrientation.name)
            .putBoolean(KEY_SHOW_ROOM_DIAGRAM, settings.showRoomDiagram)
            .putBoolean(KEY_SHOW_CUTTING_PLAN, settings.showCuttingPlan)
            .putBoolean(KEY_SHOW_CALCULATION_BREAKDOWN, settings.showCalculationBreakdown)
            .putBoolean(KEY_SHOW_WASTE_BREAKDOWN, settings.showWasteBreakdown)
            .putBoolean(KEY_SHOW_UNIT_CONVERSIONS, settings.showUnitConversions)
            .putInt(KEY_DECIMAL_PLACES, settings.decimalPlaces)
            .putBoolean(KEY_LIVE_CALCULATION, settings.liveCalculation)
            .putBoolean(KEY_HAPTICS, settings.haptics)
            .putBoolean(KEY_ANIMATIONS, settings.animations)
            .putBoolean(KEY_CONFIRM_DELETE, settings.confirmDelete)
            .putBoolean(KEY_RESTORE_LAST, settings.restoreLastCalculation)
            .apply()
    }

    fun resetSettings(): AppSettings {
        val defaultSettings = AppSettings()
        saveSettings(defaultSettings)
        return defaultSettings
    }

    fun saveLastCalculation(
        roomName: String,
        lengthMm: Double,
        widthMm: Double,
        rollWidthMm: Double,
        orientation: CarpetOrientation,
        wastePercent: Double,
        patternRepeatMm: Double,
        seamAllowanceMm: Double,
        pileDirection: PileDirection,
        unit: UnitType
    ) {
        prefs.edit()
            .putString(KEY_LAST_ROOM_NAME, roomName)
            .putFloat(KEY_LAST_LENGTH_MM, lengthMm.toFloat())
            .putFloat(KEY_LAST_WIDTH_MM, widthMm.toFloat())
            .putFloat(KEY_LAST_ROLL_WIDTH_MM, rollWidthMm.toFloat())
            .putString(KEY_LAST_ORIENTATION, orientation.name)
            .putFloat(KEY_LAST_WASTE_PERCENT, wastePercent.toFloat())
            .putFloat(KEY_LAST_PATTERN_REPEAT_MM, patternRepeatMm.toFloat())
            .putFloat(KEY_LAST_SEAM_ALLOWANCE_MM, seamAllowanceMm.toFloat())
            .putString(KEY_LAST_PILE_DIRECTION, pileDirection.name)
            .putString(KEY_LAST_UNIT, unit.name)
            .apply()
    }

    data class LastCalculationData(
        val roomName: String,
        val lengthMm: Double,
        val widthMm: Double,
        val rollWidthMm: Double,
        val orientation: CarpetOrientation,
        val wastePercent: Double,
        val patternRepeatMm: Double,
        val seamAllowanceMm: Double,
        val pileDirection: PileDirection,
        val unit: UnitType
    )

    fun getLastCalculation(): LastCalculationData? {
        if (!prefs.contains(KEY_LAST_LENGTH_MM)) return null
        return LastCalculationData(
            roomName = prefs.getString(KEY_LAST_ROOM_NAME, "Room 1") ?: "Room 1",
            lengthMm = prefs.getFloat(KEY_LAST_LENGTH_MM, 5000f).toDouble(),
            widthMm = prefs.getFloat(KEY_LAST_WIDTH_MM, 4000f).toDouble(),
            rollWidthMm = prefs.getFloat(KEY_LAST_ROLL_WIDTH_MM, 4000f).toDouble(),
            orientation = runCatching {
                CarpetOrientation.valueOf(prefs.getString(KEY_LAST_ORIENTATION, CarpetOrientation.AUTOMATIC.name)!!)
            }.getOrDefault(CarpetOrientation.AUTOMATIC),
            wastePercent = prefs.getFloat(KEY_LAST_WASTE_PERCENT, 0f).toDouble(),
            patternRepeatMm = prefs.getFloat(KEY_LAST_PATTERN_REPEAT_MM, 0f).toDouble(),
            seamAllowanceMm = prefs.getFloat(KEY_LAST_SEAM_ALLOWANCE_MM, 50f).toDouble(),
            pileDirection = runCatching {
                PileDirection.valueOf(prefs.getString(KEY_LAST_PILE_DIRECTION, PileDirection.SAME_DIRECTION.name)!!)
            }.getOrDefault(PileDirection.SAME_DIRECTION),
            unit = runCatching {
                UnitType.valueOf(prefs.getString(KEY_LAST_UNIT, UnitType.CM.name)!!)
            }.getOrDefault(UnitType.CM)
        )
    }
}
