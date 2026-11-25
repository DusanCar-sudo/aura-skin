package com.minimal.launcher.ui.home

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import com.minimal.launcher.R
import com.minimal.launcher.model.AppItem
import com.minimal.launcher.ui.components.AppListItem
import com.minimal.launcher.ui.components.AppMenuSheet
import com.minimal.launcher.ui.components.ClockHeader

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAppClick: (AppItem) -> Unit,
    onAppLongPress: (AppItem) -> Unit,
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
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount < -50f) {
                        onOpenDrawer()
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Clock and Date
            ClockHeader(
                currentTime = uiState.currentTime,
                currentDate = uiState.currentDate
            )
            
            // Favorites List
            if (uiState.favorites.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.no_favorites),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                uiState.favorites.forEach { app ->
                    AppListItem(
                        label = app.label,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongPress(app) }
                    )
                }
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
