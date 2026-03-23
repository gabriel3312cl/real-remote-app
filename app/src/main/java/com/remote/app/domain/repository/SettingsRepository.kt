package com.remote.app.domain.repository

import com.remote.app.i18n.AppLanguage

interface SettingsRepository {
    fun getLanguage(): AppLanguage
    fun setLanguage(language: AppLanguage)
}
