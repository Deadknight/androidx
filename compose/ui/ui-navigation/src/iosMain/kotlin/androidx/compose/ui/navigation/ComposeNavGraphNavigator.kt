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

import UUID
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import cocoapods.ToppingCompose.NavGraphNavigator
import cocoapods.ToppingCompose.NavigationProvider
import cocoapods.ToppingCompose.Navigator

/**
 * Custom subclass of [NavGraphNavigator] that adds support for defining
 * transitions at the navigation graph level.
 */
internal class ComposeNavGraphNavigator(
    navigatorProvider: NavigationProvider
) : NavGraphNavigator(navigatorProvider) {
    override fun createDestination(): PlatformNavGraph {
        return ComposeNavGraph(this)
    }

    override fun getName(): String {
        return "composeNavGraphNavigator"
    }

    internal class ComposeNavGraph(
        navGraphNavigator: Navigator
    ) : PlatformNavGraph(navGraphNavigator) {
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