package com.borkozic.library.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

object MapLoader {

    // Премахваме полето appContext - вече не е нужно
    private var currentZoom: Int = 10
    private var currentMapInfo: MapInformation? = null
    private var mapIndex: MapIndex? = null
    private var mapsDir: File? = null
    private var tilesDir: File? = null

    fun initialize(mapsDirectory: File, tilesDirectory: File) {
        mapsDir = mapsDirectory
        tilesDir = tilesDirectory
        MapIndex.loadMaps(mapsDirectory)
        mapIndex = MapIndex
    }

    fun setMap(mapInfo: MapInformation) {
        currentMapInfo = mapInfo
    }

    fun setZoom(zoom: Int) {
        currentZoom = zoom.coerceIn(1, 19)
    }

    fun getTile(zoom: Int, x: Int, y: Int): Bitmap? {
        val tilesDirectory = tilesDir ?: return null
        val osmTile = loadOsmTile(zoom, x, y, tilesDirectory)
        if (osmTile != null) return osmTile

        return null
    }

    private fun loadOsmTile(zoom: Int, x: Int, y: Int, tilesDir: File): Bitmap? {
        val tileFile = File(tilesDir, "$zoom/$x-$y")
        if (!tileFile.exists()) return null
        return try {
            BitmapFactory.decodeFile(tileFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun hasOsmTile(zoom: Int, x: Int, y: Int, tilesDir: File): Boolean {
        val tileFile = File(tilesDir, "$zoom/$x-$y")
        return tileFile.exists()
    }

    fun getCurrentMap(): MapInformation? = currentMapInfo
    fun getCurrentZoom(): Int = currentZoom
}