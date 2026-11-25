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
