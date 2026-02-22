package com.borkozic.compose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.borkozic.compose.screens.map.MapScreen
import com.borkozic.compose.screens.splash.SplashScreen
import com.borkozic.compose.ui.theme.BorkozicComposeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BorkozicComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    var isReady by remember { mutableStateOf(false) }

                    if (!isReady) {
                        SplashScreen(
                            onInitialized = {
                                isReady = true
                            }
                        )
                    } else {
                        Log.d("MapScreen", "MapScreen composable entered")
                        MapScreen()
                    }
                }
            }
        }
    }
}