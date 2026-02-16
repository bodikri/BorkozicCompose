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
import com.borkozic.map.MapIndex
import com.borkozic.map.MapLoader
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
    MapLoader.initialize(context)
    val scope = rememberCoroutineScope()

    // ⚡⚡⚡ GPS функционалност ⚡⚡⚡
    val hasPermission = remember { LocationPermissionHelper.hasPermissions(context) }
    val location by locationViewModel.currentLocation.collectAsState()
    val isServiceRunning by locationViewModel.isServiceRunning.collectAsState()
    var showPermissionDialog by remember { mutableStateOf(!hasPermission) }

    // ⚡⚡⚡ Карти функционалност ⚡⚡⚡
    val mapFiles by mapViewModel.mapFiles.collectAsState()
    val currentMap by mapViewModel.currentMap.collectAsState()
    val currentMapInfo = remember(currentMap) {
        currentMap?.let { MapIndex.getMapByFile(it) }
    }

    val mapInfo by mapViewModel.mapInfo.collectAsState()
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
        //  1. КАРТА (цял екран)
        if (currentMapInfo != null) {
            MapView(
                mapInfo = currentMapInfo,
                initialZoom = 10
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Няма намерени карти в ${BorkozicStorage.getMapsDir(LocalContext.current).path}")
            }
        }

// 2. GPS ИНФОРМАЦИЯ (горе-ляво)
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


        //3. ⚡⚡⚡ КАРТА ИНФОРМАЦИЯ (горе-дясно)
        if (mapInfo != null) {
            Card(// Име на карта, размери, проекция
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Карта",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = mapInfo?.name ?: "Без име",
                        maxLines = 1,
                        modifier = Modifier.widthIn(max = 200.dp)
                    )

                    Text(
                        text = "Размер: ${mapInfo?.mapWidth} x ${mapInfo?.mapHeight}",
                        style = MaterialTheme.typography.bodySmall
                    )
// 4. КОНТРОЛИ ЗА КАРТИ (долу-дясно)
                    mapInfo?.projection?.let {
                        if (it.isNotEmpty()) {
                            Text(
                                text = "Проекция: $it",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                modifier = Modifier.widthIn(max = 200.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
