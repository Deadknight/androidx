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

import android.content.Context
import android.os.Bundle
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavGraphNavigator
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.NavigatorProvider
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.navigation

/**
 * Provides in place in the Compose hierarchy for self contained navigation to occur.
 *
 * Once this is called, any Composable within the given [PlatformNavGraphBuilder] can be navigated to from
 * the provided [navController].
 *
 * @param navController the navController for this host
 * @param graph the graph for this host
 * @param modifier The modifier to be applied to the layout.
 * @param contentAlignment The [Alignment] of the [AnimatedContent]
 * @param enterTransition callback to define enter transitions for destination in this host
 * @param exitTransition callback to define exit transitions for destination in this host
 * @param popEnterTransition callback to define popEnter transitions for destination in this host
 * @param popExitTransition callback to define popExit transitions for destination in this host
 */
@Composable
actual fun PlatformNavHost(
    navController: PlatformNavHostController,
    graph: PlatformNavGraph,
    modifier: Modifier,
    contentAlignment: Alignment,
    enterTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition,
    exitTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition,
    popEnterTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition,
    popExitTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition
) {
    androidx.navigation.compose.NavHost(navController.navHostController, graph, modifier, contentAlignment, enterTransition, exitTransition, popEnterTransition, popExitTransition)
}

/**
 * Provides in place in the Compose hierarchy for self contained navigation to occur.
 *
 * Once this is called, any Composable within the given [PlatformNavGraphBuilder] can be navigated to from
 * the provided [navController].
 *
 * The graph passed into this method is [remember]ed. This means that for this PlatformNavHost, the graph
 * cannot be changed.
 *
 * @param navController the navController for this host
 * @param graph the graph for this host
 * @param modifier The modifier to be applied to the layout.
 */
/*@Composable
@Deprecated(
    message = "Deprecated in favor of PlatformNavHost that supports AnimatedContent",
    level = DeprecationLevel.HIDDEN
)
actual fun PlatformNavHost(
    navController: PlatformNavHostController,
    graph: PlatformNavGraph,
    modifier: Modifier
) {
    androidx.navigation.compose.NavHost(navController.navHostController, graph, modifier)
}*/

/**
 * Provides in place in the Compose hierarchy for self contained navigation to occur.
 *
 * Once this is called, any Composable within the given [PlatformNavGraphBuilder] can be navigated to from
 * the provided [navController].
 *
 * The builder passed into this method is [remember]ed. This means that for this PlatformNavHost, the
 * contents of the builder cannot be changed.
 *
 * @param navController the navController for this host
 * @param startDestination the route for the start destination
 * @param modifier The modifier to be applied to the layout.
 * @param contentAlignment The [Alignment] of the [AnimatedContent]
 * @param route the route for the graph
 * @param enterTransition callback to define enter transitions for destination in this host
 * @param exitTransition callback to define exit transitions for destination in this host
 * @param popEnterTransition callback to define popEnter transitions for destination in this host
 * @param popExitTransition callback to define popExit transitions for destination in this host
 * @param builder the builder used to construct the graph
 */
@Composable
actual fun PlatformNavHost(
    navController: PlatformNavHostController,
    startDestination: String,
    modifier: Modifier,
    contentAlignment: Alignment,
    route: String?,
    enterTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition,
    exitTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition,
    popEnterTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition,
    popExitTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition,
    builder: PlatformNavGraphBuilder.() -> Unit
) {
    androidx.navigation.compose.NavHost(
        navController.navHostController,
        remember(route, startDestination, builder) {
            navController.createGraph(startDestination, route, builder)
        },
        modifier,
        contentAlignment,
        enterTransition,
        exitTransition,
        popEnterTransition,
        popExitTransition
    )
}

/**
 * Provides in place in the Compose hierarchy for self contained navigation to occur.
 *
 * Once this is called, any Composable within the given [NavGraphBuilder] can be navigated to from
 * the provided [navController].
 *
 * The builder passed into this method is [remember]ed. This means that for this PlatformNavHost, the
 * contents of the builder cannot be changed.
 *
 * @sample androidx.navigation.compose.samples.NavScaffold
 *
 * @param navController the navController for this host
 * @param startDestination the route for the start destination
 * @param modifier The modifier to be applied to the layout.
 * @param route the route for the graph
 * @param builder the builder used to construct the graph
 */
/*@Composable
@Deprecated(
    message = "Deprecated in favor of PlatformNavHost that supports AnimatedContent",
    level = DeprecationLevel.HIDDEN
)
actual fun PlatformNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier,
    route: String?,
    builder: NavGraphBuilder.() -> Unit
) {
    androidx.navigation.compose.PlatformNavHost(
        navController,
        remember(route, startDestination, builder) {
            navController.createGraph(startDestination, route, builder)
        },
        modifier
    )
}*/
/**
 * Creates a NavHostController that handles the adding of the [ComposeNavigator] and
 * [DialogNavigator]. Additional [Navigator] instances can be passed through [navigators] to
 * be applied to the returned NavController. Note that each [Navigator] must be separately
 * remembered before being passed in here: any changes to those inputs will cause the
 * NavController to be recreated.
 *
 * @see PlatformNavHost
 */
@Composable
actual fun rememberNavController(vararg navigators: PlatformNavigator<out PlatformNavDestination>): PlatformNavHostController {
    val context = LocalContext.current
    return rememberSaveable(inputs = navigators, saver = PlatformNavControllerSaver(context)) {
        PlatformNavHostController(createNavController(context))
    }.apply {
        for (navigator in navigators) {
            navHostController.navigatorProvider.addNavigator(navigator.navigator)
        }
    }
}

internal class ComposeNavGraphNavigator(
    navigatorProvider: NavigatorProvider
) : NavGraphNavigator(navigatorProvider) {
    override fun createDestination(): NavGraph {
        return ComposeNavGraph(this)
    }

    internal class ComposeNavGraph(
        navGraphNavigator: Navigator<out NavGraph>
    ) : NavGraph(navGraphNavigator) {
        internal var enterTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null

        internal var exitTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null

        internal var popEnterTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null

        internal var popExitTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null
    }
}

private fun createNavController(context: Context) =
    NavHostController(context).apply {
        navigatorProvider.addNavigator(ComposeNavGraphNavigator(navigatorProvider))
        navigatorProvider.addNavigator(ComposeNavigator())
        navigatorProvider.addNavigator(DialogNavigator())
    }

private fun NavControllerSaver(
    context: Context
): Saver<NavHostController, *> = Saver<NavHostController, Bundle>(
    save = { it.saveState() },
    restore = { createNavController(context).apply { restoreState(it) } }
)

private fun PlatformNavControllerSaver(
    context: Context
): Saver<PlatformNavHostController, *> = Saver(
    save = { it.navHostController.saveState() },
    restore = { PlatformNavHostController(createNavController(context).apply { restoreState(it) }) }
)

public inline fun PlatformNavHostController.createGraph(
    startDestination: String,
    route: String? = null,
    builder: PlatformNavGraphBuilder.() -> Unit
): NavGraph = navHostController.navigatorProvider.navigation(startDestination, route, builder)

public inline fun NavigatorProvider.navigation(
    startDestination: String,
    route: String? = null,
    builder: PlatformNavGraphBuilder.() -> Unit
): NavGraph = PlatformNavGraphBuilder(this, startDestination, route).apply(builder)
    .build()