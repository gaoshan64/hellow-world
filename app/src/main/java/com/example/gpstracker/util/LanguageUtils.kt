package com.example.gpstracker.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageUtils {
    fun applyLanguagePreference(language: String) {
        val locales = when (language) {
            "system" -> LocaleListCompat.getEmptyLocaleList()
            else -> LocaleListCompat.forLanguageTags(language)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
