package com.remote.app.domain.repository

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val isPro: StateFlow<Boolean>
    fun buyProduct(activity: Activity, productId: String, productType: String)
    fun restorePurchases()
}
