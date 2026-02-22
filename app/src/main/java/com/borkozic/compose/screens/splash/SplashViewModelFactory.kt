package com.borkozic.compose.screens.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.borkozic.compose.Borkozic

class SplashViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SplashViewModel(
                context,
                context.applicationContext as Borkozic
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}