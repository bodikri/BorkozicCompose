package com.borkozic.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.borkozic.R

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = viewModel(
        factory = SplashViewModelFactory(LocalContext.current)
    ),
    onInitialized: () -> Unit
) {
    val status by viewModel.status.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isReady by viewModel.isReady.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    LaunchedEffect(isReady) {
        if (isReady) {
            onInitialized()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Borkozic",
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(48.dp))

            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = status,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}