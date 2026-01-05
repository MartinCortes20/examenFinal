package com.escom.examenfinal.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "selected_theme"
    private const val KEY_SHOW_NOTIFICATION = "show_notification"
    
    const val THEME_IPN = "ipn"
    const val THEME_ESCOM = "escom"
    
    fun saveTheme(context: Context, theme: String) {
        getPrefs(context).edit().putString(KEY_THEME, theme).apply()
    }
    
    fun getTheme(context: Context): String {
        return getPrefs(context).getString(KEY_THEME, THEME_IPN) ?: THEME_IPN
    }
    
    fun saveShowNotification(context: Context, show: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SHOW_NOTIFICATION, show).apply()
    }
    
    fun getShowNotification(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SHOW_NOTIFICATION, true)
    }
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun applyTheme(theme: String) {
        // El tema se aplica mediante estilos en XML
        // Aquí solo controlamos dark/light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
