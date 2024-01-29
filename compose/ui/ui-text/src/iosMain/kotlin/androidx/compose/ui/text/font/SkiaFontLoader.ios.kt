/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.compose.ui.text.font

import Context
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinByteArray
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontLoadingStrategy.Companion.Async
import androidx.compose.ui.text.font.FontLoadingStrategy.Companion.Blocking
import androidx.compose.ui.text.font.FontLoadingStrategy.Companion.OptionalLocal
import androidx.compose.ui.text.platform.FontCache
import androidx.compose.ui.text.platform.FontLoadResult
import androidx.compose.ui.text.platform.PlatformFont
import cocoapods.Topping.TIOSKHSkikoData
import cocoapods.Topping.TIOSKHSkikoFontCollection
import cocoapods.Topping.TIOSKHSkikoTypeface
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFStringRef
import platform.CoreText.CTFontCopyTable
import platform.CoreText.CTFontCreateWithName
import platform.CoreText.kCTFontTableCFF
import platform.CoreText.kCTFontTableOptionNoOptions
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.posix.memcpy

internal class SkiaFontLoader(
    val context: Context,
    private val fontCache: FontCache = FontCache()
) : PlatformFontLoader {

    val fontCollection: TIOSKHSkikoFontCollection
        get() = fontCache.fonts

    @OptIn(ExperimentalTextApi::class, ExperimentalForeignApi::class)
    override fun loadBlocking(font: Font): FontLoadResult? {
        return when(font) {
            is ResourceFont -> {
                 val typeface = when (font.loadingStrategy) {
                        Blocking -> font.load(context)
                        OptionalLocal -> runCatching { font.load(context) }.getOrNull()
                        Async -> throw UnsupportedOperationException("Unsupported Async font load path")
                        else -> throw IllegalArgumentException(
                            "Unknown loading type ${font.loadingStrategy}"
                        )
                    }.setFontVariationSettings(font.variationSettings, context)
                @Suppress("UNCHECKED_CAST")
                val fontNameRef = CFBridgingRetain(typeface!!.font!!.fontName) as CFStringRef?
                val fontRef = CTFontCreateWithName(fontNameRef, typeface.font!!.pointSize, null)
                val data = CTFontCopyTable(fontRef, kCTFontTableCFF, kCTFontTableOptionNoOptions)
                val nsdata = CFBridgingRelease(data) as NSData?
                if(nsdata != null) {
                    val b = ByteArray(nsdata.length.toInt()).apply {
                        usePinned {
                            memcpy(it.addressOf(0), nsdata.bytes, nsdata.length)
                        }
                    }.toTIOSKotlinByteArray()
                    val skikoData = TIOSKHSkikoData.companion().makeFromBytesBytes(b, 0, b.size())
                    val skikoTypeface = TIOSKHSkikoTypeface.companion().makeFromDataData(skikoData, 0)
                    val names = typeface.font!!.familyName()
                    FontLoadResult(skikoTypeface, listOf(names))
                }
                throw IllegalArgumentException("Unsupported font type: $font")
            }
            is PlatformFont -> when (font.loadingStrategy) {
                Blocking -> fontCache.load(font)
                OptionalLocal -> kotlin.runCatching { fontCache.load(font) }.getOrNull()
                Async -> throw UnsupportedOperationException("Unsupported Async font load path")
                else -> throw IllegalArgumentException(
                    "Unknown loading type ${font.loadingStrategy}"
                )
            }
            else -> {
                if (font.loadingStrategy != OptionalLocal) {
                    throw IllegalArgumentException("Unsupported font type: $font")
                }
                return null
            }
        }
    }

    internal fun loadPlatformTypes(
        fontFamily: FontFamily,
        fontWeight: FontWeight = FontWeight.Normal,
        fontStyle: FontStyle = FontStyle.Normal
    ): FontLoadResult = fontCache.loadPlatformTypes(fontFamily, fontWeight, fontStyle)

    override suspend fun awaitLoad(font: Font): FontLoadResult? {
        // TODO: This should actually do async loading, but for now desktop only supports local
        //  fonts which are allowed to block during loading.

        // When desktop is extended to allow async font resource declarations, this needs updated.
        return loadBlocking(font)
    }

    override val cacheKey: Any = fontCache // results are valid for all shared caches
}
