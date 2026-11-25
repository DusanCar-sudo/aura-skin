package com.minimal.launcher.ui.components

import android.graphics.PathMeasure
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun rememberSparkPhase(scrolling: Boolean): State<Float> {
    if (scrolling) return remember { mutableFloatStateOf(0f) }
    val clock = rememberInfiniteTransition(label = "Icon outlines")
    return clock.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "Spark position"
    )
}

fun Modifier.electricEdge(phase: State<Float>, seed: Int, tint: Color) = drawWithCache {
    val inset = 4.dp.toPx()
    val radius = 13.dp.toPx()
    val outline = android.graphics.Path().apply {
        addRoundRect(inset, inset, size.width - inset, size.height - inset,
            radius, radius, android.graphics.Path.Direction.CW)
    }
    val rail = outline.asComposePath()
    val measure = PathMeasure(outline, true)
    val perimeter = measure.length
    val offset = (seed and 0xffff) / 65536f
    val position = FloatArray(2)
    val tangent = FloatArray(2)
    val bolt = Path()
    val forks = Path()
    val random = kotlin.random.Random(seed)
    // Precompute discharge shapes so the drawer allocates no random samples per frame.
    val shapes = Array(16) {
        FloatArray(17) { (random.nextFloat() - .5f) * 4.4f.dp.toPx() }
    }
    val spacing = 1.85.dp.toPx()
    val line = Stroke(.7.dp.toPx())
    val glow = Stroke(5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val arc = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val core = Stroke(.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val filament = Stroke(.65.dp.toPx(), cap = StrokeCap.Round)
    val cyan = Color(0xFF24EDFA)
    val hot = Color(0xFFEAFFFF)
    onDrawBehind {
        drawPath(rail, tint.copy(alpha = .30f), style = line)
        val progress = phase.value + offset
        val head = progress * perimeter
        val bends = shapes[(progress * 16).toInt() % shapes.size]
        bolt.reset()
        forks.reset()
        for (i in bends.indices) {
            val distance = (head + perimeter - spacing * (bends.lastIndex - i)) % perimeter
            measure.getPosTan(distance, position, tangent)
            val tx = tangent[0]
            val ty = tangent[1]
            val bend = bends[i]
            val x = position[0] - ty * bend
            val y = position[1] + tx * bend
            if (i == 0) bolt.moveTo(x, y) else bolt.lineTo(x, y)
            if (i == 5 || i == 12) {
                val spread = if (i == 5) spacing else -spacing
                forks.moveTo(x, y)
                forks.relativeLineTo(-tx * spacing - ty * spread, -ty * spacing + tx * spread)
                forks.relativeLineTo(-tx * spacing * 1.6f, -ty * spacing * 1.6f)
                forks.relativeLineTo(-tx * spacing - ty * spread, -ty * spacing + tx * spread)
            }
        }
        drawPath(bolt, cyan.copy(alpha = .16f), style = glow)
        drawPath(bolt, cyan, style = arc)
        drawPath(forks, cyan.copy(alpha = .85f), style = filament)
        drawPath(bolt, hot, style = core)
        val tip = Offset(position[0] - tangent[1] * bends.last(), position[1] + tangent[0] * bends.last())
        drawCircle(hot, 1.15.dp.toPx(), tip)
        drawCircle(cyan, .55.dp.toPx(), tip + Offset(tangent[0], tangent[1]) * (spacing * 2.4f))
    }
}
