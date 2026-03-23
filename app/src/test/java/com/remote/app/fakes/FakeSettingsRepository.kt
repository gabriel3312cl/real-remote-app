package com.remote.app.fakes

import com.remote.app.domain.repository.SettingsRepository
import com.remote.app.i18n.AppLanguage

class FakeSettingsRepository : SettingsRepository {

    private var currentLanguage: AppLanguage = AppLanguage.SYSTEM

    var setLanguageCalled = false
    var lastLanguageSet: AppLanguage? = null

    override fun getLanguage(): AppLanguage = currentLanguage

    override fun setLanguage(language: AppLanguage) {
        setLanguageCalled = true
        lastLanguageSet = language
        currentLanguage = language
    }
}
