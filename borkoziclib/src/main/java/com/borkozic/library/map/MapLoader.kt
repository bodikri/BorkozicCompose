package com.borkozic.library.map

import android.graphics.Bitmap
import com.borkozic.library.map.online.TileController
import com.borkozic.library.map.online.TileLoader
import com.borkozic.library.map.online.TileProvider
import com.borkozic.library.map.online.TileCache
import kotlinx.coroutines.CoroutineScope
import android.util.Log
import java.io.File

object MapLoader {

    private var currentZoom: Int = 10
    private var currentMapInfo: MapInformation? = null
    private var mapIndex: MapIndex? = null
    private var mapsDir: File? = null
    private var tilesDir: File? = null
    private var tileController: TileController? = null
    private var baseTilesDir: File? = null
    private var tileCache: TileCache? = null
    private var coroutineScope: CoroutineScope? = null
    private var onTileLoadedCallback: (() -> Unit)? = null

    fun initialize(mapsDirectory: File, tilesDirectory: File) {
        mapsDir = mapsDirectory
        tilesDir = tilesDirectory
    }

    fun initializeTileSystem(
        baseTilesDirectory: File,
        scope: CoroutineScope,
        onTileLoaded: () -> Unit
    ) {
        this.baseTilesDir = baseTilesDirectory
        this.coroutineScope = scope
        this.onTileLoadedCallback = onTileLoaded
        tileCache = TileCache(50) // капацитет 50 тайла в паметта
        tileController = TileController(
            baseDir = baseTilesDirectory,
            tileCache = tileCache!!,
            coroutineScope = scope,
            onTileLoaded = onTileLoaded
        )
    }

    fun setMap(mapInfo: MapInformation) {
        currentMapInfo = mapInfo
    }

    fun setZoom(zoom: Int) {
        currentZoom = zoom.coerceIn(1, 19)
    }

    fun getTile(zoom: Int, x: Int, y: Int): Bitmap? {
        val tilesDir = tilesDir ?: return null
        val provider = TileProvider.OSM // засега само OSM

        // Първо опитваме от диска
        val diskBitmap = TileLoader.loadTileFromDisk(tilesDir, provider, x, y, zoom)
        if (diskBitmap != null) {
            Log.d("MapLoader", "✅ Loaded from disk: $zoom/$x/$y")
            return diskBitmap
        }

        // Ако не е на диска, заявяваме изтегляне
        tileController?.requestTile(provider, x, y, zoom)
        Log.d("MapLoader", "⏳ Requested download: $zoom/$x/$y")
        return null
    }

    fun shutdownTileSystem() {
        tileController?.shutdown()
        tileController = null
        tileCache = null
        baseTilesDir = null
        coroutineScope = null
        onTileLoadedCallback = null
    }

    fun getCurrentMap(): MapInformation? = currentMapInfo
    fun getCurrentZoom(): Int = currentZoom
}