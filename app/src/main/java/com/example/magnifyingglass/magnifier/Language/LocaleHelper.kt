package com.example.magnifyingglass.magnifier.Language

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.*

object LocaleHelper {

    private const val PREF_SELECTED_LANGUAGE = "selected_language"

    fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)

        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)

        // Store the selected language in SharedPreferences
        val prefs: SharedPreferences = context.getSharedPreferences("language_shown", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_SELECTED_LANGUAGE, language).apply()



    }

    fun getSelectedLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences("language_shown", Context.MODE_PRIVATE)
        return prefs.getString(PREF_SELECTED_LANGUAGE, "en") ?: "en"
    }
}

