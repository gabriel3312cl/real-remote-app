package com.remote.app.fakes

import android.app.Activity
import com.remote.app.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeBillingRepository : BillingRepository {

    val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro

    var restorePurchasesCalled = false
    var buyProductCalled = false
    var lastProductId: String? = null

    override fun restorePurchases() {
        restorePurchasesCalled = true
    }

    override fun buyProduct(activity: Activity, productId: String, productType: String) {
        buyProductCalled = true
        lastProductId = productId
    }
}
