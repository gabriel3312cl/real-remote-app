package com.remote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import android.view.HapticFeedbackConstants
import com.kunal52.remote.Remotemessage
import com.remote.app.i18n.*

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { enUsStrings }

enum class Screen { HOME, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RemoteApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteApp(viewModel: RemoteViewModel = viewModel()) {
    val connectionState by viewModel.connectionState.collectAsState()
    val discoveredTVs by viewModel.discoveredTVs.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    
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
                                androidx.compose.material3.Text(strings.noTvsFound, color = Color.Gray, style = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center))
                            } else {
                                discoveredTVs.forEach { tv ->
                                    val diff = currentTime - tv.lastSeenMillis
                                    val isOnline = tv.lastSeenMillis > 0L && diff < 120_000L // 2 minutes threshold
                                    
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: RemoteViewModel, onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val currentLanguage by viewModel.appLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settings) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(strings.language, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            AppLanguage.values().forEach { lang ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setAppLanguage(lang) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentLanguage == lang,
                        onClick = { viewModel.setAppLanguage(lang) },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(lang.displayName, fontSize = 16.sp)
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 24.dp))
            
            Text(strings.proVersion, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
            Text(strings.buyProDesc, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            Button(onClick = { /* Future IAP stub */ }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text(strings.proVersion)
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp))
            
            Text(strings.about, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${strings.developer}: Squir", color = Color.Gray)
            Text("${strings.website}: https://realremote.app", color = Color(0xFF64B5F6))
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HapticButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val view = LocalView.current
    Box(modifier = Modifier.clickable {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        onClick()
    }) {
        content()
    }
}

@Composable
fun RemoteControlPad(viewModel: RemoteViewModel) {
    val view = LocalView.current
    val strings = LocalAppStrings.current
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        // Top Action Buttons
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).padding(bottom = 16.dp)
        ) {
            FloatingActionButton(
                onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    viewModel.disconnect() 
                },
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(Icons.Default.Close, contentDescription = strings.disconnect)
            }
            FloatingActionButton(
                onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_POWER) 
                },
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(strings.power, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        // D-PAD
        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
                .background(Color.DarkGray.copy(alpha = 0.3f))
        ) {
            // UP
            ControlButton(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
                icon = Icons.Default.KeyboardArrowUp,
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_UP) }
            )
            // DOWN
            ControlButton(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                icon = Icons.Default.KeyboardArrowDown,
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_DOWN) }
            )
            // LEFT
            ControlButton(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp),
                icon = Icons.Default.KeyboardArrowLeft,
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_LEFT) }
            )
            // RIGHT
            ControlButton(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                icon = Icons.Default.KeyboardArrowRight,
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_RIGHT) }
            )
            // CENTER (OK)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { 
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_CENTER) 
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("OK", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            FloatingActionButton(
                onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_BACK) 
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            FloatingActionButton(
                onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_HOME) 
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
        }

        // Volume Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            FloatingActionButton(
                onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_DOWN) 
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            FloatingActionButton(
                onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_UP) 
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ControlButton(modifier: Modifier = Modifier, icon: ImageVector, onClick: () -> Unit) {
    val view = LocalView.current
    IconButton(
        onClick = { 
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClick() 
        },
        modifier = modifier.size(64.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.White)
    }
}
