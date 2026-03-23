package com.remote.app.data.settings

import android.content.SharedPreferences
import com.remote.app.domain.repository.SettingsRepository
import com.remote.app.i18n.AppLanguage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val prefs: SharedPreferences
) : SettingsRepository {

    override fun getLanguage(): AppLanguage {
        val name = prefs.getString("selected_language", "SYSTEM") ?: "SYSTEM"
        return AppLanguage.valueOf(name)
    }

    override fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("selected_language", language.name).apply()
    }
}
