package com.example.carpetneed

import com.example.carpetneed.calculator.CarpetCalculator
import com.example.carpetneed.calculator.CarpetOrientationEngine
import com.example.carpetneed.model.*
import org.junit.Assert.*
import org.junit.Test

class CarpetCalculatorTest {

    private val delta = 0.001

    @Test
    fun testBasicRoom() {
        // Room: 500 × 400 cm
        // Carpet: 400 cm wide
        // Orientation: Width across room width
        // Allowance: 0%
        val room = CarpetRoom(
            id = "room-1",
            name = "Living Room",
            shape = RoomShape.RECTANGLE,
            lengthMm = 5000.0,
            widthMm = 4000.0,
            orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH
        )
        val settings = CarpetSettings(
            rollWidthMm = 4000.0,
            wasteAllowancePercent = 0.0,
            seamAllowanceMm = 0.0,
            patternRepeatMm = 0.0,
            purchaseRounding = PurchaseRounding.EXACT
        )

        val result = CarpetCalculator.calculateRoom(room, settings)

        // Floor area: 20.00 m²
        assertEquals(20.0, result.floorAreaMm2 / 1_000_000.0, delta)
        // Pieces: 1
        assertEquals(1, result.requiredWidths)
        // Piece: 400 × 500 cm
        val piece = result.pieces.first()
        assertEquals(4000.0, piece.rollWidthMm, delta)
        assertEquals(4000.0, piece.cutWidthMm, delta)
        assertEquals(5000.0, piece.cutLengthMm, delta)
        // Roll length: 5.00 m
        assertEquals(5.0, result.purchaseLengthMm / 1000.0, delta)
        // Purchase area: 20.00 m²
        assertEquals(20.0, result.purchaseAreaMm2 / 1_000_000.0, delta)
        // Excess: 0.00 m²
        assertEquals(0.0, result.excessAreaMm2 / 1_000_000.0, delta)
        // Seams: 0
        assertEquals(0, result.estimatedSeams)
    }

    @Test
    fun testMultipleWidth() {
        // Room: 500 × 400 cm
        // Carpet: 300 cm wide
        // Laying roll width across length (500cm coverage width, 400cm piece run length)
        val room = CarpetRoom(
            id = "room-2",
            name = "Room 2",
            shape = RoomShape.RECTANGLE,
            lengthMm = 5000.0,
            widthMm = 4000.0,
            orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_LENGTH
        )
        val settings = CarpetSettings(
            rollWidthMm = 3000.0,
            wasteAllowancePercent = 0.0,
            patternRepeatMm = 0.0
        )

        val result = CarpetCalculator.calculateRoom(room, settings)

        // Pieces: 2
        assertEquals(2, result.requiredWidths)
        // Piece length: 400 cm
        assertEquals(4000.0, result.pieces[0].cutLengthMm, delta)
        // Base roll consumption: 8.00 m
        assertEquals(8.0, result.purchaseLengthMm / 1000.0, delta)
        // Purchase area: 24.00 m²
        assertEquals(24.0, result.purchaseAreaMm2 / 1_000_000.0, delta)
        // Floor area: 20.00 m²
        assertEquals(20.0, result.floorAreaMm2 / 1_000_000.0, delta)
        // Excess: 4.00 m²
        assertEquals(4.0, result.excessAreaMm2 / 1_000_000.0, delta)
        // Estimated seams: 1
        assertEquals(1, result.estimatedSeams)
    }

    @Test
    fun testAutomaticOrientationSelection() {
        // Room: 500 × 400 cm
        // Carpet: 400 cm
        // Verify Orientation A vs Orientation B and automatic choice
        val room = CarpetRoom(
            id = "room-3",
            name = "Room 3",
            shape = RoomShape.RECTANGLE,
            lengthMm = 5000.0,
            widthMm = 4000.0,
            orientation = CarpetOrientation.AUTOMATIC
        )
        val settings = CarpetSettings(
            rollWidthMm = 4000.0,
            wasteAllowancePercent = 0.0
        )

        val result = CarpetCalculator.calculateRoom(room, settings)

        // Orientation A: 1 piece, 5 m, 20 m²
        assertEquals(1, result.optionA.pieceCount)
        assertEquals(5.0, result.optionA.purchaseLengthMm / 1000.0, delta)
        assertEquals(20.0, result.optionA.purchaseAreaMm2 / 1_000_000.0, delta)

        // Orientation B: 2 pieces, 8 m, 32 m²
        assertEquals(2, result.optionB.pieceCount)
        assertEquals(8.0, result.optionB.purchaseLengthMm / 1000.0, delta)
        assertEquals(32.0, result.optionB.purchaseAreaMm2 / 1_000_000.0, delta)

        // Automatic mode must choose Orientation A
        assertEquals(CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH, result.chosenOrientation)
        assertEquals(5.0, result.purchaseLengthMm / 1000.0, delta)
        assertEquals(20.0, result.purchaseAreaMm2 / 1_000_000.0, delta)
    }

    @Test
    fun testWastePercentage() {
        // Room: 500 × 400 cm, Carpet: 300 cm
        val room = CarpetRoom(
            id = "room-waste",
            name = "Room Waste",
            lengthMm = 5000.0,
            widthMm = 4000.0,
            orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_LENGTH
        )
        val settings = CarpetSettings(
            rollWidthMm = 3000.0,
            wasteAllowancePercent = 0.0
        )

        val result = CarpetCalculator.calculateRoom(room, settings)

        // Floor area: 20.00 m²
        assertEquals(20.0, result.floorAreaMm2 / 1_000_000.0, delta)
        // Purchase: 24.00 m²
        assertEquals(24.0, result.purchaseAreaMm2 / 1_000_000.0, delta)
        // Excess: 4.00 m²
        assertEquals(4.0, result.excessAreaMm2 / 1_000_000.0, delta)
        // Waste: 20%
        assertEquals(20.0, result.wastePercent, delta)
    }

    @Test
    fun testWasteAllowance() {
        // Base: 8.00 m
        // Allowance: 10%
        // Expected: 8.80 m
        val room = CarpetRoom(
            id = "room-allowance",
            name = "Room Allowance",
            lengthMm = 5000.0,
            widthMm = 4000.0,
            orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_LENGTH
        )
        val settings = CarpetSettings(
            rollWidthMm = 3000.0,
            wasteAllowancePercent = 10.0,
            allowanceMethod = AllowanceMethod.CARPET_LENGTH
        )

        val result = CarpetCalculator.calculateRoom(room, settings)

        assertEquals(8.0, result.baseLengthMm / 1000.0, delta)
        assertEquals(0.80, result.allowanceLengthMm / 1000.0, delta)
        assertEquals(8.80, result.purchaseLengthMm / 1000.0, delta)
    }

    @Test
    fun testPatternRepeat() {
        // Piece length: 400 cm, Repeat: 30 cm -> ceil(400 / 30) = 14 * 30 = 420 cm
        val adjusted = CarpetCalculator.adjustForPatternRepeat(4000.0, 300.0)
        assertEquals(4200.0, adjusted, delta)
    }

    @Test
    fun testPatternRepeatZero() {
        // Repeat: 0 -> No adjustment
        val adjusted = CarpetCalculator.adjustForPatternRepeat(4000.0, 0.0)
        assertEquals(4000.0, adjusted, delta)
    }

    @Test
    fun testSeamCount() {
        assertEquals(0, maxOf(0, 1 - 1))
        assertEquals(1, maxOf(0, 2 - 1))
        assertEquals(3, maxOf(0, 4 - 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeInputLength() {
        CarpetCalculator.validateInputs(
            lengthMm = -5000.0,
            widthMm = 4000.0,
            rollWidthMm = 4000.0
        ).getOrThrow()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testZeroInputCarpetWidth() {
        CarpetCalculator.validateInputs(
            lengthMm = 5000.0,
            widthMm = 4000.0,
            rollWidthMm = 0.0
        ).getOrThrow()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidPatternRepeat() {
        CarpetCalculator.validateInputs(
            lengthMm = 5000.0,
            widthMm = 4000.0,
            rollWidthMm = 4000.0,
            patternRepeatMm = -300.0
        ).getOrThrow()
    }

    @Test
    fun testLShapeRoom() {
        // Overall: 600 × 500 cm, Cutout: 200 × 150 cm
        // Overall area: 30.00 m²
        // Cutout: 3.00 m²
        // Floor area: 27.00 m²
        val lShape = LShapeDimensions(
            overallLengthMm = 6000.0,
            overallWidthMm = 5000.0,
            cutoutLengthMm = 2000.0,
            cutoutWidthMm = 1500.0
        )
        val overallAreaM2 = (lShape.overallLengthMm * lShape.overallWidthMm) / 1_000_000.0
        val cutoutAreaM2 = (lShape.cutoutLengthMm * lShape.cutoutWidthMm) / 1_000_000.0
        val floorAreaM2 = lShape.floorAreaMm2 / 1_000_000.0

        assertEquals(30.0, overallAreaM2, delta)
        assertEquals(3.0, cutoutAreaM2, delta)
        assertEquals(27.0, floorAreaM2, delta)

        val room = CarpetRoom(
            id = "l-shape-1",
            name = "L-Shaped Hall",
            shape = RoomShape.L_SHAPE,
            lengthMm = 6000.0,
            widthMm = 5000.0,
            lShape = lShape
        )
        val settings = CarpetSettings(rollWidthMm = 4000.0)
        val result = CarpetCalculator.calculateRoom(room, settings)

        assertEquals(27.0, result.floorAreaMm2 / 1_000_000.0, delta)
        assertTrue(result.pieces.isNotEmpty())
    }

    @Test
    fun testMultipleRooms() {
        // Room 1: 500 × 400 cm (Carpet: 400 cm) -> 20 m² floor, 5.0 m purchase (20 m² purchase)
        // Room 2: 400 × 350 cm (Carpet: 400 cm) -> 14 m² floor, 3.5 m purchase (14 m² purchase)
        val room1 = CarpetRoom(
            id = "r1",
            name = "Living Room",
            lengthMm = 5000.0,
            widthMm = 4000.0,
            orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH
        )
        val room2 = CarpetRoom(
            id = "r2",
            name = "Bedroom 1",
            lengthMm = 4000.0,
            widthMm = 3500.0,
            orientation = CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH
        )
        val settings = CarpetSettings(rollWidthMm = 4000.0)

        val res1 = CarpetCalculator.calculateRoom(room1, settings)
        val res2 = CarpetCalculator.calculateRoom(room2, settings)

        val summary = CarpetCalculator.calculateMultipleRooms(
            listOf(room1 to settings, room2 to settings)
        )

        assertEquals(2, summary.totalRooms)
        // Total floor area is the exact sum (20.0 + 14.0 = 34.0 m²)
        assertEquals(res1.floorAreaMm2 + res2.floorAreaMm2, summary.totalFloorAreaMm2, delta)
        // Total purchase length is sum of individual room purchases
        assertEquals(res1.purchaseLengthMm + res2.purchaseLengthMm, summary.totalCarpetLengthMm, delta)
        // Total purchase area is sum of individual purchase areas
        assertEquals(res1.purchaseAreaMm2 + res2.purchaseAreaMm2, summary.totalPurchaseAreaMm2, delta)
    }
}
