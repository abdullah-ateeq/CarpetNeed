package com.example.carpetneed.calculator

import com.example.carpetneed.model.CarpetOrientation
import com.example.carpetneed.model.OrientationOptionResult

object CarpetOrientationEngine {

    /**
     * Chooses between Option A and Option B according to requested orientation.
     * When AUTOMATIC is requested, compares both and picks the most efficient option:
     * 1. Smallest purchase area
     * 2. If equal, fewer estimated seams (fewer widths)
     * 3. If equal, lower purchase length
     * 4. If equal, lower excess area
     */
    fun chooseOrientation(
        requestedOrientation: CarpetOrientation,
        optionA: OrientationOptionResult,
        optionB: OrientationOptionResult
    ): OrientationOptionResult {
        return when (requestedOrientation) {
            CarpetOrientation.WIDTH_ACROSS_ROOM_WIDTH -> optionA
            CarpetOrientation.WIDTH_ACROSS_ROOM_LENGTH -> optionB
            CarpetOrientation.AUTOMATIC -> {
                val areaDiff = optionA.purchaseAreaMm2 - optionB.purchaseAreaMm2
                when {
                    areaDiff < -1.0 -> optionA
                    areaDiff > 1.0 -> optionB
                    else -> {
                        // Areas are equal (within floating tolerance)
                        // Tie breaker 1: Fewer seams
                        if (optionA.estimatedSeams != optionB.estimatedSeams) {
                            if (optionA.estimatedSeams < optionB.estimatedSeams) optionA else optionB
                        }
                        // Tie breaker 2: Lower purchase length
                        else if (kotlin.math.abs(optionA.purchaseLengthMm - optionB.purchaseLengthMm) > 1.0) {
                            if (optionA.purchaseLengthMm < optionB.purchaseLengthMm) optionA else optionB
                        }
                        // Tie breaker 3: Lower excess
                        else if (optionA.excessAreaMm2 < optionB.excessAreaMm2) {
                            optionA
                        } else {
                            optionA
                        }
                    }
                }
            }
        }
    }
}
