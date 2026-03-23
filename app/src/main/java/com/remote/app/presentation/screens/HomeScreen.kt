package com.remote.app.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.remote.app.domain.model.ConnectionState
import com.remote.app.i18n.getAppStrings
import com.remote.app.presentation.RemoteViewModel
import com.remote.app.presentation.components.AdBanner
import com.remote.app.presentation.components.RemoteControlPad
import com.remote.app.presentation.theme.LocalAppStrings
import com.remote.app.presentation.theme.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: RemoteViewModel = hiltViewModel()) {
    val connectionState by viewModel.connectionState.collectAsState()
    val discoveredTVs by viewModel.discoveredTVs.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isPro by viewModel.isPro.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val strings = getAppStrings(language)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkConnectionAndReconnect()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000)
            currentTime = System.currentTimeMillis()
        }
    }

    val focusManager = LocalFocusManager.current

    CompositionLocalProvider(LocalAppStrings provides strings) {
        if (currentScreen == Screen.SETTINGS) {
            SettingsScreen(viewModel) { currentScreen = Screen.HOME }
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Minimal TV Remote") },
                        actions = {
                            IconButton(onClick = { currentScreen = Screen.SETTINGS }) {
                                Icon(Icons.Default.Settings, contentDescription = strings.settings)
                            }
                        }
                    )
                },
                bottomBar = {
                    if (!isPro) {
                        AdBanner()
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (connectionState) {
                        ConnectionState.DISCONNECTED, ConnectionState.ERROR -> {
                            val isScanning by viewModel.isScanning.collectAsState()

                            if (connectionState == ConnectionState.ERROR) {
                                Text("${strings.error}: $errorMessage", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
                            }

                            Button(
                                onClick = { viewModel.startDiscovery() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                enabled = !isScanning
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(strings.scanning, fontSize = 18.sp)
                                } else {
                                    Text(strings.scanForTvs, fontSize = 18.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            if (discoveredTVs.isEmpty()) {
                                Text(strings.noTvsFound, color = Color.Gray, style = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center))
                            } else {
                                discoveredTVs.forEach { tv ->
                                    val diff = currentTime - tv.lastSeenMillis
                                    val isOnline = tv.lastSeenMillis > 0L && diff < 120_000L

                                    val timeString = when {
                                        tv.lastSeenMillis == 0L -> strings.never
                                        diff < 60_000 -> strings.secsAgo
                                        diff < 3600_000 -> strings.minsAgo(diff / 60_000)
                                        diff < 86400_000 -> strings.hoursAgo(diff / 3600_000)
                                        else -> strings.daysAgo(diff / 86400_000)
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable { viewModel.connectToTv(tv) },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(tv.name, fontSize = 20.sp, color = if (isOnline) MaterialTheme.colorScheme.onSurface else Color.Gray)
                                                Spacer(modifier = Modifier.weight(1f))
                                                if (isOnline) {
                                                    Text("${strings.online} ($timeString)", fontSize = 12.sp, color = Color.Green)
                                                } else {
                                                    Text("${strings.offline} ($timeString)", fontSize = 12.sp, color = Color.Gray)
                                                }
                                            }
                                            Text(tv.host, fontSize = 14.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                        ConnectionState.PAIRING_PIN_REQUESTED -> {
                            var pin by remember { mutableStateOf("") }
                            Text(strings.enterPinPrompt, fontSize = 24.sp, style = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center))
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = pin,
                                onValueChange = { pin = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                singleLine = true,
                                label = { Text("PIN") }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.providePairingPin(pin) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                                Text(strings.pair, fontSize = 18.sp)
                            }
                        }
                        ConnectionState.CONNECTING -> {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Connecting...")
                        }
                        ConnectionState.CONNECTED -> {
                            RemoteControlPad(viewModel)
                        }
                    }
                }
            }
        }
    }
}
