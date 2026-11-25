package com.minimal.launcher.ui.drawer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimal.launcher.data.AppsRepository
import com.minimal.launcher.data.FavoritesRepository
import com.minimal.launcher.data.HiddenAppsRepository
import com.minimal.launcher.model.AppItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DrawerUiState(
    val allApps: List<AppItem> = emptyList(),
    val visibleApps: List<AppItem> = emptyList(),
    val searchQuery: String = "",
    val selectedApp: AppItem? = null,
    val showAppMenu: Boolean = false,
    val favoritePackages: Set<String> = emptySet(),
    val hiddenPackages: Set<String> = emptySet()
)

class DrawerViewModel(
    private val context: Context,
    private val appsRepository: AppsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val hiddenAppsRepository: HiddenAppsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DrawerUiState())
    val uiState: StateFlow<DrawerUiState> = _uiState.asStateFlow()
    
    init {
        loadApps()
        observeFavorites()
        observeHiddenApps()
    }
    
    private fun loadApps() {
        viewModelScope.launch {
            val apps = appsRepository.getInstalledApps(context.packageName)
            _uiState.value = _uiState.value.copy(allApps = apps)
            updateVisibleApps()
        }
    }
    
    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.favoritesFlow.collect { packages ->
                _uiState.value = _uiState.value.copy(favoritePackages = packages)
            }
        }
    }
    
    private fun observeHiddenApps() {
        viewModelScope.launch {
            hiddenAppsRepository.hiddenAppsFlow.collect { packages ->
                _uiState.value = _uiState.value.copy(hiddenPackages = packages)
                updateVisibleApps()
            }
        }
    }
    
    private fun updateVisibleApps() {
        val hidden = _uiState.value.hiddenPackages
        val query = _uiState.value.searchQuery.lowercase()
        
        val filtered = _uiState.value.allApps.filter { app ->
            app.packageName !in hidden && 
            app.label.lowercase().contains(query)
        }
        
        _uiState.value = _uiState.value.copy(visibleApps = filtered)
    }
    
    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        updateVisibleApps()
    }
    
    fun onAppClick(app: AppItem) {
        viewModelScope.launch {
            appsRepository.launchApp(app.packageName)
        }
    }
    
    fun onAppLongPress(app: AppItem) {
        _uiState.value = _uiState.value.copy(
            selectedApp = app,
            showAppMenu = true
        )
    }
    
    fun dismissAppMenu() {
        _uiState.value = _uiState.value.copy(
            selectedApp = null,
            showAppMenu = false
        )
    }
    
    fun toggleFavorite(app: AppItem) {
        viewModelScope.launch {
            val isFavorite = app.packageName in _uiState.value.favoritePackages
            if (isFavorite) {
                favoritesRepository.removeFavorite(app.packageName)
            } else {
                favoritesRepository.addFavorite(app.packageName)
            }
        }
    }
    
    fun hideApp(app: AppItem) {
        viewModelScope.launch {
            hiddenAppsRepository.hideApp(app.packageName)
            // Also remove from favorites if it's a favorite
            if (app.packageName in _uiState.value.favoritePackages) {
                favoritesRepository.removeFavorite(app.packageName)
            }
        }
    }
    
    fun openAppInfo(app: AppItem) {
        viewModelScope.launch {
            appsRepository.openAppInfo(app.packageName)
        }
    }
}
