package org.schabi.newpipe.views.glass

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import org.schabi.newpipe.R

/**
 * Search bar container with a "Liquid Glass" look:
 *  - translucent pill with a soft gloss gradient,
 *  - specular rim light (bright on the top-left / bottom-right edges),
 *  - a highlight that follows the finger,
 *  - a one-shot sheen that sweeps across when the field gets focus,
 *  - springy scale animations on touch and focus.
 *
 * It only draws behind/over its children, so the children (EditText, clear button) keep
 * working exactly as before.
 */
class GlassSearchBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private val bounds = RectF()
    private val pillPath = Path()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glossPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = RIM_WIDTH_DP * density
    }
    private val spotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var spotX = 0f
    private var spotY = 0f
    private var spotAlpha = 0f
    private var spotAnimator: ValueAnimator? = null

    /** 0 = idle, 1 = focused. Drives brightness and rim strength. */
    private var focusLevel = 0f
    private var focusAnimator: ValueAnimator? = null

    /** Sheen position across the pill, <0 or >1 means not visible. */
    private var sheenProgress = -1f
    private var sheenAnimator: ValueAnimator? = null

    private var hadFocus = false

    private val focusListener = ViewTreeObserver.OnGlobalFocusChangeListener { _, _ ->
        val focused = hasFocus()
        if (focused != hadFocus) {
            hadFocus = focused
            animateFocus(focused)
        }
    }

    init {
        setWillNotDraw(false)
        clipChildren = false
        clipToPadding = false
        elevation = ELEVATION_DP * density
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, view.height / 2f)
                outline.alpha = 0.55f
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<View>(R.id.toolbar_search_clear)?.let { BounceTouch.attach(it, CLEAR_PRESSED_SCALE) }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalFocusChangeListener(focusListener)
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnGlobalFocusChangeListener(focusListener)
        spotAnimator?.cancel()
        focusAnimator?.cancel()
        sheenAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bounds.set(0f, 0f, w.toFloat(), h.toFloat())
        val radius = h / 2f
        pillPath.reset()
        pillPath.addRoundRect(bounds, radius, radius, Path.Direction.CW)
        invalidateOutline()

        // Vertical gloss: brighter at the top, clearer in the middle, slight lift at the bottom.
        glossPaint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                Color.argb(70, 255, 255, 255),
                Color.argb(14, 255, 255, 255),
                Color.argb(34, 255, 255, 255)
            ),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        // Rim light: strong on opposite corners, nearly transparent elsewhere.
        rimPaint.shader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            intArrayOf(
                Color.argb(235, 255, 255, 255),
                Color.argb(40, 255, 255, 255),
                Color.argb(40, 255, 255, 255),
                Color.argb(170, 255, 255, 255)
            ),
            floatArrayOf(0f, 0.35f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        // Glass body.
        fillPaint.color = Color.argb((46 + 30 * focusLevel).toInt(), 255, 255, 255)
        canvas.drawPath(pillPath, fillPaint)
        canvas.drawPath(pillPath, glossPaint)
        super.onDraw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        val save = canvas.save()
        canvas.clipPath(pillPath)

        // Sheen sweeping across on focus.
        if (sheenProgress in 0f..1f) {
            val w = bounds.width()
            val band = w * 0.35f
            val cx = -band + (w + band * 2f) * sheenProgress
            sheenPaint.shader = LinearGradient(
                cx - band, 0f, cx + band, bounds.height(),
                intArrayOf(Color.TRANSPARENT, Color.argb(95, 255, 255, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(bounds, sheenPaint)
        }

        // Highlight under the finger.
        if (spotAlpha > 0.01f) {
            val radius = bounds.height() * 1.4f
            spotPaint.shader = RadialGradient(
                spotX, spotY, radius,
                intArrayOf(
                    Color.argb((110 * spotAlpha).toInt(), 255, 255, 255),
                    Color.TRANSPARENT
                ),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(bounds, spotPaint)
        }
        canvas.restoreToCount(save)

        // Rim on top of everything, inset by half its width so it is not clipped.
        rimPaint.alpha = (200 + 55 * focusLevel).toInt().coerceAtMost(255)
        rimPaint.strokeWidth = (RIM_WIDTH_DP + 0.6f * focusLevel) * density
        val inset = rimPaint.strokeWidth / 2f
        val radius = bounds.height() / 2f - inset
        canvas.drawRoundRect(
            inset, inset, bounds.width() - inset, bounds.height() - inset,
            radius, radius, rimPaint
        )
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                spotX = ev.x
                spotY = ev.y
                animateSpot(1f)
                animate().cancel()
                animate()
                    .scaleX(PRESSED_SCALE)
                    .scaleY(PRESSED_SCALE)
                    .setDuration(130)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            MotionEvent.ACTION_MOVE -> {
                spotX = ev.x
                spotY = ev.y
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                animateSpot(0f)
                animate().cancel()
                animate()
                    .scaleX(if (hadFocus) FOCUSED_SCALE else 1f)
                    .scaleY(if (hadFocus) FOCUSED_SCALE else 1f)
                    .setDuration(520)
                    .setInterpolator(SpringInterpolator.BOUNCY)
                    .start()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun animateSpot(target: Float) {
        spotAnimator?.cancel()
        spotAnimator = ValueAnimator.ofFloat(spotAlpha, target).apply {
            duration = if (target > 0f) 120 else 380
            addUpdateListener {
                spotAlpha = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun animateFocus(focused: Boolean) {
        focusAnimator?.cancel()
        focusAnimator = ValueAnimator.ofFloat(focusLevel, if (focused) 1f else 0f).apply {
            duration = 420
            interpolator = SpringInterpolator.SOFT
            addUpdateListener {
                focusLevel = (it.animatedValue as Float).coerceIn(0f, 1f)
                invalidate()
            }
            start()
        }
        animate().cancel()
        animate()
            .scaleX(if (focused) FOCUSED_SCALE else 1f)
            .scaleY(if (focused) FOCUSED_SCALE else 1f)
            .setDuration(520)
            .setInterpolator(SpringInterpolator.BOUNCY)
            .start()

        if (focused) {
            sheenAnimator?.cancel()
            sheenAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 800
                interpolator = DecelerateInterpolator(1.4f)
                addUpdateListener {
                    sheenProgress = it.animatedValue as Float
                    invalidate()
                }
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        sheenProgress = -1f
                        invalidate()
                    }
                })
                start()
            }
        }
    }

    /** Entry animation: the bar "pours" in from the side with a small overshoot. */
    fun playEnterAnimation() {
        animate().cancel()
        alpha = 0f
        translationX = 48f * density
        scaleX = 0.9f
        scaleY = 0.9f
        animate()
            .alpha(1f)
            .translationX(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(560)
            .setInterpolator(SpringInterpolator.SOFT)
            .start()
    }

    private companion object {
        const val RIM_WIDTH_DP = 1.1f
        const val ELEVATION_DP = 6f
        const val PRESSED_SCALE = 0.975f
        const val FOCUSED_SCALE = 1.015f
        const val CLEAR_PRESSED_SCALE = 0.82f
    }
}
