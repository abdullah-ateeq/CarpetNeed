package com.example.carpetneed.calculator

import com.example.carpetneed.model.*
import kotlin.math.ceil
import kotlin.math.max

object CarpetCalculator {

    fun validateInputs(
        lengthMm: Double,
        widthMm: Double,
        rollWidthMm: Double,
        wasteAllowancePercent: Double = 0.0,
        patternRepeatMm: Double = 0.0,
        seamAllowanceMm: Double = 0.0
    ): Result<Unit> {
        if (lengthMm <= 0.0) {
            return Result.failure(IllegalArgumentException("Room length must be greater than zero."))
        }
        if (widthMm <= 0.0) {
            return Result.failure(IllegalArgumentException("Room width must be greater than zero."))
        }
        if (rollWidthMm <= 0.0) {
            return Result.failure(IllegalArgumentException("Carpet roll width must be greater than zero."))
        }
        if (wasteAllowancePercent < 0.0) {
            return Result.failure(IllegalArgumentException("Waste allowance cannot be negative."))
        }
        if (patternRepeatMm < 0.0) {
            return Result.failure(IllegalArgumentException("Pattern repeat cannot be negative."))
        }
        if (seamAllowanceMm < 0.0) {
            return Result.failure(IllegalArgumentException("Seam allowance cannot be negative."))
        }
        return Result.success(Unit)
    }

    fun calculateFloorArea(lengthMm: Double, widthMm: Double): Double {
        require(lengthMm > 0) { "Room length must be positive" }
        require(widthMm > 0) { "Room width must be positive" }
        return lengthMm * widthMm
    }

    fun calculateRequiredWidths(coverageWidthMm: Double, carpetWidthMm: Double): Int {
        require(coverageWidthMm > 0) { "Coverage width must be positive" }
        require(carpetWidthMm > 0) { "Carpet width must be positive" }
        return ceil(coverageWidthMm / carpetWidthMm).toInt()
    }

    fun calculateBaseLength(coverageLengthMm: Double, requiredWidths: Int): Double {
        require(coverageLengthMm > 0) { "Coverage length must be positive" }
        require(requiredWidths > 0) { "Required widths must be positive" }
        return requiredWidths * coverageLengthMm
    }

    fun adjustForPatternRepeat(lengthMm: Double, repeatMm: Double): Double {
        if (repeatMm <= 0.0) return lengthMm
        return ceil(lengthMm / repeatMm) * repeatMm
    }

    fun calculateOrientationOption(
        coverageWidthMm: Double,
        coverageLengthMm: Double,
        floorAreaMm2: Double,
        settings: CarpetSettings,
        orientation: CarpetOrientation,
        sectionName: String = "Main Room"
    ): OrientationOptionResult {
        validateInputs(
            lengthMm = coverageLengthMm,
            widthMm = coverageWidthMm,
            rollWidthMm = settings.rollWidthMm,
            wasteAllowancePercent = settings.wasteAllowancePercent,
            patternRepeatMm = settings.patternRepeatMm,
            seamAllowanceMm = settings.seamAllowanceMm
        ).getOrThrow()

        val rollWidthMm = settings.rollWidthMm
        val pieceCount = calculateRequiredWidths(coverageWidthMm, rollWidthMm)
        val adjustedPieceLengthMm = adjustForPatternRepeat(coverageLengthMm, settings.patternRepeatMm)
        val baseRollLengthMm = pieceCount * adjustedPieceLengthMm

        val allowanceLengthMm = when (settings.allowanceMethod) {
            AllowanceMethod.CARPET_LENGTH -> {
                baseRollLengthMm * (settings.wasteAllowancePercent / 100.0)
            }
            AllowanceMethod.TOTAL_CARPET_AREA -> {
                val baseArea = rollWidthMm * baseRollLengthMm
                val extraArea = baseArea * (settings.wasteAllowancePercent / 100.0)
                extraArea / rollWidthMm
            }
        }

        val unroundedPurchaseLengthMm = baseRollLengthMm + allowanceLengthMm
        val purchaseLengthMm = settings.purchaseRounding.roundUp(unroundedPurchaseLengthMm)

        val purchaseAreaMm2 = rollWidthMm * purchaseLengthMm
        val excessAreaMm2 = max(0.0, purchaseAreaMm2 - floorAreaMm2)
        val wastePercent = if (floorAreaMm2 > 0.0) (excessAreaMm2 / floorAreaMm2) * 100.0 else 0.0
        val estimatedSeams = max(0, pieceCount - 1)

        // Generate individual pieces layout
        val pieces = mutableListOf<CarpetPiece>()
        var remainingCoverageWidth = coverageWidthMm
        var currentRollOffset = 0.0

        for (i in 0 until pieceCount) {
            val cutWidth = if (remainingCoverageWidth < rollWidthMm) remainingCoverageWidth else rollWidthMm
            val piece = CarpetPiece(
                pieceIndex = i + 1,
                cutWidthMm = cutWidth,
                rollWidthMm = rollWidthMm,
                cutLengthMm = adjustedPieceLengthMm,
                unusedWidthMm = max(0.0, rollWidthMm - cutWidth),
                startRollMm = currentRollOffset,
                endRollMm = currentRollOffset + adjustedPieceLengthMm,
                roomXOffsetMm = i * rollWidthMm,
                roomYOffsetMm = 0.0,
                sectionName = sectionName
            )
            pieces.add(piece)
            currentRollOffset += adjustedPieceLengthMm
            remainingCoverageWidth = max(0.0, remainingCoverageWidth - rollWidthMm)
        }

        // Decompose waste
        val usedFloorAreaFromCutsMm2 = coverageWidthMm * coverageLengthMm
        val totalUnadjustedPiecesAreaMm2 = pieceCount * rollWidthMm * coverageLengthMm
        val widthWasteAreaMm2 = max(0.0, totalUnadjustedPiecesAreaMm2 - usedFloorAreaFromCutsMm2)
        val patternWasteAreaMm2 = max(0.0, (baseRollLengthMm * rollWidthMm) - totalUnadjustedPiecesAreaMm2)
        val allowanceAreaMm2 = max(0.0, (purchaseLengthMm - baseRollLengthMm) * rollWidthMm)

        return OrientationOptionResult(
            orientation = orientation,
            coverageWidthMm = coverageWidthMm,
            coverageLengthMm = coverageLengthMm,
            pieceCount = pieceCount,
            pieceLengthMm = adjustedPieceLengthMm,
            baseRollLengthMm = baseRollLengthMm,
            allowanceLengthMm = allowanceLengthMm,
            purchaseLengthMm = purchaseLengthMm,
            purchaseAreaMm2 = purchaseAreaMm2,
            excessAreaMm2 = excessAreaMm2,
            wastePercent = wastePercent,
            estimatedSeams = estimatedSeams,
            pieces = pieces,
            widthWasteAreaMm2 = widthWasteAreaMm2,
            lengthWasteAreaMm2 = patternWasteAreaMm2,
            allowanceAreaMm2 = allowanceAreaMm2
        )
    }

    fun calculateRoom(
        room: CarpetRoom,
        settings: CarpetSettings
    ): CarpetResult {
        val (floorAreaMm2, optionA, optionB) = when (room.shape) {
            RoomShape.RECTANGLE -> {
                validateInputs(
                    lengthMm = room.lengthMm,
                    widthMm = room.widthMm,
                    rollWidthMm = settings.rollWidthMm,
                    wasteAllowancePercent = settings.wasteAllowancePercent,
                    patternRepeatMm = settings.patternRepeatMm,
                    seamAllowanceMm = settings.seamAllowanceMm
                ).getOrThrow()

                val floorArea = calculateFloorArea(room.lengthMm, room.widthMm)

                // Option A: Roll width across room width
                // Coverage width = room.widthMm, Coverage length = room.lengthMm
                val optA = calculateOrientationOption(
                    coverageWidthMm = room.widthMm,
                    coverageLengthMm = room.lengthMm,
                    floorAreaMm2 = floorArea,
                    settings = settings,
                    orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH,
                    sectionName = room.name
                )

                // Option B: Roll width across room length
                // Coverage width = room.lengthMm, Coverage length = room.widthMm
                val optB = calculateOrientationOption(
                    coverageWidthMm = room.lengthMm,
                    coverageLengthMm = room.widthMm,
                    floorAreaMm2 = floorArea,
                    settings = settings,
                    orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_LENGTH,
                    sectionName = room.name
                )

                Triple(floorArea, optA, optB)
            }
            RoomShape.L_SHAPE -> {
                val lShape = room.lShape ?: throw IllegalArgumentException("L-shape dimensions required for L-shaped room")
                val floorArea = lShape.floorAreaMm2

                // For L-shape, we decompose into 2 rectangles:
                // Primary rectangle: overallLength x (overallWidth - cutoutWidth)
                // Wing rectangle: (overallLength - cutoutLength) x cutoutWidth
                // Option A: Roll width laid across overallWidth
                val optA = calculateLShapeOrientationOption(
                    lShape = lShape,
                    floorAreaMm2 = floorArea,
                    settings = settings,
                    orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH,
                    roomName = room.name
                )

                // Option B: Roll width laid across overallLength
                val optB = calculateLShapeOrientationOption(
                    lShape = lShape,
                    floorAreaMm2 = floorArea,
                    settings = settings,
                    orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_LENGTH,
                    roomName = room.name
                )

                Triple(floorArea, optA, optB)
            }
        }

        val chosenOption = CarpetOrientationEngine.chooseOrientation(
            requestedOrientation = room.orientation,
            optionA = optionA,
            optionB = optionB
        )

        // Include any additional room sections
        val finalPieces = chosenOption.pieces.toMutableList()
        var totalPurchaseLengthMm = chosenOption.purchaseLengthMm
        var totalBaseLengthMm = chosenOption.baseRollLengthMm
        var totalAllowanceLengthMm = chosenOption.allowanceLengthMm
        var totalFloorAreaWithSectionsMm2 = floorAreaMm2

        if (room.sections.isNotEmpty()) {
            for (section in room.sections) {
                if (section.lengthMm > 0 && section.widthMm > 0) {
                    val sectionArea = section.lengthMm * section.widthMm
                    totalFloorAreaWithSectionsMm2 += sectionArea

                    // For section, calculate best orientation
                    val sOptA = calculateOrientationOption(
                        coverageWidthMm = section.widthMm,
                        coverageLengthMm = section.lengthMm,
                        floorAreaMm2 = sectionArea,
                        settings = settings,
                        orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH,
                        sectionName = section.name
                    )
                    val sOptB = calculateOrientationOption(
                        coverageWidthMm = section.lengthMm,
                        coverageLengthMm = section.widthMm,
                        floorAreaMm2 = sectionArea,
                        settings = settings,
                        orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_LENGTH,
                        sectionName = section.name
                    )
                    val bestSectionOpt = CarpetOrientationEngine.chooseOrientation(
                        CarpetOrientation.AUTOMATIC, sOptA, sOptB
                    )

                    totalBaseLengthMm += bestSectionOpt.baseRollLengthMm
                    totalAllowanceLengthMm += bestSectionOpt.allowanceLengthMm
                    totalPurchaseLengthMm += bestSectionOpt.purchaseLengthMm
                    finalPieces.addAll(bestSectionOpt.pieces)
                }
            }
        }

        val finalPurchaseAreaMm2 = settings.rollWidthMm * totalPurchaseLengthMm
        val finalExcessAreaMm2 = max(0.0, finalPurchaseAreaMm2 - totalFloorAreaWithSectionsMm2)
        val finalWastePercent = if (totalFloorAreaWithSectionsMm2 > 0.0) {
            (finalExcessAreaMm2 / totalFloorAreaWithSectionsMm2) * 100.0
        } else 0.0

        val totalWidths = finalPieces.size
        val estimatedSeams = max(0, totalWidths - 1)

        val geometryWasteMm2 = max(0.0, (totalBaseLengthMm * settings.rollWidthMm) - totalFloorAreaWithSectionsMm2)
        val userAllowanceMm2 = max(0.0, (totalPurchaseLengthMm - totalBaseLengthMm) * settings.rollWidthMm)

        val steps = buildCalculationSteps(
            room = room,
            settings = settings,
            floorAreaMm2 = totalFloorAreaWithSectionsMm2,
            chosenOption = chosenOption,
            totalPurchaseLengthMm = totalPurchaseLengthMm,
            totalPurchaseAreaMm2 = finalPurchaseAreaMm2,
            totalExcessMm2 = finalExcessAreaMm2
        )

        val notes = mutableListOf<String>()
        if (room.shape == RoomShape.L_SHAPE) {
            notes.add("Estimated rectangular-piece calculation used for L-shaped room.")
        }
        if (room.exclusions.isNotEmpty()) {
            notes.add("For carpet ordering, subtracting small exclusions does not necessarily reduce the number of full carpet widths required.")
        }
        if (settings.patternMatch != PatternMatch.NONE) {
            notes.add("Pattern matching may require additional material. Verify carpet manufacturer's repeat and installation requirements.")
        }
        if (settings.pileDirection == PileDirection.SAME_DIRECTION) {
            notes.add("All joined pieces maintain the same pile/pattern direction.")
        }

        return CarpetResult(
            floorAreaMm2 = totalFloorAreaWithSectionsMm2,
            requiredWidths = totalWidths,
            baseLengthMm = totalBaseLengthMm,
            allowanceLengthMm = totalAllowanceLengthMm,
            purchaseLengthMm = totalPurchaseLengthMm,
            purchaseAreaMm2 = finalPurchaseAreaMm2,
            excessAreaMm2 = finalExcessAreaMm2,
            wastePercent = finalWastePercent,
            estimatedSeams = estimatedSeams,
            chosenOrientation = chosenOption.orientation,
            optionA = optionA,
            optionB = optionB,
            pieces = finalPieces,
            geometryWasteMm2 = geometryWasteMm2,
            userAllowanceMm2 = userAllowanceMm2,
            totalExcessMm2 = finalExcessAreaMm2,
            calculationSteps = steps,
            notes = notes
        )
    }

    private fun calculateLShapeOrientationOption(
        lShape: LShapeDimensions,
        floorAreaMm2: Double,
        settings: CarpetSettings,
        orientation: CarpetOrientation,
        roomName: String
    ): OrientationOptionResult {
        // Decompose L-shape into 2 rectangles:
        // Rect 1 (Main corridor/body): overallLength x (overallWidth - cutoutWidth)
        // Rect 2 (Wing): (overallLength - cutoutLength) x cutoutWidth
        val rect1Width = lShape.overallWidthMm - lShape.cutoutWidthMm
        val rect1Length = lShape.overallLengthMm

        val rect2Width = lShape.cutoutWidthMm
        val rect2Length = lShape.overallLengthMm - lShape.cutoutLengthMm

        val (cov1Width, cov1Length) = if (orientation == CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH) {
            rect1Width to rect1Length
        } else {
            rect1Length to rect1Width
        }

        val (cov2Width, cov2Length) = if (orientation == CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH) {
            rect2Width to rect2Length
        } else {
            rect2Length to rect2Width
        }

        val r1 = calculateOrientationOption(
            coverageWidthMm = cov1Width,
            coverageLengthMm = cov1Length,
            floorAreaMm2 = rect1Width * rect1Length,
            settings = settings,
            orientation = orientation,
            sectionName = "$roomName (Part 1)"
        )

        val r2 = calculateOrientationOption(
            coverageWidthMm = cov2Width,
            coverageLengthMm = cov2Length,
            floorAreaMm2 = rect2Width * rect2Length,
            settings = settings,
            orientation = orientation,
            sectionName = "$roomName (Part 2)"
        )

        val totalPieceCount = r1.pieceCount + r2.pieceCount
        val totalBaseRollLengthMm = r1.baseRollLengthMm + r2.baseRollLengthMm
        val totalAllowanceLengthMm = r1.allowanceLengthMm + r2.allowanceLengthMm
        val totalPurchaseLengthMm = settings.purchaseRounding.roundUp(totalBaseRollLengthMm + totalAllowanceLengthMm)
        val purchaseAreaMm2 = settings.rollWidthMm * totalPurchaseLengthMm
        val excessAreaMm2 = max(0.0, purchaseAreaMm2 - floorAreaMm2)
        val wastePercent = if (floorAreaMm2 > 0.0) (excessAreaMm2 / floorAreaMm2) * 100.0 else 0.0
        val seams = max(0, totalPieceCount - 1)

        val combinedPieces = mutableListOf<CarpetPiece>()
        var rollOffset = 0.0
        for (p in r1.pieces) {
            combinedPieces.add(p.copy(startRollMm = rollOffset, endRollMm = rollOffset + p.cutLengthMm))
            rollOffset += p.cutLengthMm
        }
        for (p in r2.pieces) {
            combinedPieces.add(
                p.copy(
                    pieceIndex = combinedPieces.size + 1,
                    startRollMm = rollOffset,
                    endRollMm = rollOffset + p.cutLengthMm
                )
            )
            rollOffset += p.cutLengthMm
        }

        return OrientationOptionResult(
            orientation = orientation,
            coverageWidthMm = lShape.overallWidthMm,
            coverageLengthMm = lShape.overallLengthMm,
            pieceCount = totalPieceCount,
            pieceLengthMm = max(r1.pieceLengthMm, r2.pieceLengthMm),
            baseRollLengthMm = totalBaseRollLengthMm,
            allowanceLengthMm = totalAllowanceLengthMm,
            purchaseLengthMm = totalPurchaseLengthMm,
            purchaseAreaMm2 = purchaseAreaMm2,
            excessAreaMm2 = excessAreaMm2,
            wastePercent = wastePercent,
            estimatedSeams = seams,
            pieces = combinedPieces,
            widthWasteAreaMm2 = r1.widthWasteAreaMm2 + r2.widthWasteAreaMm2,
            lengthWasteAreaMm2 = r1.lengthWasteAreaMm2 + r2.lengthWasteAreaMm2,
            allowanceAreaMm2 = (totalPurchaseLengthMm - totalBaseRollLengthMm) * settings.rollWidthMm
        )
    }

    fun calculateMultipleRooms(
        rooms: List<Pair<CarpetRoom, CarpetSettings>>
    ): MultipleRoomSummary {
        var totalRooms = 0
        var totalFloorAreaMm2 = 0.0
        var totalCarpetLengthMm = 0.0
        var totalPurchaseAreaMm2 = 0.0
        val rollGroups = mutableMapOf<Double, Double>()

        for ((room, settings) in rooms) {
            val result = calculateRoom(room, settings)
            totalRooms++
            totalFloorAreaMm2 += result.floorAreaMm2
            totalCarpetLengthMm += result.purchaseLengthMm
            totalPurchaseAreaMm2 += result.purchaseAreaMm2

            val currentGroupLen = rollGroups.getOrDefault(settings.rollWidthMm, 0.0)
            rollGroups[settings.rollWidthMm] = currentGroupLen + result.purchaseLengthMm
        }

        val totalExcessAreaMm2 = max(0.0, totalPurchaseAreaMm2 - totalFloorAreaMm2)
        val totalWastePercent = if (totalFloorAreaMm2 > 0.0) {
            (totalExcessAreaMm2 / totalFloorAreaMm2) * 100.0
        } else 0.0

        return MultipleRoomSummary(
            totalRooms = totalRooms,
            totalFloorAreaMm2 = totalFloorAreaMm2,
            totalCarpetLengthMm = totalCarpetLengthMm,
            totalPurchaseAreaMm2 = totalPurchaseAreaMm2,
            totalExcessAreaMm2 = totalExcessAreaMm2,
            totalWastePercent = totalWastePercent,
            rollWidthGroups = rollGroups
        )
    }

    private fun buildCalculationSteps(
        room: CarpetRoom,
        settings: CarpetSettings,
        floorAreaMm2: Double,
        chosenOption: OrientationOptionResult,
        totalPurchaseLengthMm: Double,
        totalPurchaseAreaMm2: Double,
        totalExcessMm2: Double
    ): List<String> {
        val steps = mutableListOf<String>()
        val floorAreaM2 = floorAreaMm2 / 1_000_000.0
        val rollWidthM = settings.rollWidthMm / 1000.0
        val purchaseLengthM = totalPurchaseLengthMm / 1000.0
        val purchaseAreaM2 = totalPurchaseAreaMm2 / 1_000_000.0
        val excessM2 = totalExcessMm2 / 1_000_000.0

        if (room.shape == RoomShape.RECTANGLE) {
            val lengthM = room.lengthMm / 1000.0
            val widthM = room.widthMm / 1000.0
            steps.add("1. Room Floor Area = Length × Width = ${formatNum(lengthM)} m × ${formatNum(widthM)} m = ${formatNum(floorAreaM2)} m²")
        } else {
            val l = room.lShape ?: return steps
            val overallM2 = (l.overallLengthMm * l.overallWidthMm) / 1_000_000.0
            val cutoutM2 = (l.cutoutLengthMm * l.cutoutWidthMm) / 1_000_000.0
            steps.add("1. L-Shape Floor Area = Overall Area (${formatNum(overallM2)} m²) − Cutout Area (${formatNum(cutoutM2)} m²) = ${formatNum(floorAreaM2)} m²")
        }

        val covWidthM = chosenOption.coverageWidthMm / 1000.0
        val covLengthM = chosenOption.coverageLengthMm / 1000.0
        steps.add("2. Carpet Roll Width = ${formatNum(rollWidthM)} m")
        steps.add("3. Laying Direction: ${chosenOption.orientation.label}")
        steps.add("4. Required Widths = ceil(${formatNum(covWidthM)} m / ${formatNum(rollWidthM)} m) = ${chosenOption.pieceCount} piece(s)")

        if (settings.patternRepeatMm > 0.0) {
            val repeatM = settings.patternRepeatMm / 1000.0
            val pieceLenM = chosenOption.pieceLengthMm / 1000.0
            steps.add("5. Pattern Repeat (${formatNum(repeatM)} m): Adjusted run length per piece = ${formatNum(pieceLenM)} m")
        } else {
            val pieceLenM = chosenOption.pieceLengthMm / 1000.0
            steps.add("5. Run length per piece = ${formatNum(pieceLenM)} m")
        }

        val baseLenM = chosenOption.baseRollLengthMm / 1000.0
        steps.add("6. Base Roll Consumption = ${chosenOption.pieceCount} piece(s) × ${formatNum(chosenOption.pieceLengthMm / 1000.0)} m = ${formatNum(baseLenM)} linear m")

        if (settings.wasteAllowancePercent > 0.0) {
            val allowLenM = chosenOption.allowanceLengthMm / 1000.0
            steps.add("7. Waste Allowance (${formatNum(settings.wasteAllowancePercent)}%): +${formatNum(allowLenM)} m")
        }

        if (settings.purchaseRounding != PurchaseRounding.EXACT) {
            steps.add("8. Purchase Rounding (${settings.purchaseRounding.label}): Rounded to ${formatNum(purchaseLengthM)} linear m")
        }

        steps.add("9. Carpet Purchase Area = ${formatNum(rollWidthM)} m × ${formatNum(purchaseLengthM)} m = ${formatNum(purchaseAreaM2)} m²")
        steps.add("10. Excess / Waste = ${formatNum(purchaseAreaM2)} m² − ${formatNum(floorAreaM2)} m² = ${formatNum(excessM2)} m²")
        steps.add("11. Estimated Seams = max(0, ${chosenOption.pieceCount} − 1) = ${chosenOption.estimatedSeams}")

        return steps
    }

    private fun formatNum(value: Double): String {
        return String.format(java.util.Locale.US, "%.2f", value)
    }
}
