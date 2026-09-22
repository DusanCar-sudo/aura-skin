package com.minimal.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HiddenAppsRepository(private val context: Context) {
    
    companion object {
        private val HIDDEN_APPS_KEY = stringSetPreferencesKey("hidden_apps")
    }
    
    val hiddenAppsFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[HIDDEN_APPS_KEY] ?: emptySet()
    }
    
    suspend fun setHiddenApps(packages: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[HIDDEN_APPS_KEY] = packages
        }
    }
    
    suspend fun hideApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[HIDDEN_APPS_KEY] ?: emptySet()
            preferences[HIDDEN_APPS_KEY] = current + packageName
        }
    }
    
    suspend fun unhideApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[HIDDEN_APPS_KEY] ?: emptySet()
            preferences[HIDDEN_APPS_KEY] = current - packageName
        }
    }
}
