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

import cocoapods.Topping.TIOSKHBoolean
import cocoapods.Topping.TIOSKHByte
import cocoapods.Topping.TIOSKHDouble
import cocoapods.Topping.TIOSKHFloat
import cocoapods.Topping.TIOSKHInt
import cocoapods.Topping.TIOSKHKotlinArray
import cocoapods.Topping.TIOSKHKotlinBooleanArray
import cocoapods.Topping.TIOSKHKotlinByteArray
import cocoapods.Topping.TIOSKHKotlinCharArray
import cocoapods.Topping.TIOSKHKotlinDoubleArray
import cocoapods.Topping.TIOSKHKotlinFloatArray
import cocoapods.Topping.TIOSKHKotlinIntArray
import cocoapods.Topping.TIOSKHKotlinLongArray
import cocoapods.Topping.TIOSKHKotlinShortArray
import cocoapods.Topping.TIOSKHLong
import cocoapods.Topping.TIOSKHShort
import cocoapods.Topping.TIOSKHSkikoMatrix44

fun <E> List<E>.toTIOSKHKotlinArray(): TIOSKHKotlinArray {
    return TIOSKHKotlinArray.arrayWithSize(size) {
        this[it!!.intValue]
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> TIOSKHKotlinArray.toMutableList(): MutableList<T> {
    return MutableList<T>(size()) {
        getIndex(it) as T
    }
}

fun TIOSKHKotlinBooleanArray.toMutableList(): MutableList<Boolean> {
    return MutableList(size()) {
        getIndex(it)
    }
}

fun TIOSKHKotlinByteArray.toMutableList(): MutableList<Byte> {
    return MutableList(size()) {
        getIndex(it)
    }
}

fun TIOSKHKotlinCharArray.toMutableList(): MutableList<Char> {
    return MutableList(size()) {
        Char(getIndex(it))
    }
}

fun TIOSKHKotlinShortArray.toMutableList(): MutableList<Short> {
    return MutableList(size()) {
        getIndex(it)
    }
}

fun TIOSKHKotlinIntArray.toMutableList(): MutableList<Int> {
    return MutableList(size()) {
        getIndex(it)
    }
}

fun TIOSKHKotlinLongArray.toMutableList(): MutableList<Long> {
    return MutableList(size()) {
        getIndex(it)
    }
}

fun TIOSKHKotlinFloatArray.toMutableList(): MutableList<Float> {
    return MutableList(size()) {
        getIndex(it)
    }
}

fun TIOSKHKotlinDoubleArray.toMutableList(): MutableList<Double> {
    return MutableList(size()) {
        getIndex(it)
    }
}

fun TIOSKHKotlinArray.toArray() = Array(size()) {
    getIndex(it)
}

fun TIOSKHKotlinArray.toStringArray() = Array(size()) {
    getIndex(it) as String
}

fun <T> Array<T>.toTIOSKHKotlinArray() = TIOSKHKotlinArray.arrayWithSize(size) {
    this[it!!.intValue]
}

fun FloatArray.toTIOSKotlinFloatArray() = TIOSKHKotlinFloatArray.arrayWithSize(size) {
    TIOSKHFloat(this[it!!.intValue])
}

fun DoubleArray.toTIOSKotlinDoubleArray() = TIOSKHKotlinDoubleArray.arrayWithSize(size) {
    TIOSKHDouble(this[it!!.intValue])
}

fun BooleanArray.toTIOSKotlinBooleanArray() = TIOSKHKotlinBooleanArray.arrayWithSize(size) {
    TIOSKHBoolean(this[it!!.intValue])
}

fun IntArray.toTIOSKotlinIntArray() = TIOSKHKotlinIntArray.arrayWithSize(size) {
    TIOSKHInt(this[it!!.intValue])
}

fun LongArray.toTIOSKotlinLongArray() = TIOSKHKotlinLongArray.arrayWithSize(size) {
    TIOSKHLong.numberWithLongLong(this[it!!.intValue])
}

fun ShortArray.toTIOSKotlinShortArray() = TIOSKHKotlinShortArray.arrayWithSize(size) {
    TIOSKHShort(this[it!!.intValue])
}

fun ByteArray.toTIOSKotlinByteArray() = TIOSKHKotlinByteArray.arrayWithSize(size) {
    TIOSKHByte(this[it!!.intValue])
}

fun CharArray.toTIOSKotlinCharArray() = TIOSKHKotlinCharArray.arrayWithSize(size) {
    TIOSKHByte(this[it!!.intValue].code)
}

inline operator fun TIOSKHSkikoMatrix44.get(row: Int, column: Int) = mat().getIndex((row * 4) + column)

inline operator fun TIOSKHSkikoMatrix44.set(row: Int, column: Int, v: Float) {
    mat().setIndex((row * 4) + column, v)
}