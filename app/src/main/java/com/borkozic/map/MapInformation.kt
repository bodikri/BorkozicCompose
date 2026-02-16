package com.borkozic.map

import java.io.File

data class MapInformation(
    val file: File,                     // основният .map файл
    val name: String,                   // име на картата
    val path: String,                    // път до изображението (.ozf2/.ozfx3)
    val width: Int,                      // ширина в пиксели
    val height: Int,                      // височина в пиксели
    val tileWidth: Int = 64,              // ширина на тайл (обикновено 64)
    val tileHeight: Int = 64,              // височина на тайл
    val projection: String,                // проекция (напр. "Mercator")
    val calibrationPoints: List<CalibrationPoint> = emptyList()
) {
    data class CalibrationPoint(
        val latitude: Double,
        val longitude: Double,
        val x: Double,   // в пиксели
        val y: Double
    )
}