/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import net.newpipe.app.theme.AppMotion
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

/**
 * Navigation display for compose screens
 * @param startDestination Starting destination for the app
 * @param navigator Navigator to help with navigation, injected by Koin
 * @param onCloseRequest Callback to close the app
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun NavDisplay(
    startDestination: Destination,
    onCloseRequest: () -> Unit,
    navigator: Navigator = koinInject {
        parametersOf(startDestination, onCloseRequest)
    }
) {
    val backstack = rememberSerializable(serializer = SnapshotStateListSerializer<Destination>()) {
        navigator.backstack
    }

    NavDisplay(
        backStack = backstack,
        onBack = { navigator.navigateUp() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            (
                slideInHorizontally(animationSpec = pageSlideSpec()) { fullWidth -> fullWidth } +
                    fadeIn(animationSpec = pageFadeSpec())
                ) togetherWith (
                slideOutHorizontally(animationSpec = pageSlideSpec()) { fullWidth -> -fullWidth / PARALLAX_DIVISOR } +
                    fadeOut(animationSpec = pageFadeSpec())
                )
        },
        popTransitionSpec = {
            (
                slideInHorizontally(animationSpec = pageSlideSpec()) { fullWidth -> -fullWidth / PARALLAX_DIVISOR } +
                    fadeIn(animationSpec = pageFadeSpec())
                ) togetherWith (
                slideOutHorizontally(animationSpec = pageSlideSpec()) { fullWidth -> fullWidth } +
                    fadeOut(animationSpec = pageFadeSpec())
                )
        },
        entryProvider = koinEntryProvider()
    )
}

/** Screens glide in with a soft spring (fluid at any refresh rate, tiny settle at the end). */
private fun pageSlideSpec() = spring<IntOffset>(
    dampingRatio = AppMotion.SMOOTH_DAMPING,
    stiffness = AppMotion.SMOOTH_STIFFNESS,
    visibilityThreshold = IntOffset(1, 1)
)

private fun pageFadeSpec() = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium
)

private const val PARALLAX_DIVISOR = 4
