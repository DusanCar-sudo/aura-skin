package com.minimal.launcher.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimal.launcher.data.AppsRepository
import com.minimal.launcher.data.FavoritesRepository
import com.minimal.launcher.data.HiddenAppsRepository
import com.minimal.launcher.model.AppItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HomeUiState(
    val currentTime: String = "",
    val currentDate: String = "",
    val favorites: List<AppItem> = emptyList(),
    val available: List<AppItem> = emptyList(),
    val folders: Map<String, String> = emptyMap(),
    val selectedApp: AppItem? = null,
    val showAppMenu: Boolean = false
)

class HomeViewModel(
    private val context: Context,
    private val appsRepository: AppsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val hiddenAppsRepository: HiddenAppsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val allApps = MutableStateFlow<List<AppItem>>(emptyList())
    private val favoritePackages = MutableStateFlow<Set<String>>(emptySet())
    private val hiddenPackages = MutableStateFlow<Set<String>>(emptySet())
    
    init {
        loadApps()
        observeFavorites()
        observeHiddenApps()
        viewModelScope.launch { favoritesRepository.foldersFlow.collect { folders ->
            _uiState.update { it.copy(folders = folders) }
        } }
        updateTime()
    }
    
    fun loadApps() {
        viewModelScope.launch {
            val apps = appsRepository.getInstalledApps(context.packageName)
            allApps.value = apps
            updateFavorites()
        }
    }
    
    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.favoritesFlow.collect { packages ->
                favoritePackages.value = packages
                updateFavorites()
            }
        }
    }
    
    private fun observeHiddenApps() {
        viewModelScope.launch {
            hiddenAppsRepository.hiddenAppsFlow.collect { packages ->
                hiddenPackages.value = packages
                updateFavorites()
            }
        }
    }
    
    private fun updateFavorites() {
        val favorites = favoritePackages.value
        val hidden = hiddenPackages.value
        val apps = allApps.value
        
        val favoriteApps = apps.filter { it.packageName in favorites && it.packageName !in hidden }
        _uiState.value = _uiState.value.copy(favorites = favoriteApps, available = apps.filter { it.packageName !in hidden })
    }
    
    private fun updateTime() {
        viewModelScope.launch {
            val timeFormat = android.text.format.DateFormat.getTimeFormat(context)
            val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            while (true) {
                val now = Date()
                _uiState.value = _uiState.value.copy(
                    currentTime = timeFormat.format(now),
                    currentDate = dateFormat.format(now)
                )
                kotlinx.coroutines.delay(60_000L - now.time % 60_000L)
            }
        }
    }
    
    fun placeApps(packages: Set<String>, folder: String) {
        viewModelScope.launch { favoritesRepository.placeApps(packages, folder) }
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
            val isFavorite = app.packageName in favoritePackages.value
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
            if (app.packageName in favoritePackages.value) {
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
