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

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.ui.util.ID
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination
import androidx.navigation.NavDestinationBuilder
import androidx.navigation.NavDirections
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.NavigatorProvider

actual typealias PlatformNavGraph = androidx.navigation.NavGraph
@SuppressWarnings("TopLevelBuilder", "MissingBuildMethod", "MissingGetterMatchingBuilder")
actual class PlatformNavGraphBuilder {
    private val navGraphBuilder: NavGraphBuilder

    actual constructor(
        provider: PlatformNavigatonProvider,
        id: ID,
        startDestination: ID
    ) {
        navGraphBuilder = NavGraphBuilder(provider = provider, id = id, startDestination = startDestination)
    }

    @SuppressWarnings("OptionalBuilderConstructorArgument")
    actual constructor(
        provider: PlatformNavigatonProvider,
        startDestination: String,
        @SuppressWarnings("OptionalBuilderConstructorArgument")
        route: String?
    ) {
        navGraphBuilder = NavGraphBuilder(provider = provider, startDestination = startDestination, route = route)
    }

    @SuppressWarnings("BuilderSetStyle")
    actual fun <D : PlatformNavDestination> destination(navDestination: PlatformNavDestinationBuilder<D>) {
        navGraphBuilder.destination(navDestination.navDestinationBuilder)
    }

    @SuppressWarnings("MissingGetterMatchingBuilder", "SetterReturnsThis")
    actual fun addDestination(destination: PlatformNavDestination) {
        navGraphBuilder.addDestination(destination)
    }

    actual fun build(): PlatformNavGraph {
        return navGraphBuilder.build()
    }

    @SuppressWarnings("GetterOnBuilder")
    actual fun getPlatform(): Any {
        return navGraphBuilder
    }
}
@SuppressWarnings("TopLevelBuilder", "GetterOnBuilder", "MissingBuildMethod", "OptionalBuilderConstructorArgument")
actual class PlatformNavDestinationBuilder<T : NavDestination>(
    internal val navDestinationBuilder: NavDestinationBuilder<T>
)
actual typealias PlatformNavigatonProvider = NavigatorProvider
actual typealias PlatformNavBackStackEntry = androidx.navigation.NavBackStackEntry
actual typealias PlatformNavDestination = androidx.navigation.NavDestination
actual typealias PlatformNavOptions = NavOptions
actual typealias PlatformNavOptionsBuilder = NavOptionsBuilder
actual typealias PlatformNavigatorExtras = Navigator.Extras
actual typealias PlatformNamedNavArgument = NamedNavArgument
actual typealias PlatformNavDeepLink = NavDeepLink

actual class PlatformDialogProperties actual constructor(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    securePolicy: PlatformSecureFlagPolicy,
    usePlatformDefaultWidth: Boolean,
    decorFitsSystemWindows: Boolean
) {
    val properties = DialogProperties(dismissOnBackPress, dismissOnClickOutside,
        when(securePolicy) {
            PlatformSecureFlagPolicy.Inherit -> SecureFlagPolicy.Inherit
            PlatformSecureFlagPolicy.SecureOn -> SecureFlagPolicy.SecureOn
            PlatformSecureFlagPolicy.SecureOff -> SecureFlagPolicy.SecureOff
        },
        usePlatformDefaultWidth, decorFitsSystemWindows)

    actual constructor() : this(true, true, PlatformSecureFlagPolicy.Inherit, true, true)
}

actual class PlatformNavigator<T : PlatformNavDestination>(val navigator: Navigator<T>)
actual typealias PlatformNavDirections = NavDirections

actual open class PlatformNavHostController(val navHostController: NavHostController) {
    /**
     * Navigate via the given [PlatformNavDirections]
     *
     * @param directions directions that describe this navigation operation
     */
    actual open fun navigate(directions: PlatformNavDirections) {
        navHostController.navigate(directions, null)
    }

    /**
     * Navigate via the given [PlatformNavDirections]
     *
     * @param directions directions that describe this navigation operation
     * @param navOptions special options for this navigation operation
     */
    actual open fun navigate(
        directions: PlatformNavDirections,
        navOptions: PlatformNavOptions?
    ) {
        navHostController.navigate(directions, navOptions)
    }

    /**
     * Navigate via the given [PlatformNavDirections]
     *
     * @param directions directions that describe this navigation operation
     * @param navigatorExtras extras to pass to the [PlatformNavigator]
     */
    actual open fun navigate(
        directions: PlatformNavDirections,
        navigatorExtras: PlatformNavigatorExtras
    ) {
        navHostController.navigate(directions, navigatorExtras)
    }

    /**
     * Navigate to a route in the current NavGraph. If an invalid route is given, an
     * [IllegalArgumentException] will be thrown.
     *
     * @param route route for the destination
     * @param builder DSL for constructing a new [PlatformNavOptions]
     *
     * @throws IllegalArgumentException if the given route is invalid
     */
    actual fun navigate(
        route: String,
        builder: PlatformNavOptionsBuilder.() -> Unit
    ) {
        navHostController.navigate(route, builder)
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
    actual open fun navigate(resId: ID) {
        navHostController.navigate(resId, null as Bundle?)
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
    actual open fun navigate(
        resId: ID,
        args: PlatformBundle?
    ) {
        navHostController.navigate(resId, args?.bundle, null)
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
    actual open fun navigate(
        resId: ID,
        args: PlatformBundle?,
        navOptions: PlatformNavOptions?
    ) {
        navHostController.navigate(resId, args?.bundle, navOptions, null)
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
    actual open fun navigate(
        resId: ID,
        args: PlatformBundle?,
        navOptions: PlatformNavOptions?,
        navigatorExtras: PlatformNavigatorExtras?
    ) {
        navHostController.navigate(resId, args?.bundle, navOptions, navigatorExtras)
    }

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
    actual fun navigate(
        route: String,
        navOptions: PlatformNavOptions?,
        navigatorExtras: PlatformNavigatorExtras?
    ) {
        navHostController.navigate(route,  navOptions, navigatorExtras)
    }

    /**
     * Attempts to pop the controller's back stack. Analogous to when the user presses
     * the system [Back][android.view.KeyEvent.KEYCODE_BACK] button when the associated
     * navigation host has focus.
     *
     * @return true if the stack was popped at least once and the user has been navigated to
     * another destination, false otherwise
     */
    actual open fun popBackStack(): Boolean {
        return navHostController.popBackStack()
    }

    /**
     * Attempts to pop the controller's back stack. Analogous to when the user presses
     * the system [Back][android.view.KeyEvent.KEYCODE_BACK] button when the associated
     * navigation host has focus.
     *
     * @return true if the stack was popped at least once and the user has been navigated to
     * another destination, false otherwise
     */
    actual open fun navigateUp(): Boolean {
        return navHostController.navigateUp()
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
    actual open fun popBackStack(
        destinationId: ID,
        inclusive: Boolean
    ): Boolean {
        return navHostController.popBackStack(destinationId, inclusive, false)
    }

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
    actual open fun popBackStackId(
        destinationId: ID,
        inclusive: Boolean,
        saveState: Boolean
    ): Boolean {
        return navHostController.popBackStack(destinationId, inclusive, saveState)
    }

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
    actual fun popBackStack(
        route: String,
        inclusive: Boolean,
        saveState: Boolean
    ): Boolean {
        return navHostController.popBackStack(route, inclusive, saveState)
    }
}

actual class PlatformBundle actual constructor() {
    var bundle: Bundle = Bundle()

    constructor(bundle: Bundle) : this() {
        this.bundle = bundle
    }

    actual fun getArray(key: String, def: Array<*>): Array<*> {
        return bundle.getParcelableArray(key) ?: def
    }

    actual fun getArray(key: String): Array<*> {
        return bundle.getParcelableArray(key) ?: arrayOf<Any>()
    }

    actual fun getBoolean(key: String): Boolean {
        return bundle.getBoolean(key)
    }

    actual fun getBoolean(key: String, def: Boolean): Boolean {
        return bundle.getBoolean(key, def)
    }

    actual fun getBooleanArray(key: String): BooleanArray? {
        return bundle.getBooleanArray(key)?.toMutableList()?.toBooleanArray()
    }

    actual fun getBooleanArray(key: String, def: BooleanArray): BooleanArray {
        return bundle.getBooleanArray(key) ?: def
    }

    actual fun getBundle(key: String, def: PlatformBundle): PlatformBundle {
        return PlatformBundle(bundle.getBundle(key) ?: def.bundle)
    }

    actual fun getBundle(key: String): PlatformBundle? {
        val bnd = bundle.getBundle(key) ?: return null
        return PlatformBundle(bnd)
    }

    actual fun getBytes(key: String, def: Int): Int {
        return bundle.getByte(key, def.toByte()).toInt()
    }

    actual fun getBytes(key: String): Int {
        return bundle.getByte(key).toInt()
    }

    actual fun getByteArray(key: String): ByteArray? {
        return bundle.getByteArray(key)
    }

    actual fun getByteArray(key: String, def: ByteArray): ByteArray {
        return bundle.getByteArray(key) ?: def
    }

    actual fun getChar(key: String): Char {
        return bundle.getChar(key)
    }

    actual fun getChar(key: String, def: Char): Char {
        return bundle.getChar(key, def)
    }

    actual fun getCharArray(key: String, def: CharArray): CharArray {
        return bundle.getCharArray(key) ?: def
    }

    actual fun getCharArray(key: String): CharArray? {
        return bundle.getCharArray(key)
    }

    actual fun getDouble(key: String, def: Double): Double {
        return bundle.getDouble(key, def)
    }

    actual fun getDouble(key: String): Double {
        return bundle.getDouble(key)
    }

    actual fun getDoubleArray(key: String): DoubleArray? {
        return bundle.getDoubleArray(key)
    }

    actual fun getDoubleArray(key: String, def: DoubleArray): DoubleArray {
        return bundle.getDoubleArray(key) ?: def
    }

    actual fun getFloat(key: String): Float {
        return bundle.getFloat(key)
    }

    actual fun getFloat(key: String, def: Float): Float {
        return bundle.getFloat(key, def)
    }

    actual fun getFloatArray(key: String): FloatArray? {
        return bundle.getFloatArray(key)
    }

    actual fun getFloatArray(key: String, def: FloatArray): FloatArray {
        return bundle.getFloatArray(key) ?: def
    }

    actual fun getInt(key: String): Int {
        return bundle.getInt(key)
    }

    actual fun getInt(key: String, def: Int): Int {
        return bundle.getInt(key, def)
    }

    actual fun getIntArray(key: String): IntArray? {
        return bundle.getIntArray(key)
    }

    actual fun getIntArray(key: String, def: IntArray): IntArray {
        return bundle.getIntArray(key) ?: def
    }

    actual fun getLong(key: String): Long {
        return bundle.getLong(key)
    }

    actual fun getLong(key: String, def: Long): Long {
        return bundle.getLong(key, def)
    }

    actual fun getLongArray(key: String): LongArray? {
        return bundle.getLongArray(key)
    }

    actual fun getLongArray(key: String, def: LongArray): LongArray {
        return bundle.getLongArray(key) ?: def
    }

    actual fun getObject(key: String, def: Any): Any {
        return bundle.getParcelable(key) ?: def
    }

    actual fun getObject(key: String): Any? {
        return bundle.getParcelable(key)
    }

    actual fun getShort(key: String, def: Int): Int {
        return bundle.getShort(key, def.toShort()).toInt()
    }

    actual fun getShort(key: String): Int {
        return bundle.getShort(key).toInt()
    }

    actual fun getShortArray(key: String): ShortArray? {
        return bundle.getShortArray(key)
    }

    actual fun getShortArray(key: String, def: ShortArray): ShortArray {
        return bundle.getShortArray(key) ?: def
    }

    actual fun getString(key: String, def: String): String {
        return bundle.getString(key, def)!!
    }

    actual fun getString(key: String): String? {
        return bundle.getString(key)
    }

    actual fun getStringArray(key: String, def: Array<String>): Array<String> {
        return bundle.getStringArray(key) ?: def
    }

    actual fun getStringArray(key: String): Array<String> {
        return bundle.getStringArray(key) ?: arrayOf()
    }

    actual fun putArray(key: String, value: Array<*>) {
        bundle.putParcelableArray(key, value as Array<out Parcelable>)
    }

    actual fun putBoolean(key: String, value: Boolean) {
        bundle.putBoolean(key, value)
    }

    actual fun putBooleanArray(key: String, value: BooleanArray) {
        bundle.putBooleanArray(key, value)
    }

    actual fun putBundle(key: String, value: PlatformBundle) {
        bundle.putBundle(key, value.bundle)
    }

    actual fun putByte(key: String, value: Int) {
        bundle.putByte(key, value.toByte())
    }

    actual fun putByteArray(key: String, value: ByteArray) {
        bundle.putByteArray(key, value)
    }

    actual fun putChar(key: String, value: Char) {
        bundle.putChar(key, value)
    }

    actual fun putCharArray(key: String, value: CharArray) {
        bundle.putCharArray(key, value)
    }

    actual fun putDouble(key: String, value: Double) {
        bundle.putDouble(key, value)
    }

    actual fun putDoubleArray(key: String, value: DoubleArray) {
        bundle.putDoubleArray(key, value)
    }

    actual fun putFloat(key: String, value: Float) {
        bundle.putFloat(key, value)
    }

    actual fun putFloatArray(key: String, value: FloatArray) {
        bundle.putFloatArray(key, value)
    }

    actual fun putInt(key: String, value: Int) {
        bundle.putInt(key, value)
    }

    actual fun putIntArray(key: String, value: IntArray) {
        bundle.putIntArray(key, value)
    }

    actual fun putLong(key: String, value: Long) {
        bundle.putLong(key, value)
    }

    actual fun putLongArray(key: String, value: LongArray) {
        bundle.putLongArray(key, value)
    }

    actual fun putObject(key: String, value: Any) {
        bundle.putParcelable(key, value as Parcelable)
    }

    actual fun putShort(key: String, value: Int) {
        bundle.putShort(key, value.toShort())
    }

    actual fun putShortArray(key: String, value: ShortArray) {
        bundle.putShortArray(key, value)
    }

    actual fun putString(key: String, value: String) {
        bundle.putString(key, value)
    }

    actual fun putStringArray(key: String, value: Array<String>) {
        bundle.putStringArray(key, value)
    }
}