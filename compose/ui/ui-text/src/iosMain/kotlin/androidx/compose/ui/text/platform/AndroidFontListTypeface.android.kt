/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.compose.ui.text.platform

import Context
import DoNotInline
import Typeface
import androidx.annotation.RequiresApi
import androidx.collection.LruCache
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.AndroidFont
import androidx.compose.ui.text.font.AndroidPreloadedFont
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontListFontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontMatcher
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.ResourceFont
import androidx.compose.ui.text.font.synthesizeTypeface
import androidx.compose.ui.util.ID
import androidx.compose.ui.util.fastDistinctBy
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFilterNotNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import resources

/**
 * An implementation of [AndroidTypeface] for [FontListFontFamily]
 */
// internal constructor for injecting FontMatcher for testing purpose
@Suppress("DEPRECATION")
@Deprecated("This is not supported after downloadable fonts.")
internal class AndroidFontListTypeface(
    fontFamily: FontListFontFamily,
    context: Context,
    necessaryStyles: List<Pair<FontWeight, FontStyle>>? = null,
    val fontMatcher: FontMatcher = AndroidFontListTypeface.fontMatcher
) : AndroidTypeface {

    private companion object {
        val fontMatcher = FontMatcher()
    }
    private val loadedTypefaces: Map<Font, Typeface>

    init {
        val blockingFonts = fontFamily.fonts.fastFilter {
            it.loadingStrategy == FontLoadingStrategy.Blocking
        }
        val matchedFonts: List<Font>? = necessaryStyles?.fastMap { (weight, style) ->
            fontMatcher.matchFont(blockingFonts, weight, style).firstOrNull()
        }?.fastFilterNotNull()?.fastDistinctBy { it }
        val targetFonts = matchedFonts ?: blockingFonts
        check(targetFonts.isNotEmpty()) { "Could not match font" }

        val typefaces = mutableMapOf<Font, Typeface>()
        targetFonts.fastForEach {
            try {
                typefaces[it] = AndroidTypefaceCache.getOrCreate(context, it)
            } catch (e: Exception) {
                throw IllegalStateException("Cannot create Typeface from $it")
            }
        }

        loadedTypefaces = typefaces
    }

    override val fontFamily: FontFamily = fontFamily

    override fun getNativeTypeface(
        fontWeight: FontWeight,
        fontStyle: FontStyle,
        synthesis: FontSynthesis
    ): Typeface {
        val font = fontMatcher
            .matchFont(ArrayList(loadedTypefaces.keys), fontWeight, fontStyle)
            .firstOrNull()
        checkNotNull(font) { "Could not load font" }

        val typeface = loadedTypefaces[font]
        checkNotNull(typeface) { "Could not load typeface" }

        return synthesis.synthesizeTypeface(typeface, font, fontWeight, fontStyle) as Typeface
    }
}

/**
 * Global Android NativeTypeface cache.
 */
@Deprecated("Duplicate cache")
internal object AndroidTypefaceCache {

    // TODO multiple TypefaceCache's, would be good to unify
    private val cache = LruCache<String, Typeface>(16)

    /**
     * Returns NativeTypeface for [font] if it is in cache. Otherwise create new NativeTypeface and
     * put it into internal cache.
     */
    fun getOrCreate(context: Context, font: Font): Typeface {
        val key = getKey(context, font)

        key?.let {
            cache.get(key)?.let { return it }
        }

        val typeface = when (font) {
            is ResourceFont ->
                    ResourcesCompat.getFont(context, font.resId)!!
            is AndroidFont -> font.typefaceLoader.loadBlocking(context, font)
            else -> throw IllegalArgumentException("Unknown font type: $font")
        } ?: throw IllegalArgumentException("Unable to load font $font")

        key?.let { cache.put(key, typeface) }

        return typeface
    }

    /**
     * Utility method to generate a key for caching purposes.
     */
    fun getKey(context: Context, font: Font): String? {
        return when (font) {
            is ResourceFont -> {
                "res:${font.resId?.toString()!!}"
            }
            is AndroidPreloadedFont -> font.cacheKey
            else -> throw IllegalArgumentException("Unknown font type: $font")
        }
    }
}
