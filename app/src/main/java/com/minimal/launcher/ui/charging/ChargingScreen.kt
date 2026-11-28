package com.minimal.launcher.ui.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.minimal.launcher.R
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.os.Build
import android.view.WindowManager.LayoutParams
import android.os.BatteryManager
import android.view.Window
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
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
fun ChargingSurface(window: Window, enabled: Boolean, homeRequest: Int, onDisable: () -> Unit, launcher: @Composable () -> Unit) {
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
    LaunchedEffect(homeRequest) { if (homeRequest > 0) dismissed = true }
    if (!enabled || !powered || dismissed) {
        launcher()
        return
    }
    DisposableEffect(window) {
        val bars = WindowCompat.getInsetsController(window, window.decorView)
        val originalCutout = window.attributes.layoutInDisplayCutoutMode
        val statusColor = window.statusBarColor
        val navigationColor = window.navigationBarColor
        window.attributes = window.attributes.apply {
            layoutInDisplayCutoutMode = if (Build.VERSION.SDK_INT >= 30)
                LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            else LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        val behavior = bars.systemBarsBehavior
        bars.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        bars.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            window.attributes = window.attributes.apply { layoutInDisplayCutoutMode = originalCutout }
            window.statusBarColor = statusColor
            window.navigationBarColor = navigationColor
            bars.show(WindowInsetsCompat.Type.systemBars())
            bars.systemBarsBehavior = behavior
        }
    }
    val touch = remember { FluidTouch() }
    BackHandler { dismissed = true }
    Box(Modifier.fillMaxSize().background(Color(0xff1b141c))) {
        Box(Modifier.fillMaxSize().followFluid(touch) { dismissed = true }
        .semantics { onClick("Dismiss charging animation") { dismissed = true; true } }) {
            VaporField(touch)
        }
        Row(Modifier.align(Alignment.TopCenter).padding(top = 44.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            androidx.compose.material3.FilledTonalButton(onClick = { dismissed = true }) { Text("Home") }
            androidx.compose.material3.FilledTonalButton(onClick = { dismissed = true; onDisable() }) { Text("Turn off") }
        }
        Text("ϟ  $percent%", color = Color.White, fontSize = 18.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 46.dp)
                .background(Color.Black.copy(alpha = .56f), CircleShape)
                .padding(horizontal = 19.dp, vertical = 9.dp))
    }
}

internal class FluidTouch {
    var point by mutableStateOf(Offset.Zero)
    var active by mutableStateOf(false)
}

// Observe the initial pass without consuming events needed by launcher icons and scrolling.
internal fun Modifier.followFluid(touch: FluidTouch, onTap: (() -> Unit)? = null) = pointerInput(touch) {
    try {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            touch.point = down.position
            touch.active = true
            var tap = true
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                touch.point = pointer.position
                if (event.changes.size > 1 || (pointer.position - down.position).getDistance() > viewConfiguration.touchSlop)
                    tap = false
                if (!pointer.pressed) {
                    if (tap && pointer.uptimeMillis - down.uptimeMillis < viewConfiguration.longPressTimeoutMillis) onTap?.invoke()
                    break
                }
            } while (true)
            touch.active = false
        }
    } finally {
        touch.active = false
    }
}

// Keep the outer texture edges fixed as the knot travels toward the finger.
private fun steer(value: Float, pivot: Float, target: Float, extent: Float): Float =
    if (value < pivot) value * target / pivot
    else target + (value - pivot) * (extent - target) / (extent - pivot)

@Volatile private var vortexTexture: Bitmap? = null

@Composable
internal fun VaporField(touch: FluidTouch) {
    val resources = LocalContext.current.resources
    val graded by produceState<Bitmap?>(vortexTexture, resources) {
        if (value != null) return@produceState
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val source = BitmapFactory.decodeResource(resources, R.drawable.charging_vortex)
            val pixels = IntArray(source.width * source.height)
            source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
            for (i in pixels.indices) {
                val color = pixels[i]
                val r = AndroidColor.red(color)
                val g = AndroidColor.green(color)
                val b = AndroidColor.blue(color)
                val peak = maxOf(r, g, b)
                val pink = ((r - g) / 68f).coerceIn(0f, 1f)
                val white = ((minOf(r, g, b) - 180) / 58f).coerceIn(0f, 1f)
                val depth = ((peak - 20) / 210f).coerceIn(0f, 1f)
                val teal = (.5f + .5f * sin(8f * (i / source.width).toFloat() / source.height))
                fun channel(cool: Float, warm: Float): Int {
                    val hue = cool + (warm - cool) * pink
                    return (7 + (hue * sqrt(depth) * (1 - white) + 248 * white)).toInt().coerceIn(0, 255)
                }
                pixels[i] = AndroidColor.rgb(channel(9f, 253f), channel(72f + teal * 177f, 49f),
                    channel(253f - teal * 42f, 242f))
            }
            Bitmap.createBitmap(pixels, source.width, source.height, Bitmap.Config.ARGB_8888).also { vortexTexture = it }
        }
    }
    val texture = graded ?: return
    val phase = rememberInfiniteTransition(label = "Charging vortex").animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "Smoke curl")
    val influence = animateFloatAsState(if (touch.active) 1f else 0f,
        animationSpec = tween(if (touch.active) 130 else 700), label = "Finger pull")
    val columns = 48
    val rows = 32
    val vertices = remember { FloatArray((columns + 1) * (rows + 1) * 2) }
    val paint = remember { Paint(Paint.FILTER_BITMAP_FLAG) }
    Canvas(Modifier.fillMaxSize()) {
        val portrait = size.height > size.width
        val w = if (portrait) size.height else size.width
        val h = if (portrait) size.width else size.height
        val scale = max(w / texture.width, h / texture.height)
        val time = phase.value
        val width = texture.width * scale
        val height = texture.height * scale
        val restX = .54f * width + .29f * height * .9932f * cos(time)
        val restY = .49f * height + .19f * height * .9932f * sin(time)
        val finger = touch.point
        val fingerX = (if (portrait) finger.y else finger.x) + (width-w)/2
        val fingerY = (if (portrait) size.width-finger.x else finger.y) + (height-h)/2
        val targetX = restX + influence.value * (fingerX.coerceIn(0f,width) - restX)
        val targetY = restY + influence.value * (fingerY.coerceIn(0f,height) - restY)
        var index = 0
        for (row in 0..rows) {
            val v = row.toFloat() / rows
            for (column in 0..columns) {
                val u = column.toFloat() / columns
                val x = (u - .54f) * texture.width / texture.height
                val y = v - .49f
                val radius = sqrt(x*x + y*y)
                val envelope = 16f * u * (1f-u) * v * (1f-v)
                val angle = envelope * (.34f * sin(time - 10f * radius) +
                    .12f * sin(2f*time + 9f*y + 3f*x))
                // Move the luminous knot itself. More distant smoke follows later.
                val follow = time - 3.8f * radius
                val pull = envelope * exp(-2.4f * radius * radius)
                val dx = x*cos(angle) - y*sin(angle) - x + .29f * pull * cos(follow)
                val dy = x*sin(angle) + y*cos(angle) - y + .19f * pull * sin(follow)
                vertices[index++] = (w-width)/2 + steer(u*width + dx*height, restX, targetX, width)
                vertices[index++] = (h-height)/2 + steer((v+dy)*height, restY, targetY, height)
            }
        }
        val canvas = drawContext.canvas.nativeCanvas
        canvas.save()
        if (portrait) {
            canvas.translate(size.width, 0f)
            canvas.rotate(90f)
        }
        canvas.drawBitmapMesh(texture, columns, rows, vertices, 0, null, 0, paint)
        canvas.restore()
    }
}
