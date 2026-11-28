package com.minimal.launcher.ui.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minimal.launcher.R
import com.minimal.launcher.model.AppItem
import com.minimal.launcher.ui.components.travellingBolt
import com.minimal.launcher.ui.components.AppListItem
import com.minimal.launcher.ui.components.AppMenuSheet
import com.minimal.launcher.ui.components.SearchField

@Composable
fun DrawerScreen(
    uiState: DrawerUiState,
    onSearchQueryChange: (String) -> Unit,
    showIcons: Boolean,
    onAppClick: (AppItem) -> Unit,
    onAppLongPress: (AppItem) -> Unit,
    onCloseDrawer: () -> Unit,
    onDismissAppMenu: () -> Unit,
    onToggleFavorite: (AppItem) -> Unit,
    onHideApp: (AppItem) -> Unit,
    onOpenAppInfo: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Image(painterResource(R.drawable.aura_rainbow), contentDescription = null,
                        modifier = Modifier.size(36.dp))
                    Text(stringResource(R.string.aura), style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onCloseDrawer) { Text(stringResource(R.string.home)) }
            }
            SearchField(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange
            )
            
            // App List
            if (uiState.visibleApps.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (uiState.searchQuery.isNotEmpty()) 
                        stringResource(R.string.no_matching_apps) 
                    else 
                        stringResource(R.string.no_apps_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                val gridState = rememberLazyGridState()
                val bolt = Modifier.travellingBolt(gridState, showIcons)
                LazyVerticalGrid(state = gridState, columns = GridCells.Adaptive(76.dp),
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.fillMaxSize().then(bolt)
                ) {
                    items(uiState.visibleApps, key = { it.packageName }) { app ->
                        AppListItem(
                            app = app, showIcons = showIcons,
                            onClick = { onAppClick(app) },
                            onLongClick = { onAppLongPress(app) }
                        )
                    }
                }
            }
        }
        
        // App Menu Sheet
        if (uiState.showAppMenu && uiState.selectedApp != null) {
            val isFavorite = uiState.selectedApp!!.packageName in uiState.favoritePackages
            val isHidden = uiState.selectedApp!!.isHidden
            
            AppMenuSheet(
                app = uiState.selectedApp!!,
                isFavorite = isFavorite,
                isHidden = isHidden,
                onOpen = { onAppClick(uiState.selectedApp!!) },
                onToggleFavorite = { onToggleFavorite(uiState.selectedApp!!) },
                onHide = { onHideApp(uiState.selectedApp!!) },
                onOpenAppInfo = { onOpenAppInfo(uiState.selectedApp!!) },
                onDismiss = onDismissAppMenu
            )
        }
    }
}
