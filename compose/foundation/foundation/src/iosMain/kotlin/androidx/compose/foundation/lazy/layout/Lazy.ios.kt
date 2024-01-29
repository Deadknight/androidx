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

package androidx.compose.foundation.lazy.layout

import DecodedValue
import Parcelable
import androidx.compose.foundation.ExperimentalFoundationApi
import platform.Foundation.NSCoder
import platform.Foundation.NSCodingProtocol
import platform.Foundation.decodeObjectForKey
import platform.Foundation.encodeObject
import platform.darwin.NSObject

@ExperimentalFoundationApi
actual fun getDefaultLazyLayoutKey(index: Int): Any = DefaultLazyKey(index)

private data class DefaultLazyKey(private val index: Int) : Parcelable {
    override fun coding(): NSCodingProtocol = CodingImpl(this)

    private class CodingImpl(
        private val data: DefaultLazyKey
    ) : NSObject(), NSCodingProtocol {
        override fun encodeWithCoder(coder: NSCoder) {
            coder.encodeObject(data.index, forKey = "index")
        }

        override fun initWithCoder(coder: NSCoder): DecodedValue =
            DecodedValue(
                DefaultLazyKey(
                    index = coder.decodeObjectForKey(key = "index") as Int
                )
            )
    }
}
