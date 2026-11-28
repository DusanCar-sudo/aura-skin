package com.minimal.launcher.ui.components

import android.graphics.PathMeasure
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun Modifier.travellingBolt(grid: LazyGridState, enabled: Boolean): Modifier {
    if (!enabled || grid.isScrollInProgress) return this
    val density = LocalDensity.current
    val info = grid.layoutInfo
    val centers = with(density) {
        info.visibleItemsInfo.map {
            Offset(it.offset.x + it.size.width / 2f,
                it.offset.y - info.viewportStartOffset + 36.dp.toPx())
        }.filter { it.y >= 24.dp.toPx() && it.y + 24.dp.toPx() <= info.viewportSize.height }
    }
    if (centers.isEmpty()) return this
    val progress = remember { Animatable(0f) }
    var route by remember(centers) { mutableStateOf<PathMeasure?>(null) }
    LaunchedEffect(centers, density) {
        val radius = with(density) { 24.dp.toPx() }
        val corner = with(density) { 13.dp.toPx() }
        var current = centers.random()
        while (true) {
            val next = centers.filter { it != current }.sortedBy { (it - current).getDistance() }
                .take(4).randomOrNull() ?: current
            val path = android.graphics.Path()
            val x = current.x
            val y = current.y
            val r = radius
            val c = corner
            path.moveTo(x, y-r)
            path.lineTo(x+r-c, y-r)
            path.arcTo(x+r-2*c,y-r,x+r,y-r+2*c,270f,90f,false)
            path.lineTo(x+r,y+r-c)
            path.arcTo(x+r-2*c,y+r-2*c,x+r,y+r,0f,90f,false)
            path.lineTo(x-r+c,y+r)
            path.arcTo(x-r,y+r-2*c,x-r+2*c,y+r,90f,90f,false)
            path.lineTo(x-r,y-r+c)
            path.arcTo(x-r,y-r,x-r+2*c,y-r+2*c,180f,90f,false)
            path.lineTo(x,y-r)
            path.lineTo(next.x,next.y-r)
            val measure = PathMeasure(path,false)
            progress.snapTo(0f)
            route = measure
            val speed = with(density) { 145.dp.toPx() }
            progress.animateTo(measure.length, tween((1000 * measure.length / speed).toInt(), easing = LinearEasing))
            current = next
        }
    }
    return drawWithCache {
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        val path = Path()
        val random = Random(819)
        val shapes = Array(12) { FloatArray(14) { (random.nextFloat()-.5f)*3.dp.toPx() } }
        val step = 1.8.dp.toPx()
        val cyan = Color(0xff24edfa)
        val edge = Stroke(.85.dp.toPx())
        val core = Stroke(.35.dp.toPx())
        onDrawWithContent {
            drawContent()
            val measure = route ?: return@onDrawWithContent
            val head = progress.value
            val bends = shapes[(head / (step*2)).toInt() % shapes.size]
            path.reset()
            for (i in bends.indices) {
                val distance = (head - step*(bends.lastIndex-i)).coerceAtLeast(0f)
                measure.getPosTan(distance,pos,tan)
                val bend = if (i == bends.lastIndex || distance == 0f) 0f else bends[i]
                val x = pos[0]-tan[1]*bend
                val y = pos[1]+tan[0]*bend
                if (i == 0) path.moveTo(x,y) else path.lineTo(x,y)
            }
            clipRect {
                drawPath(path, cyan.copy(alpha=.68f), style=edge)
                drawPath(path, Color.White.copy(alpha=.65f), style=core)
                val tip = Offset(pos[0],pos[1])
                drawCircle(cyan.copy(alpha=.30f),2.dp.toPx(),tip)
                drawCircle(Color.White,1.05.dp.toPx(),tip)
            }
        }
    }
}
