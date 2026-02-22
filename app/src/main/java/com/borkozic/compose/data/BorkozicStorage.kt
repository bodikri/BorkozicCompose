package com.borkozic.compose.data

import android.content.Context
import android.util.Log
import java.io.File

object BorkozicStorage {

    private const val ROOT_DIR_NAME = "Borkozic"

    fun getRootDir(context: Context): File =
        File(context.getExternalFilesDir(null), ROOT_DIR_NAME)

    fun getMapsDir(context: Context): File = File(getRootDir(context), "maps")      // променено от "Maps"

    fun getDataDir(context: Context): File =  File(getRootDir(context), "data")      // променено от "BorkozicData"

    fun getIconsDir(context: Context): File =   File(getRootDir(context), "icons")     // променено от "BorkozicIcons"

    fun getPlanesDir(context: Context): File =   File(getRootDir(context), "planes")    // променено от "BorkozicPlanes"

    fun getTilesDir(context: Context): File {
        val dir = File(getRootDir(context), "tiles")
        Log.d("BorkozicStorage", "tilesDir = ${dir.absolutePath}")
        return dir
    }

    fun getSasDir(context: Context): File =   File(getMapsDir(context), "SAS")       // ако използвате SAS


    fun ensureDirectory(dir: File): Boolean {
        return if (!dir.exists()) {
            dir.mkdirs().also {
                if (it) {
                    File(dir, ".nomedia").createNewFile()
                }
            }
        } else true
    }
}