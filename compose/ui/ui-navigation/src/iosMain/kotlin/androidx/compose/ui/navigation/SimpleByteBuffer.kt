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

package androidx.compose.ui.navigation

class SimpleByteBuffer(size: Int) {
    val buffer = ByteArray(size) {
        0
    }
    var readMode = false
    var limit = size
    var position = 0

    fun put(byte: Byte) {
        if(position > limit)
            throw Exception()
        buffer[position] = byte
        position++
    }

    fun size(): Int {
        return position
    }

    fun position(): Int {
        return position
    }

    fun flip() {
        readMode = !readMode
        position = 0
        if(!readMode) {
            limit = buffer.size
        }
    }

    fun limit(limit: Int) {
        if(readMode)
            this.limit = limit
    }

    fun getBuffer() : ByteArray {
        return ByteArray(limit) {
            buffer[it]
        }
    }

    fun capacity(): Int {
        return buffer.size
    }

    operator fun get(index: Int) : Byte {
        return buffer[index]
    }

    operator fun set(index: Int, byte: Byte) {
        buffer[index] = byte
    }
}