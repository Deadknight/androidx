/*
 * Copyright 2020 The Android Open Source Project
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

import androidx.compose.ui.geometry.Offset
import cocoapods.Topping.TIOSKHSkikoPathMeasure

/**
 * Convert the [org.jetbrains.skia.PathMeasure] instance into a Compose-compatible PathMeasure
 */
fun TIOSKHSkikoPathMeasure.asComposePathEffect(): PathMeasure = SkiaBackedPathMeasure(this)

/**
 * Obtain a reference to skia PathMeasure type
 */
fun PathMeasure.asSkiaPathMeasure(): TIOSKHSkikoPathMeasure =
    (this as SkiaBackedPathMeasure).skia

internal class SkiaBackedPathMeasure(
    internal val skia: TIOSKHSkikoPathMeasure = TIOSKHSkikoPathMeasure()
) : PathMeasure {

    override fun setPath(path: Path?, forceClosed: Boolean) {
        skia.setPathPath(path?.asSkiaPath(), forceClosed)
    }

    override fun getSegment(
        startDistance: Float,
        stopDistance: Float,
        destination: Path,
        startWithMoveTo: Boolean
    ) = skia.getSegmentStartD(
        startDistance,
        stopDistance,
        destination.asSkiaPath(),
        startWithMoveTo
    )

    override val length: Float
        get() = skia.length()

    override fun getPosition(
        distance: Float
    ): Offset {
        val result = skia.getPositionDistance(distance)
        return if (result != null) {
            Offset(result.x(), result.y())
        } else {
            Offset.Unspecified
        }
    }

    override fun getTangent(
        distance: Float
    ): Offset {
        val result = skia.getTangentDistance(distance)
        return if (result != null) {
            Offset(result.x(), result.y())
        } else {
            Offset.Unspecified
        }
    }
}

actual fun PathMeasure(): PathMeasure =
    SkiaBackedPathMeasure()
