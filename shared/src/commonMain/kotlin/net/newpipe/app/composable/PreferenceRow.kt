/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import net.newpipe.app.preview.ThemePreviewProvider
import net.newpipe.app.theme.appShapes
import net.newpipe.app.theme.glassPanel
import net.newpipe.app.theme.pressBounce
import newpipe.shared.generated.resources.Res
import newpipe.shared.generated.resources.ic_headset
import newpipe.shared.generated.resources.settings_category_video_audio_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val DISABLED_ALPHA = 0.38f
private const val ROW_PRESSED_SCALE = 0.985f

/**
 * A preference row that uses the shared navy glass surface and spring press feedback.
 */
@Composable
fun PreferenceRow(
    title: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    summary: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .animateContentSize()
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f),
                appShapes.medium
            )
            .glassPanel(appShapes.medium)
            .clickable(enabled = enabled, onClick = onClick)
            .pressBounce(ROW_PRESSED_SCALE),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(text = title) },
        supportingContent = summary?.let { text -> { Text(text = text) } },
        leadingContent = icon?.let { painter ->
            { androidx.compose.material3.Icon(painter = painter, contentDescription = null) }
        }
    )
}

@PreviewWrapper(ThemePreviewProvider::class)
@PreviewLightDark
@Composable
private fun PreferenceRowPreview() {
    PreferenceRow(
        title = stringResource(Res.string.settings_category_video_audio_title),
        icon = painterResource(Res.drawable.ic_headset),
        summary = "Playback, audio and display preferences"
    )
}
