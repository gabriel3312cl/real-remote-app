package com.remote.app.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.remote.app.i18n.AppStrings
import com.remote.app.i18n.enUsStrings

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { enUsStrings }

enum class Screen { HOME, SETTINGS }
