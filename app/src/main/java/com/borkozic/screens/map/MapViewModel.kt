package com.borkozic.screens.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borkozic.data.BorkozicStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class MapViewModel(
    private val context: Context
) : ViewModel() {

    private val _mapFiles = MutableStateFlow<List<File>>(emptyList())
    val mapFiles: StateFlow<List<File>> = _mapFiles

    private val _currentMap = MutableStateFlow<File?>(null)
    val currentMap: StateFlow<File?> = _currentMap

    init {
        loadMapFiles()
    }

    fun loadMapFiles() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val mapsDir = BorkozicStorage.getMapsDir(context)
                if (mapsDir.exists()) {
                    val files = mapsDir.listFiles { file ->
                        file.extension.lowercase(Locale.getDefault()) in listOf("ozf2", "ozfx3", "map")
                    }?.toList() ?: emptyList()
                    _mapFiles.value = files

                    // Автоматично зареждане на първата карта
                    if (files.isNotEmpty() && _currentMap.value == null) {
                        _currentMap.value = files.firstOrNull {
                            it.extension.lowercase(Locale.getDefault()) == "map"
                        } ?: files.first()
                    }
                }
            }
        }
    }

    fun selectMap(mapFile: File) {
        _currentMap.value = mapFile
    }
}