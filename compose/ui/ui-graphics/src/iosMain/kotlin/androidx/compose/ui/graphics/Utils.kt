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

import cocoapods.Topping.TIOSKHByte
import cocoapods.Topping.TIOSKHFloat
import cocoapods.Topping.TIOSKHInt
import cocoapods.Topping.TIOSKHKotlinArray
import cocoapods.Topping.TIOSKHKotlinByteArray
import cocoapods.Topping.TIOSKHKotlinFloatArray
import cocoapods.Topping.TIOSKHKotlinIntArray
import cocoapods.Topping.TIOSKHKotlinShortArray
import cocoapods.Topping.TIOSKHShort
import cocoapods.Topping.TIOSKHSkikoMatrix33
import cocoapods.Topping.TIOSKHSkikoMatrix44

fun TIOSKHKotlinArray.toArray() = Array(size()) {
    getIndex(it)
}

fun <T> Array<T>.toTIOSKHKotlinArray() = TIOSKHKotlinArray.arrayWithSize(size) {
    this[it!!.intValue]
}

fun FloatArray.toTIOSKotlinFloatArray() = TIOSKHKotlinFloatArray.arrayWithSize(size) {
    TIOSKHFloat(it!!.floatValue)
}

fun IntArray.toTIOSKotlinIntArray() = TIOSKHKotlinIntArray.arrayWithSize(size) {
    TIOSKHInt(it!!.intValue)
}

fun ShortArray.toTIOSKotlinShortArray() = TIOSKHKotlinShortArray.arrayWithSize(size) {
    TIOSKHShort(it!!.intValue)
}

fun ByteArray.toTIOSKotlinByteArray() = TIOSKHKotlinByteArray.arrayWithSize(size) {
    TIOSKHByte(it!!.intValue)
}

inline operator fun TIOSKHSkikoMatrix44.get(row: Int, column: Int) = mat().getIndex((row * 4) + column)

inline operator fun TIOSKHSkikoMatrix44.set(row: Int, column: Int, v: Float) {
    mat().setIndex((row * 4) + column, v)
}