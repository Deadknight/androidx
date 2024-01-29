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

package androidx.compose.foundation

actual class AtomicReference<V> actual constructor(value: V) {
    val vInternal = kotlin.native.concurrent.AtomicReference<V>(value)

    actual fun get(): V {
        return vInternal.value
    }

    actual fun set(value: V) {
        vInternal.value = value
    }

    actual fun getAndSet(value: V): V {
        return vInternal.getAndSet(value)
    }

    actual fun compareAndSet(expect: V, newValue: V): Boolean {
        return vInternal.compareAndSet(expect, newValue)
    }
}

actual class AtomicLong actual constructor(value: Long) {
    val vInternal = kotlin.native.concurrent.AtomicLong(value)

    actual fun get(): Long {
        return vInternal.value
    }

    actual fun set(value: Long) {
        vInternal.value = value
    }

    actual fun getAndIncrement(): Long {
        return vInternal.getAndIncrement()
    }
}