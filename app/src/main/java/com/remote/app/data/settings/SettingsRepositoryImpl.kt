package com.remote.app.data.settings

import android.content.SharedPreferences
import com.remote.app.domain.PrefsKeys
import com.remote.app.domain.repository.SettingsRepository
import com.remote.app.i18n.AppLanguage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val prefs: SharedPreferences
) : SettingsRepository {

    override fun getLanguage(): AppLanguage {
        val name = prefs.getString(PrefsKeys.KEY_SELECTED_LANGUAGE, PrefsKeys.DEFAULT_LANGUAGE)
            ?: PrefsKeys.DEFAULT_LANGUAGE
        return AppLanguage.valueOf(name)
    }

    override fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(PrefsKeys.KEY_SELECTED_LANGUAGE, language.name).apply()
    }
}
