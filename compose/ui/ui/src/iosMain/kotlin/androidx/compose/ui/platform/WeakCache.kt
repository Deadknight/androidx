/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.compose.ui.platform

import androidx.compose.runtime.collection.mutableVectorOf
import kotlin.native.ref.WeakReference

internal class WeakCache<T : Any> {
    private val values = mutableVectorOf<WeakReference<T>>()
    private val referenceQueue = ArrayList<WeakReference<T>>()

    /**
     * Add [element] to the collection as a [WeakReference]. It will be removed when
     * garbage collected or from [pop].
     */
    fun push(element: T) {
        clearWeakReferences()
        values += WeakReference(element)
    }

    /**
     * Remove an element from the collection and return it. If no element is
     * available, `null` is returned.
     */
    fun pop(): T? {
        clearWeakReferences()

        while (values.isNotEmpty()) {
            val item = values.removeAt(values.lastIndex).get()
            if (item != null) {
                return item
            }
        }
        return null
    }

    /**
     * The number of elements currently in the collection. This may change between
     * calls if the references have been garbage collected.
     */
    val size: Int
        get() {
            clearWeakReferences()
            return values.size
        }

    private fun clearWeakReferences() {
        referenceQueue.forEach {
            values.remove(it)
        }
    }
}
