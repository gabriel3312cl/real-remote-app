package com.remote.app.presentation.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kunal52.remote.Remotemessage
import com.remote.app.presentation.RemoteViewModel
import com.remote.app.presentation.theme.LocalAppStrings

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

        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
                .background(Color.DarkGray.copy(alpha = 0.3f))
        ) {
            ControlButton(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
                icon = Icons.Default.KeyboardArrowUp,
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_UP) }
            )
            ControlButton(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                icon = Icons.Default.KeyboardArrowDown,
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_DOWN) }
            )
            ControlButton(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp),
                icon = Icons.Default.KeyboardArrowLeft,
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_LEFT) }
            )
            ControlButton(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                icon = Icons.Default.KeyboardArrowRight,
                onClick = { viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_RIGHT) }
            )
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
                Text(strings.ok, color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

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
                Icon(Icons.Default.ArrowBack, contentDescription = strings.back)
            }
            FloatingActionButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_HOME)
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.Home, contentDescription = strings.home)
            }
        }

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
