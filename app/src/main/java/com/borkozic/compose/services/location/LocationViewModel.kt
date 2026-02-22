package com.borkozic.compose.services.location

import android.content.*
import android.location.Location
import android.os.Build
import androidx.lifecycle.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationViewModel(private val context: Context) : ViewModel() {

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(LocationService.EXTRA_LOCATION, Location::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(LocationService.EXTRA_LOCATION)
                }
                _currentLocation.value = location
            }
        }
    }

    init {
        ContextCompat.registerReceiver(
            context,
            locationReceiver,
            IntentFilter(LocationService.ACTION_LOCATION_UPDATE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun startLocationService() {
        val intent = Intent(context, LocationService::class.java)
        context.startForegroundService(intent)
        _isServiceRunning.value = true
    }

    fun stopLocationService() {
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
        _isServiceRunning.value = false
    }

    override fun onCleared() {
        super.onCleared()
        context.unregisterReceiver(locationReceiver)
    }
}