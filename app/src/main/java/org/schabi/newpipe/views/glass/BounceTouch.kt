package org.schabi.newpipe.views.glass

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * Adds an Apple-like press feedback to a view: it shrinks slightly while pressed and
 * springs back with a small bounce when released. Touch events are not consumed, so
 * click handling keeps working as before.
 */
object BounceTouch {
    private const val PRESSED_SCALE = 0.94f
    private const val PRESS_MS = 140L
    private const val RELEASE_MS = 460L

    @JvmStatic
    @JvmOverloads
    @SuppressLint("ClickableViewAccessibility")
    fun attach(view: View, pressedScale: Float = PRESSED_SCALE) {
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().cancel()
                    v.animate()
                        .scaleX(pressedScale)
                        .scaleY(pressedScale)
                        .setDuration(PRESS_MS)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().cancel()
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(RELEASE_MS)
                        .setInterpolator(SpringInterpolator.BOUNCY)
                        .start()
                }
            }
            false
        }
    }
}
