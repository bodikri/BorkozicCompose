package com.borkozic.library.map

import java.io.File
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

class OzfDecoder {

    companion object {
        init {
            System.loadLibrary("ozfdecoder")
        }
    }

    private var nativePointer: Long = 0
    private var mapInfo: MapInfo? = null

    external fun openImageNative(path: String): Long
    external fun closeImageNative(ptr: Long)
    external fun getTileNative(
        ptr: Long,
        type: Int,
        key: Int,
        depth: Int,
        offset: Int,
        index: Int,
        width: Int,
        height: Int,
        palette: ByteArray
    ): IntArray?

    fun openImage(file: File): Boolean {
        nativePointer = openImageNative(file.absolutePath)
        return nativePointer != 0L
    }

    fun closeImage() {
        if (nativePointer != 0L) {
            closeImageNative(nativePointer)
            nativePointer = 0
        }
    }
    fun getNativePointer(): Long = nativePointer
    fun getTile(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        palette: ByteArray
    ): IntArray? {
        if (nativePointer == 0L) return null

        val mapInfo = getMapInfo() ?: return null
        val tilesPerRow = mapInfo.mapWidth / mapInfo.tileWidth
        val index = y * tilesPerRow + x

        return getTileNative(
            nativePointer,
            1, 0, -1, 0, index,
            width, height, palette
        )
    }

    // ⚡⚡⚡ НОВО: Четене на .map файл ⚡⚡⚡
    fun loadMapFile(mapFile: File): MapInfo? {
        if (!mapFile.exists()) return null

        return try {
            BufferedReader(FileReader(mapFile)).use { reader ->
                val lines = reader.readLines()
                if (lines.isEmpty()) return null

                val info = MapInfo()

                // OziExplorer .map формат
                // Ред 0: OziExplorer Map Data File Version 2.2
                // Ред 1: Име на картата
                info.name = lines.getOrNull(1) ?: mapFile.name

                // Ред 2: Път до .ozf2/.ozfx3 файла
                info.imagePath = lines.getOrNull(2) ?: ""

                // Ред 4: Калибрационни точки
                lines.getOrNull(4)?.split(",")?.let { parts ->
                    if (parts.size >= 4) {
                        info.latitude1 = parts[0].toDoubleOrNull() ?: 0.0
                        info.longitude1 = parts[1].toDoubleOrNull() ?: 0.0
                        info.x1 = parts[2].toDoubleOrNull() ?: 0.0
                        info.y1 = parts[3].toDoubleOrNull() ?: 0.0
                    }
                }

                // Ред 5: Втора калибрационна точка
                lines.getOrNull(5)?.split(",")?.let { parts ->
                    if (parts.size >= 4) {
                        info.latitude2 = parts[0].toDoubleOrNull() ?: 0.0
                        info.longitude2 = parts[1].toDoubleOrNull() ?: 0.0
                        info.x2 = parts[2].toDoubleOrNull() ?: 0.0
                        info.y2 = parts[3].toDoubleOrNull() ?: 0.0
                    }
                }

                // Ред 6: Трета калибрационна точка
                lines.getOrNull(6)?.split(",")?.let { parts ->
                    if (parts.size >= 4) {
                        info.latitude3 = parts[0].toDoubleOrNull() ?: 0.0
                        info.longitude3 = parts[1].toDoubleOrNull() ?: 0.0
                        info.x3 = parts[2].toDoubleOrNull() ?: 0.0
                        info.y3 = parts[3].toDoubleOrNull() ?: 0.0
                    }
                }

                // Ред 7: Брой тайлове и размери
                lines.getOrNull(7)?.split(",")?.let { parts ->
                    if (parts.size >= 4) {
                        info.mapWidth = parts[0].toIntOrNull() ?: 0
                        info.mapHeight = parts[1].toIntOrNull() ?: 0
                        info.tileWidth = parts[2].toIntOrNull() ?: 64
                        info.tileHeight = parts[3].toIntOrNull() ?: 64
                    }
                }

                // Ред 8: Проекция
                info.projection = lines.getOrNull(8) ?: ""

                info.isValid = true
                mapInfo = info
                info
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            null
        }
    }

    fun getMapInfo(): MapInfo? = mapInfo

    data class MapInfo(
        var name: String = "",
        var imagePath: String = "",
        var mapWidth: Int = 0,
        var mapHeight: Int = 0,
        var tileWidth: Int = 64,
        var tileHeight: Int = 64,
        var latitude1: Double = 0.0,
        var longitude1: Double = 0.0,
        var x1: Double = 0.0,
        var y1: Double = 0.0,
        var latitude2: Double = 0.0,
        var longitude2: Double = 0.0,
        var x2: Double = 0.0,
        var y2: Double = 0.0,
        var latitude3: Double = 0.0,
        var longitude3: Double = 0.0,
        var x3: Double = 0.0,
        var y3: Double = 0.0,
        var projection: String = "",
        var isValid: Boolean = false
    )
}