package com.minimal.launcher.ui.components

import android.graphics.PathMeasure
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
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
fun rememberSparkPhase(): State<Float> {
    val clock = rememberInfiniteTransition(label = "Icon outlines")
    return clock.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "Spark position"
    )
}

fun Modifier.electricEdge(phase: State<Float>, seed: Int, tint: Color) = drawWithCache {
    val inset = 2.5.dp.toPx()
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
    // Offsets across the track give the short tail its lightning shape.
    val bends = floatArrayOf(0f, .45f, -.8f, .3f, -1f, .8f, 0f)
    val amplitude = 1.6.dp.toPx()
    val spacing = 3.dp.toPx()
    val line = Stroke(.9.dp.toPx())
    val halo = Stroke(4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val core = Stroke(1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val spark = Color(0xFF8A68FF)
    onDrawBehind {
        drawPath(rail, tint.copy(alpha = .52f), style = line)
        val head = (phase.value + offset) * perimeter
        bolt.reset()
        for (i in bends.indices) {
            val distance = (head + perimeter - spacing * (bends.lastIndex - i)) % perimeter
            measure.getPosTan(distance, position, tangent)
            val bend = bends[i] * amplitude
            val x = position[0] - tangent[1] * bend
            val y = position[1] + tangent[0] * bend
            if (i == 0) bolt.moveTo(x, y) else bolt.lineTo(x, y)
        }
        drawPath(bolt, spark.copy(alpha = .40f), style = halo)
        drawPath(bolt, spark, style = core)
        val tip = Offset(position[0], position[1])
        drawCircle(Color(0xFF7CEFFF), 2.dp.toPx(), tip)
        drawCircle(Color.White, 1.dp.toPx(), tip)
    }
}
