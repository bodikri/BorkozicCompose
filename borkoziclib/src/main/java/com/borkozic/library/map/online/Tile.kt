package com.borkozic.library.map.online

import android.graphics.Bitmap

/**
 * Представлява един тайл от картата.
 * @param x X координата на тайла (в OSM координати)
 * @param y Y координата на тайла (в OSM координати)
 * @param zoomLevel ниво на приближаване
 */
data class Tile(
    val x: Int,
    val y: Int,
    val zoomLevel: Int
) {
    var bitmap: Bitmap? = null
    var generated: Boolean = false
    var provider: TileProvider? = null

    val key: Long
        get() = (zoomLevel.toLong() shl 48) or (y.toLong() shl 24) or (x.toLong() and 0xFFFFFF)
}