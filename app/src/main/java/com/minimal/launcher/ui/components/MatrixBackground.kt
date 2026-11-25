package com.minimal.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.minimal.launcher.theme.MinimalLauncherTheme
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
fun MatrixBackground(content: @Composable () -> Unit) {
    val elapsed = produceState(0L) {
        val start = withFrameMillis { it }
        while (true) {
            value = withFrameMillis { it - start }
            delay(36)
        }
    }
    MinimalLauncherTheme(darkTheme = true) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            Box(Modifier.fillMaxSize()) {
                Canvas(Modifier.fillMaxSize().graphicsLayer().drawWithCache {
                    val cell = 13.dp.roundToPx()
                    val tile = Bitmap.createBitmap(cell * 29, cell * 38, Bitmap.Config.ARGB_8888)
                    tile.eraseColor(android.graphics.Color.rgb(6, 7, 8))
                    val canvas = android.graphics.Canvas(tile)
                    val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = Typeface.MONOSPACE
                        textSize = 11.dp.toPx()
                    }
                    val glyphs = "01:/+=|<>[]ｱｲｴｶｷｻｼﾂﾈﾊﾐﾘ"
                    val rng = Random(603)
                    for (column in 0 until 29) {
                        val start = rng.nextInt(38)
                        val length = rng.nextInt(7, 24)
                        for (step in 0 until length) {
                            val trailShade = 23 + 32 * step / (length - 1)
                            val shade = if (step == length - 1) 76 else trailShade + rng.nextInt(4)
                            ink.color = android.graphics.Color.rgb(shade, shade + 1, shade + 2)
                            canvas.drawText(glyphs[rng.nextInt(glyphs.length)].toString(),
                                (column + .24f) * cell, ((start + step) % 38 + .85f) * cell, ink)
                        }
                    }
                    val pattern = BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                    val paint = Paint().apply { shader = pattern }
                    val columns = kotlin.math.ceil(size.width / cell).toInt()
                    val speeds = FloatArray(columns) { rng.nextInt(32, 85).dp.toPx() / 1000f }
                    onDrawBehind {
                        val native = drawContext.canvas.nativeCanvas
                        val time = elapsed.value
                        for (column in speeds.indices) {
                            val left = (column * cell).toFloat()
                            val y = (time * speeds[column] % tile.height).toInt().toFloat()
                            native.save()
                            native.translate(0f, y)
                            native.drawRect(left, -y, left + cell, size.height - y, paint)
                            native.restore()
                        }
                    }
                }) {}
                content()
            }
        }
    }
}
