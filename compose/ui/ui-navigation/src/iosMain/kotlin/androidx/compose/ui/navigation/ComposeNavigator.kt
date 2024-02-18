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
import androidx.compose.runtime.mutableStateOf
import cocoapods.Topping.NavDestination
import cocoapods.Topping.NavOptions
import cocoapods.Topping.Navigator
import cocoapods.Topping.NavigatorExtrasProtocol



val ComposeNavigator.state : NavigatorState?
    get() {
        return getState() as NavigatorState
    }

val ComposeNavigatorNAME: String
    get() {
        return "composable"
    }

/**
 * Navigator that navigates through [Composable]s. Every destination using this Navigator must
 * set a valid [Composable] by setting it directly on an instantiated [Destination] or calling
 * [composable].
 */
public class ComposeNavigator : Navigator() {

    /**
     * Get the map of transitions currently in progress from the [state].
     */
    internal val transitionsInProgress get() = state?.transitionsInProgress

    /**
     * Get the back stack from the [state].
     */
    public val backStack get() = state?.backStack

    internal val isPop = mutableStateOf(false)

    override fun navigateWithEntries(
        entries: List<*>,
        navOptions: NavOptions?,
        navigatorExtras: NavigatorExtrasProtocol?
    ) {
        entries.forEach { entry ->
            state?.pushWithBackStackEntry(entry as cocoapods.Topping.NavBackStackEntry)
        }
        isPop.value = false
    }

    override fun createDestination(): Destination {
        return Destination(this) { }
    }

    override fun popBackStackWithPopUpTo(
        popUpTo: cocoapods.Topping.NavBackStackEntry,
        savedState: Boolean
    ) {
        state?.popWithTransitionWithPopUpTo(popUpTo, savedState)
        isPop.value = true
    }

    /**
     * Callback to mark a navigation in transition as complete.
     *
     * This should be called in conjunction with [navigate] and [popBackStack] as those
     * calls merely start a transition to the target destination, and requires manually marking
     * the transition as complete by calling this method.
     *
     * Failing to call this method could result in entries being prevented from reaching their
     * final [Lifecycle.State].
     */
    public fun onTransitionComplete(entry: PlatformNavBackStackEntry) {
        state?.markTransitionCompleteWithEntry(entry)
    }

    /**
     * NavDestination specific to [ComposeNavigator]
     */
    public class Destination(
        navigator: ComposeNavigator,
        internal val content:
        @Composable AnimatedContentScope.(PlatformNavBackStackEntry) -> Unit
    ) : NavDestination(navigator) {

        @Deprecated(
            message = "Deprecated in favor of Destination that supports AnimatedContent",
            level = DeprecationLevel.HIDDEN,
        )
        constructor(
            navigator: ComposeNavigator,
            content: @Composable (PlatformNavBackStackEntry) -> Unit
        ) : this(navigator, content = { entry -> content(entry) })

        internal var enterTransition: (
        AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?)? = null

        internal var exitTransition: (
        AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?)? = null

        internal var popEnterTransition: (
        AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> EnterTransition?)? = null

        internal var popExitTransition: (
        AnimatedContentTransitionScope<PlatformNavBackStackEntry>.() -> ExitTransition?)? = null
    }
}