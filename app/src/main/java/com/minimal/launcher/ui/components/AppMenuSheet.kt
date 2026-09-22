package com.minimal.launcher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.minimal.launcher.R
import com.minimal.launcher.model.AppItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMenuSheet(
    app: AppItem,
    isFavorite: Boolean,
    isHidden: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onHide: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Open
            Text(
                text = stringResource(R.string.open),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        onOpen()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp)
            )
            
            // Add/Remove Favorite (only if not hidden)
            if (!isHidden) {
                Text(
                    text = if (isFavorite) stringResource(R.string.remove_from_favorites) else stringResource(R.string.add_to_favorites),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            onToggleFavorite()
                            onDismiss()
                        }
                        .padding(vertical = 12.dp)
                )
            }
            
            // Hide/Unhide
            Text(
                text = if (isHidden) stringResource(R.string.unhide_app) else stringResource(R.string.hide_app),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        onHide()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp)
            )
            
            // App Info
            Text(
                text = stringResource(R.string.app_info),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        onOpenAppInfo()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
