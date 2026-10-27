package com.example.carpetneed.model

enum class UnitType(
    val label: String,
    val symbol: String,
    val mmPerUnit: Double
) {
    MM("Millimeters", "mm", 1.0),
    CM("Centimeters", "cm", 10.0),
    M("Meters", "m", 1000.0),
    IN("Inches", "in", 25.4),
    FT("Feet", "ft", 304.8);

    fun toMm(value: Double): Double = value * mmPerUnit
    fun fromMm(mm: Double): Double = mm / mmPerUnit

    companion object {
        fun fromSymbol(symbol: String): UnitType =
            entries.find { it.symbol.equals(symbol, ignoreCase = true) } ?: CM
    }
}
