package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    companion object {
        private const val PREFS_THEME = "PREFS"
        private const val DARK_THEME = "DARK_THEME"
    }

    var darkTheme = false
        private set

    override fun onCreate() {
        super.onCreate()
        darkTheme = SharedPreferencesTheme()
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        saveSharedPreferencesTheme(darkThemeEnabled)
    }

    private fun SharedPreferencesTheme(): Boolean {
        val sharedPreferences = getSharedPreferences(PREFS_THEME, MODE_PRIVATE)
        return sharedPreferences.getBoolean(DARK_THEME, false)
    }

    private fun saveSharedPreferencesTheme(darkThemeEnabled: Boolean) {
        val sharedPreferences = getSharedPreferences(PREFS_THEME, MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean(DARK_THEME, darkThemeEnabled)
            .apply()
    }
}