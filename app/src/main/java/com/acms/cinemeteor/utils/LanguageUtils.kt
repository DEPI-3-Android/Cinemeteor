package com.acms.cinemeteor.utils

import android.content.Context
import android.content.SharedPreferences

object LanguageUtils {
    private const val PREFS_NAME = "settings"
    private const val LANG_KEY = "lang"
    
    /**
     * Gets the language code from SharedPreferences and converts it to TMDB API format
     * "en" -> "en-US", "ar" -> "ar", default -> "en-US"
     */
    fun getLanguageCode(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val langCode = prefs.getString(LANG_KEY, "en") ?: "en"
        
        return when (langCode) {
            "en" -> "en-US"
            "ar" -> "ar"
            else -> "en-US"
        }
    }
    
    /**
     * Gets the language code from SharedPreferences (raw format: "en" or "ar")
     */
    fun getLanguageRaw(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANG_KEY, "en") ?: "en"
    }
}

