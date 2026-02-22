package com.borkozic.compose.data

import android.content.Context
import java.io.File

object BorkozicStorage {

    fun getRootDir(context: Context): File =
        File(context.getExternalFilesDir(null), "Borkozic")

    fun getMapsDir(context: Context): File =
        File(context.getExternalFilesDir("Maps"), "BorkozicMaps")

    fun getDataDir(context: Context): File =
        File(context.getExternalFilesDir("Data"), "BorkozicData")

    fun getIconsDir(context: Context): File =
        File(context.getExternalFilesDir("Icons"), "BorkozicIcons")

    fun getPlanesDir(context: Context): File =
        File(context.getExternalFilesDir("Planes"), "BorkozicPlanes")
    fun getTilesDir(context: Context): File =
        File(getRootDir(context), "tiles")
    fun getSasDir(context: Context): File =
        File(getMapsDir(context), "SAS")

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