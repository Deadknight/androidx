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

import Activity
import Bundle
import Context
import LifecycleOwner
import Parcelable
import UUID
import androidx.annotation.CallSuper
import androidx.annotation.RestrictTo
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toMutableList
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toStringArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKHKotlinArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinBooleanArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinByteArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinCharArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinDoubleArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinFloatArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinIntArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinLongArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinShortArray
import androidx.compose.ui.util.ID
import cocoapods.ToppingCompose.DeepLinkMatch
import cocoapods.ToppingCompose.LGNavigationParser
import cocoapods.ToppingCompose.Lifecycle
import cocoapods.ToppingCompose.LifecycleEvent
import cocoapods.ToppingCompose.LifecycleEventObserverProtocol
import cocoapods.ToppingCompose.LifecycleOwnerProtocol
import cocoapods.ToppingCompose.LifecycleState
import cocoapods.ToppingCompose.LuaBundle
import cocoapods.ToppingCompose.LuaFormIntent
import cocoapods.ToppingCompose.NavAction
import cocoapods.ToppingCompose.NavArgument
import cocoapods.ToppingCompose.NavBackStackEntry
import cocoapods.ToppingCompose.NavBackStackEntryState
import cocoapods.ToppingCompose.NavDestination
import cocoapods.ToppingCompose.NavGraph
import cocoapods.ToppingCompose.NavGraphNavigator
import cocoapods.ToppingCompose.NavOptions
import cocoapods.ToppingCompose.NavViewModelStoreProviderProtocol
import cocoapods.ToppingCompose.NavigationProvider
import cocoapods.ToppingCompose.Navigator
import cocoapods.ToppingCompose.NavigatorExtrasProtocol
import cocoapods.ToppingCompose.OnBackPressedCallback
import cocoapods.ToppingCompose.OnBackPressedDispatcher
import cocoapods.ToppingCompose.TNavigatorStateProtocol
import cocoapods.ToppingCompose.ViewModelStore
import cocoapods.ToppingCompose.ViewModelStoreOwnerProtocol
import currentState
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.asReversed
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.containsKey
import kotlin.collections.count
import kotlin.collections.emptyList
import kotlin.collections.filter
import kotlin.collections.filterNot
import kotlin.collections.first
import kotlin.collections.firstOrNull
import kotlin.collections.fold
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.getOrPut
import kotlin.collections.indexOfLast
import kotlin.collections.isNotEmpty
import kotlin.collections.iterator
import kotlin.collections.last
import kotlin.collections.lastIndex
import kotlin.collections.lastOrNull
import kotlin.collections.listOf
import kotlin.collections.listOfNotNull
import kotlin.collections.map
import kotlin.collections.mapNotNullTo
import kotlin.collections.maxWithOrNull
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.none
import kotlin.collections.plus
import kotlin.collections.plusAssign
import kotlin.collections.remove
import kotlin.collections.removeAll
import kotlin.collections.removeFirst
import kotlin.collections.removeLast
import kotlin.collections.reversed
import kotlin.collections.set
import kotlin.collections.toList
import kotlin.collections.toMutableList
import kotlin.collections.toTypedArray
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import lifecycle
import platform.Foundation.NSString
import platform.Foundation.NSUUID
import platform.Foundation.allKeys
import platform.darwin.NSObject
import putAll
import randomUUID

typealias Intent = LuaFormIntent

private val NavAction.defaultArguments: LuaBundle?
    get() {
        return mDefaultArguments
    }
private val NavAction.destinationId: String
    get() {
        return mDestinationId!!
    }
private val NavAction.navOptions: NavOptions?
    get() {
        return mNavOptions
    }
private val LifecycleEvent.targetState: LifecycleState
    get() {
        return Lifecycle.getTargetState(this)
    }
var cocoapods.ToppingCompose.NavBackStackEntry.maxLifecycle
    get() = getMaxLifecycle()
    set(value) {
        setMaxLifecycleWithMaxState(value)
    }

var cocoapods.ToppingCompose.NavBackStackEntry.destination : NavDestination
    get() = getDestination()
    set(value) {
        setMDestination(value)
    }

val NavDestination.hierarchy: Sequence<NavDestination>
    get() = generateSequence(this) { it.mParent }

fun NavDestination.Companion.createRoute(route: String) : String {
    return if (route != null) "android-app://androidx.navigation/$route" else ""
}

fun NavDestination.buildDeepLinkIds(previousDestination: NavDestination? = null): Array<String> {
    val hierarchy = ArrayDeque<NavDestination>()
    var current: NavDestination? = this
    do {
        val parent = current!!.mParent
        if (
        // If the current destination is a sibling of the previous, just add it straightaway
            previousDestination?.mParent != null &&
            previousDestination.mParent!!.findNode(current.idVal) === current
        ) {
            hierarchy.addFirst(current)
            break
        }
        if (parent == null || parent.mStartDestinationId != current.idVal) {
            hierarchy.addFirst(current)
        }
        if (parent == previousDestination) {
            break
        }
        current = parent
    } while (current != null)
    return hierarchy.toList().map { it.idVal!! }.toTypedArray()
}

fun PlatformNavGraph.findStartDestination(): NavDestination =
    generateSequence(findNode(mStartDestinationId)) {
        if (it is PlatformNavGraph) {
            it.findNode(it.mStartDestinationId)
        } else {
            null
        }
    }.last()

interface NavDirections {
    val actionId: ID
    val arguments: Bundle?
}

private const val TAG = "NavController"
private const val KEY_NAVIGATOR_STATE = "android-support-nav:controller:navigatorState"
private const val KEY_NAVIGATOR_STATE_NAMES =
    "android-support-nav:controller:navigatorState:names"
private const val KEY_BACK_STACK = "android-support-nav:controller:backStack"
private const val KEY_BACK_STACK_DEST_IDS =
    "android-support-nav:controller:backStackDestIds"
private const val KEY_BACK_STACK_IDS =
    "android-support-nav:controller:backStackIds"
private const val KEY_BACK_STACK_STATES_IDS =
    "android-support-nav:controller:backStackStates"
private const val KEY_BACK_STACK_STATES_PREFIX =
    "android-support-nav:controller:backStackStates:"
@field:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public const val KEY_DEEP_LINK_IDS: String = "android-support-nav:controller:deepLinkIds"
@field:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public const val KEY_DEEP_LINK_ARGS: String = "android-support-nav:controller:deepLinkArgs"
@field:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Suppress("IntentName")
public const val KEY_DEEP_LINK_EXTRAS: String =
    "android-support-nav:controller:deepLinkExtras"
@field:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public const val KEY_DEEP_LINK_HANDLED: String =
    "android-support-nav:controller:deepLinkHandled"

/**
 * The [Intent] that triggered a deep link to the current destination.
 */
public const val KEY_DEEP_LINK_INTENT: String =
    "android-support-nav:controller:deepLinkIntent"

private var deepLinkSaveState = true

public fun enableDeepLinkSaveState(saveState: Boolean) {
    deepLinkSaveState = saveState
}

public open class NavController(
    val context: Context
) {
    private var activity: Activity? = context.form

    private var inflater: LGNavigationParser? = null

    private var _graph: PlatformNavGraph? = null

    /**
     * The topmost navigation graph associated with this NavController.
     *
     * When this is set any current navigation graph data (including back stack) will be replaced.
     *
     * @see NavController.setGraph
     * @throws IllegalStateException if called before `setGraph()`.
     */
    public open var graph: PlatformNavGraph
        get() {
            checkNotNull(_graph) { "You must call setGraph() before calling getGraph()" }
            return _graph as PlatformNavGraph
        }
        set(graph) {
            setGraph(graph, null)
        }

    private var navigatorStateToRestore: Bundle? = null
    private var backStackToRestore: Array<NSObject>? = null
    private var deepLinkHandled = false

    internal val backQueue: ArrayDeque<PlatformNavBackStackEntry> = ArrayDeque()

    private val _currentBackStack: MutableStateFlow<List<PlatformNavBackStackEntry>> =
        MutableStateFlow(emptyList())

    /**
     * Retrieve the current back stack.
     *
     * @return The current back stack.
     */
    public val currentBackStack: StateFlow<List<PlatformNavBackStackEntry>> =
        _currentBackStack.asStateFlow()

    private val _visibleEntries: MutableStateFlow<List<PlatformNavBackStackEntry>> =
        MutableStateFlow(emptyList())

    /**
     * A [StateFlow] that will emit the currently visible [NavBackStackEntries][PlatformNavBackStackEntry]
     * whenever they change. If there is no visible [PlatformNavBackStackEntry], this will be set to an
     * empty list.
     *
     * - `CREATED` entries are listed first and include all entries that are in the process of
     * completing their exit transition. Note that this can include entries that have been
     * popped off the Navigation back stack.
     * - `STARTED` entries on the back stack are next and include all entries that are running
     * their enter transition and entries whose destination is partially covered by a
     * `FloatingWindow` destination
     * - The last entry in the list is the topmost entry in the back stack and is in the `RESUMED`
     * state only if its enter transition has completed. Otherwise it too will be `STARTED`.
     *
     * Note that the `Lifecycle` of any entry cannot be higher than the containing
     * Activity/Fragment - if the Activity is not `RESUMED`, no entry will be `RESUMED`, no matter
     * what the transition state is.
     */
    public val visibleEntries: StateFlow<List<PlatformNavBackStackEntry>> =
        _visibleEntries.asStateFlow()

    private val childToParentEntries = mutableMapOf<PlatformNavBackStackEntry, PlatformNavBackStackEntry>()
    private val parentToChildCount = mutableMapOf<PlatformNavBackStackEntry, AtomicInt>()

    private fun linkChildToParent(child: PlatformNavBackStackEntry, parent: PlatformNavBackStackEntry) {
        childToParentEntries[child] = parent
        if (parentToChildCount[parent] == null) {
            parentToChildCount[parent] = atomic(0)
        }
        parentToChildCount[parent]!!.incrementAndGet()
    }

    internal fun unlinkChildFromParent(child: PlatformNavBackStackEntry): PlatformNavBackStackEntry? {
        val parent = childToParentEntries.remove(child) ?: return null
        val count = parentToChildCount[parent]?.decrementAndGet()
        if (count == 0) {
            val navGraphNavigator: Navigator =
                _navigatorProvider.getNavigatorWithName(parent.getDestination().mNavigatorName.toString())
            navigatorState[navGraphNavigator]?.markTransitionCompleteWithEntry(parent)
            parentToChildCount.remove(parent)
        }
        return parent
    }

    private val backStackMap = mutableMapOf<String, String?>()
    private val backStackStates = mutableMapOf<String, ArrayDeque<NavBackStackEntryState>>()
    private var lifecycleOwner: LifecycleOwner? = null
    private var onBackPressedDispatcher: OnBackPressedDispatcher? = null
    private var viewModel: NavControllerViewModel? = null
    private val onDestinationChangedListeners = mutableListOf<OnDestinationChangedListener>()
    internal var hostLifecycleState: LifecycleState = LifecycleState.LIFECYCLESTATE_INITIALIZED
        get() {
            // A LifecycleOwner is not required by NavController.
            // In the cases where one is not provided, always keep the host lifecycle at CREATED
            return if (lifecycleOwner == null) {
                LifecycleState.LIFECYCLESTATE_CREATED
            } else {
                field
            }
        }

    private val lifecycleObserver = object : NSObject(), LifecycleEventObserverProtocol {
        val key = UUID.UUID().UUIDString
        override fun getKey(): String? {
            return key
        }

        override fun onStateChanged(source: LifecycleOwnerProtocol?, _1: LifecycleEvent) {
            hostLifecycleState = _1.targetState
            if (_graph != null) {
                for (entry in backQueue) {
                    entry.handleLifecycleEventWithEvent(_1)
                }
            }
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                popBackStack()
            }
        }
    private var enableOnBackPressedCallback = true

    /**
     * OnDestinationChangedListener receives a callback when the
     * [currentDestination] or its arguments change.
     */
    public fun interface OnDestinationChangedListener {
        /**
         * Callback for when the [currentDestination] or its arguments change.
         * This navigation may be to a destination that has not been seen before, or one that
         * was previously on the back stack. This method is called after navigation is complete,
         * but associated transitions may still be playing.
         *
         * @param controller the controller that navigated
         * @param destination the new destination
         * @param arguments the arguments passed to the destination
         */
        public fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
        )
    }

    private var _navigatorProvider = NavigationProvider()

    public open var navigatorProvider: NavigationProvider
        get() = _navigatorProvider
        /**
         */
        set(navigatorProvider) {
            check(backQueue.isEmpty()) { "NavigatorProvider must be set before setGraph call" }
            _navigatorProvider = navigatorProvider
        }

    private val navigatorState =
        mutableMapOf<Navigator, NavControllerNavigatorState>()
    private var addToBackStackHandler: ((backStackEntry: PlatformNavBackStackEntry) -> Unit)? = null
    private var popFromBackStackHandler: ((popUpTo: PlatformNavBackStackEntry) -> Unit)? = null
    private val entrySavedState = mutableMapOf<PlatformNavBackStackEntry, Boolean>()

    private fun Navigator.navigateInternal(
        entries: List<PlatformNavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: NavigatorExtrasProtocol?,
        handler: (backStackEntry: PlatformNavBackStackEntry) -> Unit = {}
    ) {
        addToBackStackHandler = handler
        println("navigateWithEntries " + this.getName())
        navigateWithEntries(entries, navOptions, navigatorExtras)
        addToBackStackHandler = null
    }

    private fun Navigator.popBackStackInternal(
        popUpTo: PlatformNavBackStackEntry,
        saveState: Boolean,
        handler: (popUpTo: PlatformNavBackStackEntry) -> Unit = {}
    ) {
        popFromBackStackHandler = handler
        popBackStackWithPopUpTo(popUpTo, saveState)
        popFromBackStackHandler = null
    }

    inner class NavControllerNavigatorState(
        val navigator: Navigator
    ) : NSObject(), TNavigatorStateProtocol {
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

        override fun pushWithBackStackEntry(backStackEntry: cocoapods.ToppingCompose.NavBackStackEntry) {
            val destinationNavigator: Navigator =
                _navigatorProvider.getNavigatorWithName(backStackEntry.getDestination().mNavigatorName.toString())
            if (destinationNavigator == navigator) {
                val handler = addToBackStackHandler
                if (handler != null) {
                    handler(backStackEntry)
                    addInternal(backStackEntry)
                } else {
                    // TODO handle the Navigator calling add() outside of a call to navigate()
                    println(
                        "Ignoring add of destination ${backStackEntry.getDestination().toString()} " +
                            "outside of the call to navigate(). "
                    )
                }
            } else {
                val navigatorBackStack = checkNotNull(navigatorState[destinationNavigator]) {
                    "NavigatorBackStack for ${backStackEntry.getDestination().mNavigatorName} should " +
                        "already be created"
                }
                navigatorBackStack.pushWithBackStackEntry(backStackEntry)
            }
        }

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

        fun createBackStackEntry(
            destination: NavDestination,
            arguments: Bundle?
        ): PlatformNavBackStackEntry {
            return NavBackStackEntry.create(context, destination, arguments)
        }

        fun superpushWithBackStackEntry(backStackEntry: cocoapods.ToppingCompose.NavBackStackEntry) {
            backStackLock.withLock {
                _backStack.value = _backStack.value + backStackEntry
                println("backstack size" + _backStack.value.size)
            }
        }

        fun addInternal(backStackEntry: PlatformNavBackStackEntry) {
            superpushWithBackStackEntry(backStackEntry)
        }

        override fun createBackStackEntryWithDestination(
            destination: NavDestination,
            arguments: LuaBundle?
        ): cocoapods.ToppingCompose.NavBackStackEntry {
            return NavBackStackEntry.create(context, destination, arguments, hostLifecycleState, viewModel)
        }

        override fun getBackStack(): Any {
            return backStack
        }

        fun superpopWithPopUpTo(
            popUpTo: cocoapods.ToppingCompose.NavBackStackEntry,
            saveState: Boolean
        ) {
            backStackLock.withLock {
                _backStack.value = _backStack.value.takeWhile { it != popUpTo }
            }
        }

        override fun popWithPopUpTo(
            popUpTo: cocoapods.ToppingCompose.NavBackStackEntry,
            saveState: Boolean
        ) {
            val destinationNavigator: Navigator =
                _navigatorProvider.getNavigatorWithName(popUpTo.getDestination().mNavigatorName.toString())
            if (destinationNavigator == navigator) {
                val handler = popFromBackStackHandler
                if (handler != null) {
                    handler(popUpTo)
                    superpopWithPopUpTo(popUpTo, saveState)
                } else {
                    popBackStackFromNavigator(popUpTo) {
                        superpopWithPopUpTo(popUpTo, saveState)
                    }
                }
            } else {
                navigatorState[destinationNavigator]!!.popWithPopUpTo(popUpTo, saveState)
            }
        }

        fun superpopWithTransitionWithPopUpTo(
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

        override fun popWithTransitionWithPopUpTo(
            popUpTo: cocoapods.ToppingCompose.NavBackStackEntry,
            saveState: Boolean
        ) {
            superpopWithTransitionWithPopUpTo(popUpTo, saveState)
            entrySavedState[popUpTo] = saveState
        }

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

        override fun onLaunchSingleTopWithTransitionWithBackStackEntry(backStackEntry: cocoapods.ToppingCompose.NavBackStackEntry) {
            val oldEntry = backStack.value.last { it.getId() == backStackEntry.getId() }
            _transitionsInProgress.value = _transitionsInProgress.value + oldEntry + backStackEntry
            onLaunchSingleTopWithBackStackEntry(backStackEntry)
        }

        fun supermarkTransitionCompleteWithEntry(entry: cocoapods.ToppingCompose.NavBackStackEntry) {
            _transitionsInProgress.value = _transitionsInProgress.value - entry
        }

        override fun markTransitionCompleteWithEntry(entry: cocoapods.ToppingCompose.NavBackStackEntry) {
            val savedState = entrySavedState[entry] == true
            supermarkTransitionCompleteWithEntry(entry)
            entrySavedState.remove(entry)
            if (!backQueue.contains(entry)) {
                unlinkChildFromParent(entry)
                // If the entry is no longer part of the backStack, we need to manually move
                // it to DESTROYED, and clear its view model
                if (entry.lifecycle!!.getCurrentState().isAtLeast(LifecycleState.LIFECYCLESTATE_CREATED)) {
                    entry.setMaxLifecycleWithMaxState(LifecycleState.LIFECYCLESTATE_DESTROYED)
                }
                if (backQueue.none { it.getId() == entry.getId() } && !savedState) {
                    viewModel?.clear(entry.getId())
                }
                updateBackStackLifecycle()
                // Nothing in backQueue changed, so unlike other places where
                // we change visibleEntries, we don't need to emit a new
                // currentBackStack
                _visibleEntries.tryEmit(populateVisibleEntries())
            } else if (!this@NavControllerNavigatorState.isNavigating) {
                updateBackStackLifecycle()
                _currentBackStack.tryEmit(backQueue.toMutableList())
                _visibleEntries.tryEmit(populateVisibleEntries())
            }
            // else, updateBackStackLifecycle() will be called after any ongoing navigate() call
            // completes
        }

        fun superprepareForTransitionWithEntry(entry: cocoapods.ToppingCompose.NavBackStackEntry) {
            _transitionsInProgress.value = _transitionsInProgress.value + entry
        }

        override fun prepareForTransitionWithEntry(entry: cocoapods.ToppingCompose.NavBackStackEntry) {
            superprepareForTransitionWithEntry(entry)
            if (backQueue.contains(entry)) {
                entry.setMaxLifecycleWithMaxState(LifecycleState.LIFECYCLESTATE_STARTED)
            } else {
                throw IllegalStateException("Cannot transition entry that is not in the back stack")
            }
        }
    }

    init {
        _navigatorProvider.addNavigatorWithNavigator(NavGraphNavigator(_navigatorProvider))
        //_navigatorProvider.addNavigator(ActivityNavigator(context))
    }

    /**
     * Adds an [OnDestinationChangedListener] to this controller to receive a callback
     * whenever the [currentDestination] or its arguments change.
     *
     * The current destination, if any, will be immediately sent to your listener.
     *
     * @param listener the listener to receive events
     */
    public open fun addOnDestinationChangedListener(listener: OnDestinationChangedListener) {
        onDestinationChangedListeners.add(listener)

        // Inform the new listener of our current state, if any
        if (backQueue.isNotEmpty()) {
            val backStackEntry = backQueue.last()
            listener.onDestinationChanged(
                this,
                backStackEntry.getDestination(),
                backStackEntry.getArguments()
            )
        }
    }

    /**
     * Removes an [OnDestinationChangedListener] from this controller.
     * It will no longer receive callbacks.
     *
     * @param listener the listener to remove
     */
    public open fun removeOnDestinationChangedListener(listener: OnDestinationChangedListener) {
        onDestinationChangedListeners.remove(listener)
    }
    
    public open fun popBackStack(): Boolean {
        return if (backQueue.isEmpty()) {
            // Nothing to pop if the back stack is empty
            false
        } else {
            popBackStackId(currentDestination!!.idVal!!, true)
        }
    }

    /**
     * Attempts to pop the controller's back stack back to a specific destination.
     *
     * @param destinationId The topmost destination to retain
     * @param inclusive Whether the given destination should also be popped.
     *
     * @return true if the stack was popped at least once and the user has been navigated to
     * another destination, false otherwise
     */
    
    public open fun popBackStackId(destinationId: String, inclusive: Boolean): Boolean {
        return popBackStackId(destinationId, inclusive, false)
    }
    
    public open fun popBackStackId(
        destinationId: String,
        inclusive: Boolean,
        saveState: Boolean
    ): Boolean {
        val popped = popBackStackInternalId(destinationId, inclusive, saveState)
        // Only return true if the pop succeeded and we've dispatched
        // the change to a new destination
        return popped && dispatchOnDestinationChanged()
    }
    
    public fun popBackStack(
        route: String,
        inclusive: Boolean,
        saveState: Boolean = false
    ): Boolean {
        val popped = popBackStackInternal(route, inclusive, saveState)
        // Only return true if the pop succeeded and we've dispatched
        // the change to a new destination
        return popped && dispatchOnDestinationChanged()
    }
    
    private fun popBackStackInternalId(
        destinationId: String,
        inclusive: Boolean,
        saveState: Boolean = false
    ): Boolean {
        if (backQueue.isEmpty()) {
            // Nothing to pop if the back stack is empty
            return false
        }
        val popOperations = mutableListOf<Navigator>()
        val iterator = backQueue.reversed().iterator()
        var foundDestination: NavDestination? = null
        while (iterator.hasNext()) {
            val destination = iterator.next().getDestination()
            val navigator = _navigatorProvider.getNavigatorWithName(
                destination.mNavigatorName.toString()
            )
            if (inclusive || destination.idVal!! != destinationId) {
                popOperations.add(navigator)
            }
            if (destination.idVal!! == destinationId) {
                foundDestination = destination
                break
            }
        }
        if (foundDestination == null) {
            // We were passed a destinationId that doesn't exist on our back stack.
            // Better to ignore the popBackStack than accidentally popping the entire stack
            /*val destinationName = NavDestination.getDisplayName(
                context, destinationId
            )
            Log.i(
                TAG,
                "Ignoring popBackStack to destination $destinationName as it was not found " +
                    "on the current back stack"
            )*/
            return false
        }
        return executePopOperations(popOperations, foundDestination, inclusive, saveState)
    }

    private fun popBackStackInternal(
        route: String,
        inclusive: Boolean,
        saveState: Boolean,
    ): Boolean {
        if (backQueue.isEmpty()) {
            // Nothing to pop if the back stack is empty
            return false
        }

        val popOperations = mutableListOf<Navigator>()
        val foundDestination = backQueue.lastOrNull { entry ->
            val hasRoute = entry.getDestination().hasRoute(route, entry.getArguments())
            if (inclusive || !hasRoute) {
                val navigator = _navigatorProvider.getNavigatorWithName(
                    entry.getDestination().mNavigatorName.toString()
                )
                popOperations.add(navigator)
            }
            hasRoute
        }?.getDestination()

        if (foundDestination == null) {
            // We were passed a route that doesn't exist on our back stack.
            // Better to ignore the popBackStack than accidentally popping the entire stack
            println(
                "Ignoring popBackStack to route $route as it was not found " +
                    "on the current back stack"
            )
            return false
        }
        return executePopOperations(popOperations, foundDestination, inclusive, saveState)
    }

    private fun executePopOperations(
        popOperations: List<Navigator>,
        foundDestination: NavDestination,
        inclusive: Boolean,
        saveState: Boolean,
    ): Boolean {
        var popped = false
        val savedState = ArrayDeque<NavBackStackEntryState>()
        for (navigator in popOperations) {
            var receivedPop = false
            navigator.popBackStackInternal(backQueue.last(), saveState) { entry ->
                receivedPop = true
                popped = true
                popEntryFromBackStack(entry, saveState, savedState)
            }
            if (!receivedPop) {
                // The pop did not complete successfully, so stop immediately
                break
            }
        }
        if (saveState) {
            if (!inclusive) {
                // If this isn't an inclusive pop, we need to explicitly map the
                // saved state to the destination you've actually passed to popUpTo
                // as well as its parents (if it is the start destination)
                generateSequence(foundDestination) { destination ->
                    if (destination.mParent?.mStartDestinationId == destination.idVal) {
                        destination.mParent
                    } else {
                        null
                    }
                }.takeWhile { destination ->
                    // Only add the state if it doesn't already exist
                    !backStackMap.containsKey(destination.idVal!!)
                }.forEach { destination ->
                    backStackMap[destination.idVal!!] = savedState.firstOrNull()?.getId()
                }
            }
            if (savedState.isNotEmpty()) {
                val firstState = savedState.first()
                // Whether is is inclusive or not, we need to map the
                // saved state to the destination that was popped
                // as well as its parents (if it is the start destination)
                val firstStateDestination = findDestinationId(firstState.getDestinationId())
                generateSequence(firstStateDestination) { destination ->
                    if (destination.mParent?.mStartDestinationId == destination.idVal) {
                        destination.mParent
                    } else {
                        null
                    }
                }.takeWhile { destination ->
                    // Only add the state if it doesn't already exist
                    !backStackMap.containsKey(destination.idVal)
                }.forEach { destination ->
                    backStackMap[destination.idVal!!] = firstState.getId()
                }
                // And finally, store the actual state itself
                backStackStates[firstState.getId()] = savedState
            }
        }
        updateOnBackPressedCallbackEnabled()
        return popped
    }

    internal fun popBackStackFromNavigator(popUpTo: PlatformNavBackStackEntry, onComplete: () -> Unit) {
        val popIndex = backQueue.indexOf(popUpTo)
        if (popIndex < 0) {
            println(
                "Ignoring pop of $popUpTo as it was not found on the current back stack"
            )
            return
        }
        if (popIndex + 1 != backQueue.size) {
            // There's other destinations stacked on top of this destination that
            // we need to pop first
            popBackStackInternalId(
                backQueue[popIndex + 1].getDestination().idVal!!,
                inclusive = true,
                saveState = false
            )
        }
        // Now record the pop of the actual entry - we don't use popBackStackInternal
        // here since we're being called from the Navigator already
        popEntryFromBackStack(popUpTo)
        onComplete()
        updateOnBackPressedCallbackEnabled()
        dispatchOnDestinationChanged()
    }

    private fun popEntryFromBackStack(
        popUpTo: PlatformNavBackStackEntry,
        saveState: Boolean = false,
        savedState: ArrayDeque<NavBackStackEntryState> = ArrayDeque()
    ) {
        val entry = backQueue.last()
        check(entry == popUpTo) {
            "Attempted to pop ${popUpTo.getDestination().toString()}, which is not the top of the back stack " +
                "(${entry.getDestination().toString()})"
        }
        backQueue.removeLast()
        val navigator = navigatorProvider
            .getNavigatorWithName(entry.getDestination().mNavigatorName.toString())
        val state = navigatorState[navigator]
        // If we pop an entry with transitions, but not the graph, we will not make a call to
        // popBackStackInternal, so the graph entry will not be marked as transitioning so we
        // need to check if it still has children.
        val transitioning = state?.transitionsInProgress?.value?.contains(entry) == true ||
            parentToChildCount.containsKey(entry)
        if (entry.lifecycle!!.currentState.isAtLeast(LifecycleState.LIFECYCLESTATE_CREATED)) {
            if (saveState) {
                // Move the state through STOPPED
                entry.maxLifecycle = LifecycleState.LIFECYCLESTATE_CREATED
                // Then save the state of the NavBackStackEntry
                savedState.addFirst(NavBackStackEntryState(entry))
            }
            if (!transitioning) {
                entry.maxLifecycle = LifecycleState.LIFECYCLESTATE_DESTROYED
                unlinkChildFromParent(entry)
            } else {
                entry.maxLifecycle = LifecycleState.LIFECYCLESTATE_CREATED
            }
        }
        if (!saveState && !transitioning) {
            viewModel?.clear(entry.getId())
        }
    }

    /**
     * Clears any saved state associated with [route] that was previously saved
     * via [popBackStack] when using a `saveState` value of `true`.
     *
     * @param route The route of the destination previously used with [popBackStack] with a
     * `saveState` value of `true`. May contain filled in arguments as long as
     * it is exact match with route used with [popBackStack].
     *
     * @return true if the saved state of the stack associated with [route] was cleared.
     */
    
    public fun clearBackStack(route: String): Boolean {
        val cleared = clearBackStackInternal(route)
        // Only return true if the clear succeeded and we've dispatched
        // the change to a new destination
        return cleared && dispatchOnDestinationChanged()
    }

    /**
     * Clears any saved state associated with [destinationId] that was previously saved
     * via [popBackStack] when using a `saveState` value of `true`.
     *
     * @param destinationId The ID of the destination previously used with [popBackStack] with a
     * `saveState`value of `true`
     *
     * @return true if the saved state of the stack associated with [destinationId] was cleared.
     */
    
    public fun clearBackStackId(destinationId: String): Boolean {
        val cleared = clearBackStackInternalId(destinationId)
        // Only return true if the clear succeeded and we've dispatched
        // the change to a new destination
        return cleared && dispatchOnDestinationChanged()
    }

    
    private fun clearBackStackInternalId(destinationId: String): Boolean {
        navigatorState.values.forEach { state ->
            state.isNavigating = true
        }
        val restored = restoreStateInternalId(destinationId, null,
            navOptions { restoreState = true }, null)
        navigatorState.values.forEach { state ->
            state.isNavigating = false
        }
        return restored && popBackStackInternalId(destinationId, inclusive = true, saveState = false)
    }

    
    private fun clearBackStackInternal(route: String): Boolean {
        navigatorState.values.forEach { state ->
            state.isNavigating = true
        }
        val restored = restoreStateInternal(route)
        navigatorState.values.forEach { state ->
            state.isNavigating = false
        }
        return restored && popBackStackInternal(route, inclusive = true, saveState = false)
    }
    
    public open fun navigateUp(): Boolean {
        // If there's only one entry, then we may have deep linked into a specific destination
        // on another task.
        if (destinationCountOnBackStack == 1) {
            val extras = activity?.intent?.getBundle()
            if (extras?.getIntArray(KEY_DEEP_LINK_IDS) != null) {
                return tryRelaunchUpToExplicitStack()
            } else {
                return tryRelaunchUpToGeneratedStack()
            }
        } else {
            return popBackStack()
        }
    }

    /** Starts a new Activity directed to the next-upper Destination in the explicit deep link
     * stack used to start this Activity. Returns false if
     * the current destination was already the root of the deep link.
     */
    @Suppress("DEPRECATION")
    private fun tryRelaunchUpToExplicitStack(): Boolean {
        if (!deepLinkHandled) {
            return false
        }

        val intent = activity!!.intent
        val extras = intent!!.getBundle()

        val deepLinkIds: MutableList<String> = extras!!.getStringArray(KEY_DEEP_LINK_IDS)!!.toMutableList()
        val deepLinkArgs: MutableList<Bundle>? = extras.getArray(KEY_DEEP_LINK_ARGS)?.toMutableList()

        // Remove the leaf destination to pop up to one level above it
        var leafDestinationId = deepLinkIds.removeLast()
        deepLinkArgs?.removeLast()

        // Probably deep linked to a single destination only.
        if (deepLinkIds.isEmpty()) {
            return false
        }

        // Find the destination if the leaf destination was a NavGraph
        with(graph.findDestinationId(leafDestinationId)) {
            if (this is PlatformNavGraph) {
                leafDestinationId = this.findStartDestination().idVal!!
            }
        }

        // The final element of the deep link couldn't have brought us to the current location.
        if (leafDestinationId != currentDestination?.idVal) {
            return false
        }

        val navDeepLinkBuilder = createDeepLink()

        // Attach the original global arguments, and also the original calling Intent.
        val arguments = bundleOf(KEY_DEEP_LINK_INTENT to intent)
        extras.getBundle(KEY_DEEP_LINK_EXTRAS)?.let {
            arguments.putAll(it)
        }
        navDeepLinkBuilder.setArguments(arguments)

        deepLinkIds.forEachIndexed { index, deepLinkId ->
            navDeepLinkBuilder.addDestination(deepLinkId, deepLinkArgs?.get(index))
        }

        //TODO
        //navDeepLinkBuilder.createTaskStackBuilder().startActivities()
        activity?.close()
        return true
    }

    /**
     * Starts a new Activity directed to the parent of the current Destination. Returns false if
     * the current destination was already the root of the deep link.
     */
    private fun tryRelaunchUpToGeneratedStack(): Boolean {
        //TODO
        /*val currentDestination = currentDestination
        var destId = currentDestination!!.idVal
        var parent = currentDestination.mParent
        while (parent != null) {
            if (parent.mStartDestinationId != destId) {
                val args = LuaBundle()
                if (activity != null && activity!!.intent != null) {
                    val data = null//activity!!.intent.data

                    // We were started via a URI intent.
                    if (data != null) {
                        // Include the original deep link Intent so the Destinations can
                        // synthetically generate additional arguments as necessary.
                        args.putObject(
                            KEY_DEEP_LINK_INTENT,
                            activity!!.intent
                        )
                        val matchingDeepLink = _graph!!.matchDeepLink(
                            NavDeepLinkRequest(activity!!.intent)
                        )
                        if (matchingDeepLink?.matchingArgs != null) {
                            val destinationArgs = matchingDeepLink.destination.addInDefaultArgs(
                                matchingDeepLink.matchingArgs
                            )
                            args.putAll(destinationArgs)
                        }
                    }
                }
                val parentIntents = NavDeepLinkBuilder(this)
                    .setDestination(parent.idVal)
                    .setArguments(args)
                    .createTaskStackBuilder()
                parentIntents.startActivities()
                activity?.close()
                return true
            }
            destId = parent.idVal
            parent = parent.mParent
        }*/
        return false
    }

    /**
     * Gets the number of non-NavGraph destinations on the back stack
     */
    private val destinationCountOnBackStack: Int
        get() = backQueue.count { entry ->
            entry.getDestination() !is PlatformNavGraph
        }

    private var dispatchReentrantCount = 0
    private val backStackEntriesToDispatch = mutableListOf<PlatformNavBackStackEntry>()

    /**
     * Dispatch changes to all OnDestinationChangedListeners.
     *
     * If the back stack is empty, no events get dispatched.
     *
     * @return If changes were dispatched.
     */
    private fun dispatchOnDestinationChanged(): Boolean {
        // We never want to leave NavGraphs on the top of the stack
        while (!backQueue.isEmpty() && backQueue.last().getDestination() is PlatformNavGraph) {
            popEntryFromBackStack(backQueue.last())
        }
        val lastBackStackEntry = backQueue.lastOrNull()
        if (lastBackStackEntry != null) {
            backStackEntriesToDispatch += lastBackStackEntry
        }
        // Track that we're updating the back stack lifecycle
        // just in case updateBackStackLifecycle() results in
        // additional calls to navigate() or popBackStack()
        dispatchReentrantCount++
        updateBackStackLifecycle()
        dispatchReentrantCount--

        if (dispatchReentrantCount == 0) {
            // Only the outermost dispatch should dispatch
            val dispatchList = backStackEntriesToDispatch.toMutableList()
            backStackEntriesToDispatch.clear()
            for (backStackEntry in dispatchList) {
                // Now call all registered OnDestinationChangedListener instances
                for (listener in onDestinationChangedListeners) {
                    listener.onDestinationChanged(
                        this,
                        backStackEntry.getDestination(),
                        backStackEntry.getArguments()
                    )
                }
                _currentBackStackEntryFlow.tryEmit(backStackEntry)
            }
            _currentBackStack.tryEmit(backQueue.toMutableList())
            _visibleEntries.tryEmit(populateVisibleEntries())
        }
        return lastBackStackEntry != null
    }

    internal fun updateBackStackLifecycle() {
        // Operate on a copy of the queue to avoid issues with reentrant
        // calls if updating the Lifecycle calls navigate() or popBackStack()
        val backStack = backQueue.toMutableList()
        if (backStack.isEmpty()) {
            // Nothing to update
            return
        }
        // First determine what the current resumed destination is and, if and only if
        // the current resumed destination is a FloatingWindow, what destinations are
        // underneath it that must remain started.
        var nextResumed: NavDestination? = backStack.last().getDestination()
        val nextStarted: MutableList<NavDestination> = mutableListOf()
        /*if (nextResumed is FloatingWindow) {
            // Find all visible destinations in the back stack as they
            // should still be STARTED when the FloatingWindow destination is above it.
            val iterator = backStack.reversed().iterator()
            while (iterator.hasNext()) {
                val destination = iterator.next().destination
                // Add all visible destinations (e.g., FloatingWindow destinations, their
                // NavGraphs, and the screen directly below all FloatingWindow destinations)
                // to nextStarted
                nextStarted.add(destination)
                // break if we find first visible screen
                if (destination !is FloatingWindow && destination !is NavGraph) {
                    break
                }
            }
        }*/
        // First iterate downward through the stack, applying downward Lifecycle
        // transitions and capturing any upward Lifecycle transitions to apply afterwards.
        // This ensures proper nesting where parent navigation graphs are started before
        // their children and stopped only after their children are stopped.
        val upwardStateTransitions = HashMap<PlatformNavBackStackEntry, LifecycleState>()
        var iterator = backStack.reversed().iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val currentMaxLifecycle = entry.maxLifecycle
            val destination = entry.destination
            if (nextResumed != null && destination.idVal == nextResumed.idVal) {
                // Upward Lifecycle transitions need to be done afterwards so that
                // the parent navigation graph is resumed before their children
                if (currentMaxLifecycle != LifecycleState.LIFECYCLESTATE_RESUMED) {
                    val navigator = navigatorProvider
                        .getNavigatorWithName(entry.destination.mNavigatorName.toString())
                    val state = navigatorState[navigator]
                    val transitioning = state?.transitionsInProgress?.value?.contains(entry)
                    if (transitioning != true && parentToChildCount[entry]?.value != 0) {
                        upwardStateTransitions[entry] = LifecycleState.LIFECYCLESTATE_RESUMED
                    } else {
                        upwardStateTransitions[entry] = LifecycleState.LIFECYCLESTATE_STARTED
                    }
                }
                if (nextStarted.firstOrNull()?.idVal == destination.idVal) nextStarted.removeFirst()
                nextResumed = nextResumed.mParent
            } else if (nextStarted.isNotEmpty() && destination.idVal == nextStarted.first().idVal) {
                val started = nextStarted.removeFirst()
                if (currentMaxLifecycle == LifecycleState.LIFECYCLESTATE_RESUMED) {
                    // Downward transitions should be done immediately so children are
                    // paused before their parent navigation graphs
                    entry.maxLifecycle = LifecycleState.LIFECYCLESTATE_STARTED
                } else if (currentMaxLifecycle != LifecycleState.LIFECYCLESTATE_STARTED) {
                    // Upward Lifecycle transitions need to be done afterwards so that
                    // the parent navigation graph is started before their children
                    upwardStateTransitions[entry] = LifecycleState.LIFECYCLESTATE_STARTED
                }
                started.mParent?.let {
                    if (!nextStarted.contains(it)) { nextStarted.add(it) }
                }
            } else {
                entry.maxLifecycle = LifecycleState.LIFECYCLESTATE_CREATED
            }
        }
        // Apply all upward Lifecycle transitions by iterating through the stack again,
        // this time applying the new lifecycle to the parent navigation graphs first
        iterator = backStack.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val newState = upwardStateTransitions[entry]
            if (newState != null) {
                entry.maxLifecycle = newState
            } else {
                // Ensure the state is up to date
                entry.updateState()
            }
        }
    }

    internal fun populateVisibleEntries(): List<PlatformNavBackStackEntry> {
        val entries = mutableListOf<PlatformNavBackStackEntry>()
        // Add any transitioning entries that are not at least STARTED
        navigatorState.values.forEach { state ->
            entries += state.transitionsInProgress.value.filter { entry ->
                !entries.contains(entry) &&
                    !entry.maxLifecycle.isAtLeast(LifecycleState.LIFECYCLESTATE_STARTED)
            }
        }
        // Add any STARTED entries from the backQueue. This will include the topmost
        // non-FloatingWindow destination plus every FloatingWindow destination above it.
        entries += backQueue.filter { entry ->
            !entries.contains(entry) &&
                entry.maxLifecycle.isAtLeast(LifecycleState.LIFECYCLESTATE_STARTED)
        }
        return entries.filter {
            it.destination !is PlatformNavGraph
        }
    }

    public open val navInflater: LGNavigationParser by lazy {
        inflater ?: LGNavigationParser.getInstance()!!
    }

    /**
     * Sets the [navigation graph][PlatformNavGraph] to the specified resource.
     * Any current navigation graph data (including back stack) will be replaced.
     *
     * The inflated graph can be retrieved via [graph].
     *
     * @param graphResId resource id of the navigation graph to inflate
     *
     * @see NavController.navInflater
     * @see NavController.setGraph
     * @see NavController.graph
     */
    
    @CallSuper
    public open fun setGraph(graphResId: String) {
        setGraph(navInflater.getNavigationProvider(graphResId, navigatorProvider)!!, null)
    }

    /**
     * Sets the [navigation graph][PlatformNavGraph] to the specified resource.
     * Any current navigation graph data (including back stack) will be replaced.
     *
     * The inflated graph can be retrieved via [graph].
     *
     * @param graphResId resource id of the navigation graph to inflate
     * @param startDestinationArgs arguments to send to the start destination of the graph
     *
     * @see NavController.navInflater
     * @see NavController.setGraph
     * @see NavController.graph
     */
    
    @CallSuper
    public open fun setGraph(graphResId: String, startDestinationArgs: Bundle?) {
        setGraph(navInflater.getNavigationProvider(graphResId, this.navigatorProvider)!!, startDestinationArgs)
    }

    /**
     * Sets the [navigation graph][PlatformNavGraph] to the specified graph.
     * Any current navigation graph data (including back stack) will be replaced.
     *
     * The graph can be retrieved later via [graph].
     *
     * @param graph graph to set
     * @see NavController.setGraph
     * @see NavController.graph
     */
    
    @CallSuper
    public open fun setGraph(graph: PlatformNavGraph, startDestinationArgs: Bundle?) {
        if (_graph != graph) {
            _graph?.let { previousGraph ->
                // Clear all saved back stacks by iterating through a copy of the saved keys,
                // thus avoiding any concurrent modification exceptions
                val savedBackStackIds = ArrayList(backStackMap.keys)
                savedBackStackIds.forEach { id ->
                    clearBackStackInternalId(id)
                }
                // Pop everything from the old graph off the back stack
                popBackStackInternalId(previousGraph.idVal!!, true)
            }
            _graph = graph
            onGraphCreated(startDestinationArgs)
        } else {
            // first we update _graph with new instances from graph
            val keys = graph.mNodes?.allKeys
            for (i in 0 until (graph.mNodes?.count?.toInt() ?: 0)) {
                val key = keys?.get(i)
                val newDestination = graph.mNodes?.objectForKey(key)
                //TODO
                _graph?.mNodes?.setObject(newDestination, key as NSString)
            }
            // then we update backstack with the new instances
            backQueue.forEach { entry ->
                // we will trace this hierarchy in new graph to get new destination instance
                val hierarchy = entry.destination.hierarchy.toList().asReversed()
                val newDestination = hierarchy.fold(_graph!!) {
                        newDest: NavDestination, oldDest: NavDestination ->
                    if (oldDest == _graph && newDest == graph) {
                        // if root graph, it is already the node that matches with oldDest
                        newDest
                    } else if (newDest is PlatformNavGraph) {
                        // otherwise we walk down the hierarchy to the next child
                        newDest.findNode(oldDest.idVal)!!
                    } else {
                        // final leaf node found
                        newDest
                    }
                }
                entry.destination = newDestination
            }
        }
    }

    
    private fun onGraphCreated(startDestinationArgs: Bundle?) {
        navigatorStateToRestore?.let { navigatorStateToRestore ->
            val navigatorNames = navigatorStateToRestore.getStringArray(
                KEY_NAVIGATOR_STATE_NAMES
            )?.toStringArray()
            if (navigatorNames != null) {
                for (name in navigatorNames) {
                    val navigator = _navigatorProvider.getNavigatorWithName(name)
                    val bundle = navigatorStateToRestore.getBundle(name)
                    if (bundle != null) {
                        navigator.onRestoreStateWithSavedState(bundle)
                    }
                }
            }
        }
        backStackToRestore?.let { backStackToRestore ->
            for (parcelable in backStackToRestore) {
                val state = parcelable as NavBackStackEntryState
                val node = findDestinationId(state.getDestinationId())
                if (node == null) {
                    /*val dest = NavDestination.getDisplayName(
                        context,
                        state.getDestinationId()
                    )*/
                    throw IllegalStateException(
                        "Restoring the Navigation back stack failed: destination ${state.getDestinationId()} cannot be " +
                            "found from the current destination $currentDestination"
                    )
                }
                val entry = state.instantiate(context, node, hostLifecycleState, viewModel)
                val navigator = _navigatorProvider.getNavigatorWithName(node.mNavigatorName!!)
                val navigatorBackStack = navigatorState.getOrPut(navigator) {
                    NavControllerNavigatorState(navigator)
                }
                backQueue.add(entry)
                navigatorBackStack.addInternal(entry)
                val parent = entry.destination.mParent
                if (parent != null) {
                    linkChildToParent(entry, getBackStackEntryId(parent.idVal!!))
                }
            }
            updateOnBackPressedCallbackEnabled()
            this.backStackToRestore = null
        }
        // Mark all Navigators as attached
        (_navigatorProvider.getNavigators() as Map<String, Navigator>).values.filterNot { it.isAttached() }.forEach { navigator ->
            val navigatorBackStack = navigatorState.getOrPut(navigator) {
                NavControllerNavigatorState(navigator)
            }
            navigator.onAttachWithState(navigatorBackStack)
        }
        if (_graph != null && backQueue.isEmpty()) {
            val deepLinked =
                !deepLinkHandled && activity != null && handleDeepLink(activity!!.intent)
            if (!deepLinked) {
                // Navigate to the first destination in the graph
                // if we haven't deep linked to a destination
                navigate(_graph!!, startDestinationArgs, null, null)
            }
        } else {
            dispatchOnDestinationChanged()
        }
    }
    
    @Suppress("DEPRECATION")
    public open fun handleDeepLink(intent: Intent?): Boolean {
        /*if (intent == null) {
            return false
        }
        val extras = intent.getBundle()
        var deepLink = try {
            extras?.getIntArray(KEY_DEEP_LINK_IDS)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "handleDeepLink() could not extract deepLink from $intent",
                e
            )
            null
        }
        var deepLinkArgs = extras?.getParcelableArrayList<Bundle>(KEY_DEEP_LINK_ARGS)
        val globalArgs = Bundle()
        val deepLinkExtras = extras?.getBundle(KEY_DEEP_LINK_EXTRAS)
        if (deepLinkExtras != null) {
            globalArgs.putAll(deepLinkExtras)
        }
        if (deepLink == null || deepLink.isEmpty()) {
            val matchingDeepLink = _graph!!.matchDeepLink(NavDeepLinkRequest(intent))
            if (matchingDeepLink != null) {
                val destination = matchingDeepLink.destination
                deepLink = destination.buildDeepLinkIds()
                deepLinkArgs = null
                val destinationArgs = destination.addInDefaultArgs(matchingDeepLink.matchingArgs)
                if (destinationArgs != null) {
                    globalArgs.putAll(destinationArgs)
                }
            }
        }
        if (deepLink == null || deepLink.isEmpty()) {
            return false
        }
        val invalidDestinationDisplayName = findInvalidDestinationDisplayNameInDeepLink(deepLink)
        if (invalidDestinationDisplayName != null) {
            Log.i(
                TAG,
                "Could not find destination $invalidDestinationDisplayName in the " +
                    "navigation graph, ignoring the deep link from $intent"
            )
            return false
        }
        globalArgs.putParcelable(KEY_DEEP_LINK_INTENT, intent)
        val args = arrayOfNulls<Bundle>(deepLink.size)
        for (index in args.indices) {
            val arguments = Bundle()
            arguments.putAll(globalArgs)
            if (deepLinkArgs != null) {
                val deepLinkArguments = deepLinkArgs[index]
                if (deepLinkArguments != null) {
                    arguments.putAll(deepLinkArguments)
                }
            }
            args[index] = arguments
        }
        val flags = intent.flags
        if (flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0 &&
            flags and Intent.FLAG_ACTIVITY_CLEAR_TASK == 0
        ) {
            // Someone called us with NEW_TASK, but we don't know what state our whole
            // task stack is in, so we need to manually restart the whole stack to
            // ensure we're in a predictably good state.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val taskStackBuilder = TaskStackBuilder
                .create(context)
                .addNextIntentWithParentStack(intent)
            taskStackBuilder.startActivities()
            activity?.let { activity ->
                activity.finish()
                // Disable second animation in case where the Activity is created twice.
                activity.overridePendingTransition(0, 0)
            }
            return true
        }
        if (flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0) {
            // Start with a cleared task starting at our root when we're on our own task
            if (!backQueue.isEmpty()) {
                popBackStackInternal(_graph!!.id, true)
            }
            var index = 0
            while (index < deepLink.size) {
                val destinationId = deepLink[index]
                val arguments = args[index++]
                val node = findDestination(destinationId)
                if (node == null) {
                    val dest = NavDestination.getDisplayName(
                        context, destinationId
                    )
                    throw IllegalStateException(
                        "Deep Linking failed: destination $dest cannot be found from the current " +
                            "destination $currentDestination"
                    )
                }
                navigate(
                    node, arguments,
                    navOptions {
                        anim {
                            enter = 0
                            exit = 0
                        }
                        val changingGraphs = node is NavGraph &&
                            node.hierarchy.none { it == currentDestination?.parent }
                        if (changingGraphs && deepLinkSaveState) {
                            // If we are navigating to a 'sibling' graph (one that isn't part
                            // of the current destination's hierarchy), then we need to saveState
                            // to ensure that each graph has its own saved state that users can
                            // return to
                            popUpTo(graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Note we specifically don't call restoreState = true
                            // as our deep link should support multiple instances of the
                            // same graph in a row
                        }
                    }, null
                )
            }
            return true
        }
        // Assume we're on another apps' task and only start the final destination
        var graph = _graph
        for (i in deepLink.indices) {
            val destinationId = deepLink[i]
            val arguments = args[i]
            val node = if (i == 0) _graph else graph!!.findNode(destinationId)
            if (node == null) {
                val dest = NavDestination.getDisplayName(context, destinationId)
                throw IllegalStateException(
                    "Deep Linking failed: destination $dest cannot be found in graph $graph"
                )
            }
            if (i != deepLink.size - 1) {
                // We're not at the final NavDestination yet, so keep going through the chain
                if (node is NavGraph) {
                    graph = node
                    // Automatically go down the navigation graph when
                    // the start destination is also a NavGraph
                    while (graph!!.findNode(graph.startDestinationId) is NavGraph) {
                        graph = graph.findNode(graph.startDestinationId) as NavGraph?
                    }
                }
            } else {
                // Navigate to the last NavDestination, clearing any existing destinations
                navigate(
                    node,
                    arguments,
                    NavOptions.Builder()
                        .setPopUpTo(_graph!!.id, true)
                        .setEnterAnim(0)
                        .setExitAnim(0)
                        .build(),
                    null
                )
            }
        }
        deepLinkHandled = true
        return true*/
        return false
    }

    /**
     * Looks through the deep link for invalid destinations, returning the display name of
     * any invalid destinations in the deep link array.
     *
     * @param deepLink array of deep link IDs that are expected to match the graph
     * @return The display name of the first destination not found in the graph or null if
     * all destinations were found in the graph.
     */
    private fun findInvalidDestinationDisplayNameInDeepLink(deepLink: IntArray): String? {
        /*var graph = _graph
        for (i in deepLink.indices) {
            val destinationId = deepLink[i]
            val node =
                (
                    if (i == 0)
                        if (_graph!!.id == destinationId) _graph
                        else null
                    else
                        graph!!.findNode(destinationId)
                    ) ?: return NavDestination.getDisplayName(context, destinationId)
            if (i != deepLink.size - 1) {
                // We're not at the final NavDestination yet, so keep going through the chain
                if (node is NavGraph) {
                    graph = node
                    // Automatically go down the navigation graph when
                    // the start destination is also a NavGraph
                    while (graph!!.findNode(graph.startDestinationId) is NavGraph) {
                        graph = graph.findNode(graph.startDestinationId) as NavGraph?
                    }
                }
            }
        }*/
        // We found every destination in the deepLink array, yay!
        return null
    }

    /**
     * The current destination.
     */
    public open val currentDestination: NavDestination?
        get() {
            return currentBackStackEntry?.destination
        }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public fun findDestinationId(destinationId: String): NavDestination? {
        if (_graph == null) {
            return null
        }
        if (_graph!!.idVal == destinationId) {
            return _graph
        }
        val currentNode = backQueue.lastOrNull()?.getDestination() ?: _graph!!
        return currentNode.findDestinationId(destinationId)
    }

    private fun NavDestination.findDestinationId(destinationId: String): NavDestination? {
        if (idVal == destinationId) {
            return this
        }
        val currentGraph = if (this is PlatformNavGraph) this else mParent!!
        return currentGraph.findNode(destinationId)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public fun findDestination(route: String): NavDestination? {
        if (_graph == null) {
            return null
        }
        // if not matched by routePattern, try matching with route args
        if (_graph!!.route == route || _graph!!.matchDeepLink(route) != null) {
            return _graph
        }
        val currentNode = backQueue.lastOrNull()?.destination ?: _graph!!
        val currentGraph = if (currentNode is PlatformNavGraph) currentNode else currentNode.mParent!!
        return currentGraph.findNode(route)
    }

    /**
     * Navigate to a destination from the current navigation graph. This supports both navigating
     * via an [action][NavDestination.getAction] and directly navigating to a destination.
     *
     * @param resId an [action][NavDestination.getAction] id or a destination id to
     * navigate to
     *
     * @throws IllegalStateException if there is no current navigation node
     * @throws IllegalArgumentException if the desired destination cannot be found from the
     *                                  current destination
     */
    
    public open fun navigateId(resId: String) {
        navigateId(resId, null)
    }

    /**
     * Navigate to a destination from the current navigation graph. This supports both navigating
     * via an [action][NavDestination.getAction] and directly navigating to a destination.
     *
     * @param resId an [action][NavDestination.getAction] id or a destination id to
     * navigate to
     * @param args arguments to pass to the destination
     *
     * @throws IllegalStateException if there is no current navigation node
     * @throws IllegalArgumentException if the desired destination cannot be found from the
     *                                  current destination
     */
    
    public open fun navigateId(resId: String, args: Bundle?) {
        navigateId(resId, args, null)
    }

    /**
     * Navigate to a destination from the current navigation graph. This supports both navigating
     * via an [action][NavDestination.getAction] and directly navigating to a destination.
     *
     * @param resId an [action][NavDestination.getAction] id or a destination id to
     * navigate to
     * @param args arguments to pass to the destination
     * @param navOptions special options for this navigation operation
     *
     * @throws IllegalStateException if there is no current navigation node
     * @throws IllegalArgumentException if the desired destination cannot be found from the
     *                                  current destination
     */
    
    public open fun navigateId(resId: String, args: Bundle?, navOptions: NavOptions?) {
        navigateId(resId, args, navOptions, null)
    }

    /**
     * Navigate to a destination from the current navigation graph. This supports both navigating
     * via an [action][NavDestination.getAction] and directly navigating to a destination.
     *
     * @param resId an [action][NavDestination.getAction] id or a destination id to
     * navigate to
     * @param args arguments to pass to the destination
     * @param navOptions special options for this navigation operation
     * @param navigatorExtras extras to pass to the Navigator
     *
     * @throws IllegalStateException if navigation graph has not been set for this NavController
     * @throws IllegalArgumentException if the desired destination cannot be found from the
     *                                  current destination
     */
    
    public open fun navigateId(
        resId: String,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: NavigatorExtrasProtocol?
    ) {
        var finalNavOptions = navOptions
        val currentNode = (
            if (backQueue.isEmpty())
                _graph
            else
                backQueue.last().destination
            ) ?: throw IllegalStateException(
            "No current destination found. Ensure a navigation graph has been set for " +
                "NavController $this."
        )

        var destId = resId
        val navAction = currentNode.getAction(resId)
        var combinedArgs: Bundle? = null
        if (navAction != null) {
            if (finalNavOptions == null) {
                finalNavOptions = navAction.navOptions
            }
            destId = navAction.destinationId
            val navActionArgs = navAction.defaultArguments
            if (navActionArgs != null) {
                combinedArgs = LuaBundle()
                combinedArgs.putAll(navActionArgs)
            }
        }
        if (args != null) {
            if (combinedArgs == null) {
                combinedArgs = Bundle()
            }
            combinedArgs.putAll(args)
        }
        if (destId == "" && finalNavOptions != null && finalNavOptions.mPopUpToId != "-1") {
            popBackStackId(finalNavOptions.mPopUpToId!!, finalNavOptions.mPopUpToInclusive)
            return
        }
        require(destId != "0") {
            "Destination id == 0 can only be used in conjunction with a valid navOptions.popUpTo"
        }
        val node = findDestinationId(destId)
        if (node == null) {
            require(navAction == null) {
                "Navigation destination $destId referenced from action " +
                    "$resId cannot be found from " +
                    "the current destination $currentNode"
            }
            throw IllegalArgumentException(
                "Navigation action/destination $destId cannot be found from the current " +
                    "destination $currentNode"
            )
        }
        navigate(node, combinedArgs, finalNavOptions, navigatorExtras)
    }
    
    public open fun navigate(deepLink: Uri) {
        navigate(NavDeepLinkRequest(deepLink, null, null))
    }
    
    public open fun navigate(deepLink: Uri, navOptions: NavOptions?) {
        navigate(NavDeepLinkRequest(deepLink, null, null), navOptions, null)
    }
    
    public open fun navigate(
        deepLink: Uri,
        navOptions: NavOptions?,
        navigatorExtras: NavigatorExtrasProtocol?
    ) {
        navigate(NavDeepLinkRequest(deepLink, null, null), navOptions, navigatorExtras)
    }
    
    public open fun navigate(request: NavDeepLinkRequest) {
        navigate(request, null)
    }
    
    public open fun navigate(request: NavDeepLinkRequest, navOptions: NavOptions?) {
        navigate(request, navOptions, null)
    }
    
    public open fun navigate(
        request: NavDeepLinkRequest,
        navOptions: NavOptions?,
        navigatorExtras: NavigatorExtrasProtocol?
    ) {
        requireNotNull(_graph) {
            "Cannot navigate to $request. Navigation graph has not been set for " +
                "NavController $this."
        }
        val deepLinkMatch = _graph!!.matchDeepLink(request)
        if (deepLinkMatch != null) {
            val destination = deepLinkMatch.destination
            val args = destination?.addInDefaultArgs(deepLinkMatch.matchingArgs) ?: Bundle()
            val node = deepLinkMatch.destination
            //TODO
            /*val intent = Intent().apply {
                setDataAndType(request.uri, request.mimeType)
                action = request.action
            }
            args.putParcelable(KEY_DEEP_LINK_INTENT, intent)*/
            navigate(node!!, args, navOptions, navigatorExtras)
        } else {
            throw IllegalArgumentException(
                "Navigation destination that matches request $request cannot be found in the " +
                    "navigation graph $_graph"
            )
        }
    }

    
    private fun navigate(
        node: NavDestination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: NavigatorExtrasProtocol?
    ) {
        navigatorState.values.forEach { state ->
            state.isNavigating = true
        }
        var popped = false
        var launchSingleTop = false
        var navigated = false
        if (navOptions != null) {
            if (navOptions.mPopUpToId != "-1") {
                popped = popBackStackInternalId(
                    navOptions.mPopUpToId!!,
                    navOptions.mPopUpToInclusive,
                    navOptions.mPopUpToSaveState
                )
            }
        }
        val finalArgs = node.addInDefaultArgs(args)
        // Now determine what new destinations we need to add to the back stack
        if (navOptions?.mRestoreState == true && backStackMap.containsKey(node.idVal)) {
            navigated = restoreStateInternalId(node.idVal!!, finalArgs, navOptions, navigatorExtras)
        } else {
            launchSingleTop = navOptions?.mSingleTop == true &&
                launchSingleTopInternal(node, args)

            if (!launchSingleTop) {
                // Not a single top operation, so we're looking to add the node to the back stack
                val backStackEntry = NavBackStackEntry.create(
                    context, node, finalArgs, hostLifecycleState, viewModel
                )
                val navigator = _navigatorProvider.getNavigatorWithName(
                    node.mNavigatorName.toString()
                )
                navigator.navigateInternal(listOf(backStackEntry), navOptions, navigatorExtras) {
                    navigated = true
                    addEntryToBackStack(node, finalArgs, it)
                }
            }
        }
        updateOnBackPressedCallbackEnabled()
        navigatorState.values.forEach { state ->
            state.isNavigating = false
        }
        if (popped || navigated || launchSingleTop) {
            dispatchOnDestinationChanged()
        } else {
            updateBackStackLifecycle()
        }
    }

    private fun launchSingleTopInternal(
        node: NavDestination,
        args: Bundle?
    ): Boolean {
        val currentBackStackEntry = currentBackStackEntry
        val nodeId = if (node is PlatformNavGraph) node.findStartDestination().idVal else node.idVal
        if (nodeId != currentBackStackEntry?.destination?.idVal) return false

        val tempBackQueue: ArrayDeque<PlatformNavBackStackEntry> = ArrayDeque()
        // pop from startDestination back to original node and create a new entry for each
        backQueue.indexOfLast { it.destination === node }.let { nodeIndex ->
            while (backQueue.lastIndex >= nodeIndex) {
                val oldEntry = backQueue.removeLast()
                unlinkChildFromParent(oldEntry)
                val newEntry = NavBackStackEntry.create(
                    oldEntry,
                    oldEntry.destination.addInDefaultArgs(args)
                )
                tempBackQueue.addFirst(newEntry)
            }
        }

        // add each new entry to backQueue starting from original node to startDestination
        tempBackQueue.forEach { newEntry ->
            val parent = newEntry.destination.mParent
            if (parent != null) {
                val newParent = getBackStackEntryId(parent.idVal!!)
                linkChildToParent(newEntry, newParent)
            }
            backQueue.add(newEntry)
        }

        // we replace NavState entries here only after backQueue has been finalized
        tempBackQueue.forEach { newEntry ->
            val navigator = _navigatorProvider.getNavigatorWithName(
                newEntry.destination.mNavigatorName.toString()
            )
            navigator.onLaunchSingleTopWithBackStackEntry(newEntry)
        }

        return true
    }

    private fun restoreStateInternalId(
        id: String,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: NavigatorExtrasProtocol?
    ): Boolean {
        if (!backStackMap.containsKey(id)) {
            return false
        }
        val backStackId = backStackMap[id]
        // Clear out the state we're going to restore so that it isn't restored a second time
        backStackMap.values.removeAll { it == backStackId }
        val backStackState = backStackStates.remove(backStackId)
        // Now restore the back stack from its saved state
        val entries = instantiateBackStack(backStackState)
        return executeRestoreState(entries, args, navOptions, navigatorExtras)
    }

    private fun restoreStateInternal(route: String): Boolean {
        var id = NavDestination.createRoute(route)
        // try to match based on routePattern
        return if (backStackMap.containsKey(id)) {
            restoreStateInternalId(id, null, null, null)
        } else {
            // if it didn't match, it means the route contains filled in arguments and we need
            // to find the destination that matches this route's general pattern
            val matchingDestination = findDestination(route)
            check(matchingDestination != null) {
                "Restore State failed: route $route cannot be found from the current " +
                    "destination $currentDestination"
            }

            id = matchingDestination.idVal!!
            val backStackId = backStackMap[id]
            // Clear out the state we're going to restore so that it isn't restored a second time
            backStackMap.values.removeAll { it == backStackId }
            val backStackState = backStackStates.remove(backStackId)

            val matchingDeepLink = matchingDestination.matchDeepLink(route)
            // check if the topmost NavBackStackEntryState contains the arguments in this
            // matchingDeepLink. If not, we didn't find the correct stack.
            val isCorrectStack = matchingDeepLink!!.hasMatchingArgs(
                backStackState?.firstOrNull()?.getArguments()
            )
            if (!isCorrectStack) return false
            val entries = instantiateBackStack(backStackState)
            executeRestoreState(entries, null, null, null)
        }
    }

    private fun executeRestoreState(
        entries: List<PlatformNavBackStackEntry>,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: NavigatorExtrasProtocol?
    ): Boolean {
        // Split up the entries by Navigator so we can restore them as an atomic operation
        val entriesGroupedByNavigator = mutableListOf<MutableList<PlatformNavBackStackEntry>>()
        entries.filterNot { entry ->
            // Skip navigation graphs - they'll be added by addEntryToBackStack()
            entry.destination is PlatformNavGraph
        }.forEach { entry ->
            val previousEntryList = entriesGroupedByNavigator.lastOrNull()
            val previousNavigatorName = previousEntryList?.last()?.destination?.mNavigatorName.toString()
            if (previousNavigatorName == entry.destination.mNavigatorName.toString()) {
                // Group back to back entries associated with the same Navigator together
                previousEntryList!! += entry
            } else {
                // Create a new group for the new Navigator
                entriesGroupedByNavigator += mutableListOf(entry)
            }
        }
        var navigated = false
        // Now actually navigate to each set of entries
        for (entryList in entriesGroupedByNavigator) {
            val navigator = _navigatorProvider.getNavigatorWithName(
                entryList.first().destination.mNavigatorName.toString()
            )
            var lastNavigatedIndex = 0
            navigator.navigateInternal(entryList, navOptions, navigatorExtras) { entry ->
                navigated = true
                // If this destination is part of the restored back stack,
                // pass all destinations between the last navigated entry and this one
                // to ensure that any navigation graphs are properly restored as well
                val entryIndex = entries.indexOf(entry)
                val restoredEntries = if (entryIndex != -1) {
                    entries.subList(lastNavigatedIndex, entryIndex + 1).also {
                        lastNavigatedIndex = entryIndex + 1
                    }
                } else {
                    emptyList()
                }
                addEntryToBackStack(entry.destination, args, entry, restoredEntries)
            }
        }
        return navigated
    }

    private fun instantiateBackStack(
        backStackState: ArrayDeque<NavBackStackEntryState>?
    ): List<PlatformNavBackStackEntry> {
        val backStack = mutableListOf<PlatformNavBackStackEntry>()
        var currentDestination = backQueue.lastOrNull()?.destination ?: graph
        backStackState?.forEach { state ->
            val node = currentDestination.findDestinationId(state.getDestinationId())
            checkNotNull(node) {
                /*val dest = NavDestination.getDisplayName(
                    context, state.destinationId
                )*/
                "Restore State failed: destination ${state.getDestinationId()} cannot be found from the current " +
                    "destination $currentDestination"
            }
            backStack += state.instantiate(context, node, hostLifecycleState, viewModel)
            currentDestination = node
        }
        return backStack
    }

    private fun addEntryToBackStack(
        node: NavDestination,
        finalArgs: Bundle?,
        backStackEntry: PlatformNavBackStackEntry,
        restoredEntries: List<PlatformNavBackStackEntry> = emptyList()
    ) {
        val newDest = backStackEntry.destination
        //TODO:FloatingWindow
        /*if (newDest !is FloatingWindow) {
            // We've successfully navigating to the new destination, which means
            // we should pop any FloatingWindow destination off the back stack
            // before updating the back stack with our new destination
            while (!backQueue.isEmpty() &&
                backQueue.last().destination is FloatingWindow &&
                popBackStackInternal(backQueue.last().destination.id, true)
            ) {
                // Keep popping
            }
        }*/

        // When you navigate() to a NavGraph, we need to ensure that a new instance
        // is always created vs reusing an existing copy of that destination
        val hierarchy = ArrayDeque<PlatformNavBackStackEntry>()
        var destination: NavDestination? = newDest
        if (node is PlatformNavGraph) {
            do {
                val parent = destination!!.mParent
                if (parent != null) {
                    val entry = restoredEntries.lastOrNull { restoredEntry ->
                        restoredEntry.destination == parent
                    } ?: NavBackStackEntry.create(
                        context, parent,
                        finalArgs, hostLifecycleState, viewModel
                    )
                    hierarchy.addFirst(entry)
                    // Pop any orphaned copy of that navigation graph off the back stack
                    if (backQueue.isNotEmpty() && backQueue.last().destination === parent) {
                        popEntryFromBackStack(backQueue.last())
                    }
                }
                destination = parent
            } while (destination != null && destination !== node)
        }

        // Now collect the set of all intermediate NavGraphs that need to be put onto
        // the back stack. Destinations can have multiple parents, so we check referential
        // equality to ensure that same destinations with a parent that is not this _graph
        // will also have their parents added to the hierarchy.
        destination = if (hierarchy.isEmpty()) newDest else hierarchy.first().destination
        while (destination != null && findDestinationId(destination.idVal!!) !== destination) {
            val parent = destination.mParent
            if (parent != null) {
                val args = if (finalArgs?.bundle?.count == 0UL) null else finalArgs
                val entry = restoredEntries.lastOrNull { restoredEntry ->
                    restoredEntry.destination == parent
                } ?: NavBackStackEntry.create(
                    context, parent, parent.addInDefaultArgs(args), hostLifecycleState,
                    viewModel
                )
                hierarchy.addFirst(entry)
            }
            destination = parent
        }
        val overlappingDestination: NavDestination =
            if (hierarchy.isEmpty())
                newDest
            else
                hierarchy.first().destination
        // Pop any orphaned navigation graphs that don't connect to the new destinations
        while (!backQueue.isEmpty() && backQueue.last().destination is PlatformNavGraph &&
            (backQueue.last().destination as PlatformNavGraph).findNode(
                overlappingDestination.idVal, false
            ) == null
        ) {
            popEntryFromBackStack(backQueue.last())
        }

        // The _graph should always be on the top of the back stack after you navigate()
        val firstEntry = backQueue.firstOrNull() ?: hierarchy.firstOrNull()
        if (firstEntry?.destination != _graph) {
            val entry = restoredEntries.lastOrNull { restoredEntry ->
                restoredEntry.destination == _graph!!
            } ?: NavBackStackEntry.create(
                context, _graph!!, _graph!!.addInDefaultArgs(finalArgs), hostLifecycleState,
                viewModel
            )
            hierarchy.addFirst(entry)
        }

        // Now add the parent hierarchy to the NavigatorStates and back stack
        hierarchy.forEach { entry ->
            val navigator = _navigatorProvider.getNavigatorWithName(
                entry.destination.mNavigatorName.toString()
            )
            val navigatorBackStack = checkNotNull(navigatorState[navigator]) {
                "NavigatorBackStack for ${node.mNavigatorName.toString()} should already be created"
            }
            navigatorBackStack.addInternal(entry)
        }
        backQueue.addAll(hierarchy)

        // And finally, add the new destination
        backQueue.add(backStackEntry)

        // Link the newly added hierarchy and entry with the parent NavBackStackEntry
        // so that we can track how many destinations are associated with each NavGraph
        (hierarchy + backStackEntry).forEach {
            val parent = it.destination.mParent
            if (parent != null) {
                linkChildToParent(it, getBackStackEntryId(parent!!.idVal!!))
            }
        }
    }

    /**
     * Navigate via the given [NavDirections]
     *
     * @param directions directions that describe this navigation operation
     */

    public open fun navigate(directions: NavDirections) {
        navigateId(directions.actionId, directions.arguments, null as NavOptions?)
    }

    /**
     * Navigate via the given [NavDirections]
     *
     * @param directions directions that describe this navigation operation
     * @param navOptions special options for this navigation operation
     */

    public open fun navigate(directions: NavDirections, navOptions: NavOptions?) {
        navigateId(directions.actionId, directions.arguments, navOptions)
    }

    /**
     * Navigate via the given [NavDirections]
     *
     * @param directions directions that describe this navigation operation
     * @param navigatorExtras extras to pass to the [Navigator]
     */

    public open fun navigate(directions: NavDirections, navigatorExtras: NavigatorExtrasProtocol) {
        navigateId(directions.actionId, directions.arguments, null, navigatorExtras)
    }

    /**
     * Navigate to a route in the current NavGraph. If an invalid route is given, an
     * [IllegalArgumentException] will be thrown.
     *
     * @param route route for the destination
     * @param builder DSL for constructing a new [NavOptions]
     *
     * @throws IllegalArgumentException if the given route is invalid
     */
    
    public fun navigate(route: String, builder: NavOptionsBuilder.() -> Unit) {
        navigate(route, navOptions(builder))
    }

    /**
     * Navigate to a route in the current NavGraph. If an invalid route is given, an
     * [IllegalArgumentException] will be thrown.
     *
     * @param route route for the destination
     * @param navOptions special options for this navigation operation
     * @param navigatorExtras extras to pass to the [Navigator]
     *
     * @throws IllegalArgumentException if the given route is invalid
     */
    
    
    public fun navigate(
        route: String,
        navOptions: NavOptions? = null,
        navigatorExtras: NavigatorExtrasProtocol? = null
    ) {
        navigate(
            NavDeepLinkRequest.Builder.fromUri(NavDestination.createRoute(route).toUri()).build(), navOptions,
            navigatorExtras
        )
    }

    public open fun createDeepLink(): NavDeepLinkBuilder {
        return NavDeepLinkBuilder(this)
    }

    /**
     * Saves all navigation controller state to a Bundle.
     *
     * State may be restored from a bundle returned from this method by calling
     * [restoreState]. Saving controller state is the responsibility
     * of a [PlatformNavHost].
     *
     * @return saved state for this controller
     */
    @CallSuper
    public open fun saveState(): Bundle? {
        var b: Bundle? = null
        val navigatorNames: MutableList<String> = mutableListOf()
        val navigatorState = Bundle()
        for ((name, value) in _navigatorProvider.getNavigators()) {
            name as String
            value as Navigator
            val savedState = value.onSaveState()
            if (savedState != null) {
                navigatorNames.add(name)
                navigatorState.putBundle(name, savedState)
            }
        }
        if (navigatorNames.isNotEmpty()) {
            b = Bundle()
            navigatorState.putStringArray(KEY_NAVIGATOR_STATE_NAMES, navigatorNames.toTIOSKHKotlinArray())
            b.putBundle(KEY_NAVIGATOR_STATE, navigatorState)
        }
        if (backQueue.isNotEmpty()) {
            if (b == null) {
                b = Bundle()
            }
            val backStack = arrayOfNulls<NavBackStackEntryState>(backQueue.size)
            var index = 0
            for (backStackEntry in this.backQueue) {
                backStack[index++] = NavBackStackEntryState(backStackEntry)
            }
            b.putObject(KEY_BACK_STACK, backStack)
        }
        if (backStackMap.isNotEmpty()) {
            if (b == null) {
                b = Bundle()
            }
            val backStackDestIds = Array<String>(backStackMap.size) {
                ""
            }
            val backStackIds = ArrayList<String?>()
            var index = 0
            for ((destId, id) in backStackMap) {
                backStackDestIds[index++] = destId
                backStackIds += id
            }
            b.putStringArray(KEY_BACK_STACK_DEST_IDS, backStackDestIds.toTIOSKHKotlinArray())
            b.putStringArray(KEY_BACK_STACK_IDS, backStackIds.toTIOSKHKotlinArray())
        }
        if (backStackStates.isNotEmpty()) {
            if (b == null) {
                b = Bundle()
            }
            val backStackStateIds = ArrayList<String>()
            for ((id, backStackStates) in backStackStates) {
                backStackStateIds += id
                val states = arrayOfNulls<NavBackStackEntryState>(backStackStates.size)
                backStackStates.forEachIndexed { stateIndex, backStackState ->
                    states[stateIndex] = backStackState
                }
                b.putObject(KEY_BACK_STACK_STATES_PREFIX + id, states)
            }
            b.putStringArray(KEY_BACK_STACK_STATES_IDS, backStackStateIds.toTIOSKHKotlinArray())
        }
        if (deepLinkHandled) {
            if (b == null) {
                b = Bundle()
            }
            b.putBoolean(KEY_DEEP_LINK_HANDLED, deepLinkHandled)
        }
        return b
    }

    /**
     * Restores all navigation controller state from a bundle. This should be called before any
     * call to [setGraph].
     *
     * State may be saved to a bundle by calling [saveState].
     * Restoring controller state is the responsibility of a [PlatformNavHost].
     *
     * @param navState state bundle to restore
     */
    @CallSuper
    @Suppress("DEPRECATION")
    public open fun restoreState(navState: Bundle?) {
        if (navState == null) {
            return
        }
        //navState.classLoader = context.classLoader
        navigatorStateToRestore = navState.getBundle(KEY_NAVIGATOR_STATE)
        backStackToRestore = navState.getArray(KEY_BACK_STACK)?.toMutableList<NSObject>()?.toTypedArray()
        backStackStates.clear()
        val backStackDestIds = navState.getStringArray(KEY_BACK_STACK_DEST_IDS)?.toMutableList<String>()
        val backStackIds = navState.getStringArray(KEY_BACK_STACK_IDS)?.toArray()
        if (backStackDestIds != null && backStackIds != null) {
            backStackDestIds.forEachIndexed { index, id ->
                backStackMap[id] = backStackIds[index]?.toString()
            }
        }
        val backStackStateIds = navState.getStringArray(KEY_BACK_STACK_STATES_IDS)?.toMutableList<String>()
        backStackStateIds?.forEach { id ->
            val backStackState = navState.getObject(KEY_BACK_STACK_STATES_PREFIX + id) as Array<NavBackStackEntryState>?
            if (backStackState != null) {
                backStackStates[id] = ArrayDeque<NavBackStackEntryState>(
                    backStackState.size
                ).apply {
                    for (parcelable in backStackState) {
                        add(parcelable as NavBackStackEntryState)
                    }
                }
            }
        }
        deepLinkHandled = navState.getBoolean(KEY_DEEP_LINK_HANDLED)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public open fun setLifecycleOwner(owner: LifecycleOwner) {
        if (owner == lifecycleOwner) {
            return
        }
        lifecycleOwner?.lifecycle?.removeObserver(lifecycleObserver)
        lifecycleOwner = owner
        owner.lifecycle?.addObserver(lifecycleObserver)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public open fun setOnBackPressedDispatcher(dispatcher: OnBackPressedDispatcher) {
        if (dispatcher == onBackPressedDispatcher) {
            return
        }
        val lifecycleOwner = checkNotNull(lifecycleOwner) {
            "You must call setLifecycleOwner() before calling setOnBackPressedDispatcher()"
        }
        // Remove the callback from any previous dispatcher
        onBackPressedCallback.remove()
        // Then add it to the new dispatcher
        onBackPressedDispatcher = dispatcher
        dispatcher.addCallbackWithOwner(lifecycleOwner, onBackPressedCallback)

        // Make sure that listener for updating the NavBackStackEntry lifecycles comes after
        // the dispatcher
        lifecycleOwner.lifecycle?.apply {
            removeObserver(lifecycleObserver)
            addObserver(lifecycleObserver)
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public open fun enableOnBackPressed(enabled: Boolean) {
        enableOnBackPressedCallback = enabled
        updateOnBackPressedCallbackEnabled()
    }

    private fun updateOnBackPressedCallbackEnabled() {
        onBackPressedCallback.setEnabledWithEnabled((
            enableOnBackPressedCallback && destinationCountOnBackStack > 1
            ))
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public open fun setViewModelStore(viewModelStore: ViewModelStore) {
        if (viewModel == NavControllerViewModelgetInstance(viewModelStore)) {
            return
        }
        check(backQueue.isEmpty()) { "ViewModelStore should be set before setGraph call" }
        viewModel = NavControllerViewModelgetInstance(viewModelStore)
    }

    public open fun getViewModelStoreOwner(navGraphId: String): ViewModelStoreOwnerProtocol {
        checkNotNull(viewModel) {
            "You must call setViewModelStore() before calling getViewModelStoreOwner()."
        }
        val lastFromBackStack = getBackStackEntryId(navGraphId)
        require(lastFromBackStack.destination is PlatformNavGraph) {
            "No NavGraph with ID $navGraphId is on the NavController's back stack"
        }
        return lastFromBackStack
    }

    public open fun getBackStackEntryId(destinationId: String): PlatformNavBackStackEntry {
        val lastFromBackStack: PlatformNavBackStackEntry? = backQueue.lastOrNull { entry ->
            entry.destination.idVal == destinationId
        }
        requireNotNull(lastFromBackStack) {
            "No destination with ID $destinationId is on the NavController's back stack. The " +
                "current destination is $currentDestination"
        }
        return lastFromBackStack
    }

    public fun getBackStackEntry(route: String): PlatformNavBackStackEntry {
        val lastFromBackStack: PlatformNavBackStackEntry? = backQueue.lastOrNull { entry ->
            entry.destination.hasRoute(route, entry.getArguments())
        }
        requireNotNull(lastFromBackStack) {
            "No destination with route $route is on the NavController's back stack. The " +
                "current destination is $currentDestination"
        }
        return lastFromBackStack
    }

    /**
     * The topmost [PlatformNavBackStackEntry].
     *
     * @return the topmost entry on the back stack or null if the back stack is empty
     */
    public open val currentBackStackEntry: PlatformNavBackStackEntry?
        get() = backQueue.lastOrNull()

    private val _currentBackStackEntryFlow: MutableSharedFlow<PlatformNavBackStackEntry> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /**
     * A [Flow] that will emit the currently active [PlatformNavBackStackEntry] whenever it changes. If
     * there is no active [PlatformNavBackStackEntry], no item will be emitted.
     */
    public val currentBackStackEntryFlow: Flow<PlatformNavBackStackEntry> =
        _currentBackStackEntryFlow.asSharedFlow()

    /**
     * The previous visible [PlatformNavBackStackEntry].
     *
     * This skips over any [PlatformNavBackStackEntry] that is associated with a [PlatformNavGraph].
     *
     * @return the previous visible entry on the back stack or null if the back stack has less
     * than two visible entries
     */
    public open val previousBackStackEntry: PlatformNavBackStackEntry?
        get() {
            val iterator = backQueue.reversed().iterator()
            // throw the topmost destination away.
            if (iterator.hasNext()) {
                iterator.next()
            }
            return iterator.asSequence().firstOrNull { entry ->
                entry.destination !is PlatformNavGraph
            }
        }
}

private fun NavBackStackEntryState.instantiate(
    context: Context,
    destination: NavDestination,
    hostLifecycleState: LifecycleState,
    viewModel: NavControllerViewModel?
): PlatformNavBackStackEntry {
    return NavBackStackEntry.create(
        context, destination, getArguments(),
        hostLifecycleState, viewModel,
        getId(), getSavedState()
    )
}

/**
 * Returns a new [Bundle] with the given key/value pairs as elements.
 *
 * @throws IllegalArgumentException When a value is not a supported type of [Bundle].
 */
public fun bundleOf(vararg pairs: Pair<String, Any?>): Bundle = Bundle().apply {
    for ((key, value) in pairs) {
        when (value) {
            null -> putString(key, null) // Any nullable type will suffice.

            // Scalars
            is Boolean -> putBoolean(key, value)
            is Byte -> putByte(key, value)
            is Char -> putChar(key, value as Byte)
            is Double -> putDouble(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Short -> putShort(key, value)

            // References
            is Bundle -> putBundle(key, value)
            is CharSequence -> putString(key, value.toString())
            is Parcelable -> putObject(key, value)

            // Scalar arrays
            is BooleanArray -> putBooleanArray(key, value.toTIOSKotlinBooleanArray())
            is ByteArray -> putByteArray(key, value.toTIOSKotlinByteArray())
            is CharArray -> putCharArray(key, value.toTIOSKotlinCharArray())
            is DoubleArray -> putDoubleArray(key, value.toTIOSKotlinDoubleArray())
            is FloatArray -> putFloatArray(key, value.toTIOSKotlinFloatArray())
            is IntArray -> putIntArray(key, value.toTIOSKotlinIntArray())
            is LongArray -> putLongArray(key, value.toTIOSKotlinLongArray())
            is ShortArray -> putShortArray(key, value.toTIOSKotlinShortArray())

            // Reference arrays
            is Array<*> -> putArray(key, value.toTIOSKHKotlinArray())

            // Last resort. Also we must check this after Array<*> as all arrays are serializable.
            else -> putObject(key, value)
        }
    }
}

/**
 * Returns a new empty [Bundle].
 */
public fun bundleOf(): Bundle = Bundle()

private fun cocoapods.ToppingCompose.NavBackStackEntry.Companion.create(
    entry: PlatformNavBackStackEntry, arguments: Bundle? = entry.getArguments()
): PlatformNavBackStackEntry = NavBackStackEntry.create(
    entry.mContext(), entry.getDestination(), arguments,
    entry.mHostLifecycle(),
    entry.viewModelStoreProvider() as NavViewModelStoreProviderProtocol?, entry.getId(), entry.savedState()
).also {
    it.setMHostLifecycle(entry.mHostLifecycle())
    it.maxLifecycle = entry.maxLifecycle
}

private fun cocoapods.ToppingCompose.NavBackStackEntry.Companion.create(
    context: Context,
    destination: NavDestination,
    arguments: Bundle? = null,
    hostLifecycleState: LifecycleState = LifecycleState.LIFECYCLESTATE_CREATED,
    viewModelStoreProvider: NavViewModelStoreProviderProtocol? = null,
    id: String = UUID.randomUUID().toString(),
    savedState: Bundle? = null
): PlatformNavBackStackEntry = NavBackStackEntry.createIn(
    context, destination, arguments,
    hostLifecycleState, viewModelStoreProvider, id, savedState
)

private fun cocoapods.ToppingCompose.NavBackStackEntry.Companion.createIn(
    context: Context,
    destination: NavDestination,
    arguments: Bundle?,
    hostLifecycleState: LifecycleState,
    viewModelStoreProvider: NavViewModelStoreProviderProtocol?,
    id: String,
    savedState: Bundle?) : PlatformNavBackStackEntry
{
    val result = PlatformNavBackStackEntry(context, destination, arguments, null, if(viewModelStoreProvider is NavControllerViewModel) viewModelStoreProvider else null)
    result.setMHostLifecycle(hostLifecycleState)
    result.setViewModelStoreProvider(viewModelStoreProvider)
    result.setMId(NSUUID(id))
    result.setSavedState(savedState)
    return result
}

inline fun String.toUri(): Uri = Uri.parse(this)

private fun NavDestination.matchDeepLinkExcludingChildren(request: NavDeepLinkRequest): DeepLinkMatch? =
    matchDeepLink(request)

private fun NavDestination.matchDeepLink(route: String): DeepLinkMatch? {
    val request = NavDeepLinkRequest.Builder.fromUri(createRoute(route)!!.toUri()).build()
    val matchingDeepLink = if (this is PlatformNavGraph) {
        matchDeepLinkExcludingChildren(request)
    } else {
        matchDeepLink(request)
    }
    return matchingDeepLink
}

var NavDestination.arguments : MutableMap<String, NavArgument>
    get() {
        if(customArgs == null)
            customArgs = LuaBundle()
        return customArgs!!.getObject("arguments", mutableMapOf<String, NavArgument>()) as MutableMap<String, NavArgument>
    }
    set(value) {
        if(customArgs == null)
            customArgs = LuaBundle()
        customArgs!!.putObject("arguments", value)
    }

private fun NavDestination.matchDeepLink(navDeepLinkRequest: NavDeepLinkRequest): DeepLinkMatch? {
    var bestMatch: DeepLinkMatch? = null
    if(deepLinks?.isNotEmpty() == true) {
        for (deepLink in deepLinks!!) {
            (deepLink as NavDeepLink)
            val uri = navDeepLinkRequest.uri
            // includes matching args for path, query, and fragment
            val matchingArguments =
                if (uri != null) deepLink.getMatchingArguments(uri, arguments) else null
            val matchingPathSegments = deepLink.calculateMatchingPathSegments(uri)
            val requestAction = navDeepLinkRequest.action
            val matchingAction = requestAction != null && requestAction ==
                deepLink.action
            val mimeType = navDeepLinkRequest.mimeType
            val mimeTypeMatchLevel =
                if (mimeType != null) deepLink.getMimeTypeMatchRating(mimeType) else -1
            if (matchingArguments != null || ((matchingAction || mimeTypeMatchLevel > -1) &&
                    hasRequiredArguments(deepLink, uri, arguments))
            ) {
                val newMatch = DeepLinkMatch(
                    this, matchingArguments,
                    deepLink.isExactDeepLink, matchingPathSegments, matchingAction,
                    mimeTypeMatchLevel
                )
                if (bestMatch == null || (newMatch.compare(bestMatch).toInt() > 0)) {
                    bestMatch = newMatch
                }
            }
        }
    }

    var nodeList: MutableList<NavDestination> = mutableListOf()
    if(this is NavGraph) {
        // Then search through all child destinations for a matching deep link
        /*val v = mParent?.mNodes?.count?.toInt()
        var nodeList: MutableList<NavDestination> = mutableListOf()
        if (v != null && mParent != null && mParent?.mNodes != null) {
            nodeList = mutableListOf()
            for (key in mParent?.mNodes?.allKeys() ?: listOf<NavDestination>()) {
                nodeList.add(mParent!!.mNodes!!.objectForKey(key) as NavDestination)
            }
        }*/

        val v = mNodes?.count?.toInt()
        if (v != null && mNodes != null) {
            nodeList = mutableListOf()
            for (key in mNodes?.allKeys() ?: listOf<NavDestination>()) {
                nodeList.add(mNodes!!.objectForKey(key) as NavDestination)
            }
        }
    }

    val bestChildMatch = nodeList.mapNotNull { child ->
        child.matchDeepLink(navDeepLinkRequest)
    }.maxWithOrNull { a, b -> a.compare(b).toInt() }

    return listOfNotNull(bestMatch, bestChildMatch).maxWithOrNull { a, b -> a.compare(b).toInt() }
}

private fun hasRequiredArguments(
    deepLink: NavDeepLink,
    uri: Uri?,
    arguments: Map<String, NavArgument>
): Boolean {
    val matchingArgs = deepLink.getMatchingPathAndQueryArgs(uri, arguments)
    val missingRequiredArguments = arguments.missingRequiredArguments { key ->
        matchingArgs.bundle?.objectForKey(key) == null
    }
    return missingRequiredArguments.isEmpty()
}

private fun LifecycleState.isAtLeast(lifecyclestate: LifecycleState): Boolean {
    return Lifecycle.isAtLeast(this, lifecyclestate)
}

public inline fun <T, R : Any> Iterable<T>.mapNotNull(transform: (T) -> R?): List<R> {
    return mapNotNullTo(ArrayList<R>(), transform)
}

/**
 * Construct a new [PlatformNavGraph]
 *
 * @param id the graph's unique id
 * @param startDestination the route for the start destination
 * @param builder the builder used to construct the graph
 */
@Suppress("Deprecation")
@Deprecated(
    "Use routes to create your NavGraph instead",
    ReplaceWith(
        "createGraph(startDestination = startDestination.toString(), route = id.toString()) " +
            "{ builder.invoke() }"
    )
)
public fun NavController.createGraphId(
    id: String = "",
    startDestination: String,
    builder: (NavGraphBuilder) -> Unit
): PlatformNavGraph = navigatorProvider.navigationId(id, startDestination, builder)

/**
 * Construct a new [PlatformNavGraph]
 *
 * @param startDestination the route for the start destination
 * @param route the route for the graph
 * @param builder the builder used to construct the graph
 */
public fun NavController.createGraph(
    startDestination: String,
    route: String? = null,
    builder: (NavGraphBuilder) -> Unit
): PlatformNavGraph = navigatorProvider.navigation(startDestination, route, builder)

public fun NavController.createGraph(
    startDestination: String,
    route: String? = null,
    builder: (PlatformNavGraphBuilder) -> Unit
): PlatformNavGraph = navigatorProvider.navigation(startDestination, route, builder)