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

actual open class PlatformNavHostController {
    /**
     * Navigate via the given [PlatformNavDirections]
     *
     * @param directions directions that describe this navigation operation
     */
    actual open fun navigate(directions: PlatformNavDirections) {
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
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }
}
actual class PlatformNavGraph
actual class PlatformNavGraphBuilder {
    actual fun <D : PlatformNavDestination> destination(navDestination: PlatformNavDestinationBuilder<D>) {
    }

    actual fun addDestination(destination: PlatformNavDestination) {
    }

    actual fun build(): PlatformNavGraph {
        TODO("Not yet implemented")
    }

    actual constructor(
        provider: PlatformNavigatonProvider,
        id: ID,
        startDestination: ID
    ) {
        TODO("Not yet implemented")
    }

    actual constructor(
        provider: PlatformNavigatonProvider,
        startDestination: String,
        route: String?
    ) {
        TODO("Not yet implemented")
    }

    actual fun getPlatform(): Any {
        TODO("Not yet implemented")
    }
}
actual class PlatformNavDestinationBuilder<T : PlatformNavDestination>()
actual class PlatformNavigatonProvider
actual class PlatformNavBackStackEntry

actual class PlatformNavigator<T : PlatformNavDestination>
actual class PlatformNavDestination
actual interface PlatformNavDirections

actual class PlatformNavOptions
actual class PlatformNavOptionsBuilder
actual interface PlatformNavigatorExtras
actual class PlatformNamedNavArgument
actual class PlatformNavDeepLink
actual class PlatformDialogProperties actual constructor(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    securePolicy: PlatformSecureFlagPolicy,
    usePlatformDefaultWidth: Boolean,
    decorFitsSystemWindows: Boolean
) {
    actual constructor() : this(true, true, PlatformSecureFlagPolicy.Inherit, true, true)
}

actual class PlatformBundle {
    actual fun getArray(key: String, def: Array<*>): Array<*> {
        TODO("Not yet implemented")
    }

    actual fun getArray(key: String): Array<*> {
        TODO("Not yet implemented")
    }

    actual fun getBoolean(key: String): Boolean {
        TODO("Not yet implemented")
    }

    actual fun getBoolean(key: String, def: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    actual fun getBooleanArray(key: String): BooleanArray? {
        TODO("Not yet implemented")
    }

    actual fun getBooleanArray(key: String, def: BooleanArray): BooleanArray {
        TODO("Not yet implemented")
    }

    actual fun getBundle(key: String, def: PlatformBundle): PlatformBundle {
        TODO("Not yet implemented")
    }

    actual fun getBundle(key: String): PlatformBundle? {
        TODO("Not yet implemented")
    }

    actual fun getBytes(key: String, def: Int): Int {
        TODO("Not yet implemented")
    }

    actual fun getBytes(key: String): Int {
        TODO("Not yet implemented")
    }

    actual fun getByteArray(key: String): ByteArray? {
        TODO("Not yet implemented")
    }

    actual fun getByteArray(key: String, def: ByteArray): ByteArray {
        TODO("Not yet implemented")
    }

    actual fun getChar(key: String): Char {
        TODO("Not yet implemented")
    }

    actual fun getChar(key: String, def: Char): Char {
        TODO("Not yet implemented")
    }

    actual fun getCharArray(key: String, def: CharArray): CharArray {
        TODO("Not yet implemented")
    }

    actual fun getCharArray(key: String): CharArray? {
        TODO("Not yet implemented")
    }

    actual fun getDouble(key: String, def: Double): Double {
        TODO("Not yet implemented")
    }

    actual fun getDouble(key: String): Double {
        TODO("Not yet implemented")
    }

    actual fun getDoubleArray(key: String): DoubleArray? {
        TODO("Not yet implemented")
    }

    actual fun getDoubleArray(key: String, def: DoubleArray): DoubleArray {
        TODO("Not yet implemented")
    }

    actual fun getFloat(key: String): Float {
        TODO("Not yet implemented")
    }

    actual fun getFloat(key: String, def: Float): Float {
        TODO("Not yet implemented")
    }

    actual fun getFloatArray(key: String): FloatArray? {
        TODO("Not yet implemented")
    }

    actual fun getFloatArray(key: String, def: FloatArray): FloatArray {
        TODO("Not yet implemented")
    }

    actual fun getInt(key: String): Int {
        TODO("Not yet implemented")
    }

    actual fun getInt(key: String, def: Int): Int {
        TODO("Not yet implemented")
    }

    actual fun getIntArray(key: String): IntArray? {
        TODO("Not yet implemented")
    }

    actual fun getIntArray(key: String, def: IntArray): IntArray {
        TODO("Not yet implemented")
    }

    actual fun getLong(key: String): Long {
        TODO("Not yet implemented")
    }

    actual fun getLong(key: String, def: Long): Long {
        TODO("Not yet implemented")
    }

    actual fun getLongArray(key: String): LongArray? {
        TODO("Not yet implemented")
    }

    actual fun getLongArray(key: String, def: LongArray): LongArray {
        TODO("Not yet implemented")
    }

    actual fun getObject(key: String, def: Any): Any {
        TODO("Not yet implemented")
    }

    actual fun getObject(key: String): Any? {
        TODO("Not yet implemented")
    }

    actual fun getShort(key: String, def: Int): Int {
        TODO("Not yet implemented")
    }

    actual fun getShort(key: String): Int {
        TODO("Not yet implemented")
    }

    actual fun getShortArray(key: String): ShortArray? {
        TODO("Not yet implemented")
    }

    actual fun getShortArray(key: String, def: ShortArray): ShortArray {
        TODO("Not yet implemented")
    }

    actual fun getString(key: String, def: String): String {
        TODO("Not yet implemented")
    }

    actual fun getString(key: String): String? {
        TODO("Not yet implemented")
    }

    actual fun getStringArray(key: String, def: Array<String>): Array<String> {
        TODO("Not yet implemented")
    }

    actual fun getStringArray(key: String): Array<String> {
        TODO("Not yet implemented")
    }

    actual fun putArray(key: String, value: Array<*>) {
        TODO("Not yet implemented")
    }

    actual fun putBoolean(key: String, value: Boolean) {
        TODO("Not yet implemented")
    }

    actual fun putBooleanArray(key: String, value: BooleanArray) {
        TODO("Not yet implemented")
    }

    actual fun putBundle(key: String, value: PlatformBundle) {
        TODO("Not yet implemented")
    }

    actual fun putByte(key: String, value: Int) {
        TODO("Not yet implemented")
    }

    actual fun putByteArray(key: String, value: ByteArray) {
        TODO("Not yet implemented")
    }

    actual fun putChar(key: String, value: Char) {
        TODO("Not yet implemented")
    }

    actual fun putCharArray(key: String, value: CharArray) {
        TODO("Not yet implemented")
    }

    actual fun putDouble(key: String, value: Double) {
        TODO("Not yet implemented")
    }

    actual fun putDoubleArray(key: String, value: DoubleArray) {
        TODO("Not yet implemented")
    }

    actual fun putFloat(key: String, value: Float) {
        TODO("Not yet implemented")
    }

    actual fun putFloatArray(key: String, value: FloatArray) {
        TODO("Not yet implemented")
    }

    actual fun putInt(key: String, value: Int) {
        TODO("Not yet implemented")
    }

    actual fun putIntArray(key: String, value: IntArray) {
        TODO("Not yet implemented")
    }

    actual fun putLong(key: String, value: Long) {
        TODO("Not yet implemented")
    }

    actual fun putLongArray(key: String, value: LongArray) {
        TODO("Not yet implemented")
    }

    actual fun putObject(key: String, value: Any) {
        TODO("Not yet implemented")
    }

    actual fun putShort(key: String, value: Int) {
        TODO("Not yet implemented")
    }

    actual fun putShortArray(key: String, value: ShortArray) {
        TODO("Not yet implemented")
    }

    actual fun putString(key: String, value: String) {
        TODO("Not yet implemented")
    }

    actual fun putStringArray(key: String, value: Array<String>) {
        TODO("Not yet implemented")
    }
}