package com.borkozic.screens.map

import android.Manifest
import android.content.pm.PackageManager
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
import com.borkozic.data.BorkozicStorage
import com.borkozic.services.location.LocationPermissionHelper
import com.borkozic.services.location.LocationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.borkozic.services.location.LocationViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(LocalContext.current)
    ),
    mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ⚡⚡⚡ GPS функционалност ⚡⚡⚡
    val hasPermission = remember { LocationPermissionHelper.hasPermissions(context) }
    val location by locationViewModel.currentLocation.collectAsState()
    val isServiceRunning by locationViewModel.isServiceRunning.collectAsState()
    var showPermissionDialog by remember { mutableStateOf(!hasPermission) }

    // ⚡⚡⚡ Карти функционалност ⚡⚡⚡
    val mapFiles by mapViewModel.mapFiles.collectAsState()
    val currentMap by mapViewModel.currentMap.collectAsState()
    var isMenuExpanded by remember { mutableStateOf(false) }

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

    // ⚡⚡⚡ Автоматично стартиране на LocationService ⚡⚡⚡
    LaunchedEffect(Unit) {
        if (hasPermission && !isServiceRunning) {
            locationViewModel.startLocationService()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ⚡⚡⚡ Карта ⚡⚡⚡
        if (currentMap != null) {
            MapView(mapFile = currentMap)
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Няма намерени карти в ${BorkozicStorage.getMapsDir(LocalContext.current).path}")
            }
        }

        // ⚡⚡⚡ GPS информация (горе-ляво) ⚡⚡⚡
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "GPS Статус",
                    style = MaterialTheme.typography.titleMedium
                )

                if (location != null) {
                    Text("Lat: %.6f".format(location!!.latitude))
                    Text("Lon: %.6f".format(location!!.longitude))
                    Text("Alt: %.1f m".format(location!!.altitude))
                } else {
                    Text("Търсене на GPS...")
                }

                Button(
                    onClick = {
                        if (LocationPermissionHelper.hasPermissions(context)) {
                            if (isServiceRunning) {
                                locationViewModel.stopLocationService()
                            } else {
                                locationViewModel.startLocationService()
                            }
                        } else {
                            val permissions = LocationPermissionHelper.requiredPermissions
                                .filter {
                                    ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                                }.toTypedArray()
                            multiplePermissionsLauncher.launch(permissions)
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(if (isServiceRunning) "Стоп GPS" else "Старт GPS")
                }
            }
        }

        // ⚡⚡⚡ Контроли за карти (долу-дясно) ⚡⚡⚡
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Карти: ${mapFiles.size}")

                if (mapFiles.isNotEmpty()) {
                    Button(
                        onClick = { isMenuExpanded = true }
                    ) {
                        Text(currentMap?.name ?: "Избери карта")
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        mapFiles.forEach { file ->
                            DropdownMenuItem(
                                text = { Text(file.name) },
                                onClick = {
                                    mapViewModel.selectMap(file)
                                    isMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}