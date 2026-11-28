package com.minimal.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Quiet, near-black circular Aura Robotics mark, based on the supplied brand reference. */
@Composable
internal fun RoboticsBackdrop() {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color(0xff111011))
            val cx = size.width * .54f
            val cy = size.height * .51f
            val radius = size.width * .345f
            // Soft graphite field behind the ring.
            drawCircle(
                Brush.radialGradient(
                    colors = listOf(Color(0xff1b1719), Color(0xff191517), Color(0xff171416)),
                    center = Offset(cx, cy),
                    radius = radius * 1.55f
                ), radius * 1.55f, Offset(cx, cy)
            )
            drawCircle(Color(0xff211c1f), radius * 1.19f, Offset(cx, cy))
            drawCircle(Color(0xff191618), radius * 1.08f, Offset(cx, cy))
            drawCircle(Color(0xff171416), radius, Offset(cx, cy))
            drawCircle(Color(0xff262124).copy(alpha = .42f), radius, Offset(cx, cy), style = Stroke(width = 1.dp.toPx()))
        }
        Text(
            text = "LEAN ROBOTICS IQ      BY PRAKTESS RESEARCH",
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = 22.dp),
            color = Color(0xffb4aaae).copy(alpha = .76f),
            fontSize = 6.sp,
            letterSpacing = 1.05.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
