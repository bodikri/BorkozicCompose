package com.borkozic.services.location

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.borkozic.R
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class LocationService : Service() {

    private val binder = LocationBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isUpdatingLocation = false
    private var currentLocation: Location? = null

    companion object {
        const val ACTION_LOCATION_UPDATE = "com.borkozic.LOCATION_UPDATE"
        const val EXTRA_LOCATION = "location"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "location_channel"
    }

    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createLocationCallback()
        createNotificationChannel()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000 // 5 seconds interval
        ).apply {
            setMinUpdateIntervalMillis(2500) // 2.5 seconds fastest interval
            setMaxUpdateDelayMillis(10000) // 10 seconds max delay
        }.build()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    currentLocation = location
                    broadcastLocation(location)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Получаване на GPS позиция"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Borkozic Location")
            .setContentText("Търсене на GPS сигнал...")
            .setSmallIcon(R.drawable.ic_location)  // Ще добавим икона
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
        return START_STICKY
    }

    fun startLocationUpdates() {
        if (isUpdatingLocation) return

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            isUpdatingLocation = true

            // Update notification
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Borkozic Location")
                .setContentText("GPS сигнал активен")
                .setSmallIcon(R.drawable.ic_location)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)

        } catch (e: SecurityException) {
            // No location permission
            stopSelf()
        }
    }

    fun stopLocationUpdates() {
        if (isUpdatingLocation) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isUpdatingLocation = false
            stopForeground(true)
        }
    }

    private fun broadcastLocation(location: Location) {
        val intent = Intent(ACTION_LOCATION_UPDATE).apply {
            putExtra(EXTRA_LOCATION, location)
        }
        sendBroadcast(intent)
    }

    fun getCurrentLocation(): Location? = currentLocation

    fun isLocationUpdatesActive(): Boolean = isUpdatingLocation

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
    }
}