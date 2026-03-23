package com.remote.app.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdUnitId("ca-app-pub-3940256099942544/6300978111")
                setAdSize(AdSize.BANNER)
                val adRequest = AdRequest.Builder().build()
                loadAd(adRequest)
            }
        }
    )
}
