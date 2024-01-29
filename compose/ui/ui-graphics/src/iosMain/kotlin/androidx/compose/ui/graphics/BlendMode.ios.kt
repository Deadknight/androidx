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

package androidx.compose.ui.graphics.androidx.compose.ui.graphics

import androidx.compose.ui.graphics.BlendMode
import cocoapods.Topping.TIOSKHSkikoBlendMode

internal fun BlendMode.toSkia() = when (this) {
    BlendMode.Clear -> TIOSKHSkikoBlendMode.clear()
    BlendMode.Src -> TIOSKHSkikoBlendMode.src()
    BlendMode.Dst -> TIOSKHSkikoBlendMode.dst()
    BlendMode.SrcOver -> TIOSKHSkikoBlendMode.srcOver()
    BlendMode.DstOver -> TIOSKHSkikoBlendMode.dstOver()
    BlendMode.SrcIn -> TIOSKHSkikoBlendMode.srcIn()
    BlendMode.DstIn -> TIOSKHSkikoBlendMode.dstIn()
    BlendMode.SrcOut ->TIOSKHSkikoBlendMode.srcIn()
    BlendMode.DstOut -> TIOSKHSkikoBlendMode.dstOut()
    BlendMode.SrcAtop -> TIOSKHSkikoBlendMode.srcAtop()
    BlendMode.DstAtop -> TIOSKHSkikoBlendMode.dstAtop()
    BlendMode.Xor -> TIOSKHSkikoBlendMode.xor_()
    BlendMode.Plus -> TIOSKHSkikoBlendMode.plus()
    BlendMode.Modulate -> TIOSKHSkikoBlendMode.modulate()
    BlendMode.Screen -> TIOSKHSkikoBlendMode.screen()
    BlendMode.Overlay -> TIOSKHSkikoBlendMode.overlay()
    BlendMode.Darken -> TIOSKHSkikoBlendMode.darken()
    BlendMode.Lighten -> TIOSKHSkikoBlendMode.lighten()
    BlendMode.ColorDodge -> TIOSKHSkikoBlendMode.colorDodge()
    BlendMode.ColorBurn -> TIOSKHSkikoBlendMode.colorBurn()
    BlendMode.Hardlight -> TIOSKHSkikoBlendMode.hardLight()
    BlendMode.Softlight -> TIOSKHSkikoBlendMode.softLight()
    BlendMode.Difference -> TIOSKHSkikoBlendMode.difference()
    BlendMode.Exclusion -> TIOSKHSkikoBlendMode.exclusion()
    BlendMode.Multiply -> TIOSKHSkikoBlendMode.multiply()
    BlendMode.Hue -> TIOSKHSkikoBlendMode.hue()
    BlendMode.Saturation -> TIOSKHSkikoBlendMode.saturation()
    BlendMode.Color -> TIOSKHSkikoBlendMode.color()
    BlendMode.Luminosity -> TIOSKHSkikoBlendMode.luminosity()
    // Always fallback to default blendmode of src over
    else -> TIOSKHSkikoBlendMode.srcOver()
}