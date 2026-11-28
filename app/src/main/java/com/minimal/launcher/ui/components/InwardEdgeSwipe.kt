package com.minimal.launcher.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

internal fun Modifier.inwardEdgeSwipe(screen: Any, onSwipe: () -> Unit) = pointerInput(screen) {
    val edge = 22.dp.toPx()
    val threshold = 48.dp.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val direction = when {
            down.position.x <= edge -> 1
            down.position.x >= size.width - edge -> -1
            else -> return@awaitEachGesture
        }
        while (true) {
            val pointer = awaitPointerEvent().changes.singleOrNull() ?: break
            val delta = pointer.position - down.position
            val inward = delta.x * direction
            if (pointer.id != down.id || pointer.isConsumed ||
                abs(delta.y) > maxOf(viewConfiguration.touchSlop, inward * .65f)) break
            if (inward > viewConfiguration.touchSlop) pointer.consume()
            if (!pointer.pressed) {
                if (inward >= threshold) onSwipe()
                break
            }
        }
    }
}

/** Bottom strip launcher navigation: horizontal swipes switch Home/Drawer; vertical swipes open/close it. */
internal fun Modifier.bottomEdgeNavigation(screen: Any, onLeft: () -> Unit, onRight: () -> Unit,
    onUp: () -> Unit, onDown: () -> Unit) = pointerInput(screen) {
    val edgeHeight = 76.dp.toPx()
    val threshold = 42.dp.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (down.position.y < size.height - edgeHeight) return@awaitEachGesture
        val origin = down.position
        var handled = false
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            val dx = change.position.x - origin.x
            val dy = change.position.y - origin.y
            if (!handled && maxOf(abs(dx), abs(dy)) > threshold && abs(dx) > abs(dy) * 1.15f) {
                if (dx < 0) onLeft() else onRight()
                change.consume()
                handled = true
            } else if (!handled && !change.pressed) {
                if (dy < -threshold && abs(dy) > abs(dx) * 1.15f) onUp()
                else if (dy > threshold && abs(dy) > abs(dx) * 1.15f) onDown()
                handled = true
            }
            if (!change.pressed) break
        }
    }
}
