package com.remote.app.presentation.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.BillingClient
import com.remote.app.domain.AppConstants
import com.remote.app.domain.BillingConstants
import com.remote.app.i18n.AppLanguage
import com.remote.app.presentation.RemoteViewModel
import com.remote.app.presentation.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: RemoteViewModel, onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val currentLanguage by viewModel.appLanguage.collectAsState()
    val isPro by viewModel.isPro.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settings) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = strings.back)
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

            var showLanguageDialog by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showLanguageDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("${currentLanguage.displayName} \uD83C\uDF10", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = { Text(strings.language) },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            AppLanguage.values().forEach { lang ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setAppLanguage(lang)
                                            showLanguageDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = currentLanguage == lang,
                                        onClick = {
                                            viewModel.setAppLanguage(lang)
                                            showLanguageDialog = false
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(lang.displayName, fontSize = 16.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLanguageDialog = false }) {
                            Text(strings.ok)
                        }
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            val activity = LocalContext.current as Activity

            if (isPro) {
                Text("Pro Version Unlocked! \uD83C\uDF1F", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/account/subscriptions?package=${activity.packageName}"))
                    activity.startActivity(intent)
                }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text(strings.manageSubscription)
                }
            } else {
                Text(strings.proVersion, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                Text(strings.buyProDesc, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

                Button(onClick = {
                    viewModel.billingRepository.buyProduct(activity, BillingConstants.PRODUCT_LIFETIME, BillingClient.ProductType.INAPP)
                }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text(strings.proLifetime)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    viewModel.billingRepository.buyProduct(activity, BillingConstants.PRODUCT_MONTHLY, BillingClient.ProductType.SUBS)
                }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text(strings.proSubscription)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = {
                    viewModel.billingRepository.restorePurchases()
                }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text(strings.restorePurchases)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            Text(strings.about, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${strings.developer}: ${AppConstants.DEVELOPER_NAME}", color = Color.Gray)
            Text("${strings.website}: ${AppConstants.WEBSITE_URL}", color = Color(0xFF64B5F6))
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
