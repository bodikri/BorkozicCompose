package com.borkozic.screens.map

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.borkozic.map.MapInformation
import com.borkozic.map.MapLoader
import com.borkozic.map.SASMapLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    mapInfo: MapInformation? = null,
    initialZoom: Int = 10
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var zoom by remember { mutableIntStateOf(initialZoom) }

    val tileCache = remember { mutableMapOf<String, Bitmap>() }
    val tileSize = 256

    var visibleTiles by remember { mutableStateOf(emptyList<Triple<Int, Int, Int>>()) }

    // Функция за изчисляване на видимите тайлове
    fun updateVisibleTiles(canvasSize: Size) {
        val startX = floor((-offsetX) / tileSize).toInt()
        val startY = floor((-offsetY) / tileSize).toInt()
        val endX = ceil((-offsetX + canvasSize.width) / tileSize).toInt()
        val endY = ceil((-offsetY + canvasSize.height) / tileSize).toInt()

        val newTiles = mutableListOf<Triple<Int, Int, Int>>()
        for (x in startX..endX) {
            for (y in startY..endY) {
                newTiles.add(Triple(zoom, x, y))
            }
        }
        visibleTiles = newTiles
    }

    LaunchedEffect(Unit) {
        MapLoader.initialize(context)
        SASMapLoader.initialize(context)
    }

    LaunchedEffect(mapInfo) {
        mapInfo?.let {
            MapLoader.setMap(it)
            SASMapLoader.setMap(it)
        }
    }

    LaunchedEffect(zoom) {
        MapLoader.setZoom(zoom)
        SASMapLoader.setZoom(zoom)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        zoom = min(zoom + 1, 19)
                        val scaleFactor = 2f
                        offsetX = (offsetX - tapOffset.x) * scaleFactor + tapOffset.x
                        offsetY = (offsetY - tapOffset.y) * scaleFactor + tapOffset.y
                    }
                )
            }
    ) {  // ⬅️ НЯМА параметър!
        // Размерът е достъпен чрез this.size
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height

        val startX = floor((-offsetX) / tileSize).toInt()
        val startY = floor((-offsetY) / tileSize).toInt()
        val endX = ceil((-offsetX + canvasWidth) / tileSize).toInt()
        val endY = ceil((-offsetY + canvasHeight) / tileSize).toInt()

        // Актуализираме видимите тайлове
        val newTiles = mutableListOf<Triple<Int, Int, Int>>()
        for (x in startX..endX) {
            for (y in startY..endY) {
                newTiles.add(Triple(zoom, x, y))
            }
        }
        visibleTiles = newTiles

        // Рисуваме всички видими тайлове
        for (x in startX..endX) {
            for (y in startY..endY) {
                val screenX = offsetX + x * tileSize
                val screenY = offsetY + y * tileSize
                val cacheKey = "$zoom/$x/$y"
                val tileBitmap = tileCache[cacheKey]

                if (tileBitmap != null) {
                    drawImage(
                        image = tileBitmap.asImageBitmap(),
                        dstOffset = IntOffset(screenX.toInt(), screenY.toInt()),
                        dstSize = IntSize(tileSize, tileSize)
                    )
                } else {
                    drawRect(
                        color = androidx.compose.ui.graphics.Color.LightGray,
                        topLeft = Offset(screenX, screenY),
                        size = androidx.compose.ui.geometry.Size(tileSize.toFloat(), tileSize.toFloat())
                    )
                }
            }
        }
    }

    // Асинхронно зареждане на липсващи тайлове
    LaunchedEffect(visibleTiles) {
        for (tile in visibleTiles) {
            val (z, x, y) = tile
            val cacheKey = "$z/$x/$y"
            if (tileCache.containsKey(cacheKey)) continue

            scope.launch(Dispatchers.IO) {
                var bitmap = MapLoader.getTile(z, x, y)
                if (bitmap == null) {
                    bitmap = SASMapLoader.getTile(z, x, y)
                }
                bitmap?.let {
                    withContext(Dispatchers.Main) {
                        tileCache[cacheKey] = it
                    }
                }
            }
        }
    }
}