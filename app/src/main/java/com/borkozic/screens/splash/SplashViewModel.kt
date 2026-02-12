package com.borkozic.screens.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borkozic.Borkozic
import com.borkozic.data.BorkozicStorage
import com.borkozic.utils.AssetCopier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashViewModel(
    private val context: Context,
    private val application: Borkozic
) : ViewModel() {

    private val _status = MutableStateFlow("Инициализация...")
    val status: StateFlow<String> = _status

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    fun initialize() {
        viewModelScope.launch {
            // Стъпка 1: Създаване на директории
            _status.value = "Подготовка на storage..."
            withContext(Dispatchers.IO) {
                val rootDir = BorkozicStorage.getRootDir(context)
                BorkozicStorage.ensureDirectory(rootDir)
            }
            _progress.value = 20

            // Стъпка 2: Копиране на икони
            _status.value = "Копиране на икони..."
            withContext(Dispatchers.IO) {
                val iconsDir = BorkozicStorage.getIconsDir(context)
                BorkozicStorage.ensureDirectory(iconsDir)
                AssetCopier(context).copyAssets("icons", iconsDir)
            }
            _progress.value = 40

            // Стъпка 3: Копиране на самолети
            _status.value = "Копиране на процедури..."
            withContext(Dispatchers.IO) {
                val planesDir = BorkozicStorage.getPlanesDir(context)
                BorkozicStorage.ensureDirectory(planesDir)
                AssetCopier(context).copyAssets("planes", planesDir)
            }
            _progress.value = 60

            // Стъпка 4: Копиране на зони
            _status.value = "Копиране на зони..."
            withContext(Dispatchers.IO) {
                val dataDir = BorkozicStorage.getDataDir(context)
                BorkozicStorage.ensureDirectory(dataDir)
                AssetCopier(context).copyAssets("zoni", dataDir)
            }
            _progress.value = 80

            // Стъпка 5: Инициализация на карти
            _status.value = "Инициализация на карти..."
            withContext(Dispatchers.IO) {
                application.initializeMaps()
            }
            _progress.value = 100
            _status.value = "Готово!"

            _isReady.value = true
        }
    }
}