package com.minimal.launcher.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.minimal.launcher.R
import com.minimal.launcher.model.ThemeMode
import com.minimal.launcher.util.AppLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeChange: (ThemeMode) -> Unit,
    onShowIconsChange: (Boolean) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onUnhideApp: (String) -> Unit,
    onResetFavorites: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Appearance Section
            item {
                SectionHeader(title = stringResource(R.string.appearance))
            }
            
            item {
                ThemeSelector(
                    currentTheme = uiState.themeMode,
                    onThemeSelected = onThemeChange
                )
            }
            
            item {
                var showIcons by remember { mutableStateOf(uiState.showIcons) }
                SwitchSettingItem(
                    label = stringResource(R.string.show_icons),
                    checked = showIcons,
                    onCheckedChange = { 
                        showIcons = it
                        onShowIconsChange(it)
                    }
                )
            }
            
            // Favorites Section
            item {
                SectionHeader(title = stringResource(R.string.favorites))
            }
            
            if (uiState.favorites.isEmpty()) {
                item {
                    EmptyState(text = "No favorites set")
                }
            } else {
                items(uiState.favorites, key = { it.packageName }) { app ->
                    FavoriteItem(
                        appLabel = app.label,
                        onRemove = { onRemoveFavorite(app.packageName) }
                    )
                }
            }
            
            item {
                TextButton(
                    onClick = onResetFavorites,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Reset to default favorites")
                }
            }
            
            // Hidden Apps Section
            item {
                SectionHeader(title = stringResource(R.string.hidden_apps))
            }
            
            if (uiState.hiddenApps.isEmpty()) {
                item {
                    EmptyState(text = "No hidden apps")
                }
            } else {
                items(uiState.hiddenApps, key = { it.packageName }) { app ->
                    HiddenAppItem(
                        appLabel = app.label,
                        onUnhide = { onUnhideApp(app.packageName) }
                    )
                }
            }
            
            // System Section
            item {
                SectionHeader(title = stringResource(R.string.system))
            }
            
            item {
                SettingsItem(
                    label = stringResource(R.string.set_default_launcher),
                    onClick = onSetDefaultLauncher
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun ThemeSelector(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.theme),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            ThemeMode.values().forEach { mode ->
                FilterChip(
                    selected = currentTheme == mode,
                    onClick = { onThemeSelected(mode) },
                    label = { Text(when (mode) {
                        ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                        ThemeMode.DARK -> stringResource(R.string.theme_dark)
                    }) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SwitchSettingItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FavoriteItem(
    appLabel: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(appLabel)
        TextButton(onClick = onRemove) {
            Text("Remove")
        }
    }
}

@Composable
private fun HiddenAppItem(
    appLabel: String,
    onUnhide: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(appLabel)
        TextButton(onClick = onUnhide) {
            Text(stringResource(R.string.unhide_app))
        }
    }
}

@Composable
private fun SettingsItem(
    label: String,
    onClick: () -> Unit
) {
    Text(
        text = label,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    )
}

@Composable
private fun EmptyState(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(16.dp)
    )
}
