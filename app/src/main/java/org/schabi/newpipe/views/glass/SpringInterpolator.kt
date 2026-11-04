package org.schabi.newpipe.views.glass

import android.animation.TimeInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Dependency-free damped spring easing (0 -> 1 with a small, natural overshoot).
 * Used to get the "little bounces" of Apple-style interfaces.
 *
 * @param dampingRatio below 1 bounces, 1 is critically damped
 * @param frequency number of oscillations over the whole animation
 */
class SpringInterpolator(
    private val dampingRatio: Float = 0.55f,
    private val frequency: Float = 2.2f
) : TimeInterpolator {
    override fun getInterpolation(input: Float): Float {
        if (input <= 0f) return 0f
        if (input >= 1f) return 1f
        val omega = 2.0 * PI * frequency
        val decay = exp(-dampingRatio * omega * input)
        val damped = omega * sqrt(1.0 - dampingRatio * dampingRatio)
        val sin = kotlin.math.sin(damped * input) * dampingRatio * omega / damped
        return (1.0 - decay * (cos(damped * input) + sin)).toFloat()
    }

    companion object {
        val SOFT = SpringInterpolator(0.62f, 1.9f)
        val BOUNCY = SpringInterpolator(0.5f, 2.4f)
    }
}
