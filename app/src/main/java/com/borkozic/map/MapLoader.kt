package com.borkozic.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.borkozic.data.BorkozicStorage
import java.io.File

object MapLoader {

    private var currentZoom: Int = 10
    private var currentMapInfo: MapInformation? = null
    private var mapIndex: MapIndex? = null
    private lateinit var appContext: Context  // ✅ добавяме Context

    fun initialize(context: Context) {
        appContext = context
        MapIndex.loadMaps(context)
        mapIndex = MapIndex
    }

    fun setMap(mapInfo: MapInformation) {
        currentMapInfo = mapInfo
    }

    fun setZoom(zoom: Int) {
        currentZoom = zoom.coerceIn(1, 19)
    }

    fun getTile(zoom: Int, x: Int, y: Int): Bitmap? {
        val osmTile = loadOsmTile(zoom, x, y)
        if (osmTile != null) return osmTile

        return null
    }

    private fun loadOsmTile(zoom: Int, x: Int, y: Int): Bitmap? {
        // ✅ вече използваме appContext
        val osmRoot = File(BorkozicStorage.getMapsDir(appContext), "osm")
        val tileFile = File(osmRoot, "$zoom/$x-$y")

        if (!tileFile.exists()) return null

        return try {
            BitmapFactory.decodeFile(tileFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun hasOsmTile(zoom: Int, x: Int, y: Int): Boolean {
        val osmRoot = File(BorkozicStorage.getMapsDir(appContext), "osm")
        val tileFile = File(osmRoot, "$zoom/$x-$y")
        return tileFile.exists()
    }

    fun getCurrentMap(): MapInformation? = currentMapInfo
    fun getCurrentZoom(): Int = currentZoom
}