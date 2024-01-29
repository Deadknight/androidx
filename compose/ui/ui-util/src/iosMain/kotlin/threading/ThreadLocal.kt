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

package threading

import UUID
import platform.Foundation.NSString
import platform.Foundation.NSThread

abstract class ThreadLocal<T> {
    private val uuidString = UUID().UUIDString

    open fun getKey(): String {
        return uuidString
    }

    abstract fun initialValue(): T
    fun get(): T {
        val t = NSThread.currentThread
        val value = t.threadDictionary.objectForKey(getKey() as NSString) as T?
        if(value != null) {
            return value
        }

        return setInitialValue()
    }

    private fun setInitialValue(): T {
        val value = initialValue()
        val t = NSThread.currentThread
        t.threadDictionary.setObject(value, getKey() as NSString)
        return value
    }

    fun dispose() {
        val t = NSThread.currentThread
        t.threadDictionary.removeObjectForKey(getKey() as NSString)
    }
}