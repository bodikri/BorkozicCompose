package com.borkozic.library.map.online

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
/**
 * Управлява изтеглянето на тайлове – опашка, паралелни задачи и връзка с кеша.
 * @param baseDir основна директория за запис на тайлове (напр. .../tiles)
 * @param tileCache кеш в паметта
 * @param coroutineScope скоуп за стартиране на корутини (обикновено ViewModel или глобален)
 * @param onTileLoaded callback, който се извиква, когато тайл бъде зареден (за прерисуване)
 */
@Suppress("unused")
class TileController(
    private val baseDir: File,
    private val tileCache: TileCache,
    private val coroutineScope: CoroutineScope,
    private val onTileLoaded: () -> Unit
) {
    private val pendingTiles = ConcurrentLinkedQueue<Tile>()
    private val requested = mutableSetOf<Long>()
    private val downloaderCount = 4
    @Volatile
    private var isActive = true
    private val newTileChannel = Channel<Unit>(UNLIMITED)

    init {
        repeat(downloaderCount) {
            startDownloader()
        }
    }

    private fun startDownloader() {
        coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                val tile = pendingTiles.poll()
                if (tile != null) {
                    downloadTile(tile)
                } else {
                    try {
                        newTileChannel.receive()
                    } catch (_: CancellationException) {
                        break
                    }
                }
            }
        }
    }

    private suspend fun downloadTile(tile: Tile) {
        val provider = tile.provider ?: return
        try {
            val bitmap = TileLoader.downloadTile(provider, tile.x, tile.y, tile.zoomLevel)
            if (bitmap != null) {
                tile.bitmap = bitmap
                tile.generated = false
                TileLoader.saveTile(baseDir, provider, bitmap, tile.x, tile.y, tile.zoomLevel)
                tileCache.put(tile)
                withContext(Dispatchers.Main) {
                    onTileLoaded()
                }
            } else {
                Log.e("TileController", "Failed to download tile ${tile.key}")
            }
        } catch (e: Exception) {
            Log.e("TileController", "Exception in downloadTile", e)
        }
    }

    fun requestTile(provider: TileProvider, x: Int, y: Int, zoom: Int) {
        val tile = Tile(x, y, zoom).apply {
            this.provider = provider
        }
        val key = tile.key
        if (tileCache.containsKey(key)) return
        synchronized(requested) {
            if (requested.contains(key)) return
            requested.add(key)
        }
        pendingTiles.add(tile)
        newTileChannel.trySend(Unit)
    }

    fun shutdown() {
        isActive = false
        newTileChannel.close()
        pendingTiles.clear()
        synchronized(requested) {
            requested.clear()
        }
    }
}