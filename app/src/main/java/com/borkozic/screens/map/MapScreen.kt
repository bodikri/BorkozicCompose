package com.borkozic.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.borkozic.services.location.LocationPermissionHelper
import com.borkozic.services.location.LocationViewModel
import com.borkozic.services.location.LocationViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MapScreen(
    locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hasPermission = remember { LocationPermissionHelper.hasPermissions(context) }
    val location by locationViewModel.currentLocation.collectAsState()
    val isServiceRunning by locationViewModel.isServiceRunning.collectAsState()

    var showPermissionDialog by remember { mutableStateOf(!hasPermission) }
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            locationViewModel.startLocationService()
        } else {
            // Покажете съобщение
        }
    }

    LaunchedEffect(Unit) {
        if (hasPermission && !isServiceRunning) {
            locationViewModel.startLocationService()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Картата ще бъде тук
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "GPS Статус",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (location != null) {
                        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val date = Date(location!!.time)

                        Text("Latitude: ${location!!.latitude}")
                        Text("Longitude: ${location!!.longitude}")
                        Text("Altitude: ${location!!.altitude} m")
                        Text("Speed: ${location!!.speed} m/s")
                        Text("Accuracy: ${location!!.accuracy} m")
                        Text("Time: ${timeFormat.format(date)}")
                    } else {
                        Text("Търсене на GPS сигнал...")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val permissions = LocationPermissionHelper.requiredPermissions
                        .filter {
                            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                        }.toTypedArray()

                    if (permissions.isNotEmpty()) {
                        multiplePermissionsLauncher.launch(permissions)
                    } else {
                        if (isServiceRunning) {
                            locationViewModel.stopLocationService()
                        } else {
                            locationViewModel.startLocationService()
                        }
                    }
                }
            ){
                Text(if (isServiceRunning) "Стоп GPS" else "Старт GPS")
            }
        }
    }
}