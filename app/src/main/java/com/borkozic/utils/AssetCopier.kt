package com.borkozic.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AssetCopier(private val context: Context) {

    @Throws(IOException::class)
    fun copyAssets(assetPath: String, targetDir: File) {
        val assetManager = context.assets
        val assets = assetManager.list(assetPath) ?: return

        for (asset in assets) {
            val targetFile = File(targetDir, asset)
            val fullPath = if (assetPath.isEmpty()) asset else "$assetPath/$asset"
            val isDirectory = assetManager.list(fullPath)?.isNotEmpty() == true

            if (isDirectory) {
                targetFile.mkdirs()
                copyAssets(fullPath, targetFile)
            } else {
                targetFile.parentFile?.mkdirs()
                assetManager.open(fullPath).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}