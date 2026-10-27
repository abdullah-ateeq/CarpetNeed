package com.example.carpetneed.data

import android.content.Context
import android.content.Intent
import com.example.carpetneed.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.util.Locale

object ExportManager {

    fun generateTxt(
        room: CarpetRoom,
        settings: CarpetSettings,
        result: CarpetResult,
        unit: UnitType,
        decimalPlaces: Int = 2
    ): String {
        val lenDisplay = formatValue(unit.fromMm(room.lengthMm), decimalPlaces)
        val widthDisplay = formatValue(unit.fromMm(room.widthMm), decimalPlaces)
        val rollDisplay = formatValue(unit.fromMm(settings.rollWidthMm), decimalPlaces)
        val floorAreaM2 = formatValue(result.floorAreaMm2 / 1_000_000.0, decimalPlaces)
        val purchaseLengthM = formatValue(result.purchaseLengthMm / 1000.0, decimalPlaces)
        val baseLengthM = formatValue(result.baseLengthMm / 1000.0, decimalPlaces)
        val purchaseAreaM2 = formatValue(result.purchaseAreaMm2 / 1_000_000.0, decimalPlaces)
        val excessM2 = formatValue(result.excessAreaMm2 / 1_000_000.0, decimalPlaces)

        val firstPiece = result.pieces.firstOrNull()
        val pieceWidthDisplay = if (firstPiece != null) formatValue(unit.fromMm(firstPiece.rollWidthMm), decimalPlaces) else rollDisplay
        val pieceLenDisplay = if (firstPiece != null) formatValue(unit.fromMm(firstPiece.cutLengthMm), decimalPlaces) else lenDisplay

        return buildString {
            appendLine("CARPETNEED")
            appendLine()
            appendLine("Room:")
            appendLine(room.name)
            appendLine()
            appendLine("Room Size:")
            appendLine("$lenDisplay × $widthDisplay ${unit.symbol}")
            appendLine()
            appendLine("Floor Area:")
            appendLine("$floorAreaM2 m²")
            appendLine()
            appendLine("Carpet Roll Width:")
            appendLine("$rollDisplay ${unit.symbol}")
            appendLine()
            appendLine("Orientation:")
            appendLine(result.chosenOrientation.label)
            appendLine()
            appendLine("Required Pieces:")
            appendLine("${result.requiredWidths}")
            appendLine()
            appendLine("Piece Size:")
            appendLine("$pieceWidthDisplay × $pieceLenDisplay ${unit.symbol}")
            appendLine()
            appendLine("Base Roll Length:")
            appendLine("$baseLengthM m")
            appendLine()
            appendLine("Waste Allowance:")
            appendLine("${formatValue(settings.wasteAllowancePercent, 1)}%")
            appendLine()
            appendLine("Total Purchase:")
            appendLine("$purchaseLengthM linear m")
            appendLine()
            appendLine("Purchase Area:")
            appendLine("$purchaseAreaM2 m²")
            appendLine()
            appendLine("Estimated Excess:")
            appendLine("$excessM2 m²")
            appendLine()
            appendLine("Estimated Seams:")
            appendLine("${result.estimatedSeams}")
        }
    }

    fun generateCsv(
        room: CarpetRoom,
        settings: CarpetSettings,
        result: CarpetResult,
        unit: UnitType,
        decimalPlaces: Int = 2
    ): String {
        val lenDisplay = formatValue(unit.fromMm(room.lengthMm), decimalPlaces)
        val widthDisplay = formatValue(unit.fromMm(room.widthMm), decimalPlaces)
        val floorAreaM2 = formatValue(result.floorAreaMm2 / 1_000_000.0, decimalPlaces)
        val rollDisplay = formatValue(unit.fromMm(settings.rollWidthMm), decimalPlaces)
        val pieceCount = result.requiredWidths
        val pieceLenDisplay = if (result.pieces.isNotEmpty()) formatValue(unit.fromMm(result.pieces[0].cutLengthMm), decimalPlaces) else lenDisplay
        val rollLengthM = formatValue(result.purchaseLengthMm / 1000.0, decimalPlaces)
        val purchaseAreaM2 = formatValue(result.purchaseAreaMm2 / 1_000_000.0, decimalPlaces)
        val excessM2 = formatValue(result.excessAreaMm2 / 1_000_000.0, decimalPlaces)
        val seams = result.estimatedSeams

        return buildString {
            appendLine("Room,Length,Width,Floor Area,Roll Width,Pieces,Piece Length,Roll Length,Purchase Area,Excess,Seams")
            appendLine("${escapeCsv(room.name)},$lenDisplay,$widthDisplay,$floorAreaM2,$rollDisplay,$pieceCount,$pieceLenDisplay,$rollLengthM,$purchaseAreaM2,$excessM2,$seams")
        }
    }

    fun generateJson(
        room: CarpetRoom,
        settings: CarpetSettings,
        result: CarpetResult,
        unit: UnitType
    ): String {
        val obj = JSONObject().apply {
            put("name", room.name)
            put("unit", unit.name)

            val roomObj = JSONObject().apply {
                put("length", unit.fromMm(room.lengthMm))
                put("width", unit.fromMm(room.widthMm))
                put("floorAreaM2", result.floorAreaMm2 / 1_000_000.0)
            }
            put("room", roomObj)

            val firstPieceLen = result.pieces.firstOrNull()?.cutLengthMm ?: room.lengthMm
            val carpetObj = JSONObject().apply {
                put("rollWidth", unit.fromMm(settings.rollWidthMm))
                put("orientation", result.chosenOrientation.name)
                put("pieces", result.requiredWidths)
                put("pieceLength", unit.fromMm(firstPieceLen))
                put("rollLength", unit.fromMm(result.purchaseLengthMm))
                put("purchaseAreaM2", result.purchaseAreaMm2 / 1_000_000.0)
            }
            put("carpet", carpetObj)

            put("wasteAllowancePercent", settings.wasteAllowancePercent)
            put("estimatedExcessM2", result.excessAreaMm2 / 1_000_000.0)
            put("estimatedSeams", result.estimatedSeams)
        }
        return obj.toString(2)
    }

    fun generateShareText(
        room: CarpetRoom,
        settings: CarpetSettings,
        result: CarpetResult,
        unit: UnitType,
        decimalPlaces: Int = 2
    ): String {
        val lenDisplay = formatValue(unit.fromMm(room.lengthMm), decimalPlaces)
        val widthDisplay = formatValue(unit.fromMm(room.widthMm), decimalPlaces)
        val rollDisplay = formatValue(unit.fromMm(settings.rollWidthMm), decimalPlaces)
        val floorAreaM2 = formatValue(result.floorAreaMm2 / 1_000_000.0, decimalPlaces)
        val purchaseLengthM = formatValue(result.purchaseLengthMm / 1000.0, decimalPlaces)
        val purchaseAreaM2 = formatValue(result.purchaseAreaMm2 / 1_000_000.0, decimalPlaces)
        val excessM2 = formatValue(result.excessAreaMm2 / 1_000_000.0, decimalPlaces)

        return buildString {
            appendLine("CarpetNeed")
            appendLine()
            appendLine(room.name)
            appendLine()
            appendLine("Room:")
            appendLine("$lenDisplay × $widthDisplay ${unit.symbol}")
            appendLine()
            appendLine("Floor Area:")
            appendLine("$floorAreaM2 m²")
            appendLine()
            appendLine("Carpet:")
            appendLine("$rollDisplay ${unit.symbol} wide")
            appendLine()
            appendLine("Required:")
            appendLine("$purchaseLengthM linear m")
            appendLine()
            appendLine("Purchase Area:")
            appendLine("$purchaseAreaM2 m²")
            appendLine()
            appendLine("Estimated Excess:")
            appendLine("$excessM2 m²")
            appendLine()
            appendLine("Seams:")
            appendLine("${result.estimatedSeams}")
        }
    }

    fun generateMultipleRoomsTxt(
        summary: MultipleRoomSummary,
        roomsList: List<Triple<CarpetRoom, CarpetSettings, CarpetResult>>,
        unit: UnitType,
        decimalPlaces: Int = 2
    ): String {
        return buildString {
            appendLine("CARPETNEED — PROJECT SUMMARY")
            appendLine("============================")
            appendLine("Total Rooms: ${summary.totalRooms}")
            appendLine("Total Floor Area: ${formatValue(summary.totalFloorAreaMm2 / 1_000_000.0, decimalPlaces)} m²")
            appendLine("Total Carpet Purchase: ${formatValue(summary.totalCarpetLengthMm / 1000.0, decimalPlaces)} linear m")
            appendLine("Total Purchased Area: ${formatValue(summary.totalPurchaseAreaMm2 / 1_000_000.0, decimalPlaces)} m²")
            appendLine("Total Excess: ${formatValue(summary.totalExcessAreaMm2 / 1_000_000.0, decimalPlaces)} m² (${formatValue(summary.totalWastePercent, 1)}%)")
            appendLine()
            appendLine("ROOM DETAILS:")
            for ((room, settings, result) in roomsList) {
                appendLine("----------------------------")
                appendLine("Room: ${room.name}")
                appendLine("Size: ${formatValue(unit.fromMm(room.lengthMm), decimalPlaces)} × ${formatValue(unit.fromMm(room.widthMm), decimalPlaces)} ${unit.symbol}")
                appendLine("Floor Area: ${formatValue(result.floorAreaMm2 / 1_000_000.0, decimalPlaces)} m²")
                appendLine("Roll Width: ${formatValue(unit.fromMm(settings.rollWidthMm), decimalPlaces)} ${unit.symbol}")
                appendLine("Carpet Required: ${formatValue(result.purchaseLengthMm / 1000.0, decimalPlaces)} linear m")
                appendLine("Purchase Area: ${formatValue(result.purchaseAreaMm2 / 1_000_000.0, decimalPlaces)} m²")
                appendLine("Excess: ${formatValue(result.excessAreaMm2 / 1_000_000.0, decimalPlaces)} m²")
                appendLine("Pieces: ${result.requiredWidths}, Seams: ${result.estimatedSeams}")
            }
        }
    }

    fun shareText(context: Context, text: String, title: String = "Share CarpetNeed Calculation") {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }

    fun writeToOutputStream(outputStream: OutputStream, content: String) {
        outputStream.use { it.write(content.toByteArray(Charsets.UTF_8)) }
    }

    private fun escapeCsv(str: String): String {
        return if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            "\"${str.replace("\"", "\"\"")}\""
        } else {
            str
        }
    }

    private fun formatValue(value: Double, decimals: Int): String {
        return String.format(Locale.US, "%.${decimals}f", value)
    }
}
