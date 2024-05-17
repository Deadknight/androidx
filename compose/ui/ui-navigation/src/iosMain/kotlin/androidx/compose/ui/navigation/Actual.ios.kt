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

import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toMutableList
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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import cocoapods.ToppingCompose.LuaBundle
import cocoapods.ToppingCompose.NavBackStackEntry
import cocoapods.ToppingCompose.NavDestination
import cocoapods.ToppingCompose.NavGraph
import cocoapods.ToppingCompose.NavOptions
import cocoapods.ToppingCompose.NavigationProvider
import cocoapods.ToppingCompose.Navigator
import cocoapods.ToppingCompose.NavigatorExtrasProtocol

actual typealias PlatformNavGraph = NavGraph
actual typealias PlatformNavBackStackEntry = NavBackStackEntry
actual class PlatformNavGraphBuilder {
    val navGraphBuilder: NavGraphBuilder

    actual constructor(
        provider: PlatformNavigatonProvider,
        id: ID,
        startDestination: ID
    ) {
        navGraphBuilder = NavGraphBuilder(provider = provider, id = id, startDestination = startDestination)
    }

    actual constructor(
        provider: PlatformNavigatonProvider,
        startDestination: String,
        route: String?
    ) {
        navGraphBuilder = NavGraphBuilder(provider = provider, startDestination = startDestination, route = route)
    }

    actual fun <D : PlatformNavDestination> destination(navDestination: PlatformNavDestinationBuilder<D>) {
        navGraphBuilder.destination(navDestination.navDestinationBuilder)
    }

    actual fun addDestination(destination: PlatformNavDestination) {
        navGraphBuilder.addDestination(destination)
    }

    actual fun build(): PlatformNavGraph {
        return navGraphBuilder.build()
    }

    actual fun getPlatform(): Any {
        return navGraphBuilder
    }
}
actual class PlatformNavDestinationBuilder<T : PlatformNavDestination>(val navDestinationBuilder: NavDestinationBuilder)
actual typealias PlatformNavigatonProvider = NavigationProvider
actual typealias PlatformNavDestination = NavDestination
actual typealias PlatformNavOptions = NavOptions
actual typealias PlatformNavOptionsBuilder = NavOptionsBuilder
actual typealias PlatformNavigatorExtras = NavigatorExtrasProtocol
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

actual class PlatformNavigator<T : PlatformNavDestination>(val navigator: Navigator)
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
        navHostController.navigateId(resId, null as LuaBundle?)
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
        navHostController.navigateId(resId, args?.bundle, null)
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
        navHostController.navigateId(resId, args?.bundle, navOptions, null)
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
        navHostController.navigateId(resId, args?.bundle, navOptions, navigatorExtras)
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
        return popBackStackId(destinationId, inclusive, false)
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
        return navHostController.popBackStackId(destinationId, inclusive, saveState)
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
    var bundle: LuaBundle = LuaBundle()

    constructor(bundle: LuaBundle) : this() {
        this.bundle = bundle
    }

    actual fun getArray(key: String, def: Array<*>): Array<*> {
        val defArr = def.toTIOSKHKotlinArray()
        return bundle.getArray(key, defArr)?.toArray() ?: def
    }

    actual fun getArray(key: String): Array<*> {
        return bundle.getArray(key)?.toArray() ?: arrayOf<Any>()
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
        val defArr = def.toTIOSKotlinBooleanArray()
        return bundle.getBooleanArray(key, defArr)?.toMutableList()?.toBooleanArray() ?: def
    }

    actual fun getBundle(key: String, def: PlatformBundle): PlatformBundle {
        return PlatformBundle(bundle.getBundle(key, def.bundle) ?: def.bundle)
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
        return bundle.getByteArray(key)?.toMutableList()?.toByteArray()
    }

    actual fun getByteArray(key: String, def: ByteArray): ByteArray {
        val defArr = def.toTIOSKotlinByteArray()
        return bundle.getByteArray(key, defArr)?.toMutableList()?.toByteArray() ?: def
    }

    actual fun getChar(key: String): Char {
        return bundle.getChar(key).toInt().toChar()
    }

    actual fun getChar(key: String, def: Char): Char {
        return bundle.getChar(key, def.code.toByte()).toInt().toChar()
    }

    actual fun getCharArray(key: String, def: CharArray): CharArray {
        val defArr = def.toTIOSKotlinCharArray()
        return bundle.getCharArray(key, defArr)?.toMutableList()?.toCharArray() ?: def
    }

    actual fun getCharArray(key: String): CharArray? {
        return bundle.getCharArray(key)?.toMutableList()?.toCharArray()
    }

    actual fun getDouble(key: String, def: Double): Double {
        return bundle.getDouble(key, def)
    }

    actual fun getDouble(key: String): Double {
        return bundle.getDouble(key)
    }

    actual fun getDoubleArray(key: String): DoubleArray? {
        return bundle.getDoubleArray(key)?.toMutableList()?.toDoubleArray()
    }

    actual fun getDoubleArray(key: String, def: DoubleArray): DoubleArray {
        val defArr = def.toTIOSKotlinDoubleArray()
        return bundle.getDoubleArray(key, defArr)?.toMutableList()?.toDoubleArray() ?: def
    }

    actual fun getFloat(key: String): Float {
        return bundle.getFloat(key)
    }

    actual fun getFloat(key: String, def: Float): Float {
        return bundle.getFloat(key, def)
    }

    actual fun getFloatArray(key: String): FloatArray? {
        return bundle.getFloatArray(key)?.toMutableList()?.toFloatArray()
    }

    actual fun getFloatArray(key: String, def: FloatArray): FloatArray {
        val defArr = def.toTIOSKotlinFloatArray()
        return bundle.getFloatArray(key, defArr)?.toMutableList()?.toFloatArray() ?: def
    }

    actual fun getInt(key: String): Int {
        return bundle.getInt(key)
    }

    actual fun getInt(key: String, def: Int): Int {
        return bundle.getInt(key, def)
    }

    actual fun getIntArray(key: String): IntArray? {
        return bundle.getIntArray(key)?.toMutableList()?.toIntArray()
    }

    actual fun getIntArray(key: String, def: IntArray): IntArray {
        val defArr = def.toTIOSKotlinIntArray()
        return bundle.getIntArray(key, defArr)?.toMutableList()?.toIntArray() ?: def
    }

    actual fun getLong(key: String): Long {
        return bundle.getLong(key)
    }

    actual fun getLong(key: String, def: Long): Long {
        return bundle.getLong(key, def)
    }

    actual fun getLongArray(key: String): LongArray? {
        return bundle.getLongArray(key)?.toMutableList()?.toLongArray()
    }

    actual fun getLongArray(key: String, def: LongArray): LongArray {
        val defArr = def.toTIOSKotlinLongArray()
        return bundle.getLongArray(key, defArr)?.toMutableList()?.toLongArray() ?: def
    }

    actual fun getObject(key: String, def: Any): Any {
        return bundle.getObject(key, def) ?: def
    }

    actual fun getObject(key: String): Any? {
        return bundle.getObject(key)
    }

    actual fun getShort(key: String, def: Int): Int {
        return bundle.getShort(key, def.toShort()).toInt()
    }

    actual fun getShort(key: String): Int {
        return bundle.getShort(key).toInt()
    }

    actual fun getShortArray(key: String): ShortArray? {
        return bundle.getShortArray(key)?.toMutableList()?.toShortArray()
    }

    actual fun getShortArray(key: String, def: ShortArray): ShortArray {
        val defArr = def.toTIOSKotlinShortArray()
        return bundle.getShortArray(key, defArr)?.toMutableList()?.toShortArray() ?: def
    }

    actual fun getString(key: String, def: String): String {
        return bundle.getString(key, def)!!
    }

    actual fun getString(key: String): String? {
        return bundle.getString(key)
    }

    actual fun getStringArray(key: String, def: Array<String>): Array<String> {
        val defArr = def.toTIOSKHKotlinArray()
        return bundle.getStringArray(key, defArr)?.toMutableList<String>()?.toTypedArray() ?: def
    }

    actual fun getStringArray(key: String): Array<String> {
        return bundle.getStringArray(key)?.toMutableList<String>()?.toTypedArray() ?: arrayOf()
    }

    actual fun putArray(key: String, value: Array<*>) {
        bundle.putArray(key, value.toTIOSKHKotlinArray())
    }

    actual fun putBoolean(key: String, value: Boolean) {
        bundle.putBoolean(key, value)
    }

    actual fun putBooleanArray(key: String, value: BooleanArray) {
        bundle.putBooleanArray(key, value.toTIOSKotlinBooleanArray())
    }

    actual fun putBundle(key: String, value: PlatformBundle) {
        bundle.putBundle(key, value.bundle)
    }

    actual fun putByte(key: String, value: Int) {
        bundle.putByte(key, value.toByte())
    }

    actual fun putByteArray(key: String, value: ByteArray) {
        bundle.putByteArray(key, value.toTIOSKotlinByteArray())
    }

    actual fun putChar(key: String, value: Char) {
        bundle.putChar(key, value.code.toByte())
    }

    actual fun putCharArray(key: String, value: CharArray) {
        bundle.putCharArray(key, value.toTIOSKotlinCharArray())
    }

    actual fun putDouble(key: String, value: Double) {
        bundle.putDouble(key, value)
    }

    actual fun putDoubleArray(key: String, value: DoubleArray) {
        bundle.putDoubleArray(key, value.toTIOSKotlinDoubleArray())
    }

    actual fun putFloat(key: String, value: Float) {
        bundle.putFloat(key, value)
    }

    actual fun putFloatArray(key: String, value: FloatArray) {
        bundle.putFloatArray(key, value.toTIOSKotlinFloatArray())
    }

    actual fun putInt(key: String, value: Int) {
        bundle.putInt(key, value)
    }

    actual fun putIntArray(key: String, value: IntArray) {
        bundle.putIntArray(key, value.toTIOSKotlinIntArray())
    }

    actual fun putLong(key: String, value: Long) {
        bundle.putLong(key, value)
    }

    actual fun putLongArray(key: String, value: LongArray) {
        bundle.putLongArray(key, value.toTIOSKotlinLongArray())
    }

    actual fun putObject(key: String, value: Any) {
        bundle.putObject(key, value)
    }

    actual fun putShort(key: String, value: Int) {
        bundle.putShort(key, value.toShort())
    }

    actual fun putShortArray(key: String, value: ShortArray) {
        bundle.putShortArray(key, value.toTIOSKotlinShortArray())
    }

    actual fun putString(key: String, value: String) {
        bundle.putString(key, value)
    }

    actual fun putStringArray(key: String, value: Array<String>) {
        bundle.putStringArray(key, value.toTIOSKHKotlinArray())
    }
}