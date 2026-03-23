package com.remote.app.presentation

import com.kunal52.remote.Remotemessage
import com.remote.app.domain.model.ConnectionState
import com.remote.app.domain.model.DiscoveredTV
import com.remote.app.fakes.FakeBillingRepository
import com.remote.app.fakes.FakeSettingsRepository
import com.remote.app.fakes.FakeTVConnectionRepository
import com.remote.app.fakes.FakeTVDiscoveryRepository
import com.remote.app.i18n.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteViewModelTest {

    private lateinit var settingsRepo: FakeSettingsRepository
    private lateinit var billingRepo: FakeBillingRepository
    private lateinit var discoveryRepo: FakeTVDiscoveryRepository
    private lateinit var connectionRepo: FakeTVConnectionRepository
    private lateinit var viewModel: RemoteViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepo = FakeSettingsRepository()
        billingRepo = FakeBillingRepository()
        discoveryRepo = FakeTVDiscoveryRepository()
        connectionRepo = FakeTVConnectionRepository()
        viewModel = RemoteViewModel(settingsRepo, billingRepo, discoveryRepo, connectionRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Language ---

    @Test
    fun `initial language is SYSTEM`() {
        assertEquals(AppLanguage.SYSTEM, viewModel.appLanguage.value)
    }

    @Test
    fun `setAppLanguage updates state and persists`() {
        viewModel.setAppLanguage(AppLanguage.ES_ES)

        assertEquals(AppLanguage.ES_ES, viewModel.appLanguage.value)
        assertTrue(settingsRepo.setLanguageCalled)
        assertEquals(AppLanguage.ES_ES, settingsRepo.lastLanguageSet)
    }

    @Test
    fun `setAppLanguage to multiple languages updates correctly`() {
        viewModel.setAppLanguage(AppLanguage.FR)
        assertEquals(AppLanguage.FR, viewModel.appLanguage.value)

        viewModel.setAppLanguage(AppLanguage.JA)
        assertEquals(AppLanguage.JA, viewModel.appLanguage.value)
    }

    // --- Pro Status ---

    @Test
    fun `isPro reflects billing repository state`() {
        assertFalse(viewModel.isPro.value)
        billingRepo._isPro.value = true
        assertTrue(viewModel.isPro.value)
    }

    // --- Discovery ---

    @Test
    fun `initial state is not scanning`() {
        assertFalse(viewModel.isScanning.value)
    }

    @Test
    fun `startDiscovery resets connection state`() = runTest {
        viewModel.startDiscovery()
        advanceUntilIdle()

        assertTrue(connectionRepo.resetStateCalled)
    }

    @Test
    fun `startDiscovery calls repository start and stop`() = runTest {
        viewModel.startDiscovery()
        advanceUntilIdle()

        assertTrue(discoveryRepo.startDiscoveryCalled)
        assertTrue(discoveryRepo.stopDiscoveryCalled)
    }

    @Test
    fun `discoveredTVs reflects repository state`() {
        val tvList = listOf(
            DiscoveredTV("TV1", "192.168.1.1", 6466, System.currentTimeMillis()),
            DiscoveredTV("TV2", "192.168.1.2", 6466, System.currentTimeMillis())
        )
        discoveryRepo._discoveredTVs.value = tvList

        assertEquals(2, viewModel.discoveredTVs.value.size)
        assertEquals("TV1", viewModel.discoveredTVs.value[0].name)
    }

    // --- Connection ---

    @Test
    fun `initial connection state is DISCONNECTED`() {
        assertEquals(ConnectionState.DISCONNECTED, viewModel.connectionState.value)
    }

    @Test
    fun `connectToTv stops discovery and connects`() {
        val tv = DiscoveredTV("TestTV", "192.168.1.100", 6466, System.currentTimeMillis())

        viewModel.connectToTv(tv)

        assertTrue(discoveryRepo.stopDiscoveryCalled)
        assertTrue(connectionRepo.connectToTvCalled)
        assertEquals(tv, connectionRepo.lastConnectedTv)
    }

    @Test
    fun `providePairingPin delegates to repository`() {
        viewModel.providePairingPin("123456")

        assertTrue(connectionRepo.sendSecretCalled)
        assertEquals("123456", connectionRepo.lastSecret)
    }

    @Test
    fun `cancelPairing calls disconnect`() {
        viewModel.cancelPairing()

        assertTrue(connectionRepo.disconnectCalled)
    }

    @Test
    fun `sendCommand delegates keyCode to repository`() {
        viewModel.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_UP)

        assertTrue(connectionRepo.sendCommandCalled)
        assertEquals(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_UP.number, connectionRepo.lastKeyCode)
    }

    @Test
    fun `disconnect delegates to repository`() {
        viewModel.disconnect()

        assertTrue(connectionRepo.disconnectCalled)
    }

    @Test
    fun `checkConnectionAndReconnect delegates to repository`() {
        viewModel.checkConnectionAndReconnect()

        assertTrue(connectionRepo.checkReconnectCalled)
    }

    @Test
    fun `connectionState reflects repository updates`() {
        connectionRepo._connectionState.value = ConnectionState.CONNECTING
        assertEquals(ConnectionState.CONNECTING, viewModel.connectionState.value)

        connectionRepo._connectionState.value = ConnectionState.CONNECTED
        assertEquals(ConnectionState.CONNECTED, viewModel.connectionState.value)
    }

    @Test
    fun `errorMessage reflects repository updates`() {
        assertNull(viewModel.errorMessage.value)

        connectionRepo._errorMessage.value = "Connection failed"
        assertEquals("Connection failed", viewModel.errorMessage.value)
    }

    @Test
    fun `latencyMs reflects repository updates`() {
        assertNull(viewModel.latencyMs.value)

        connectionRepo._latencyMs.value = 42L
        assertEquals(42L, viewModel.latencyMs.value)
    }
}
