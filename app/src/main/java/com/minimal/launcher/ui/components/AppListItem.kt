package com.minimal.launcher.ui.components

import android.content.ComponentName
import android.util.LruCache
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.minimal.launcher.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val icons = LruCache<String, ImageBitmap>(96)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(app: AppItem, showIcons: Boolean, onClick: () -> Unit, onLongClick: () -> Unit,
    onDropAt: ((Offset) -> Unit)? = null) {
    val pm = LocalContext.current.packageManager
    val size = with(LocalDensity.current) { 48.dp.roundToPx() }
    var rootPosition by remember { mutableStateOf(Offset.Zero) }
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .onGloballyPositioned { rootPosition = it.positionInRoot() }
            .then(if (onDropAt != null) Modifier.pointerInput(app.packageName, rootPosition) {
                var dropPosition = rootPosition
                detectDragGesturesAfterLongPress(
                    onDragEnd = { onDropAt?.invoke(dropPosition) },
                    onDragCancel = {},
                    onDrag = { change, amount ->
                        change.consume()
                        dropPosition += amount
                    },
                    onDragStart = { dropPosition = rootPosition + it }
                )
            } else Modifier)
            .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick)
            .heightIn(min = 48.dp).padding(horizontal = 3.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showIcons) {
            val component = ComponentName(app.packageName, app.componentName)
            val key = "${component.flattenToString()}:$size"
            val bitmap by produceState(icons.get(key), key) {
                if (value == null) value = withContext(Dispatchers.IO) {
                    runCatching { pm.getActivityIcon(component) }.getOrElse { pm.defaultActivityIcon }
                        .toBitmap(size, size).asImageBitmap().also { icons.put(key, it) }
                }
            }
            Box(
                modifier = Modifier.size(56.dp).padding(4.dp).border(.7.dp, MaterialTheme.colorScheme.primary.copy(alpha = .30f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                bitmap?.let { Image(it, contentDescription = null, modifier = Modifier.size(46.dp)) }
            }
            Spacer(Modifier.height(7.dp))
        }
        Text(app.label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
