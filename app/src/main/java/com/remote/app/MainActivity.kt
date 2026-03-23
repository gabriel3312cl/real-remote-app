package com.remote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kunal52.remote.Remotemessage

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (connectionState) {
            ConnectionState.DISCONNECTED, ConnectionState.ERROR -> {
                if (connectionState == ConnectionState.ERROR) {
                    Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
                }
                Button(onClick = { viewModel.startDiscovery() }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Scan for TVs", fontSize = 18.sp)
                }
            }
            ConnectionState.DISCOVERING -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Searching for TVs on Wi-Fi...")
                
                Spacer(modifier = Modifier.height(24.dp))
                discoveredTVs.forEach { tv ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { viewModel.connectToTv(tv) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(tv.name, fontSize = 20.sp)
                            Text(tv.host, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }
            ConnectionState.PAIRING_PIN_REQUESTED -> {
                var pin by remember { mutableStateOf("") }
                Text("Enter PIN shown on TV", fontSize = 24.sp)
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
                    Text("Pair", fontSize = 18.sp)
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

@Composable
fun RemoteControlPad(viewModel: RemoteViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.padding(16.dp)
    ) {
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
                    .clickable { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_CENTER) },
                contentAlignment = Alignment.Center
            ) {
                Text("OK", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp)
            }
        }

        // Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_BACK) },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            FloatingActionButton(
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_HOME) },
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
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_DOWN) },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text("-", fontSize = 24.sp)
            }
            FloatingActionButton(
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_UP) },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text("+", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun ControlButton(modifier: Modifier = Modifier, icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(64.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.White)
    }
}
