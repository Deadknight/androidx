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

package bytebuffer

import kotlinx.cinterop.UnsafeNumber
import platform.Foundation.NSASCIIStringEncoding
import platform.Foundation.NSISOLatin1StringEncoding
import platform.Foundation.NSStringEncoding
import platform.Foundation.NSUTF16BigEndianStringEncoding
import platform.Foundation.NSUTF16LittleEndianStringEncoding
import platform.Foundation.NSUTF16StringEncoding
import platform.Foundation.NSUTF32BigEndianStringEncoding
import platform.Foundation.NSUTF32LittleEndianStringEncoding
import platform.Foundation.NSUTF32StringEncoding
import platform.Foundation.NSUTF8StringEncoding

@OptIn(UnsafeNumber::class)
fun Charset.toEncoding(): NSStringEncoding = when (this) {
    Charset.UTF8 -> NSUTF8StringEncoding
    Charset.UTF16 -> NSUTF16StringEncoding
    Charset.UTF16BigEndian -> NSUTF16BigEndianStringEncoding
    Charset.UTF16LittleEndian -> NSUTF16LittleEndianStringEncoding
    Charset.ASCII -> NSASCIIStringEncoding
    Charset.ISOLatin1 -> NSISOLatin1StringEncoding
    Charset.UTF32 -> NSUTF32StringEncoding
    Charset.UTF32LittleEndian -> NSUTF32LittleEndianStringEncoding
    Charset.UTF32BigEndian -> NSUTF32BigEndianStringEncoding
}