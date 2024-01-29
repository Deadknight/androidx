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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldCharSequence
import androidx.compose.foundation.text2.input.toCharArray

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

@OptIn(ExperimentalFoundationApi::class)
internal actual fun CharSequence.toCharArray(
    destination: CharArray,
    destinationOffset: Int,
    startIndex: Int,
    endIndex: Int
) {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    when (this) {
        is TextFieldCharSequence -> toCharArray(
            destination,
            destinationOffset,
            startIndex,
            endIndex
        )

        is String -> {
            val value = CharArray(length) {
                this[it]
            }
            arraycopy(
                value,
                startIndex,
                destination,
                destinationOffset,
                endIndex - startIndex
            )
        }
        is StringBuilder -> {
            val str = this.toString()
            val value = CharArray(str.length) {
                str[it]
            }
            arraycopy(
                value,
                startIndex,
                destination,
                destinationOffset,
                endIndex - startIndex
            )
        }
        else -> {
            require(startIndex in indices && endIndex in 0..length) {
                "Expected source [$startIndex, $endIndex) to be in [0, $length)"
            }
            val copyLength = endIndex - startIndex
            require(
                destinationOffset in destination.indices &&
                    destinationOffset + copyLength in 0..destination.size
            ) {
                "Expected destination [$destinationOffset, ${destinationOffset + copyLength}) " +
                    "to be in [0, ${destination.size})"
            }

            for (i in 0 until copyLength) {
                destination[destinationOffset + i] = get(startIndex + i)
            }
        }
    }
}