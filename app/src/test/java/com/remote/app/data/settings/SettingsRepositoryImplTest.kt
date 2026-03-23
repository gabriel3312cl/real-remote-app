package com.remote.app.data.settings

import android.content.SharedPreferences
import com.remote.app.domain.PrefsKeys
import com.remote.app.i18n.AppLanguage
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class SettingsRepositoryImplTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var repo: SettingsRepositoryImpl

    @Before
    fun setup() {
        editor = mock {
            on { putString(any(), any()) } doReturn it
        }
        prefs = mock {
            on { edit() } doReturn editor
        }
        repo = SettingsRepositoryImpl(prefs)
    }

    @Test
    fun `getLanguage returns SYSTEM when no preference set`() {
        whenever(prefs.getString(PrefsKeys.KEY_SELECTED_LANGUAGE, PrefsKeys.DEFAULT_LANGUAGE))
            .thenReturn("SYSTEM")

        assertEquals(AppLanguage.SYSTEM, repo.getLanguage())
    }

    @Test
    fun `getLanguage returns stored language`() {
        whenever(prefs.getString(PrefsKeys.KEY_SELECTED_LANGUAGE, PrefsKeys.DEFAULT_LANGUAGE))
            .thenReturn("ES_ES")

        assertEquals(AppLanguage.ES_ES, repo.getLanguage())
    }

    @Test
    fun `getLanguage returns SYSTEM for null preference`() {
        whenever(prefs.getString(PrefsKeys.KEY_SELECTED_LANGUAGE, PrefsKeys.DEFAULT_LANGUAGE))
            .thenReturn(null)

        assertEquals(AppLanguage.SYSTEM, repo.getLanguage())
    }

    @Test
    fun `setLanguage writes to SharedPreferences`() {
        repo.setLanguage(AppLanguage.FR)

        verify(editor).putString(PrefsKeys.KEY_SELECTED_LANGUAGE, "FR")
        verify(editor).apply()
    }

    @Test
    fun `setLanguage writes correct value for each language`() {
        repo.setLanguage(AppLanguage.JA)
        verify(editor).putString(PrefsKeys.KEY_SELECTED_LANGUAGE, "JA")

        repo.setLanguage(AppLanguage.ZH_CN)
        verify(editor).putString(PrefsKeys.KEY_SELECTED_LANGUAGE, "ZH_CN")
    }
}
