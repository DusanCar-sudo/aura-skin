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
    
    val foldersFlow = context.dataStore.data.map { prefs ->
        prefs.asMap().entries.filter { it.key.name.startsWith("folder/") }
            .associate { it.key.name.removePrefix("folder/") to it.value.toString() }
    }

    suspend fun placeApps(packages: Set<String>, folder: String) {
        context.dataStore.edit { prefs ->
            prefs[FAVORITES_KEY] = (prefs[FAVORITES_KEY] ?: DEFAULT_FAVORITES) + packages
            for (pkg in packages) {
                val key = androidx.datastore.preferences.core.stringPreferencesKey("folder/$pkg")
                if (folder.isBlank()) prefs.remove(key) else prefs[key] = folder.trim()
            }
        }
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
            preferences.remove(androidx.datastore.preferences.core.stringPreferencesKey("folder/$packageName"))
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
