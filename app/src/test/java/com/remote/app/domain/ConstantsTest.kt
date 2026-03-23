package com.remote.app.domain

import org.junit.Assert.*
import org.junit.Test

class ConstantsTest {

    @Test
    fun `AppConstants values are not empty`() {
        assertTrue(AppConstants.CLIENT_NAME.isNotBlank())
        assertTrue(AppConstants.APP_NAME.isNotBlank())
        assertTrue(AppConstants.DEVELOPER_NAME.isNotBlank())
        assertTrue(AppConstants.WEBSITE_URL.isNotBlank())
        assertTrue(AppConstants.TV_DATA_SEPARATOR.isNotBlank())
    }

    @Test
    fun `AppConstants numeric values are positive`() {
        assertTrue(AppConstants.SCAN_DURATION_MS > 0)
        assertTrue(AppConstants.PING_TIMEOUT_MS > 0)
        assertTrue(AppConstants.ONLINE_THRESHOLD_MS > 0)
    }

    @Test
    fun `BillingConstants product IDs are not empty`() {
        assertTrue(BillingConstants.PRODUCT_LIFETIME.isNotBlank())
        assertTrue(BillingConstants.PRODUCT_MONTHLY.isNotBlank())
    }

    @Test
    fun `BillingConstants product IDs are different`() {
        assertNotEquals(BillingConstants.PRODUCT_LIFETIME, BillingConstants.PRODUCT_MONTHLY)
    }

    @Test
    fun `PrefsKeys values are not empty`() {
        assertTrue(PrefsKeys.PREFS_NAME.isNotBlank())
        assertTrue(PrefsKeys.KEY_SAVED_TVS.isNotBlank())
        assertTrue(PrefsKeys.KEY_SELECTED_LANGUAGE.isNotBlank())
        assertTrue(PrefsKeys.DEFAULT_LANGUAGE.isNotBlank())
    }

    @Test
    fun `PrefsKeys are unique`() {
        val keys = listOf(PrefsKeys.KEY_SAVED_TVS, PrefsKeys.KEY_SELECTED_LANGUAGE)
        assertEquals(keys.size, keys.distinct().size)
    }

    @Test
    fun `PrefsKeys DEFAULT_LANGUAGE is valid AppLanguage name`() {
        // Should not throw
        com.remote.app.i18n.AppLanguage.valueOf(PrefsKeys.DEFAULT_LANGUAGE)
    }
}
