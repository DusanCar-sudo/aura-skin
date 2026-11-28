package com.minimal.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.minimal.launcher.model.Backdrop
import com.minimal.launcher.theme.MinimalLauncherTheme
import com.minimal.launcher.ui.charging.FluidTouch
import com.minimal.launcher.ui.charging.followFluid
import com.minimal.launcher.ui.charging.VaporField
import kotlin.math.sin

@Composable
fun LauncherBackdrop(style: Backdrop, content: @Composable () -> Unit = {}) {
    if (style == Backdrop.MATRIX) {
        MatrixBackground(content)
        return
    }
    MinimalLauncherTheme(darkTheme = true) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            val touch = remember(style) { FluidTouch() }
            val tracking = if (style == Backdrop.VORTEX) Modifier.followFluid(touch) else Modifier
            Box(Modifier.fillMaxSize().background(Color.Black).then(tracking)) {
                when (style) {
                    Backdrop.FOUNDATION -> FoundationBackdrop(Backdrop.FOUNDATION)
                    Backdrop.FOUNDATION_BLUE -> FoundationBackdrop(Backdrop.FOUNDATION_BLUE)
                    Backdrop.FOUNDATION_GREY -> FoundationBackdrop(Backdrop.FOUNDATION_GREY)
                    Backdrop.FOUNDATION_GOLD -> FoundationBackdrop(Backdrop.FOUNDATION_GOLD)
                    Backdrop.ROBOTICS -> RoboticsBackdrop()
                    Backdrop.VORTEX -> {
                        VaporField(touch)
                        Spacer(Modifier.matchParentSize().background(Color.Black.copy(alpha = .46f)))
                    }
                    Backdrop.TIDE, Backdrop.TIDE_BLUE, Backdrop.TIDE_VIOLET,
                    Backdrop.TIDE_EMBER, Backdrop.TIDE_GREEN, Backdrop.DUSK -> Canvas(Modifier.fillMaxSize()) {
                        val tide = style != Backdrop.DUSK
                        val waveColor = when (style) {
                            Backdrop.TIDE_BLUE -> Color(0xff58bdff)
                            Backdrop.TIDE_VIOLET -> Color(0xffc389ff)
                            Backdrop.TIDE_EMBER -> Color(0xffff995d)
                            Backdrop.TIDE_GREEN -> Color(0xff62f0b0)
                            else -> Color(0xff5fe7e1)
                        }
                        val colors = when (style) {
                            Backdrop.TIDE_BLUE -> listOf(Color(0xff061128),Color(0xff164879),Color(0xff030a1b))
                            Backdrop.TIDE_VIOLET -> listOf(Color(0xff120c26),Color(0xff51346e),Color(0xff0a0717))
                            Backdrop.TIDE_EMBER -> listOf(Color(0xff1d0c13),Color(0xff783c38),Color(0xff100812))
                            Backdrop.TIDE_GREEN -> listOf(Color(0xff031913),Color(0xff176146),Color(0xff04100e))
                            Backdrop.DUSK -> listOf(Color(0xff301f42),Color(0xff513047),Color(0xff100c1c))
                            else -> listOf(Color(0xff021117),Color(0xff104b57),Color(0xff05121e))
                        }
                        drawRect(Brush.linearGradient(colors, Offset.Zero, Offset(size.width, size.height)))
                        if (tide) {
                            val wave = androidx.compose.ui.graphics.Path()
                            for (row in 0..10) {
                                wave.reset()
                                for (step in 0..48) {
                                    val x = step / 48f
                                    val y = .45f + row * .045f + .08f * sin(5f * x + row * .17f)
                                    if (step == 0) wave.moveTo(0f, size.height * y)
                                    else wave.lineTo(size.width * x, size.height * y)
                                }
                                drawPath(wave, waveColor.copy(alpha = .15f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
                            }
                        }
                    }
                    else -> Unit
                }
                content()
            }
        }
    }
}
