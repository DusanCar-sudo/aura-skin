package com.minimal.launcher.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.minimal.launcher.model.AppItem
import com.minimal.launcher.ui.components.AppListItem

@Composable
internal fun FolderTile(name: String, count: Int, modifier: Modifier = Modifier, open: () -> Unit) {
    Column(modifier.clickable(onClick = open).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(56.dp).padding(4.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .6f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center) {
            Text("▦ $count", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(7.dp))
        Text(name, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
internal fun FolderPanel(name: String, apps: List<AppItem>, icons: Boolean, launch: (AppItem) -> Unit,
    onEdit: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.Black.copy(alpha = .77f),
            contentColor = Color.White, border = BorderStroke(1.dp, Color.White.copy(alpha = .25f))) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(name, style = MaterialTheme.typography.titleLarge)
                LazyVerticalGrid(GridCells.Adaptive(72.dp), Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
                    items(apps, key = { it.packageName }) { app ->
                        AppListItem(app, icons, { launch(app) }, onEdit)
                    }
                }
                TextButton(onClick = onEdit) { Text("Organize apps") }
            }
        }
    }
}

@Composable
internal fun HomeAppPicker(apps: List<AppItem>, initialFolder: String, onDismiss: () -> Unit,
    onSave: (Set<String>, String) -> Unit) {
    var folder by remember { mutableStateOf(initialFolder) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(emptySet<String>()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add apps to Home") }, text = {
        Column {
            OutlinedTextField(folder, { folder = it }, label = { Text("Folder name (optional)") },
                supportingText = { Text("Leave blank to place icons directly on Home.") }, singleLine = true)
            OutlinedTextField(query, { query = it }, label = { Text("Search apps") }, singleLine = true)
            LazyColumn(Modifier.heightIn(max = 260.dp)) {
                items(apps.filter { it.label.contains(query, ignoreCase = true) }, key = { it.packageName }) { app ->
                    val checked = app.packageName in selected
                    Row(Modifier.fillMaxWidth().clickable {
                        selected = if (checked) selected - app.packageName else selected + app.packageName
                    }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked, onCheckedChange = null)
                        Spacer(Modifier.width(10.dp))
                        Text(app.label)
                    }
                }
            }
        }
    }, confirmButton = {
        TextButton(enabled = selected.isNotEmpty(), onClick = { onSave(selected, folder.trim()) }) {
            Text("Add (${selected.size})")
        }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
