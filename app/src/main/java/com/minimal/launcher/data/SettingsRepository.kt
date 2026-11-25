package com.minimal.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.minimal.launcher.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {
    
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val SHOW_ICONS_KEY = booleanPreferencesKey("show_icons")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("has_completed_onboarding")
    }
    
    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val themeString = preferences[THEME_MODE_KEY] ?: "SYSTEM"
        ThemeMode.valueOf(themeString)
    }
    
    val showIconsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_ICONS_KEY] ?: true
    }
    
    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }
    
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }
    
    suspend fun setShowIcons(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_ICONS_KEY] = show
        }
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }
}
