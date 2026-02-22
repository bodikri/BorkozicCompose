package com.borkozic.compose.screens.map

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borkozic.compose.data.BorkozicStorage
import com.borkozic.library.map.MapIndex
import com.borkozic.library.map.MapInformation
import com.borkozic.library.map.OzfDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _currentMapInfo = MutableStateFlow<MapInformation?>(null)
    val currentMapInfo: StateFlow<MapInformation?> = _currentMapInfo.asStateFlow()
    private val decoder = OzfDecoder()

    init {
        loadMapFiles()
    }

    fun loadMapFiles() {
        viewModelScope.launch {
            Log.d("MapViewModel", "loadMapFiles started")
            withContext(Dispatchers.IO) {
                val mapsDir = BorkozicStorage.getMapsDir(context)
                Log.d("MapViewModel", "mapsDir = ${mapsDir.absolutePath}")
                if (mapsDir.exists()) {
                    val files = mapsDir.listFiles { file ->
                        file.extension.lowercase() == "map"
                    }?.toList() ?: emptyList()
                    Log.d("MapViewModel", "Found ${files.size} .map files")
                    _mapFiles.value = files
                    if (files.isNotEmpty() && _currentMap.value == null) {
                        // Автоматично избираме първата карта
                        selectMap(files.first())
                        Log.d("MapViewModel", "Auto-selected map: ${files.first().name}")

                    }
                } else {
                    Log.e("MapViewModel", "mapsDir does not exist!")
                }
            }
        }
    }

    fun selectMap(mapFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val info = MapIndex.getMapByFile(mapFile)
            if (info != null) {
                _currentMapInfo.value = info
                _currentMap.value = mapFile
                Log.d("MapViewModel", "Selected map: ${info.name}")
            } else {
                Log.e("MapViewModel", "Could not load map info for ${mapFile.name}")
            }
        }
    }

    fun getDecoder(): OzfDecoder = decoder

    override fun onCleared() {
        super.onCleared()
        decoder.closeImage()
    }
}