/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.compose.ui.graphics

import cocoapods.Topping.TIOSKHSkikoColorFilter as SkiaColorFilter
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toSkia
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinFloatArray
import cocoapods.Topping.TIOSKHSkikoColorMatrix

actual typealias NativeColorFilter = SkiaColorFilter

/**
 * Obtain a [org.jetbrains.skia.ColorFilter] instance from this [ColorFilter]
 */
fun ColorFilter.asSkiaColorFilter(): SkiaColorFilter = nativeColorFilter

/**
 * Create a [ColorFilter] from the given [org.jetbrains.skia.ColorFilter] instance
 */
fun SkiaColorFilter.asComposeColorFilter(): ColorFilter = ColorFilter(this)

internal actual fun actualTintColorFilter(color: Color, blendMode: BlendMode): NativeColorFilter =
    SkiaColorFilter.companion().makeBlendColor(color.toArgb(), blendMode.toSkia())

internal actual fun actualColorMatrixColorFilter(colorMatrix: ColorMatrix): NativeColorFilter =
    SkiaColorFilter.companion().makeMatrixMatrix(
        TIOSKHSkikoColorMatrix(colorMatrix.values.toTIOSKotlinFloatArray())
    )

internal actual fun actualLightingColorFilter(multiply: Color, add: Color): NativeColorFilter =
    SkiaColorFilter.companion().makeLightingColorMul(multiply.toArgb(), add.toArgb())

internal actual fun actualColorMatrixFromFilter(filter: NativeColorFilter): ColorMatrix =
    ColorMatrix()
