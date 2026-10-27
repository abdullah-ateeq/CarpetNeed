package com.example.carpetneed.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carpetneed.model.CarpetResult
import com.example.carpetneed.model.CarpetSettings
import com.example.carpetneed.model.PileDirection
import com.example.carpetneed.model.UnitType
import java.util.Locale
import kotlin.math.max

@Composable
fun CuttingPlanCanvas(
    result: CarpetResult,
    settings: CarpetSettings,
    unit: UnitType,
    decimalPlaces: Int = 2,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(320.dp)
) {
    val rollWidthMm = settings.rollWidthMm
    val totalRollLengthMm = result.purchaseLengthMm
    val pieces = result.pieces

    val rollWidthDisplay = String.format(Locale.US, "Roll Width: %.${decimalPlaces}f %s", unit.fromMm(rollWidthMm), unit.symbol)
    val rollLengthDisplay = String.format(Locale.US, "Total Purchase: %.${decimalPlaces}f m", totalRollLengthMm / 1000.0)

    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .background(surfaceColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padLeft = 70.dp.toPx()
            val padRight = 30.dp.toPx()
            val padTop = 45.dp.toPx()
            val padBottom = 35.dp.toPx()

            val availW = size.width - padLeft - padRight
            val availH = size.height - padTop - padBottom

            if (availW <= 0 || availH <= 0 || rollWidthMm <= 0 || totalRollLengthMm <= 0) return@Canvas

            // Scale to fit canvas: Roll width along X axis, sequential length along Y axis
            // (or length along X axis if very long). Let's lay length along X axis (horizontal unrolling roll)
            // with roll width along Y axis!
            val scaleX = availW / totalRollLengthMm.toFloat()
            val scaleY = availH / rollWidthMm.toFloat()

            // To avoid excessive compression if pieces are few, ensure min scale
            val drawRollH = rollWidthMm.toFloat() * scaleY
            val rollTopY = padTop + (availH - drawRollH) / 2f

            val strokeWidthPx = 2.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

            // Draw full carpet roll boundary
            val fullRollW = totalRollLengthMm.toFloat() * scaleX
            drawRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(padLeft, rollTopY),
                size = Size(fullRollW, drawRollH)
            )

            // Draw each sequential cut piece
            var currentX = padLeft
            for (piece in pieces) {
                val pieceLenPx = piece.cutLengthMm.toFloat() * scaleX
                val cutWidthPx = piece.cutWidthMm.toFloat() * scaleY
                val unusedWidthPx = piece.unusedWidthMm.toFloat() * scaleY

                // Piece used area
                drawRect(
                    color = Color(0xFFFEF3C7),
                    topLeft = Offset(currentX, rollTopY),
                    size = Size(pieceLenPx, cutWidthPx)
                )

                // Piece unused width (waste)
                if (piece.unusedWidthMm > 1.0) {
                    drawRect(
                        color = Color(0xFFFCA5A5).copy(alpha = 0.5f),
                        topLeft = Offset(currentX, rollTopY + cutWidthPx),
                        size = Size(pieceLenPx, unusedWidthPx)
                    )

                    // Draw diagonal warning lines in waste
                    val wasteLineCount = max(2, (pieceLenPx / 15.dp.toPx()).toInt())
                    for (w in 0..wasteLineCount) {
                        val lx = currentX + (pieceLenPx / wasteLineCount) * w
                        drawLine(
                            color = Color(0xFFDC2626).copy(alpha = 0.4f),
                            start = Offset(lx, rollTopY + cutWidthPx),
                            end = Offset(lx + 10f, rollTopY + cutWidthPx + unusedWidthPx),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Piece boundary
                drawRect(
                    color = Color(0xFFD97706),
                    topLeft = Offset(currentX, rollTopY),
                    size = Size(pieceLenPx, cutWidthPx),
                    style = Stroke(width = strokeWidthPx)
                )

                // Seam cut line at end of piece
                drawLine(
                    color = Color(0xFF92400E),
                    start = Offset(currentX + pieceLenPx, rollTopY),
                    end = Offset(currentX + pieceLenPx, rollTopY + drawRollH),
                    strokeWidth = 2.5f.dp.toPx()
                )

                // Pile Direction Arrow (Standard flow along the piece length)
                val arrowY = rollTopY + cutWidthPx / 2f
                val arrowStartX = currentX + pieceLenPx * 0.25f
                val arrowEndX = currentX + pieceLenPx * 0.75f
                drawLine(
                    color = Color(0xFFB45309).copy(alpha = 0.6f),
                    start = Offset(arrowStartX, arrowY),
                    end = Offset(arrowEndX, arrowY),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color(0xFFB45309).copy(alpha = 0.6f),
                    start = Offset(arrowEndX, arrowY),
                    end = Offset(arrowEndX - 6.dp.toPx(), arrowY - 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color(0xFFB45309).copy(alpha = 0.6f),
                    start = Offset(arrowEndX, arrowY),
                    end = Offset(arrowEndX - 6.dp.toPx(), arrowY + 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )

                // Piece Label
                val textPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textSize = 11.sp.toPx()
                    color = android.graphics.Color.DKGRAY
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val label = "P${piece.pieceIndex}: ${String.format(Locale.US, "%.0f", unit.fromMm(piece.cutWidthMm))}×${String.format(Locale.US, "%.0f", unit.fromMm(piece.cutLengthMm))}"
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    currentX + pieceLenPx / 2f,
                    arrowY - 8.dp.toPx(),
                    textPaint
                )

                currentX += pieceLenPx
            }

            // Planning allowance margin if any
            if (result.allowanceLengthMm > 1.0) {
                val allowLenPx = (result.purchaseLengthMm - result.baseLengthMm).toFloat() * scaleX
                val allowStartX = padLeft + result.baseLengthMm.toFloat() * scaleX

                drawRect(
                    color = Color(0xFF6EE7B7).copy(alpha = 0.35f),
                    topLeft = Offset(allowStartX, rollTopY),
                    size = Size(allowLenPx, drawRollH)
                )

                val allowPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textSize = 10.sp.toPx()
                    color = android.graphics.Color.BLACK
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "+Allowance",
                    allowStartX + allowLenPx / 2f,
                    rollTopY + drawRollH / 2f,
                    allowPaint
                )
            }

            // Outer roll stroke
            drawRect(
                color = outlineColor,
                topLeft = Offset(padLeft, rollTopY),
                size = Size(fullRollW, drawRollH),
                style = Stroke(width = strokeWidthPx)
            )

            // Roll width dimension arrow on Left (Y axis)
            val dimX = padLeft - 14.dp.toPx()
            drawLine(color = textColor, start = Offset(dimX, rollTopY), end = Offset(dimX, rollTopY + drawRollH), strokeWidth = 1.5f.dp.toPx())
            val yPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 11.sp.toPx()
                color = textColor.hashCode()
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawContext.canvas.nativeCanvas.drawText(
                String.format(Locale.US, "%.0f %s", unit.fromMm(rollWidthMm), unit.symbol),
                dimX - 4.dp.toPx(),
                rollTopY + drawRollH / 2f + 4.dp.toPx(),
                yPaint
            )

            // Roll length dimension arrow at Bottom (X axis)
            val dimY = rollTopY + drawRollH + 16.dp.toPx()
            drawLine(color = textColor, start = Offset(padLeft, dimY), end = Offset(padLeft + fullRollW, dimY), strokeWidth = 1.5f.dp.toPx())
            val xPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 11.sp.toPx()
                color = textColor.hashCode()
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(
                rollLengthDisplay,
                padLeft + fullRollW / 2f,
                dimY + 14.dp.toPx(),
                xPaint
            )
        }

        // Header overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = rollWidthDisplay,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
