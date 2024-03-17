/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable

@Composable
expect fun createNavGraphBuilder(
    navHostController: PlatformNavHostController,
    startDestination: String,
    route: String? = null
): PlatformNavGraphBuilder

fun PlatformNavGraphBuilder.composable(
    route: String,
    content: @Composable AnimatedContentScope.(PlatformNavBackStackEntry) -> Unit
) {
    composable(route, emptyList(), emptyList(), null, null, null, null, content)
}

fun PlatformNavGraphBuilder.composable(
    route: String,
    arguments: List<PlatformNamedNavArgument>,
    content: @Composable AnimatedContentScope.(PlatformNavBackStackEntry) -> Unit
) {
    composable(route, arguments, emptyList(), null, null, null, null, content)
}

fun PlatformNavGraphBuilder.composable(
    route: String,
    arguments: List<PlatformNamedNavArgument>,
    enterTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?)?,
    exitTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?)?,
    popEnterTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?)?,
    popExitTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?)?,
    content: @Composable AnimatedContentScope.(PlatformNavBackStackEntry) -> Unit
) {
    composable(route, arguments, emptyList(), enterTransition, exitTransition, popEnterTransition, popExitTransition, content)
}

/**
 * Add the [Composable] to the [PlatformNavGraphBuilder]
 *
 * @param route route for the destination
 * @param arguments list of arguments to associate with destination
 * @param deepLinks list of deep links to associate with the destinations
 * @param enterTransition callback to determine the destination's enter transition
 * @param exitTransition callback to determine the destination's exit transition
 * @param popEnterTransition callback to determine the destination's popEnter transition
 * @param popExitTransition callback to determine the destination's popExit transition
 * @param content composable for the destination
 */
expect fun PlatformNavGraphBuilder.composable(
    route: String,
    arguments: List<PlatformNamedNavArgument>,
    deepLinks: List<PlatformNavDeepLink>,
    enterTransition: (
        AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?)?,
    exitTransition: (
        AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?)?,
    popEnterTransition: (
        AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?)?,
    popExitTransition: (
        AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?)?,
    content: @Composable AnimatedContentScope.(PlatformNavBackStackEntry) -> Unit
)

/**
 * Construct a nested [PlatformNavGraph]
 *
 * @param startDestination the starting destination's route for this NavGraph
 * @param route the destination's unique route
 * @param arguments list of arguments to associate with destination
 * @param deepLinks list of deep links to associate with the destinations
 * @param enterTransition callback to define enter transitions for destination in this NavGraph
 * @param exitTransition callback to define exit transitions for destination in this NavGraph
 * @param popEnterTransition callback to define pop enter transitions for destination in this
 * NavGraph
 * @param popExitTransition callback to define pop exit transitions for destination in this NavGraph
 * @param builder the builder used to construct the graph
 *
 * @return the newly constructed nested NavGraph
 */
expect fun PlatformNavGraphBuilder.navigation(
    startDestination: String,
    route: String,
    arguments: List<PlatformNamedNavArgument>,
    deepLinks: List<PlatformNavDeepLink>,
    enterTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?)?,
    exitTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?)?,
    popEnterTransition: (
        AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?
    )?,
    popExitTransition: (
        AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?
    )?,
    builder: PlatformNavGraphBuilder.() -> Unit
)

/**
 * Add the [Composable] to the [PlatformNavGraphBuilder] that will be hosted within a
 * [androidx.compose.ui.window.Dialog]. This is suitable only when this dialog represents
 * a separate screen in your app that needs its own lifecycle and saved state, independent
 * of any other destination in your navigation graph. For use cases such as `AlertDialog`,
 * you should use those APIs directly in the [composable] destination that wants to show that
 * dialog.
 *
 * @param route route for the destination
 * @param arguments list of arguments to associate with destination
 * @param deepLinks list of deep links to associate with the destinations
 * @param dialogProperties properties that should be passed to [androidx.compose.ui.window.Dialog].
 * @param content composable content for the destination that will be hosted within the Dialog
 */
expect fun PlatformNavGraphBuilder.dialog(
    route: String,
    arguments: List<PlatformNamedNavArgument>,
    deepLinks: List<PlatformNavDeepLink>,
    dialogProperties: PlatformDialogProperties,
    content: @Composable (PlatformNavBackStackEntry) -> Unit
)

fun PlatformNavGraphBuilder.dialog(
    route: String,
    content: @Composable (PlatformNavBackStackEntry) -> Unit
) {
    dialog(route, emptyList(), emptyList(), PlatformDialogProperties(), content)
}

fun PlatformNavGraphBuilder.dialog(
    route: String,
    arguments: List<PlatformNamedNavArgument>,
    content: @Composable (PlatformNavBackStackEntry) -> Unit
) {
    dialog(route, arguments, emptyList(), PlatformDialogProperties(), content)
}

fun PlatformNavGraphBuilder.dialog(
    route: String,
    arguments: List<PlatformNamedNavArgument>,
    dialogProperties: PlatformDialogProperties,
    content: @Composable (PlatformNavBackStackEntry) -> Unit
) {
    dialog(route, arguments, emptyList(), dialogProperties, content)
}