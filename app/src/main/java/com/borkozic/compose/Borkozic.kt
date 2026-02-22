package com.borkozic.compose

import android.app.Application
import com.borkozic.compose.data.BorkozicStorage
import com.borkozic.library.map.MapIndex
import com.borkozic.library.map.OzfDecoder

class Borkozic : Application() {

    companion object {
        lateinit var instance: Borkozic
            private set
    }

    var mapsInited = false
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Зареждаме индекса на картите веднъж при стартиране
        val mapsDir = BorkozicStorage.getMapsDir(this)
        MapIndex.loadMaps(mapsDir)

        // Тест за NDK (по желание)
        val decoder = OzfDecoder()
        mapsInited = true
    }

    fun initializeMaps() {
        // Може да остане празен или да се използва за презареждане
    }
}