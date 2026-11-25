package com.minimal.launcher.ui.home

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.minimal.launcher.R
import com.minimal.launcher.model.AppItem
import com.minimal.launcher.ui.components.rememberSparkPhase
import com.minimal.launcher.ui.components.AppListItem
import com.minimal.launcher.ui.components.AppMenuSheet
import com.minimal.launcher.ui.components.ClockHeader

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    showIcons: Boolean,
    onAppClick: (AppItem) -> Unit,
    onAppLongPress: (AppItem) -> Unit,
    onSettings: () -> Unit,
    onOpenDrawer: () -> Unit,
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
                var distance = 0f
                detectVerticalDragGestures(
                    onDragStart = { distance = 0f },
                    onDragEnd = { if (distance < -48.dp.toPx()) onOpenDrawer() }
                ) { _, delta -> distance += delta }
            }
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onSettings) { Text(stringResource(R.string.settings)) }
            }
            ClockHeader(uiState.currentTime, uiState.currentDate)
            Box(Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                if (uiState.favorites.isEmpty()) {
                    Text(stringResource(R.string.no_favorites), style = MaterialTheme.typography.bodyMedium)
                } else {
                    val gridState = rememberLazyGridState()
                    val sparkPhase = if (showIcons) rememberSparkPhase(gridState.isScrollInProgress) else null
                    LazyVerticalGrid(state = gridState, columns = GridCells.Adaptive(76.dp)) {
                        items(uiState.favorites, key = { it.packageName }) { app ->
                            AppListItem(app, sparkPhase, { onAppClick(app) }, { onAppLongPress(app) })
                        }
                    }
                }
            }
            FilledTonalButton(onClick = onOpenDrawer, modifier = Modifier.padding(vertical = 18.dp)) {
                Text(stringResource(R.string.all_apps))
            }
        }

        // App Menu Sheet
        if (uiState.showAppMenu && uiState.selectedApp != null) {
            val isFavorite = uiState.selectedApp!!.packageName in uiState.favorites.map { it.packageName }
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
