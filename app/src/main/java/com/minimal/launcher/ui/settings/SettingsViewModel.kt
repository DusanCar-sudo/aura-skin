package com.minimal.launcher.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimal.launcher.data.AppsRepository
import com.minimal.launcher.data.FavoritesRepository
import com.minimal.launcher.data.HiddenAppsRepository
import com.minimal.launcher.data.SettingsRepository
import com.minimal.launcher.model.AppItem
import com.minimal.launcher.model.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showIcons: Boolean = false,
    val favorites: List<AppItem> = emptyList(),
    val favoritePackages: Set<String> = emptySet(),
    val hiddenApps: List<AppItem> = emptyList(),
    val hiddenPackages: Set<String> = emptySet(),
    val allApps: List<AppItem> = emptyList()
)

class SettingsViewModel(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val hiddenAppsRepository: HiddenAppsRepository,
    private val appsRepository: AppsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        observeTheme()
        observeShowIcons()
        observeFavorites()
        observeHiddenApps()
        loadAllApps()
    }
    
    private fun observeTheme() {
        viewModelScope.launch {
            settingsRepository.themeModeFlow.collect { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode)
            }
        }
    }
    
    private fun observeShowIcons() {
        viewModelScope.launch {
            settingsRepository.showIconsFlow.collect { show ->
                _uiState.value = _uiState.value.copy(showIcons = show)
            }
        }
    }
    
    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.favoritesFlow.collect { packages ->
                _uiState.value = _uiState.value.copy(favoritePackages = packages)
                updateFavoritesList()
            }
        }
    }
    
    private fun observeHiddenApps() {
        viewModelScope.launch {
            hiddenAppsRepository.hiddenAppsFlow.collect { packages ->
                _uiState.value = _uiState.value.copy(hiddenPackages = packages)
                updateHiddenAppsList()
            }
        }
    }
    
    private fun loadAllApps() {
        viewModelScope.launch {
            val apps = appsRepository.getInstalledApps(context.packageName)
            _uiState.value = _uiState.value.copy(allApps = apps)
            updateFavoritesList()
            updateHiddenAppsList()
        }
    }
    
    private fun updateFavoritesList() {
        val packages = _uiState.value.favoritePackages
        val apps = _uiState.value.allApps
        _uiState.value = _uiState.value.copy(
            favorites = apps.filter { it.packageName in packages }
        )
    }
    
    private fun updateHiddenAppsList() {
        val packages = _uiState.value.hiddenPackages
        val apps = _uiState.value.allApps
        _uiState.value = _uiState.value.copy(
            hiddenApps = apps.filter { it.packageName in packages }
        )
    }
    
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }
    
    fun setShowIcons(show: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowIcons(show)
        }
    }
    
    fun removeFavorite(packageName: String) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(packageName)
        }
    }
    
    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            hiddenAppsRepository.unhideApp(packageName)
        }
    }
    
    fun resetFavoritesToDefaults() {
        viewModelScope.launch {
            favoritesRepository.resetToDefaults()
        }
    }
}
