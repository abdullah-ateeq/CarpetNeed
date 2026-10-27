package com.example.carpetneed.model

enum class RoomShape {
    RECTANGLE,
    L_SHAPE
}

data class RoomSection(
    val id: String,
    val name: String,
    val lengthMm: Double,
    val widthMm: Double
)

data class RoomExclusion(
    val id: String,
    val name: String,
    val widthMm: Double,
    val depthMm: Double
)

data class LShapeDimensions(
    val overallLengthMm: Double,
    val overallWidthMm: Double,
    val cutoutLengthMm: Double,
    val cutoutWidthMm: Double
) {
    init {
        require(overallLengthMm > 0) { "Overall length must be positive" }
        require(overallWidthMm > 0) { "Overall width must be positive" }
        require(cutoutLengthMm >= 0 && cutoutLengthMm < overallLengthMm) {
            "Cutout length must be less than overall length"
        }
        require(cutoutWidthMm >= 0 && cutoutWidthMm < overallWidthMm) {
            "Cutout width must be less than overall width"
        }
    }

    val floorAreaMm2: Double
        get() = (overallLengthMm * overallWidthMm) - (cutoutLengthMm * cutoutWidthMm)
}

data class CarpetRoom(
    val id: String,
    val name: String,
    val shape: RoomShape = RoomShape.RECTANGLE,
    val lengthMm: Double,
    val widthMm: Double,
    val lShape: LShapeDimensions? = null,
    val sections: List<RoomSection> = emptyList(),
    val exclusions: List<RoomExclusion> = emptyList(),
    val orientation: CarpetOrientation = CarpetOrientation.AUTOMATIC,
    val customSettings: CarpetSettings? = null
)
