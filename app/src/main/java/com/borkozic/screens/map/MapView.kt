package com.borkozic.screens.map

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.borkozic.map.OzfDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    mapFile: File? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mapBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var mapWidth by remember { mutableStateOf(0) }
    var mapHeight by remember { mutableStateOf(0) }

    // Позиция и мащаб
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }

    // Декодер
    val decoder = remember { OzfDecoder() }

    // Зареждане на карта
    LaunchedEffect(mapFile) {
        if (mapFile != null && mapFile.exists()) {
            withContext(Dispatchers.IO) {
                try {
                    // Отваряне на картата
                    val success = decoder.openImage(mapFile)

                    if (success) {
                        // TODO: Прочети размерите на картата от .map файл
                        // Засега слагаме тестови размери
                        mapWidth = 1024
                        mapHeight = 1024

                        // Зареждане на първия тайл
                        loadTile(decoder, 0, 0, 64, 64)?.let { bitmap ->
                            mapBitmap = bitmap
                        }
                    } else {
                        println("Failed to open map file: ${mapFile.absolutePath}")
                    }
                } catch (e: Exception) {
                    println("Error loading map: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
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
                        scale = min(scale * 1.5f, 4f)
                        offsetX = (offsetX - tapOffset.x) * 1.5f + tapOffset.x
                        offsetY = (offsetY - tapOffset.y) * 1.5f + tapOffset.y
                    }
                )
            }
    ) {
        // Рисуване на картата
        mapBitmap?.let { bitmap ->
            drawImage(
                image = bitmap.asImageBitmap(),
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(mapWidth, mapHeight),
                dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
                dstSize = IntSize(
                    width = (mapWidth * scale).toInt(),
                    height = (mapHeight * scale).toInt()
                )
            )
        }

        // Ако няма карта, рисуваме празен фон
        if (mapBitmap == null) {
            drawRect(
                color = androidx.compose.ui.graphics.Color.LightGray,
                size = size
            )
        }
    }
}

// Функция за зареждане на тайл
private fun loadTile(
    decoder: OzfDecoder,
    x: Int,
    y: Int,
    width: Int,
    height: Int
): Bitmap? {
    // TODO: Имплементирай реален тайл лоудър
    // Тестова имплементация
    val bitmap = Bitmap.createBitmap(width, height, ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    // Рисуваме тестов тайл
    paint.color = android.graphics.Color.rgb(
        (x * 20) % 255,
        (y * 20) % 255,
        128
    )
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    paint.color = android.graphics.Color.BLACK
    paint.textSize = 20f
    canvas.drawText("$x,$y", 10f, 40f, paint)

    return bitmap
}