/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Spring parameters shared by the whole app so every animation has the same "feel".
 * Springs are driven by the display's frame clock, so they run at 60/90/120 Hz as available.
 */
object AppMotion {
    /** Small, lively bounce for presses and toggles. */
    const val BOUNCY_DAMPING = 0.45f
    const val BOUNCY_STIFFNESS = Spring.StiffnessMedium

    /** Gentle settle for large surfaces (screens, dialogs). */
    const val SMOOTH_DAMPING = 0.82f
    const val SMOOTH_STIFFNESS = Spring.StiffnessMediumLow
}

/**
 * Apple-like press feedback: the content shrinks slightly while pressed and springs back with
 * a small bounce on release. It only observes touches, so it never interferes with clicks or
 * scrolling (a scroll cancels the press).
 *
 * @param pressedScale scale applied while the pointer is down
 */
@Composable
fun Modifier.pressBounce(pressedScale: Float = DEFAULT_PRESSED_SCALE): Modifier {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = AppMotion.BOUNCY_DAMPING,
            stiffness = AppMotion.BOUNCY_STIFFNESS
        ),
        label = "pressBounce"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
}

private const val DEFAULT_PRESSED_SCALE = 0.96f
