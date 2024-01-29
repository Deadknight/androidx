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

package androidx.compose.material3

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.languageCode
import platform.Foundation.systemLocale

/* Copy of androidx.compose.material.ActualJvm, mirrored from Foundation. This is used for the
   M2/M3-internal copy of MutatorMutex.
 */
actual class InternalAtomicReference<V> actual constructor(value: V) {
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

/**
 * Represents a Locale for the calendar. This locale will be used when formatting dates, determining
 * the input format, and more.
 */
actual typealias CalendarLocale = NSLocale

actual fun MatchGroup.range() : IntRange {
    return this.range
}

/**
 * Returns a string representation of an integer for the current Locale.
 */
internal actual fun Int.toLocalString(
    minDigits: Int,
    maxDigits: Int,
    isGroupingUsed: Boolean
): String {
    return getCachedDateTimeFormatter(
        minDigits = minDigits,
        maxDigits = maxDigits,
        isGroupingUsed = isGroupingUsed
    ).stringFromNumber(this as NSNumber) ?: ""
}

private val cachedFormatters = HashMap<String, NSNumberFormatter>()
private fun getCachedDateTimeFormatter(
    minDigits: Int,
    maxDigits: Int,
    isGroupingUsed: Boolean
): NSNumberFormatter {
    // Note: Using Locale.getDefault() as a best effort to obtain a unique key and keeping this
    // function non-composable.
    val key = "$minDigits.$maxDigits.$isGroupingUsed.${NSLocale.systemLocale.languageCode}"
    return cachedFormatters.getOrPut(key) {
        NSNumberFormatter().also {
            it.minimum = minDigits as NSNumber
            it.maximum = maxDigits as NSNumber
        }
    }
}