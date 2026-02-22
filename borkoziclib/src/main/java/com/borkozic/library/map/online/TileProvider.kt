package com.borkozic.library.map.online

import java.util.Locale

@Suppress("unused")
class TileProvider(
    val name: String,
    val code: String,
    val urlPattern: String,
    val minZoom: Int,
    val maxZoom: Int,
    val servers: List<String> = emptyList(),
    val inverseY: Boolean = false,
    val ellipsoid: Boolean = false,
    val secret: String? = null
) {
    private var nextServer = 0

    fun getTileUri(x: Int, y: Int, zoom: Int): String {
        var uri = urlPattern
        if (servers.isNotEmpty()) {
            if (nextServer >= servers.size) nextServer = 0
            uri = uri.replace("{s}", servers[nextServer])
            nextServer++
        }
        val finalY = if (inverseY) {
            (1 shl zoom) - 1 - y
        } else y
        uri = uri.replace("{l}", Locale.getDefault().toString())
        uri = uri.replace("{z}", zoom.toString())
        uri = uri.replace("{x}", x.toString())
        uri = uri.replace("{y}", finalY.toString())
        if (uri.contains("{q}")) {
            uri = uri.replace("{q}", encodeQuadTree(zoom, x, finalY))
        }
        if (uri.contains("{g}") && secret != null) {
            val stringLen = (3 * x + y) and 7
            uri = uri.replace("{g}", secret.substring(0, stringLen))
        }
        return uri
    }

    companion object {
        private val NUM_CHAR = arrayOf('0', '1', '2', '3')

        private fun encodeQuadTree(zoom: Int, x: Int, y: Int): String {
            var tx = x
            var ty = y
            val tileNum = CharArray(zoom)
            for (i in zoom - 1 downTo 0) {
                val num = (tx and 1) or ((ty and 1) shl 1)
                tileNum[i] = NUM_CHAR[num]
                tx = tx shr 1
                ty = ty shr 1
            }
            return String(tileNum)
        }
        val OSM = TileProvider(
            name = "OpenStreetMap",
            code = "osm",
            urlPattern = "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
            minZoom = 0,
            maxZoom = 19,
            servers = emptyList(),
            inverseY = false,
            ellipsoid = false,
            secret = null
        )


    }
}
