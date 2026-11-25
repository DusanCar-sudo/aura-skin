package com.minimal.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minimal.launcher.data.AppsRepository
import com.minimal.launcher.data.FavoritesRepository
import com.minimal.launcher.data.HiddenAppsRepository
import com.minimal.launcher.data.SettingsRepository
import com.minimal.launcher.model.ThemeMode
import com.minimal.launcher.theme.MinimalLauncherTheme
import com.minimal.launcher.ui.charging.ChargingSurface
import com.minimal.launcher.ui.drawer.DrawerScreen
import com.minimal.launcher.ui.drawer.DrawerViewModel
import com.minimal.launcher.ui.home.HomeScreen
import com.minimal.launcher.ui.home.HomeViewModel
import com.minimal.launcher.ui.settings.SettingsScreen
import com.minimal.launcher.ui.settings.SettingsViewModel
import com.minimal.launcher.util.AppLauncher

sealed class LauncherScreen {
    object Home : LauncherScreen()
    object Drawer : LauncherScreen()
    object Settings : LauncherScreen()
}

class MainActivity : ComponentActivity() {
    
    private var homeRequest by mutableIntStateOf(0)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(android.content.Intent.CATEGORY_HOME)) homeRequest++
    }

    private lateinit var appsRepository: AppsRepository
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var hiddenAppsRepository: HiddenAppsRepository
    private lateinit var settingsRepository: SettingsRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize repositories
        appsRepository = AppsRepository(this)
        favoritesRepository = FavoritesRepository(this)
        hiddenAppsRepository = HiddenAppsRepository(this)
        settingsRepository = SettingsRepository(this)
        
        setContent {
            val themeMode by settingsRepository.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            
            MinimalLauncherTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChargingSurface(window) {
                        LauncherContent(
                            homeRequest = homeRequest,
                            appsRepository = appsRepository,
                            favoritesRepository = favoritesRepository,
                            hiddenAppsRepository = hiddenAppsRepository,
                            settingsRepository = settingsRepository
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LauncherContent(
    homeRequest: Int,
    appsRepository: AppsRepository,
    favoritesRepository: FavoritesRepository,
    hiddenAppsRepository: HiddenAppsRepository,
    settingsRepository: SettingsRepository
) {
    var currentScreen by remember { mutableStateOf<LauncherScreen>(LauncherScreen.Home) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val homeViewModel: HomeViewModel = viewModel {
        HomeViewModel(
            context = context,
            appsRepository = appsRepository,
            favoritesRepository = favoritesRepository,
            hiddenAppsRepository = hiddenAppsRepository
        )
    }
    
    val drawerViewModel: DrawerViewModel = viewModel {
        DrawerViewModel(
            context = context,
            appsRepository = appsRepository,
            favoritesRepository = favoritesRepository,
            hiddenAppsRepository = hiddenAppsRepository
        )
    }
    
    val settingsViewModel: SettingsViewModel = viewModel {
        SettingsViewModel(
            context = context,
            settingsRepository = settingsRepository,
            favoritesRepository = favoritesRepository,
            hiddenAppsRepository = hiddenAppsRepository,
            appsRepository = appsRepository
        )
    }
    
    LaunchedEffect(homeRequest) {
        currentScreen = LauncherScreen.Home
        homeViewModel.dismissAppMenu()
        drawerViewModel.dismissAppMenu()
    }

    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
                homeViewModel.loadApps()
                drawerViewModel.loadApps()
                settingsViewModel.loadAllApps()
            }
        }
        val filter = android.content.IntentFilter().apply {
            addAction(android.content.Intent.ACTION_PACKAGE_ADDED)
            addAction(android.content.Intent.ACTION_PACKAGE_REMOVED)
            addAction(android.content.Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    val showIcons by settingsRepository.showIconsFlow.collectAsState(initial = true)
    androidx.activity.compose.BackHandler(currentScreen != LauncherScreen.Home) {
        currentScreen = LauncherScreen.Home
    }
    when (currentScreen) {
        is LauncherScreen.Home -> {
            val uiState by homeViewModel.uiState.collectAsState()
            HomeScreen(
                onSettings = { currentScreen = LauncherScreen.Settings },
                showIcons = showIcons,
                uiState = uiState,
                onAppClick = homeViewModel::onAppClick,
                onAppLongPress = homeViewModel::onAppLongPress,
                onOpenDrawer = { drawerViewModel.onSearchQueryChange(""); currentScreen = LauncherScreen.Drawer },
                onDismissAppMenu = homeViewModel::dismissAppMenu,
                onToggleFavorite = homeViewModel::toggleFavorite,
                onHideApp = homeViewModel::hideApp,
                onOpenAppInfo = homeViewModel::openAppInfo
            )
        }
        is LauncherScreen.Drawer -> {
            val uiState by drawerViewModel.uiState.collectAsState()
            DrawerScreen(
                showIcons = showIcons,
                uiState = uiState,
                onSearchQueryChange = drawerViewModel::onSearchQueryChange,
                onAppClick = drawerViewModel::onAppClick,
                onAppLongPress = drawerViewModel::onAppLongPress,
                onCloseDrawer = { currentScreen = LauncherScreen.Home },
                onDismissAppMenu = drawerViewModel::dismissAppMenu,
                onToggleFavorite = drawerViewModel::toggleFavorite,
                onHideApp = drawerViewModel::hideApp,
                onOpenAppInfo = drawerViewModel::openAppInfo
            )
        }
        is LauncherScreen.Settings -> {
            val uiState by settingsViewModel.uiState.collectAsState()
            SettingsScreen(
                uiState = uiState,
                onThemeChange = settingsViewModel::setThemeMode,
                onShowIconsChange = settingsViewModel::setShowIcons,
                onRemoveFavorite = settingsViewModel::removeFavorite,
                onUnhideApp = settingsViewModel::unhideApp,
                onResetFavorites = settingsViewModel::resetFavoritesToDefaults,
                onSetDefaultLauncher = { AppLauncher.openDefaultLauncherSettings(context) },
                onBack = { currentScreen = LauncherScreen.Home }
            )
        }
    }
}
