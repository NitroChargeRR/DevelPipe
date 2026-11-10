/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import net.newpipe.app.preview.ThemePreviewProvider
import net.newpipe.app.theme.GlassAlertDialog
import net.newpipe.app.theme.appShapes
import net.newpipe.app.theme.glassPanel
import net.newpipe.app.theme.pressBounce
import newpipe.shared.generated.resources.Res
import newpipe.shared.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource

private const val DISABLED_ALPHA = 0.38f
private const val ROW_PRESSED_SCALE = 0.985f

data class ListPreferenceEntry(val value: String, val title: String)

@Composable
fun ListPreference(
    title: String,
    entries: List<ListPreferenceEntry>,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    val effectiveSummary = summary ?: entries.firstOrNull { it.value == selectedValue }?.title

    ListItem(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .animateContentSize()
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f),
                appShapes.medium
            )
            .glassPanel(appShapes.medium)
            .clickable(enabled = enabled) { showDialog = true }
            .pressBounce(ROW_PRESSED_SCALE),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(text = title) },
        supportingContent = effectiveSummary?.let { text -> { Text(text = text) } }
    )

    if (showDialog) {
        GlassAlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = title) },
            text = {
                Column(modifier = Modifier.selectableGroup()) {
                    entries.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f),
                                    appShapes.small
                                )
                                .glassPanel(appShapes.small)
                                .selectable(
                                    selected = entry.value == selectedValue,
                                    role = Role.RadioButton,
                                    onClick = {
                                        onValueSelected(entry.value)
                                        showDialog = false
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = entry.value == selectedValue, onClick = null)
                            Text(text = entry.title, modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(Res.string.cancel))
                }
            }
        )
    }
}

@PreviewWrapper(ThemePreviewProvider::class)
@PreviewLightDark
@Composable
private fun ListPreferencePreview() {
    ListPreference(
        title = "Theme",
        entries = listOf(
            ListPreferenceEntry("light_theme", "Light"),
            ListPreferenceEntry("dark_theme", "Dark")
        ),
        selectedValue = "dark_theme",
        onValueSelected = {}
    )
}
