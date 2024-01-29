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

package androidx.compose.ui.node

import platform.Foundation.NSMutableOrderedSet
import platform.Foundation.addObject
import platform.Foundation.firstObject
import platform.Foundation.sortUsingComparator

internal actual class TreeSet<E> actual constructor(val comparator: Comparator<in E>) {
    val delegate = NSMutableOrderedSet()
    actual fun add(element: E): Boolean {
        delegate.addObject(element)
        delegate.sortUsingComparator { a: Any?, b: Any? ->
            comparator.compare(a as E, b as E).toLong()
        }
        return true
    }

    actual fun remove(element: E): Boolean {
        var index = ULong.MAX_VALUE
        for(i in 0UL until delegate.count) {
            if(comparator.compare(element, delegate.objectAtIndex(i) as E) == 0) {
                index = i
                break
            }
        }

        if(index != ULong.MAX_VALUE) {
            delegate.removeObjectAtIndex(index)
            return true
        }
        return false
    }

    actual fun first(): E {
        return delegate.firstObject as E
    }

    actual fun contains(element: E): Boolean {
        for(i in 0UL until delegate.count) {
            if(comparator.compare(element, delegate.objectAtIndex(i) as E) == 0) {
                return true
            }
        }
        return false
    }

    actual fun isEmpty(): Boolean {
        return delegate.count == 0UL
    }
}