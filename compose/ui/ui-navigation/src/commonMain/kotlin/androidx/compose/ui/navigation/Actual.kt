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

import androidx.compose.ui.util.ID

expect open class PlatformNavHostController {
    /**
     * Navigate via the given [PlatformNavDirections]
     *
     * @param directions directions that describe this navigation operation
     */
    public open fun navigate(directions: PlatformNavDirections)

    /**
     * Navigate via the given [PlatformNavDirections]
     *
     * @param directions directions that describe this navigation operation
     * @param navOptions special options for this navigation operation
     */
    public open fun navigate(directions: PlatformNavDirections, navOptions: PlatformNavOptions?)

    /**
     * Navigate via the given [PlatformNavDirections]
     *
     * @param directions directions that describe this navigation operation
     * @param navigatorExtras extras to pass to the [PlatformNavigator]
     */
    public open fun navigate(directions: PlatformNavDirections, navigatorExtras: PlatformNavigatorExtras)

    /**
     * Navigate to a route in the current NavGraph. If an invalid route is given, an
     * [IllegalArgumentException] will be thrown.
     *
     * @param route route for the destination
     * @param builder DSL for constructing a new [PlatformNavOptions]
     *
     * @throws IllegalArgumentException if the given route is invalid
     */
    public fun navigate(route: String, builder: PlatformNavOptionsBuilder.() -> Unit)

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
    public open fun navigate(resId: ID)

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
    public open fun navigate(resId: ID, args: PlatformBundle?)

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
    public open fun navigate(resId: ID, args: PlatformBundle?, navOptions: PlatformNavOptions?)

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
    open fun navigate(
        resId: ID,
        args: PlatformBundle?,
        navOptions: PlatformNavOptions?,
        navigatorExtras: PlatformNavigatorExtras?
    )

    /**
     * Navigate to a route in the current NavGraph. If an invalid route is given, an
     * [IllegalArgumentException] will be thrown.
     *
     * @param route route for the destination
     * @param navOptions special options for this navigation operation
     * @param navigatorExtras extras to pass to the [PlatformNavigator]
     *
     * @throws IllegalArgumentException if the given route is invalid
     */
    fun navigate(
        route: String,
        navOptions: PlatformNavOptions?,
        navigatorExtras: PlatformNavigatorExtras?
    )

    /**
     * Attempts to pop the controller's back stack. Analogous to when the user presses
     * the system [Back][android.view.KeyEvent.KEYCODE_BACK] button when the associated
     * navigation host has focus.
     *
     * @return true if the stack was popped at least once and the user has been navigated to
     * another destination, false otherwise
     */
    public open fun popBackStack(): Boolean

    /**
     * Attempts to pop the controller's back stack back to a specific destination.
     *
     * @param destinationId The topmost destination to retain
     * @param inclusive Whether the given destination should also be popped.
     *
     * @return true if the stack was popped at least once and the user has been navigated to
     * another destination, false otherwise
     */
    public open fun popBackStack(destinationId: ID, inclusive: Boolean): Boolean

    /**
     * Attempts to pop the controller's back stack back to a specific destination.
     *
     * @param destinationId The topmost destination to retain
     * @param inclusive Whether the given destination should also be popped.
     * @param saveState Whether the back stack and the state of all destinations between the
     * current destination and the [destinationId] should be saved for later
     * restoration via [NavOptions.Builder.setRestoreState] or the `restoreState` attribute using
     * the same [destinationId] (note: this matching ID is true whether
     * [inclusive] is true or false).
     *
     * @return true if the stack was popped at least once and the user has been navigated to
     * another destination, false otherwise
     */
    public open fun popBackStackId(
        destinationId: ID,
        inclusive: Boolean,
        saveState: Boolean
    ): Boolean

    /**
     * Attempts to pop the controller's back stack back to a specific destination.
     *
     * @param route The topmost destination to retain. May contain filled in arguments as long as
     * it is exact match with route used to navigate.
     * @param inclusive Whether the given destination should also be popped.
     * @param saveState Whether the back stack and the state of all destinations between the
     * current destination and the [route] should be saved for later
     * restoration via [NavOptions.Builder.setRestoreState] or the `restoreState` attribute using
     * the same [route] (note: this matching ID is true whether
     * [inclusive] is true or false).
     *
     * @return true if the stack was popped at least once and the user has been navigated to
     * another destination, false otherwise
     */
    public fun popBackStack(
        route: String,
        inclusive: Boolean,
        saveState: Boolean = false
    ): Boolean
}
expect class PlatformNavGraph
expect class PlatformNavGraphBuilder {
    constructor(
        provider: PlatformNavigatonProvider,
        id: ID,
        startDestination: ID
    )

    constructor(
        provider: PlatformNavigatonProvider,
        startDestination: String,
        route: String?
    )

    fun <D : PlatformNavDestination> destination(navDestination: PlatformNavDestinationBuilder<D>)

    fun addDestination(destination: PlatformNavDestination)

    fun build(): PlatformNavGraph

    fun getPlatform(): Any
}
expect class PlatformNavDestinationBuilder<T : PlatformNavDestination>
expect class PlatformNavigatonProvider
expect class PlatformNavBackStackEntry
expect class PlatformNavDestination
expect class PlatformNavigator<T : PlatformNavDestination>
expect interface PlatformNavDirections
expect class PlatformNavOptions
expect class PlatformNavOptionsBuilder
expect interface PlatformNavigatorExtras
expect class PlatformNamedNavArgument
expect class PlatformNavDeepLink
enum class PlatformSecureFlagPolicy {
    /**
     * Inherit [WindowManager.LayoutParams.FLAG_SECURE] from the parent window and pass it on the
     * window that is using this policy.
     */
    Inherit,

    /**
     * Forces [WindowManager.LayoutParams.FLAG_SECURE] to be set on the window that is using this
     * policy.
     */
    SecureOn,
    /**
     * No [WindowManager.LayoutParams.FLAG_SECURE] will be set on the window that is using this
     * policy.
     */
    SecureOff
}
expect class PlatformDialogProperties(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    securePolicy: PlatformSecureFlagPolicy,
    usePlatformDefaultWidth: Boolean,
    decorFitsSystemWindows: Boolean
) {
    constructor()
}
expect class PlatformBundle() {
    fun getArray(key: String, def: Array<*>): Array<*>

    fun getArray(key: String): Array<*>

    fun getBoolean(key: String): Boolean

    fun getBoolean(key: String, def: Boolean): Boolean

    fun getBooleanArray(key: String): BooleanArray?

    fun getBooleanArray(key: String, def: BooleanArray): BooleanArray

    fun getBundle(key: String, def: PlatformBundle): PlatformBundle

    fun getBundle(key: String): PlatformBundle?

    fun getBytes(key: String, def: Int): Int

    fun getBytes(key: String): Int

    fun getByteArray(key: String): ByteArray?

    fun getByteArray(key: String, def: ByteArray): ByteArray

    fun getChar(key: String): Char

    fun getChar(key: String, def: Char): Char

    fun getCharArray(key: String, def: CharArray): CharArray

    fun getCharArray(key: String): CharArray?

    fun getDouble(key: String, def: Double): Double

    fun getDouble(key: String): Double

    fun getDoubleArray(key: String): DoubleArray?

    fun getDoubleArray(key: String, def: DoubleArray): DoubleArray

    fun getFloat(key: String): Float

    fun getFloat(key: String, def: Float): Float

    fun getFloatArray(key: String): FloatArray?

    fun getFloatArray(key: String, def: FloatArray): FloatArray

    fun getInt(key: String): Int

    fun getInt(key: String, def: Int): Int

    fun getIntArray(key: String): IntArray?

    fun getIntArray(key: String, def: IntArray): IntArray

    fun getLong(key: String): Long

    fun getLong(key: String, def: Long): Long

    fun getLongArray(key: String): LongArray?

    fun getLongArray(key: String, def: LongArray): LongArray

    fun getObject(key: String, def: Any): Any

    fun getObject(key: String): Any?

    fun getShort(key: String, def: Int): Int

    fun getShort(key: String): Int

    fun getShortArray(key: String): ShortArray?

    fun getShortArray(key: String, def: ShortArray): ShortArray

    fun getString(key: String, def: String): String

    fun getString(key: String): String?

    fun getStringArray(key: String, def: Array<String>): Array<String>

    fun getStringArray(key: String): Array<String>

    fun putArray(key: String, value: Array<*>)

    fun putBoolean(key: String, value: Boolean)

    fun putBooleanArray(key: String, value: BooleanArray)

    fun putBundle(key: String, value: PlatformBundle)

    fun putByte(key: String, value: Int)

    fun putByteArray(key: String, value: ByteArray)

    fun putChar(key: String, value: Char)

    fun putCharArray(key: String, value: CharArray)

    fun putDouble(key: String, value: Double)

    fun putDoubleArray(key: String, value: DoubleArray)

    fun putFloat(key: String, value: Float)

    fun putFloatArray(key: String, value: FloatArray)

    fun putInt(key: String, value: Int)

    fun putIntArray(key: String, value: IntArray)

    fun putLong(key: String, value: Long)

    fun putLongArray(key: String, value: LongArray)

    fun putObject(key: String, value: Any)

    fun putShort(key: String, value: Int)

    fun putShortArray(key: String, value: ShortArray)

    fun putString(key: String, value: String)

    fun putStringArray(key: String, value: Array<String>)
}