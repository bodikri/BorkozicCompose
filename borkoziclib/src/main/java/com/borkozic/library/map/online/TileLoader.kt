package com.borkozic.library.map.online

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Отговорен за изтегляне на тайлове от интернет и записването им на диска.
 */
object TileLoader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Изтегля тайл от даден TileProvider.
     * @param provider източникът на тайлове
     * @param x X координата на тайла
     * @param y Y координата на тайла (в OSM координати, без инверсия)
     * @param zoom ниво на приближаване
     * @return Bitmap на тайла, или null при грешка
     */
    suspend fun downloadTile(
        provider: TileProvider,
        x: Int,
        y: Int,
        zoom: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        val url = provider.getTileUri(x, y, zoom)
        val request = Request.Builder().url(url).build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("TileLoader", "HTTP error ${response.code} for $url")
                response.close()
                return@withContext null
            }
            response.body?.byteStream()?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: null.also { Log.e("TileLoader", "Empty response body for $url") }
        } catch (e: Exception) {
            Log.e("TileLoader", "Exception downloading $url", e)
            null
        }
    }

    /**
     * Записва тайл на диска в указаната директория.
     * Файлът се записва като: [baseDir]/[provider.code]/[zoom]/[x]-[y] (без разширение)
     * @param baseDir основна директория за тайлове (например .../tiles)
     * @param provider източник на тайлове
     * @param tileBitmap битмап на тайла
     * @param x X координата
     * @param y Y координата (в OSM координати, без инверсия)
     * @param zoom ниво
     * @return true при успешен запис
     */
    fun saveTile(
        baseDir: File,
        provider: TileProvider,
        tileBitmap: Bitmap,
        x: Int,
        y: Int,
        zoom: Int
    ): Boolean {
        val tileDir = File(baseDir, provider.code + File.separator + zoom)
        if (!tileDir.exists() && !tileDir.mkdirs()) {
            return false
        }
        val tileFile = File(tileDir, "$x-$y")
        return try {
            FileOutputStream(tileFile).use { out ->
                tileBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Зарежда тайл от диска, ако съществува.
     * @param baseDir основна директория за тайлове
     * @param provider източник
     * @param x X
     * @param y Y
     * @param zoom zoom
     * @return Bitmap или null
     */
    fun loadTileFromDisk(
        baseDir: File,
        provider: TileProvider,
        x: Int,
        y: Int,
        zoom: Int
    ): Bitmap? {
        val tileFile = File(baseDir, provider.code + File.separator + zoom + File.separator + "$x-$y")
        if (!tileFile.exists()) return null
        return BitmapFactory.decodeFile(tileFile.absolutePath)
    }
}