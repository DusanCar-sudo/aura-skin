package com.minimal.launcher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.minimal.launcher.model.Backdrop
import kotlin.math.*

@Composable
internal fun FoundationBackdrop(style: Backdrop) {
    val phase = rememberInfiniteTransition(label = "Celestial sand").animateFloat(
        0f, 2f * PI.toFloat(), infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "Orbit")
    val dust = remember {
        List(420) {
            Triple(Math.random().toFloat(), Math.random().toFloat(), .45f + Math.random().toFloat() * 1.7f)
        }
    }
    val palette = when (style) {
        Backdrop.FOUNDATION_BLUE -> listOf(Color(0xffa5e8ff),Color(0xff5187b8),Color(0xff202d52),Color(0xff080e20),Color(0xffe3f8ff))
        Backdrop.FOUNDATION_GREY -> listOf(Color(0xffe2e0d9),Color(0xff9ba3ab),Color(0xff454b58),Color(0xff11151e),Color(0xfff3f2ed))
        Backdrop.FOUNDATION_GOLD -> listOf(Color(0xffffed9a),Color(0xffd59342),Color(0xff66426f),Color(0xff14101f),Color(0xfffff4c0))
        else -> listOf(Color(0xffffe3a2),Color(0xffd5a878),Color(0xff885d68),Color(0xff100e18),Color(0xffffedc7))
    }
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(Color(0xff090910), Color(0xff16111f), Color(0xff070810))))
        val center = Offset(size.width * .5f, size.height * .49f)
        val radius = size.width * .245f
        val time = phase.value
        dust.forEachIndexed { i, p ->
            val angle = p.first * 2f * PI.toFloat() + time * (.22f + p.second * .12f)
            val r = radius * (1.42f + p.second * .78f)
            val x = center.x + cos(angle) * r
            val y = center.y + sin(angle) * r * .36f
            val color = if (i % 9 == 0) palette[4] else palette[1]
            drawCircle(color.copy(alpha = .18f + .6f * ((i % 7) / 7f)),
                p.third.dp.toPx(), Offset(x,y))
            if (i % 25 == 0) drawCircle(Color(0xffffedc7).copy(alpha = .8f),
                (p.third*1.8f).dp.toPx(), Offset(x,y))
        }
        drawOval(palette[0].copy(alpha = .58f),
            topLeft = Offset(center.x-radius*1.9f,center.y-radius*.36f),
            size = androidx.compose.ui.geometry.Size(radius*3.8f,radius*.72f), style=Stroke(1.1.dp.toPx()))
        drawCircle(Brush.radialGradient(palette.take(4),center,radius),
            radius,center)
        drawCircle(palette[0].copy(alpha=.7f),radius,center,style=Stroke(1.dp.toPx()))
        drawCircle(palette[4].copy(alpha=.8f),2.dp.toPx(),Offset(center.x+radius*.34f,center.y-radius*.22f))
    }
}
