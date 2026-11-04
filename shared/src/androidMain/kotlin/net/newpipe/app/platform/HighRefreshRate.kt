/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.platform

import android.app.Activity
import android.os.Build
import android.view.Display

/**
 * Asks the system to run this window at the highest refresh rate the display offers for its
 * current resolution (e.g. 90 or 120 Hz), so the spring animations are as fluid as possible.
 * It is only a preference: the system may still lower the rate (battery saver, thermals).
 */
fun Activity.requestHighRefreshRate() {
    val display = currentDisplay() ?: return
    val current = display.mode
    val best = display.supportedModes
        .filter {
            it.physicalWidth == current.physicalWidth &&
                it.physicalHeight == current.physicalHeight
        }
        .maxByOrNull { it.refreshRate } ?: return

    if (best.refreshRate > current.refreshRate) {
        val params = window.attributes
        params.preferredDisplayModeId = best.modeId
        window.attributes = params
    }
}

@Suppress("DEPRECATION")
private fun Activity.currentDisplay(): Display? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display
    } else {
        windowManager.defaultDisplay
    }
}
