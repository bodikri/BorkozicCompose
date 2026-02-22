package com.borkozic.library.map

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.collections.forEach

object MapIndex {

    private val maps = mutableListOf<MapInformation>()

    fun loadMaps(mapsDir: File) {
        Log.d("MapIndex", "loadMaps called with dir: ${mapsDir.absolutePath}")
        if (!mapsDir.exists()) return
        Log.e("MapIndex", "mapsDir does not exist!")
        val mapFiles = mapsDir.listFiles { file ->
            file.extension.equals("map", ignoreCase = true)
        } ?: return.also { Log.e("MapIndex", "No .map files found") }

        maps.clear()
        mapFiles.forEach { file ->
            parseMapFile(file)?.let { maps.add(it)
                Log.d("MapIndex", "Parsed map: ${it.name}")}

        }
        Log.d("MapIndex", "Total maps loaded: ${maps.size}")
    }

    private fun parseMapFile(file: File): MapInformation? {
        return try {
            BufferedReader(FileReader(file)).use { reader ->
                val lines = reader.readLines()

                val name = lines.getOrNull(1)?.trim() ?: file.name
                val imagePath = lines.getOrNull(2)?.trim() ?: ""
                val proj = lines.getOrNull(8)?.trim() ?: ""

                // ред 7: ширина, височина, tileWidth, tileHeight
                val dimensions = lines.getOrNull(7)?.split(",")?.map { it.trim() }
                val width = dimensions?.getOrNull(0)?.toIntOrNull() ?: 0
                val height = dimensions?.getOrNull(1)?.toIntOrNull() ?: 0
                val tileW = dimensions?.getOrNull(2)?.toIntOrNull() ?: 64
                val tileH = dimensions?.getOrNull(3)?.toIntOrNull() ?: 64

                val calibration = mutableListOf<MapInformation.CalibrationPoint>()

                // редове 4,5,6 = калибрационни точки
                for (i in 4..6) {
                    val parts = lines.getOrNull(i)?.split(",")?.map { it.trim() } ?: continue
                    if (parts.size >= 4) {
                        calibration.add(
                            MapInformation.CalibrationPoint(
                                latitude = parts[0].toDoubleOrNull() ?: 0.0,
                                longitude = parts[1].toDoubleOrNull() ?: 0.0,
                                x = parts[2].toDoubleOrNull() ?: 0.0,
                                y = parts[3].toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                }

                MapInformation(
                    file = file,
                    name = name,
                    path = imagePath,
                    width = width,
                    height = height,
                    tileWidth = tileW,
                    tileHeight = tileH,
                    projection = proj,
                    calibrationPoints = calibration
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getMaps(): List<MapInformation> = maps.toList()

    fun getMapByName(name: String): MapInformation? =
        maps.find { it.name.equals(name, ignoreCase = true) }

    fun getMapByFile(file: File): MapInformation? =
        maps.find { it.file.absolutePath == file.absolutePath }
}