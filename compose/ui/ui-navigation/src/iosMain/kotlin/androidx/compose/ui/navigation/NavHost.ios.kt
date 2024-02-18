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

import Bundle
import Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalViewModelStoreOwner
import cocoapods.Topping.NavDestination
import kotlinx.coroutines.flow.map

private val NavDestination.navigatorName: String?
    get() {
        return mNavigatorName
    }

/**
 * Provides in place in the Compose hierarchy for self contained navigation to occur.
 *
 * Once this is called, any Composable within the given [NavGraphBuilder] can be navigated to from
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
actual fun NavHost(
    navController: PlatformNavHostController,
    graph: PlatformNavGraph,
    modifier: Modifier,
    contentAlignment: Alignment,
    enterTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition,
    exitTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition,
    popEnterTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition,
    popExitTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "NavHost requires a ViewModelStoreOwner to be provided via LocalViewModelStoreOwner"
    }

    // Intercept back only when there's a destination to pop
    val currentBackStack by remember(navController.navHostController.currentBackStack) {
        navController.navHostController.currentBackStack.map {
            it.filter { entry ->
                entry.destination.navigatorName == ComposeNavigatorNAME
            }
        }
    }.collectAsState(emptyList())
    /*BackHandler(currentBackStack.size > 1) {
        navController.popBackStack()
    }*/

    // Setup the navController with proper owners
    DisposableEffect(lifecycleOwner) {
        // Setup the navController with proper owners
        navController.navHostController.setLifecycleOwner(lifecycleOwner)
        onDispose { }
    }
    navController.navHostController.setViewModelStore(viewModelStoreOwner.getViewModelStore()!!)

    // Then set the graph
    navController.navHostController.graph = graph

    val saveableStateHolder = rememberSaveableStateHolder()

    // Find the ComposeNavigator, returning early if it isn't found
    // (such as is the case when using TestNavHostController)
    val composeNavigator = navController.navHostController.navigatorProvider.getNavigatorWithName(
        ComposeNavigatorNAME
    ) as? ComposeNavigator ?: return
    val visibleEntries by remember(navController.navHostController.visibleEntries) {
        navController.navHostController.visibleEntries.map {
            it.filter { entry ->
                entry.destination.navigatorName == ComposeNavigatorNAME
            }
        }
    }.collectAsState(emptyList())

    val backStackEntry: PlatformNavBackStackEntry? = if (LocalInspectionMode.current) {
        composeNavigator.backStack?.value?.lastOrNull()
    } else {
        visibleEntries.lastOrNull()
    }

    val zIndices = remember { mutableMapOf<String, Float>() }

    if (backStackEntry != null) {
        val finalEnter: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition = {
            val targetDestination = targetState.destination as ComposeNavigator.Destination

            if (composeNavigator.isPop.value) {
                targetDestination.hierarchy.firstNotNullOfOrNull { destination ->
                    destination.createPopEnterTransition(this)
                } ?: popEnterTransition.invoke(this)
            } else {
                targetDestination.hierarchy.firstNotNullOfOrNull { destination ->
                    destination.createEnterTransition(this)
                } ?: enterTransition.invoke(this)
            }
        }

        val finalExit: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition = {
            val initialDestination = initialState.destination as ComposeNavigator.Destination

            if (composeNavigator.isPop.value) {
                initialDestination.hierarchy.firstNotNullOfOrNull { destination ->
                    destination.createPopExitTransition(this)
                } ?: popExitTransition.invoke(this)
            } else {
                initialDestination.hierarchy.firstNotNullOfOrNull { destination ->
                    destination.createExitTransition(this)
                } ?: exitTransition.invoke(this)
            }
        }

        val transition = updateTransition(backStackEntry, label = "entry")
        transition.AnimatedContent(
            modifier,
            transitionSpec = {
                // If the initialState of the AnimatedContent is not in visibleEntries, we are in
                // a case where visible has cleared the old state for some reason, so instead of
                // attempting to animate away from the initialState, we skip the animation.
                if (initialState in visibleEntries) {
                    val initialZIndex = zIndices[initialState.getId()]
                        ?: 0f.also { zIndices[initialState.getId()] = 0f }
                    val targetZIndex = when {
                        targetState.getId() == initialState.getId() -> initialZIndex
                        composeNavigator.isPop.value -> initialZIndex - 1f
                        else -> initialZIndex + 1f
                    }.also { zIndices[targetState.getId()] = it }

                    ContentTransform(finalEnter(this), finalExit(this), targetZIndex)
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
            contentAlignment,
            contentKey = { it.getId() }
        ) {
            // In some specific cases, such as clearing your back stack by changing your
            // start destination, AnimatedContent can contain an entry that is no longer
            // part of visible entries since it was cleared from the back stack and is not
            // animating. In these cases the currentEntry will be null, and in those cases,
            // AnimatedContent will just skip attempting to transition the old entry.
            // See https://issuetracker.google.com/238686802
            val currentEntry = if (LocalInspectionMode.current) {
                // show startDestination if inspecting (preview)
                composeNavigator.backStack?.value
            } else {
                visibleEntries
            }?.lastOrNull { entry -> it == entry }

            // while in the scope of the composable, we provide the navBackStackEntry as the
            // ViewModelStoreOwner and LifecycleOwner
            currentEntry?.LocalOwnersProvider(saveableStateHolder) {
                (currentEntry.destination as ComposeNavigator.Destination)
                    .content(this, currentEntry)
            }
        }
        LaunchedEffect(transition.currentState, transition.targetState) {
            if (transition.currentState == transition.targetState) {
                visibleEntries.forEach { entry ->
                    composeNavigator.onTransitionComplete(entry)
                }
                zIndices
                    .filter { it.key != transition.targetState.getId() }
                    .forEach { zIndices.remove(it.key) }
            }
        }
    }

    val dialogNavigator = navController.navHostController.navigatorProvider.getNavigatorWithName(
        DialogNavigatorName
    ) as? DialogNavigator ?: return

    // Show any dialog destinations
    DialogHost(dialogNavigator)
}

/**
 * Provides in place in the Compose hierarchy for self contained navigation to occur.
 *
 * Once this is called, any Composable within the given [NavGraphBuilder] can be navigated to from
 * the provided [navController].
 *
 * The graph passed into this method is [remember]ed. This means that for this NavHost, the graph
 * cannot be changed.
 *
 * @param navController the navController for this host
 * @param graph the graph for this host
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
@Deprecated(
    message = "Deprecated in favor of NavHost that supports AnimatedContent",
    level = DeprecationLevel.HIDDEN
)
actual fun NavHost(
    navController: PlatformNavHostController,
    graph: PlatformNavGraph,
    modifier: Modifier
) = NavHost(navController, graph, modifier)

/**
 * Provides in place in the Compose hierarchy for self contained navigation to occur.
 *
 * Once this is called, any Composable within the given [NavGraphBuilder] can be navigated to from
 * the provided [navController].
 *
 * The builder passed into this method is [remember]ed. This means that for this NavHost, the
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
actual fun NavHost(
    navController: PlatformNavHostController,
    startDestination: String,
    modifier: Modifier,
    contentAlignment: Alignment,
    route: String?,
    enterTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition,
    exitTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition,
    popEnterTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition,
    popExitTransition: AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController,
        remember(route, startDestination, builder) {
            navController.navHostController.createGraph(startDestination, route, builder)
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
 * The builder passed into this method is [remember]ed. This means that for this NavHost, the
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
    message = "Deprecated in favor of NavHost that supports AnimatedContent",
    level = DeprecationLevel.HIDDEN
)
actual fun NavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier,
    route: String?,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController,
        remember(route, startDestination, builder) {
            navController.createGraph(startDestination, route, builder)
        },
        modifier
    )
}*/

private fun NavDestination.createEnterTransition(
    scope: AnimatedContentTransitionScope<PlatformNavBackStackEntry>
): EnterTransition? = when (this) {
    is ComposeNavigator.Destination -> this.enterTransition?.invoke(scope)
    is ComposeNavGraphNavigator.ComposeNavGraph -> this.enterTransition?.invoke(scope)
    else -> null
}

private fun NavDestination.createExitTransition(
    scope: AnimatedContentTransitionScope<PlatformNavBackStackEntry>
): ExitTransition? = when (this) {
    is ComposeNavigator.Destination -> this.exitTransition?.invoke(scope)
    is ComposeNavGraphNavigator.ComposeNavGraph -> this.exitTransition?.invoke(scope)
    else -> null
}

private fun NavDestination.createPopEnterTransition(
    scope: AnimatedContentTransitionScope<PlatformNavBackStackEntry>
): EnterTransition? = when (this) {
    is ComposeNavigator.Destination -> this.popEnterTransition?.invoke(scope)
    is ComposeNavGraphNavigator.ComposeNavGraph -> this.popEnterTransition?.invoke(scope)
    else -> null
}
private fun NavDestination.createPopExitTransition(
    scope: AnimatedContentTransitionScope<PlatformNavBackStackEntry>
): ExitTransition? = when (this) {
    is ComposeNavigator.Destination -> this.popExitTransition?.invoke(scope)
    is ComposeNavGraphNavigator.ComposeNavGraph -> this.popExitTransition?.invoke(scope)
    else -> null
}

/**
 * Creates a NavHostController that handles the adding of the [ComposeNavigator] and
 * [DialogNavigator]. Additional [Navigator] instances can be passed through [navigators] to
 * be applied to the returned NavController. Note that each [Navigator] must be separately
 * remembered before being passed in here: any changes to those inputs will cause the
 * NavController to be recreated.
 *
 * @see NavHost
 */
@Composable
actual fun rememberNavController(vararg navigators: PlatformNavigator<out PlatformNavDestination>): PlatformNavHostController {
    val context = LocalContext.current
    return rememberSaveable(inputs = navigators, saver = PlatformNavControllerSaver(context)) {
        PlatformNavHostController(createNavController(context))
    }.apply {
        for (navigator in navigators) {
            navHostController.navigatorProvider.addNavigatorWithNavigator(navigator.navigator)
        }
    }
}

private fun NavControllerSaver(
    context: Context
): Saver<NavHostController, *> = Saver<NavHostController, Bundle>(
    save = { it.saveState() },
    restore = { createNavController(context).apply { restoreState(it) } }
)

private fun createNavController(context: Context) =
    NavHostController(context).apply {
        navigatorProvider.addNavigatorWithNavigator(ComposeNavGraphNavigator(navigatorProvider))
        navigatorProvider.addNavigatorWithNavigator(ComposeNavigator())
        navigatorProvider.addNavigatorWithNavigator(DialogNavigator())
    }

private fun PlatformNavControllerSaver(
    context: Context
): Saver<PlatformNavHostController, *> = Saver(
    save = { it.navHostController.saveState() },
    restore = { PlatformNavHostController(createNavController(context).apply { restoreState(it) }) }
)