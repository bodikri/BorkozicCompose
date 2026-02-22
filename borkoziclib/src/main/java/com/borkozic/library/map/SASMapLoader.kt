package com.borkozic.library.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

object SASMapLoader {

    private var activeMap: MapInformation? = null
    private var currentZoom: Int = 10
    private var sasDir: File? = null

    fun initialize(sasDirectory: File) {
        sasDir = sasDirectory
    }

    fun setMap(mapInfo: MapInformation) {
        activeMap = mapInfo
    }

    fun setZoom(zoom: Int) {
        currentZoom = zoom.coerceIn(1, 19)
    }

    fun getTile(zoom: Int, x: Int, y: Int): Bitmap? {
        val sasRoot = sasDir ?: return null
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
        val sasRoot = sasDir ?: return false
        val tileFile = File(sasRoot, "$zoom/$x/$y.jpg")
        return tileFile.exists()
    }

    fun getCurrentMap(): MapInformation? = activeMap
    fun getCurrentZoom(): Int = currentZoom
}