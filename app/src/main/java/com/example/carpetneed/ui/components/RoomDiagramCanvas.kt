package com.example.carpetneed.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carpetneed.model.*
import java.util.Locale
import kotlin.math.min

@Composable
fun RoomDiagramCanvas(
    room: CarpetRoom,
    result: CarpetResult?,
    unit: UnitType,
    decimalPlaces: Int = 2,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(260.dp)
) {
    val roomLengthMm = if (room.shape == RoomShape.L_SHAPE) {
        room.lShape?.overallLengthMm ?: room.lengthMm
    } else room.lengthMm

    val roomWidthMm = if (room.shape == RoomShape.L_SHAPE) {
        room.lShape?.overallWidthMm ?: room.widthMm
    } else room.widthMm

    val floorAreaM2 = if (result != null) result.floorAreaMm2 / 1_000_000.0 else (roomLengthMm * roomWidthMm) / 1_000_000.0
    val floorAreaFt2 = floorAreaM2 * 10.7639

    val lengthDisplay = String.format(Locale.US, "%.${decimalPlaces}f %s", unit.fromMm(roomLengthMm), unit.symbol)
    val widthDisplay = String.format(Locale.US, "%.${decimalPlaces}f %s", unit.fromMm(roomWidthMm), unit.symbol)

    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .background(surfaceColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padX = 50.dp.toPx()
            val padY = 40.dp.toPx()
            val availableW = size.width - (padX * 2)
            val availableH = size.height - (padY * 2)

            if (roomLengthMm <= 0 || roomWidthMm <= 0 || availableW <= 0 || availableH <= 0) return@Canvas

            // Real aspect ratio: scale = min(availableWidth / length, availableHeight / width)
            val scale = min(availableW / roomLengthMm.toFloat(), availableH / roomWidthMm.toFloat())

            val drawW = roomLengthMm.toFloat() * scale
            val drawH = roomWidthMm.toFloat() * scale

            val startX = padX + (availableW - drawW) / 2f
            val startY = padY + (availableH - drawH) / 2f

            if (room.shape == RoomShape.L_SHAPE && room.lShape != null) {
                drawLShapeDiagram(
                    lShape = room.lShape,
                    scale = scale,
                    startX = startX,
                    startY = startY,
                    drawW = drawW,
                    drawH = drawH,
                    outlineColor = outlineColor,
                    primaryColor = primaryColor
                )
            } else {
                drawRectangleDiagram(
                    startX = startX,
                    startY = startY,
                    drawW = drawW,
                    drawH = drawH,
                    result = result,
                    roomLengthMm = roomLengthMm,
                    roomWidthMm = roomWidthMm,
                    outlineColor = outlineColor,
                    primaryColor = primaryColor
                )
            }

            // Draw Dimension Arrows & Labels
            drawDimensionAnnotations(
                startX = startX,
                startY = startY,
                drawW = drawW,
                drawH = drawH,
                lengthText = lengthDisplay,
                widthText = widthDisplay,
                color = textColor
            )
        }

        // Area badge at top right
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = String.format(Locale.US, "%.${decimalPlaces}f m²", floorAreaM2),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = String.format(Locale.US, "%.${decimalPlaces}f ft²", floorAreaFt2),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun DrawScope.drawRectangleDiagram(
    startX: Float,
    startY: Float,
    drawW: Float,
    drawH: Float,
    result: CarpetResult?,
    roomLengthMm: Double,
    roomWidthMm: Double,
    outlineColor: Color,
    primaryColor: Color
) {
    // Fill room background
    drawRect(
        color = Color(0xFFFEF3C7).copy(alpha = 0.6f),
        topLeft = Offset(startX, startY),
        size = Size(drawW, drawH)
    )

    // Draw carpet strip divisions if result is available
    if (result != null && result.pieces.isNotEmpty()) {
        val pieces = result.pieces
        val isWidthAcrossWidth = result.chosenOrientation == CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH

        val stripCount = result.requiredWidths
        if (stripCount > 1) {
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            for (i in 1 until stripCount) {
                if (isWidthAcrossWidth) {
                    // Strips divided along room width (drawH)
                    val y = startY + (drawH / stripCount) * i
                    drawLine(
                        color = Color(0xFFD97706),
                        start = Offset(startX, y),
                        end = Offset(startX + drawW, y),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dashEffect
                    )
                } else {
                    // Strips divided along room length (drawW)
                    val x = startX + (drawW / stripCount) * i
                    drawLine(
                        color = Color(0xFFD97706),
                        start = Offset(x, startY),
                        end = Offset(x, startY + drawH),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }
            }
        }
    }

    // Room outer boundary
    drawRect(
        color = outlineColor,
        topLeft = Offset(startX, startY),
        size = Size(drawW, drawH),
        style = Stroke(width = 2.5f.dp.toPx())
    )
}

private fun DrawScope.drawLShapeDiagram(
    lShape: LShapeDimensions,
    scale: Float,
    startX: Float,
    startY: Float,
    drawW: Float,
    drawH: Float,
    outlineColor: Color,
    primaryColor: Color
) {
    val cutoutW = lShape.cutoutLengthMm.toFloat() * scale
    val cutoutH = lShape.cutoutWidthMm.toFloat() * scale

    // L-Shape path: Top-left at startX, startY
    // Path goes:
    // (startX, startY) -> (startX + drawW, startY)
    // -> (startX + drawW, startY + drawH - cutoutH)
    // -> (startX + drawW - cutoutW, startY + drawH - cutoutH)
    // -> (startX + drawW - cutoutW, startY + drawH)
    // -> (startX, startY + drawH) -> close
    val lPath = Path().apply {
        moveTo(startX, startY)
        lineTo(startX + drawW, startY)
        lineTo(startX + drawW, startY + drawH - cutoutH)
        lineTo(startX + drawW - cutoutW, startY + drawH - cutoutH)
        lineTo(startX + drawW - cutoutW, startY + drawH)
        lineTo(startX, startY + drawH)
        close()
    }

    drawPath(
        path = lPath,
        color = Color(0xFFFEF3C7).copy(alpha = 0.6f)
    )

    // Draw dashed lines for the cutout area
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
    drawRect(
        color = Color(0xFFFCA5A5).copy(alpha = 0.25f),
        topLeft = Offset(startX + drawW - cutoutW, startY + drawH - cutoutH),
        size = Size(cutoutW, cutoutH)
    )
    drawRect(
        color = Color(0xFFEF4444).copy(alpha = 0.5f),
        topLeft = Offset(startX + drawW - cutoutW, startY + drawH - cutoutH),
        size = Size(cutoutW, cutoutH),
        style = Stroke(width = 1.5f.dp.toPx(), pathEffect = dashEffect)
    )

    drawPath(
        path = lPath,
        color = outlineColor,
        style = Stroke(width = 2.5f.dp.toPx())
    )
}

private fun DrawScope.drawDimensionAnnotations(
    startX: Float,
    startY: Float,
    drawW: Float,
    drawH: Float,
    lengthText: String,
    widthText: String,
    color: Color
) {
    val strokePx = 1.5f.dp.toPx()
    val arrowSize = 6.dp.toPx()

    // Top length arrow
    val topY = startY - 14.dp.toPx()
    drawLine(color = color, start = Offset(startX, topY), end = Offset(startX + drawW, topY), strokeWidth = strokePx)
    // Arrow heads
    drawLine(color = color, start = Offset(startX, topY), end = Offset(startX + arrowSize, topY - arrowSize / 1.5f), strokeWidth = strokePx)
    drawLine(color = color, start = Offset(startX, topY), end = Offset(startX + arrowSize, topY + arrowSize / 1.5f), strokeWidth = strokePx)
    drawLine(color = color, start = Offset(startX + drawW, topY), end = Offset(startX + drawW - arrowSize, topY - arrowSize / 1.5f), strokeWidth = strokePx)
    drawLine(color = color, start = Offset(startX + drawW, topY), end = Offset(startX + drawW - arrowSize, topY + arrowSize / 1.5f), strokeWidth = strokePx)

    // Left width arrow
    val leftX = startX - 14.dp.toPx()
    drawLine(color = color, start = Offset(leftX, startY), end = Offset(leftX, startY + drawH), strokeWidth = strokePx)
    // Arrow heads
    drawLine(color = color, start = Offset(leftX, startY), end = Offset(leftX - arrowSize / 1.5f, startY + arrowSize), strokeWidth = strokePx)
    drawLine(color = color, start = Offset(leftX, startY), end = Offset(leftX + arrowSize / 1.5f, startY + arrowSize), strokeWidth = strokePx)
    drawLine(color = color, start = Offset(leftX, startY + drawH), end = Offset(leftX - arrowSize / 1.5f, startY + drawH - arrowSize), strokeWidth = strokePx)
    drawLine(color = color, start = Offset(leftX, startY + drawH), end = Offset(leftX + arrowSize / 1.5f, startY + drawH - arrowSize), strokeWidth = strokePx)

    // Draw text with nativeCanvas
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = 12.sp.toPx()
        this.color = color.hashCode()
        textAlign = android.graphics.Paint.Align.CENTER
    }

    drawContext.canvas.nativeCanvas.drawText(
        lengthText,
        startX + drawW / 2f,
        topY - 4.dp.toPx(),
        paint
    )

    // Width text on left (rotated or centered)
    val leftPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = 12.sp.toPx()
        this.color = color.hashCode()
        textAlign = android.graphics.Paint.Align.RIGHT
    }
    drawContext.canvas.nativeCanvas.drawText(
        widthText,
        leftX - 4.dp.toPx(),
        startY + drawH / 2f + 4.dp.toPx(),
        leftPaint
    )
}
