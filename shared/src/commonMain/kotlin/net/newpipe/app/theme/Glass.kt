/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tunable values of the "liquid glass" look.
 */
object GlassDefaults {
    /** Gaussian blur applied to the app while a glass dialog is shown. */
    val BackdropBlur: Dp = 22.dp

    /** Opacity of the glass panels: the blurred app shows through the rest. */
    const val CONTAINER_ALPHA = 0.74f

    val RimWidth: Dp = 1.dp

    /** Shape of dialogs. */
    val DialogShape: Shape get() = appShapes.extraLarge
}

/**
 * Tracks how many glass dialogs are open so the app content behind them can be blurred.
 */
class GlassBackdropState {
    var openDialogs by mutableIntStateOf(0)
}

val LocalGlassBackdropState = staticCompositionLocalOf<GlassBackdropState?> { null }

/**
 * Wraps the app content and blurs it (dense gaussian blur, animated) while any
 * [GlassAlertDialog] is open. The dialogs live in their own window, so they stay sharp and
 * the translucent panel shows the blurred content behind it.
 *
 * `Modifier.blur` is a no-op on platforms/versions without GPU blur (e.g. Android 11 and
 * lower), in which case only the regular dialog scrim is shown.
 */
@Composable
fun GlassBackdrop(content: @Composable () -> Unit) {
    val state = remember { GlassBackdropState() }
    val radius by animateDpAsState(
        targetValue = if (state.openDialogs > 0) GlassDefaults.BackdropBlur else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "glassBackdropBlur"
    )

    CompositionLocalProvider(LocalGlassBackdropState provides state) {
        Box(modifier = if (radius > 0.dp) Modifier.blur(radius) else Modifier) {
            content()
        }
    }
}

/**
 * Translucent glass panel: clipped to [shape], with a soft top gloss and a specular rim.
 * The fill itself is provided by the surface this modifier is applied to.
 */
fun Modifier.glassPanel(shape: Shape, rimWidth: Dp = GlassDefaults.RimWidth): Modifier = this
    .clip(shape)
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = GLOSS_ALPHA), Color.Transparent),
                startY = 0f,
                endY = size.height * GLOSS_FRACTION
            ),
            size = Size(size.width, size.height * GLOSS_FRACTION)
        )
    }
    .border(
        width = rimWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = RIM_STRONG_ALPHA),
                Color.White.copy(alpha = RIM_SOFT_ALPHA),
                Color.White.copy(alpha = RIM_MEDIUM_ALPHA)
            )
        ),
        shape = shape
    )

/**
 * Drop-in replacement for [AlertDialog] with the glass look and the backdrop blur.
 */
@Composable
fun GlassAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null
) {
    val backdrop = LocalGlassBackdropState.current
    DisposableEffect(backdrop) {
        backdrop?.let { it.openDialogs++ }
        onDispose { backdrop?.let { it.openDialogs-- } }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { Box(modifier = Modifier.pressBounce(BUTTON_PRESSED_SCALE)) { confirmButton() } },
        modifier = modifier.glassPanel(GlassDefaults.DialogShape),
        dismissButton = dismissButton?.let { button ->
            { Box(modifier = Modifier.pressBounce(BUTTON_PRESSED_SCALE)) { button() } }
        },
        title = title,
        text = text,
        shape = GlassDefaults.DialogShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            .copy(alpha = GlassDefaults.CONTAINER_ALPHA),
        tonalElevation = 0.dp
    )
}

private const val GLOSS_ALPHA = 0.14f
private const val GLOSS_FRACTION = 0.45f
private const val RIM_STRONG_ALPHA = 0.55f
private const val RIM_SOFT_ALPHA = 0.08f
private const val RIM_MEDIUM_ALPHA = 0.32f
private const val BUTTON_PRESSED_SCALE = 0.9f
