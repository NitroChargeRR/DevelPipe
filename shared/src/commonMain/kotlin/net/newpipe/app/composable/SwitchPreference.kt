/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import net.newpipe.app.preview.ThemePreviewProvider
import net.newpipe.app.theme.appShapes
import net.newpipe.app.theme.glassPanel
import net.newpipe.app.theme.pressBounce

private const val DISABLED_ALPHA = 0.38f
private val SWITCH_END_PADDING = 16.dp

/** A preference row with a glass-styled, animated switch. */
@Composable
fun SwitchPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f),
                appShapes.medium
            )
            .glassPanel(appShapes.medium)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListItem(
            modifier = Modifier
                .weight(1f)
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange
                )
                .pressBounce(),
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = { Text(text = title) },
            supportingContent = summary?.let { text -> { Text(text = text) } }
        )
        Switch(
            modifier = Modifier
                .padding(end = SWITCH_END_PADDING)
                .clearAndSetSemantics {},
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@PreviewWrapper(ThemePreviewProvider::class)
@PreviewLightDark
@Composable
private fun SwitchPreferencePreview() {
    SwitchPreference(
        title = "Show playback tip",
        summary = "Explain the long-press action",
        checked = true,
        onCheckedChange = {}
    )
}
