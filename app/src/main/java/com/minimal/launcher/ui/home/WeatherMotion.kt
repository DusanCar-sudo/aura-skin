package com.minimal.launcher.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
internal fun WeatherMotion(sky: String) {
    val clock = rememberInfiniteTransition(label = "Weather")
    val phase = clock.animateFloat(0f, 1f, infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "Sky")
    Canvas(Modifier.size(32.dp)) {
        val t = phase.value
        val angle = (2 * PI * t).toFloat()
        val unit = size.width / 32f
        fun point(x: Float, y: Float) = Offset(x * unit, y * unit)
        val sunlight = Color(0xffffd477)
        val cloud = Color(0xffb8ccd8)
        val clear = sky == "Clear" || sky == "Mostly clear"
        if (clear || sky == "Partly cloudy" || sky.isEmpty()) {
            val center = if (clear) point(16f, 16f) else point(11f, 11f)
            drawCircle(sunlight, 5 * unit, center)
            repeat(8) {
                val a = angle / 8 + it * PI.toFloat() / 4
                val ray = Offset(cos(a), sin(a)) * unit
                drawLine(sunlight, center + ray * 7f, center + ray * 9f, unit, StrokeCap.Round)
            }
        }
        if (!clear) {
            val dx = 1.7f * sin(angle)
            drawCircle(cloud, 4 * unit, point(10f + dx, 16f))
            drawCircle(cloud, 6 * unit, point(16f + dx, 14f))
            drawCircle(cloud, 4.5f * unit, point(22f + dx, 15.5f))
            drawLine(cloud, point(10f + dx, 17f), point(22f + dx, 17f), 6 * unit, StrokeCap.Round)
        }
        when (sky) {
            "Snow", "Drizzle", "Rain", "Rain showers", "Thunderstorms" -> repeat(3) {
                val fall = (t * 3 + it / 3f) % 1f
                val drop = point(11f + it * 5 - 2 * fall, 21f + fall * 7)
                val color = Color(0xff94dcff).copy(alpha = sin(PI.toFloat() * fall))
                if (sky == "Snow") drawCircle(color, unit, drop)
                else drawLine(color, drop, drop + point(-1f, 2f), unit, StrokeCap.Round)
            }
            "Fog" -> repeat(2) {
                val x = sin(angle + it) * 2
                drawLine(cloud, point(6f + x, 23f + 3 * it), point(23f + x, 23f + 3 * it), unit)
            }
        }
        if (sky == "Thunderstorms") {
            drawLine(sunlight, point(19f, 20f), point(16f, 24f), 1.5f * unit)
            drawLine(sunlight, point(16f, 24f), point(19f, 24f), 1.5f * unit)
            drawLine(sunlight, point(19f, 24f), point(16f, 29f), 1.5f * unit)
        }
    }
}
