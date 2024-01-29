/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.compose.foundation.text2.input.internal

fun isHighSurrogate(ch: Char): Boolean
{
    return ch >= Char.MIN_HIGH_SURROGATE && ch <= Char.MAX_HIGH_SURROGATE;
}
fun isLowSurrogate(ch: Char): Boolean {
    return ch >= Char.MIN_LOW_SURROGATE && ch <= Char.MAX_LOW_SURROGATE;
}
fun codePointCount(seq: CharSequence, beginIndex : Int,
    endIndex : Int) : Int
{
    var len = seq.length
    if (beginIndex < 0 || endIndex > len || beginIndex > endIndex)
        return 0

    var count = 0
    var i = beginIndex
    while(i < endIndex)
    {
        count++
        // If there is a pairing, count it only once.
        if (isHighSurrogate(seq.get(i)) && (i + 1) < endIndex
            && isLowSurrogate(seq.get(i + 1)))
            i++
        i++
    }
    return count
}

fun toCodePoint(high: Char, low: Char) : Int
{
    return ((high - Char.MIN_HIGH_SURROGATE) * 0x400) +
    (low - Char.MIN_LOW_SURROGATE) + 0x10000;
}

fun codePointAt(sequence: CharSequence, indexP: Int): Int
{
    var index = indexP
    val len = sequence.length
    if (index < 0 || index >= len)
        return -1
    var high = sequence.get(index);
    if (!isHighSurrogate(high) || ++index >= len)
        return high.code
    var low = sequence.get(index)
    if (! isLowSurrogate(low))
         return high.code
    return toCodePoint(high, low);
}

fun charCountIn(codePoint: Int) : Int
{
    return if (codePoint >= Char.MIN_SUPPLEMENTARY_CODE_POINT) 2 else 1
}

internal actual fun CharSequence.codePointAt(index: Int): Int =
    codePointAt(this, index)

internal actual fun CharSequence.codePointCount(): Int =
    codePointCount(this, 0, length)

internal actual fun charCount(codePoint: Int): Int =
    charCountIn(codePoint)