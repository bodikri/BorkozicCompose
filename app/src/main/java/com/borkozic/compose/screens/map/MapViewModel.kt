package com.borkozic.compose.screens.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borkozic.compose.data.BorkozicStorage
import com.borkozic.library.map.OzfDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class MapViewModel(
    private val context: Context
) : ViewModel() {

    private val _mapFiles = MutableStateFlow<List<File>>(emptyList())
    val mapFiles: StateFlow<List<File>> = _mapFiles

    private val _currentMap = MutableStateFlow<File?>(null)
    val currentMap: StateFlow<File?> = _currentMap

    private val _mapInfo = MutableStateFlow<OzfDecoder.MapInfo?>(null)
    val mapInfo: StateFlow<OzfDecoder.MapInfo?> = _mapInfo

    private val decoder = OzfDecoder()

    init {
        loadMapFiles()
    }

    fun loadMapFiles() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val mapsDir = BorkozicStorage.getMapsDir(context)
                if (mapsDir.exists()) {
                    // Търсим .map файлове
                    val files = mapsDir.listFiles { file ->
                        file.extension.lowercase(Locale.getDefault()) == "map"
                    }?.toList() ?: emptyList()
                    _mapFiles.value = files
                }
            }
        }
    }

    fun selectMap(mapFile: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Затвори старата карта
                decoder.closeImage()

                // Зареди .map информация
                val info = decoder.loadMapFile(mapFile)
                _mapInfo.value = info

                // Отвори .ozf2/.ozfx3 файла
                if (info != null) {
                    val imageFile = File(info.imagePath)
                    if (imageFile.exists()) {
                        decoder.openImage(imageFile)
                        _currentMap.value = mapFile
                    } else {
                        // Опитай същата директория
                        val altFile = File(mapFile.parentFile, info.imagePath)
                        if (altFile.exists()) {
                            decoder.openImage(altFile)
                            _currentMap.value = mapFile
                        }
                    }
                }
            }
        }
    }

    fun getDecoder(): OzfDecoder = decoder

    override fun onCleared() {
        super.onCleared()
        decoder.closeImage()
    }
}