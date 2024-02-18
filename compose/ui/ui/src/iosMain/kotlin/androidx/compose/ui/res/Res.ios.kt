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

package androidx.compose.ui.res

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import cocoapods.Topping.LuaRef

/**
 * Load a color resource.
 *
 * @param id the resource identifier
 * @return the color associated with the resource
 */
@ReadOnlyComposable
@Composable
actual fun colorResource(id: Any): Color {
    return colorResource((id as LuaRef).idRef!!)
}

/**
 * Load an ImageBitmap from an image resource.
 *
 * This function is intended to be used for when low-level ImageBitmap-specific
 * functionality is required.  For simply displaying onscreen, the vector/bitmap-agnostic
 * [painterResource] is recommended instead.
 *
 * @param id the resource identifier
 * @return the decoded image data associated with the resource
 */
@Composable
actual fun ImageBitmap.Companion.imageResource(id: Any): ImageBitmap {
    return imageResource((id as LuaRef).idRef!!)
}

/**
 * Load an integer resource.
 *
 * @param id the resource identifier
 * @return the integer associated with the resource
 */
@ReadOnlyComposable
@Composable
actual fun integerResource(id: Any): Int {
    return integerResource((id as LuaRef).idRef!!)
}

/**
 * Load an array of integer resource.
 *
 * @param id the resource identifier
 * @return the integer array associated with the resource
 */
@ReadOnlyComposable
@Composable
actual fun integerArrayResource(id: Any): IntArray {
    return integerArrayResource((id as LuaRef).idRef!!)
}

/**
 * Load a boolean resource.
 *
 * @param id the resource identifier
 * @return the boolean associated with the resource
 */
@ReadOnlyComposable
@Composable
actual fun booleanResource(id: Any): Boolean {
    return booleanResource((id as LuaRef).idRef!!)
}

/**
 * Load a dimension resource.
 *
 * @param id the resource identifier
 * @return the dimension value associated with the resource
 */
@ReadOnlyComposable
@Composable
actual fun dimensionResource(id: Any): Dp {
    return dimensionResource((id as LuaRef).idRef!!)
}

/**
 * Load a string resource.
 *
 * @param id the resource identifier
 * @return the string data associated with the resource
 */
@ReadOnlyComposable
@Composable
actual fun stringResource(id: Any): String {
    return stringResource((id as LuaRef).idRef!!)
}

/**
 * Load a string resource with formatting.
 *
 * @param id the resource identifier
 * @param formatArgs the format arguments
 * @return the string data associated with the resource
 */
@ReadOnlyComposable
@Composable
actual fun stringResource(
    id: Any,
    vararg formatArgs: Any
): String {
    return stringResource((id as LuaRef).idRef!!, formatArgs)
}

/**
 * Load a string resource.
 *
 * @param id the resource identifier
 * @return the string data associated with the resource
 */
@ReadOnlyComposable
@Composable
actual fun stringArrayResource(id: Any): Array<String> {
    return stringArrayResource((id as LuaRef).idRef!!)
}

/**
 * Load a plurals resource.
 *
 * @param id the resource identifier
 * @param count the count
 * @return the pluralized string data associated with the resource
 */
@ReadOnlyComposable
@Composable
actual fun pluralStringResource(
    id: Any,
    count: Int
): String {
    return pluralStringResource((id as LuaRef).idRef!!, count)
}

/**
 * Load a plurals resource with provided format arguments.
 *
 * @param id the resource identifier
 * @param count the count
 * @param formatArgs arguments used in the format string
 * @return the pluralized string data associated with the resource
 */
@ReadOnlyComposable
@Composable
actual fun pluralStringResource(
    id: Any,
    count: Int,
    vararg formatArgs: Any
): String {
    return pluralStringResource((id as LuaRef).idRef!!, count, formatArgs)
}

/**
 * Load an ImageVector from a vector resource.
 *
 * This function is intended to be used for when low-level ImageVector-specific
 * functionality is required.  For simply displaying onscreen, the vector/bitmap-agnostic
 * [painterResource] is recommended instead.
 *
 * @param id the resource identifier
 * @return the vector data associated with the resource
 */
@Composable
actual fun ImageVector.Companion.vectorResource(id: Any): ImageVector {
    return vectorResource((id as LuaRef).idRef!!)
}

/**
 * Create a [Painter] from an Android resource id. This can load either an instance of
 * [BitmapPainter] or [VectorPainter] for [ImageBitmap] based assets or vector based assets
 * respectively. The resources with the given id must point to either fully rasterized
 * images (ex. PNG or JPG files) or VectorDrawable xml assets. API based xml Drawables
 * are not supported here.
 *
 * Example:
 * @sample androidx.compose.ui.samples.PainterResourceSample
 *
 * Alternative Drawable implementations can be used with compose by calling
 * [drawIntoCanvas] and drawing with the Android framework canvas provided through [nativeCanvas]
 *
 * Example:
 * @sample androidx.compose.ui.samples.AndroidDrawableInDrawScopeSample
 *
 * @param id Resources object to query the image file from
 *
 * @return [Painter] used for drawing the loaded resource
 */
@Composable
actual fun painterResource(id: Any): Painter {
    return painterResource((id as LuaRef).idRef!!)
}