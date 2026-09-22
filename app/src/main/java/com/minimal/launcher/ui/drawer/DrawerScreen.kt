package com.minimal.launcher.ui.drawer

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.minimal.launcher.R
import com.minimal.launcher.model.AppItem
import com.minimal.launcher.ui.components.AppListItem
import com.minimal.launcher.ui.components.AppMenuSheet
import com.minimal.launcher.ui.components.SearchField

@Composable
fun DrawerScreen(
    uiState: DrawerUiState,
    onSearchQueryChange: (String) -> Unit,
    onAppClick: (AppItem) -> Unit,
    onAppLongPress: (AppItem) -> Unit,
    onCloseDrawer: () -> Unit,
    onDismissAppMenu: () -> Unit,
    onToggleFavorite: (AppItem) -> Unit,
    onHideApp: (AppItem) -> Unit,
    onOpenAppInfo: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount > 50f) {
                        onCloseDrawer()
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Search Field
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.visibleApps, key = { it.packageName }) { app ->
                        AppListItem(
                            label = app.label,
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
