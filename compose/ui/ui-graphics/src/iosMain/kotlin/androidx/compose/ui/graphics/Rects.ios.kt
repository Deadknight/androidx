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
package androidx.compose.ui.graphics.androidx.compose.ui.graphics

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import cocoapods.ToppingCompose.TIOSKHFloat
import cocoapods.ToppingCompose.TIOSKHKotlinFloatArray
import cocoapods.ToppingCompose.TIOSKHSkikoRRect
import cocoapods.ToppingCompose.TIOSKHSkikoRect

fun Rect.toSkiaRect(): TIOSKHSkikoRect {
    return TIOSKHSkikoRect.companion().makeLTRBL(
        left,
        top,
        right,
        bottom
    )
}

fun TIOSKHSkikoRect.toComposeRect() =
    androidx.compose.ui.geometry.Rect(left(), top(), right(), bottom())

fun RoundRect.toSkiaRRect(): TIOSKHSkikoRRect {
    val radii = FloatArray(8)

    radii[0] = topLeftCornerRadius.x
    radii[1] = topLeftCornerRadius.y

    radii[2] = topRightCornerRadius.x
    radii[3] = topRightCornerRadius.y

    radii[4] = bottomRightCornerRadius.x
    radii[5] = bottomRightCornerRadius.y

    radii[6] = bottomLeftCornerRadius.x
    radii[7] = bottomLeftCornerRadius.y

    val fArr = TIOSKHKotlinFloatArray.arrayWithSize(radii.size) {
        TIOSKHFloat(radii[it!!.intValue])
    }

    return TIOSKHSkikoRRect.companion().makeComplexLTRBL(left, top, right, bottom, fArr)
}
