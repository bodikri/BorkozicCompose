package com.borkozic.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.borkozic.data.BorkozicStorage
import java.io.File

object SASMapLoader {

    private lateinit var appContext: Context
    private var activeMap: MapInformation? = null
    private var currentZoom: Int = 10

    fun initialize(context: Context) {
        appContext = context
    }

    fun setMap(mapInfo: MapInformation) {
        activeMap = mapInfo
    }

    fun setZoom(zoom: Int) {
        currentZoom = zoom.coerceIn(1, 19)
    }

    fun getTile(zoom: Int, x: Int, y: Int): Bitmap? {
        // SASPlanet формат: /storage/.../Maps/SAS/zoom/x/y.jpg
        val sasRoot = File(BorkozicStorage.getMapsDir(appContext), "SAS")
        val tileFile = File(sasRoot, "$zoom/$x/$y.jpg")

        if (!tileFile.exists()) return null

        return try {
            BitmapFactory.decodeFile(tileFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun hasTile(zoom: Int, x: Int, y: Int): Boolean {
        val sasRoot = File(BorkozicStorage.getMapsDir(appContext), "SAS")
        val tileFile = File(sasRoot, "$zoom/$x/$y.jpg")
        return tileFile.exists()
    }

    fun getCurrentMap(): MapInformation? = activeMap
    fun getCurrentZoom(): Int = currentZoom
}