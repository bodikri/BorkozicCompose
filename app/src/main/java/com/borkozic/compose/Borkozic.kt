package com.borkozic.compose

import android.app.Application
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
    }

    fun initializeMaps() {
        // Тест за NDK
        val decoder = OzfDecoder()
        mapsInited = true
    }
}