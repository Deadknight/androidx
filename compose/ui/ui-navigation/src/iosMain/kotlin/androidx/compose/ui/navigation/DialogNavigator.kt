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

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import cocoapods.ToppingCompose.FloatingWindowProtocol
import cocoapods.ToppingCompose.NavDestination
import cocoapods.ToppingCompose.NavOptions
import cocoapods.ToppingCompose.Navigator
import cocoapods.ToppingCompose.NavigatorExtrasProtocol
import cocoapods.ToppingCompose.TNavigatorStateProtocol
import kotlinx.coroutines.flow.StateFlow

val DialogNavigatorName : String
    get() {
        return "dialog"
    }

public class DialogNavigator : Navigator() {

    /**
     * Get the back stack from the [state].
     */
    internal val backStack get() = (getState()!! as TNavigatorStateProtocol).getBackStack() as StateFlow<List<PlatformNavBackStackEntry>>

    /**
     * Dismiss the dialog destination associated with the given [backStackEntry].
     */
    internal fun dismiss(backStackEntry: PlatformNavBackStackEntry) {
        popBackStackWithPopUpTo(backStackEntry, false)
    }

    override fun navigateWithEntries(
        entries: List<*>,
        navOptions: NavOptions?,
        navigatorExtras: NavigatorExtrasProtocol?
    ) {
        entries.forEach { entry ->
            (getState()!! as TNavigatorStateProtocol).pushWithBackStackEntry(entry as cocoapods.ToppingCompose.NavBackStackEntry)
        }
    }

    override fun createDestination(): NavDestination {
        return Destination(this) { }
    }

    override fun popBackStackWithPopUpTo(
        popUpTo: cocoapods.ToppingCompose.NavBackStackEntry,
        savedState: Boolean
    ) {
        getState()?.popWithTransitionWithPopUpTo(popUpTo, savedState)
        // When popping, the incoming dialog is marked transitioning to hold it in
        // STARTED. With pop complete, we can remove it from transition so it can move to RESUMED.
        val popIndex = (getState()!! as NavController.NavControllerNavigatorState).transitionsInProgress.value.indexOf(popUpTo)
        // do not mark complete for entries up to and including popUpTo
        (getState()!! as NavController.NavControllerNavigatorState).transitionsInProgress.value.forEachIndexed { index, entry ->
            if (index > popIndex) onTransitionComplete(entry)
        }
    }

    internal fun onTransitionComplete(entry: PlatformNavBackStackEntry) {
        (getState()!! as NavController.NavControllerNavigatorState).markTransitionCompleteWithEntry(entry)
    }

    override fun getName(): String {
        return DialogNavigatorName
    }

    /**
     * NavDestination specific to [DialogNavigator]
     */
    public class Destination(
        navigator: DialogNavigator,
        internal val dialogProperties: DialogProperties = DialogProperties(),
        internal val content: @Composable (PlatformNavBackStackEntry) -> Unit
    ) : NavDestination(navigator), FloatingWindowProtocol
}