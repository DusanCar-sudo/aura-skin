package com.minimal.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.minimal.launcher.model.AppItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

class FavoritesRepository(private val context: Context) {
    
    companion object {
        private val FAVORITES_KEY = stringSetPreferencesKey("favorites")
        
        val DEFAULT_FAVORITES = setOf(
            "com.google.android.youtube",
            "com.google.android.gm",
            "com.android.chrome",
            "com.deepseek.android",
            "com.anthropic.claude",
            "com.android.camera",
            "com.viber.voip"
        )
    }
    
    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITES_KEY] ?: DEFAULT_FAVORITES
    }
    
    suspend fun setFavorites(packages: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[FAVORITES_KEY] = packages
        }
    }
    
    suspend fun addFavorite(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITES_KEY] ?: DEFAULT_FAVORITES
            preferences[FAVORITES_KEY] = current + packageName
        }
    }
    
    suspend fun removeFavorite(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITES_KEY] ?: DEFAULT_FAVORITES
            preferences[FAVORITES_KEY] = current - packageName
        }
    }
    
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences[FAVORITES_KEY] = DEFAULT_FAVORITES
        }
    }
    
    suspend fun reorderFavorites(newOrder: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[FAVORITES_KEY] = newOrder.toSet()
        }
    }
}
