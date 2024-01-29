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

package androidx.compose.ui.text.input

fun arraycopy(sourceArr: CharArray, sourcePos: Int, destArr: CharArray, destPos: Int, size: Int) : CharArray {
    var list = mutableListOf<Char>()

    var count = 0
    for(i in 0 until destPos) {
        list.add(destArr[i])
        count++
    }

    for(i in sourcePos .. size) {
        list.add(sourceArr[i])
        count++
    }

    for(i in count until destArr.size) {
        list.add(destArr[i])
    }

    for(i in 0 until destArr.size) {
        destArr[i] = list[i]
    }

    return destArr
}

internal actual fun String.toCharArray(
    destination: CharArray,
    destinationOffset: Int,
    startIndex: Int,
    endIndex: Int
) {
    val value = CharArray(length) {
        this[it]
    }
    arraycopy(value, startIndex, destination, destinationOffset, endIndex - startIndex)
}