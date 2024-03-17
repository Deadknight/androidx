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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.get

@Composable
actual fun createNavGraphBuilder(
    navHostController: PlatformNavHostController,
    startDestination: String,
    route: String?
): PlatformNavGraphBuilder {
    return PlatformNavGraphBuilder(provider = navHostController.navHostController.navigatorProvider, startDestination = startDestination, route = route)
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
actual fun PlatformNavGraphBuilder.composable(
    route: String,
    arguments: List<PlatformNamedNavArgument>,
    deepLinks: List<PlatformNavDeepLink>,
    enterTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?)?,
    exitTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?)?,
    popEnterTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?)?,
    popExitTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?)?,
    content: @Composable AnimatedContentScope.(PlatformNavBackStackEntry) -> Unit
) {
    (getPlatform() as NavGraphBuilder).composable(route, arguments, deepLinks, enterTransition, exitTransition, popEnterTransition, popExitTransition, content)
}

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
actual fun PlatformNavGraphBuilder.navigation(
    startDestination: String,
    route: String,
    arguments: List<PlatformNamedNavArgument>,
    deepLinks: List<PlatformNavDeepLink>,
    enterTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?)?,
    exitTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?)?,
    popEnterTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?)?,
    popExitTransition: (AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?)?,
    builder: PlatformNavGraphBuilder.() -> Unit
) {
    addDestination(
        PlatformNavGraphBuilder((getPlatform() as NavGraphBuilder).provider, startDestination, route).apply(builder).build().apply {
            arguments.forEach { (argumentName, argument) ->
                addArgument(argumentName, argument)
            }
            deepLinks.forEach { deepLink ->
                addDeepLink(deepLink)
            }
            if (this is androidx.compose.ui.navigation.ComposeNavGraphNavigator.ComposeNavGraph) {
                this.enterTransition = enterTransition
                this.exitTransition = exitTransition
                this.popEnterTransition = popEnterTransition
                this.popExitTransition = popExitTransition
            }
        }
    )
}

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
actual fun PlatformNavGraphBuilder.dialog(
    route: String,
    arguments: List<PlatformNamedNavArgument>,
    deepLinks: List<PlatformNavDeepLink>,
    dialogProperties: PlatformDialogProperties,
    content: @Composable (PlatformNavBackStackEntry) -> Unit
) {
    addDestination(
        DialogNavigator.Destination(
            (getPlatform() as NavGraphBuilder).provider[DialogNavigator::class],
            dialogProperties.properties,
            content
        ).apply {
            this.route = route
            arguments.forEach { (argumentName, argument) ->
                addArgument(argumentName, argument)
            }
            deepLinks.forEach { deepLink ->
                addDeepLink(deepLink)
            }
        }
    )
}