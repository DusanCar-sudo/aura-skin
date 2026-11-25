package com.minimal.launcher.ui.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.os.BatteryManager
import android.view.Window
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.*

@Composable
fun ChargingSurface(window: Window, launcher: @Composable () -> Unit) {
    val context = LocalContext.current
    var powered by remember { mutableStateOf(false) }
    var dismissed by rememberSaveable { mutableStateOf(false) }
    var percent by remember { mutableIntStateOf(0) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
                if (!plugged) dismissed = false
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
                powered = plugged && (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
                percent = (100 * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) / scale).coerceIn(0, 100)
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }
    if (!powered || dismissed) {
        launcher()
        return
    }
    DisposableEffect(window) {
        val bars = WindowCompat.getInsetsController(window, window.decorView)
        val behavior = bars.systemBarsBehavior
        bars.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        bars.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            bars.show(WindowInsetsCompat.Type.systemBars())
            bars.systemBarsBehavior = behavior
        }
    }
    BackHandler { dismissed = true }
    Box(Modifier.fillMaxSize().background(Color(0xff092827))
        .clickable(onClickLabel = "Dismiss charging animation") { dismissed = true }) {
        VaporField()
        Text("ϟ  $percent%", color = Color.White, fontSize = 18.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 46.dp)
                .background(Color.Black.copy(alpha = .56f), CircleShape)
                .padding(horizontal = 19.dp, vertical = 9.dp))
    }
}

private class VaporNoise {
    private val size = 128
    private val random = java.util.Random(7126)
    private val samples = FloatArray(size * size) { random.nextFloat() }

    private fun sample(x: Int, y: Int) = samples[(y and 127) * size + (x and 127)]

    private fun smoothNoise(x: Float, y: Float): Float {
        val ix = floor(x).toInt()
        val iy = floor(y).toInt()
        val fx = (x - ix).let { it * it * (3 - 2 * it) }
        val fy = (y - iy).let { it * it * (3 - 2 * it) }
        val a = sample(ix, iy) * (1 - fx) + sample(ix + 1, iy) * fx
        val b = sample(ix, iy + 1) * (1 - fx) + sample(ix + 1, iy + 1) * fx
        return a * (1 - fy) + b * fy
    }

    fun cloud(x: Float, y: Float): Float {
        var u = x
        var v = y
        var amplitude = .55f
        var density = 0f
        repeat(4) {
            density += amplitude * smoothNoise(u, v)
            val next = 1.72f * u - 1.14f * v + 17f
            v = 1.14f * u + 1.72f * v + 9f
            u = next
            amplitude *= .5f
        }
        return density
    }
}

private fun vaporTexture(layer: Int, noise: VaporNoise): Bitmap {
    val width = 272
    val height = 408
    val pixels = IntArray(width * height)
    val cool = layer == 0
    for (y in 0 until height) for (x in 0 until width) {
        val u = x.toFloat() / width
        val v = y.toFloat() / height
        val shift = 13f * layer
        val curl = noise.cloud(2.7f * u + shift, 4.2f * v)
        val twist = noise.cloud(3.4f * u + 8f, 3.9f * v + shift)
        val folded = noise.cloud(4f * u + 3.4f * curl + shift, 6.2f * v + 3.6f * twist)
        val mist = noise.cloud(5.2f * u + 4.2f * folded, 7.3f * v + 4.7f * curl + shift)
        val center = if (cool) .42f + .24f * sin(6.5f * v + .3f)
                     else .55f + .32f * sin(7.2f * v + 2.8f)
        val across = (u - center + .8f * (curl - .5f)) / (.29f + .18f * twist)
        val envelope = exp(-1.55f * across * across)
        val ridge = exp(-abs(folded - .52f) * 19f)
        val density = ((mist - .30f) * 1.65f + .48f * ridge).coerceIn(0f, 1f)
        val alpha = (250 * envelope * density).toInt()
        val tint = ((v - .25f) * 2.1f + (twist - .5f) * 1.7f).coerceIn(0f, 1f)
        val light = (.54f + .65f * ridge + .25f * mist).coerceIn(0f, 1.16f)
        fun channel(first: Int, second: Int) =
            (((1 - tint) * first + tint * second) * light).toInt().coerceIn(0, 255)
        pixels[y * width + x] = if (cool)
            AndroidColor.argb(alpha, channel(75, 63), channel(235, 140), channel(140, 249))
        else AndroidColor.argb(alpha, channel(255, 209), channel(132, 152), channel(60, 91))
    }
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
}

@Composable
private fun VaporField() {
    val clouds by produceState<List<Bitmap>>(emptyList()) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val noise = VaporNoise()
            List(2) { vaporTexture(it, noise) }
        }
    }
    val phase = rememberInfiniteTransition(label = "Charging vapor").animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing)), label = "Drift")
    val cols = 18
    val rows = 28
    val vertices = remember { FloatArray((cols + 1) * (rows + 1) * 2) }
    val paint = remember { Paint(Paint.FILTER_BITMAP_FLAG) }
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Color(0xff09161e))
        clouds.forEachIndexed { layer, texture ->
            val time = if (layer == 0) phase.value else 2.1f - phase.value
            var index = 0
            for (j in 0..rows) {
                val v = j.toFloat() / rows
                for (i in 0..cols) {
                    val u = i.toFloat() / cols
                    val dx = .12f * sin(time + 5.6f * v + 1.4f * sin(3 * u - time))
                    val dy = .09f * cos(time + 4.6f * u + 1.2f * sin(5 * v + time))
                    // Overscan keeps the background covered as each veil drifts past the edges.
                    vertices[index++] = size.width * (1.3f * u - .15f + dx)
                    vertices[index++] = size.height * (1.22f * v - .11f + dy)
                }
            }
            drawContext.canvas.nativeCanvas.drawBitmapMesh(texture, cols, rows, vertices, 0, null, 0, paint)
        }
    }
}
