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
import androidx.annotation.CallSuper
import androidx.annotation.RestrictTo
import cocoapods.ToppingCompose.LuaBundle
import cocoapods.ToppingCompose.NavDestination
import cocoapods.ToppingCompose.TNavigatorStateProtocol
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.darwin.NSObject

/*public class NavigatorState : NSObject(), TNavigatorStateProtocol{
    private val backStackLock = ReentrantLock()
    private val _backStack: MutableStateFlow<List<PlatformNavBackStackEntry>> = MutableStateFlow(listOf())
    private val _transitionsInProgress: MutableStateFlow<Set<PlatformNavBackStackEntry>> =
        MutableStateFlow(setOf())

    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @set:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public var isNavigating = false

    /**
     * While the [NavController] is responsible for the combined back stack across all
     * Navigators, this back stack is specifically the set of destinations associated
     * with this Navigator.
     *
     * Changing the back stack must be done via [push] and [pop].
     */
    public val backStack: StateFlow<List<PlatformNavBackStackEntry>> = _backStack.asStateFlow()

    /**
     * This is the set of currently running transitions. Use this set to retrieve the entry and call
     * [markTransitionComplete] once the transition is complete.
     */
    public val transitionsInProgress: StateFlow<Set<PlatformNavBackStackEntry>> =
        _transitionsInProgress.asStateFlow()

    override fun createBackStackEntryWithDestination(
        destination: NavDestination,
        arguments: LuaBundle
    ): cocoapods.ToppingCompose.NavBackStackEntry {
        return PlatformNavBackStackEntry()
    }

    /**
     * Adds the given [backStackEntry] to the [backStack].
     */
    override fun pushWithBackStackEntry(backStackEntry: cocoapods.ToppingCompose.NavBackStackEntry) {
        backStackLock.withLock {
            _backStack.value = _backStack.value + backStackEntry
        }
    }

    /**
     * Adds the given [backStackEntry] to the [backStack]. This also adds the given and
     * previous entry to the [set of in progress transitions][transitionsInProgress].
     * Added entries have their [Lifecycle] capped at [Lifecycle.State.STARTED] until an entry is
     * passed into the [markTransitionComplete] callback, when they are allowed to go to
     * [Lifecycle.State.RESUMED].
     *
     * @see transitionsInProgress
     * @see markTransitionComplete
     * @see popWithTransition
     */
    override fun pushWithTransitionWithBackStackEntry(backStackEntry: cocoapods.ToppingCompose.NavBackStackEntry) {
        // When passed an entry that is already transitioning via a call to push, ignore the call
        // since we are already moving to the proper state.
        if (
            _transitionsInProgress.value.any { it === backStackEntry } &&
            backStack.value.any { it === backStackEntry }
        ) {
            return
        }
        val previousEntry = backStack.value.lastOrNull()
        // When navigating, we need to mark the outgoing entry as transitioning until it
        // finishes its outgoing animation.
        if (previousEntry != null) {
            _transitionsInProgress.value = _transitionsInProgress.value + previousEntry
        }
        _transitionsInProgress.value = _transitionsInProgress.value + backStackEntry
        pushWithBackStackEntry(backStackEntry)
    }

    /**
     * Create a new [PlatformNavBackStackEntry] from a given [destination] and [arguments].
     */
    public fun createBackStackEntry(
        destination: NavDestination,
        arguments: Bundle?
    ): PlatformNavBackStackEntry {
        return cocoapods.ToppingCompose.NavBackStackEntry()
    }

    override fun getBackStack(): Any {
        return backStack
    }

    /**
     * Pop all destinations up to and including [popUpTo]. This will remove those
     * destinations from the [backStack], saving their state if [saveState] is `true`.
     */
    override fun popWithPopUpTo(popUpTo: cocoapods.ToppingCompose.NavBackStackEntry, saveState: Boolean) {
        backStackLock.withLock {
            _backStack.value = _backStack.value.takeWhile { it != popUpTo }
        }
    }

    /**
     * Pops all destinations up to and including [popUpTo]. This also adds the given and
     * incoming entry to the [set of in progress transitions][transitionsInProgress]. Added
     * entries have their [Lifecycle] held at [Lifecycle.State.CREATED] until an entry is
     * passed into the [markTransitionComplete] callback, when they are allowed to go to
     * [Lifecycle.State.DESTROYED] and have their state cleared.
     *
     * This will remove those destinations from the [backStack], saving their state if
     * [saveState] is `true`.
     *
     * @see transitionsInProgress
     * @see markTransitionComplete
     * @see pushWithTransition
     */
    override fun popWithTransitionWithPopUpTo(
        popUpTo: cocoapods.ToppingCompose.NavBackStackEntry,
        saveState: Boolean
    ) {
        // When passed an entry that is already transitioning via a call to pop, ignore the call
        // since we are already moving to the proper state.
        if (
            _transitionsInProgress.value.any { it === popUpTo } &&
            backStack.value.none { it === popUpTo }
        ) {
            return
        }
        _transitionsInProgress.value = _transitionsInProgress.value + popUpTo
        val incomingEntry = backStack.value.lastOrNull { entry ->
            entry != popUpTo &&
                backStack.value.lastIndexOf(entry) < backStack.value.lastIndexOf(popUpTo)
        }
        // When popping, we need to mark the incoming entry as transitioning so we keep it
        // STARTED until the transition completes at which point we can move it to RESUMED
        if (incomingEntry != null) {
            _transitionsInProgress.value = _transitionsInProgress.value + incomingEntry
        }
        popWithPopUpTo(popUpTo, saveState)
    }

    /**
     * Informational callback indicating that the given [backStackEntry] has been
     * affected by a [NavOptions.shouldLaunchSingleTop] operation.
     *
     * Replaces the topmost entry with same id with the new [backStackEntry][PlatformNavBackStackEntry]
     *
     * @param [backStackEntry] the [PlatformNavBackStackEntry] to replace the old Entry
     * within the [backStack]
     */
    @CallSuper
    override fun onLaunchSingleTopWithBackStackEntry(backStackEntry: cocoapods.ToppingCompose.NavBackStackEntry) {
        // We update the back stack here because we don't want to leave it to the navigator since
        // it might be using transitions.
        backStackLock.withLock {
            val tempStack = backStack.value.toMutableList()
            tempStack.indexOfLast { it.getId() == backStackEntry.getId() }.let { idx ->
                tempStack[idx] = backStackEntry
            }
            _backStack.value = tempStack
        }
    }

    /**
     * Informational callback indicating that the given [backStackEntry] has been
     * affected by a [NavOptions.shouldLaunchSingleTop] operation. This also adds the given and
     * previous entry to the [set of in progress transitions][transitionsInProgress].
     * Added entries have their [Lifecycle] capped at [Lifecycle.State.STARTED] until an entry is
     * passed into the [markTransitionComplete] callback, when they are allowed to go to
     * [Lifecycle.State.RESUMED] while previous entries have their [Lifecycle] held at
     * [Lifecycle.State.CREATED] until an entry is passed into the [markTransitionComplete]
     * callback, when they are allowed to go to  [Lifecycle.State.DESTROYED] and have their state
     * cleared.
     *
     * Replaces the topmost entry with same id with the new [backStackEntry][PlatformNavBackStackEntry]
     *
     * @param [backStackEntry] the [PlatformNavBackStackEntry] to replace the old Entry
     * within the [backStack]
     */
    @CallSuper
    override fun onLaunchSingleTopWithTransitionWithBackStackEntry(backStackEntry: cocoapods.ToppingCompose.NavBackStackEntry) {
        val oldEntry = backStack.value.last { it.getId() == backStackEntry.getId() }
        _transitionsInProgress.value = _transitionsInProgress.value + oldEntry + backStackEntry
        onLaunchSingleTopWithBackStackEntry(backStackEntry)
    }

    /**
     * This removes the given [PlatformNavBackStackEntry] from the [set of the transitions in
     * progress][transitionsInProgress]. This should be called in conjunction with
     * [pushWithTransition] and [popWithTransition] as those call are responsible for adding
     * entries to [transitionsInProgress].
     *
     * This should also always be called in conjunction with [prepareForTransition] to ensure all
     * [NavBackStackEntries][PlatformNavBackStackEntry] settle into the proper state.
     *
     * Failing to call this method could result in entries being prevented from reaching their
     * final [Lifecycle.State]}.
     *
     * @see pushWithTransition
     * @see popWithTransition
     */
    override fun markTransitionCompleteWithEntry(entry: cocoapods.ToppingCompose.NavBackStackEntry) {
        _transitionsInProgress.value = _transitionsInProgress.value - entry
    }

    /**
     * This prepares the given [PlatformNavBackStackEntry] for transition. This should be called in
     * conjunction with [markTransitionComplete] as that is responsible for settling the
     * [PlatformNavBackStackEntry] into its final state.
     *
     * @see markTransitionComplete
     */
    override fun prepareForTransitionWithEntry(entry: cocoapods.ToppingCompose.NavBackStackEntry) {
        _transitionsInProgress.value = _transitionsInProgress.value + entry
    }
}*/