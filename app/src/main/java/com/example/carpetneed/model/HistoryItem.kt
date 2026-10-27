package com.example.carpetneed.model

data class HistoryItem(
    val id: String,
    val roomName: String,
    val timestamp: Long,
    val lengthMm: Double,
    val widthMm: Double,
    val floorAreaM2: Double,
    val rollWidthMm: Double,
    val purchaseLengthM: Double,
    val purchaseAreaM2: Double,
    val excessM2: Double,
    val wastePercent: Double,
    val piecesCount: Int,
    val seamsCount: Int,
    val orientationName: String,
    val unitSymbol: String
)
