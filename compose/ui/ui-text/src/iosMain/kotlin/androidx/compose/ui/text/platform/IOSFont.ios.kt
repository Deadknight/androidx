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

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontListFontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.util.fastForEach
import File
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKHKotlinArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinByteArray
import androidx.compose.ui.text.ExpireAfterAccessCache
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.ui.text.font.LoadedFontFamily
import androidx.compose.ui.text.font.Typeface
import cocoapods.ToppingCompose.LuaResource
import cocoapods.ToppingCompose.TIOSKHSkikoData
import cocoapods.ToppingCompose.TIOSKHSkikoFontCollection
import cocoapods.ToppingCompose.TIOSKHSkikoFontMgr
import cocoapods.ToppingCompose.TIOSKHSkikoTypeface
import cocoapods.ToppingCompose.TIOSKHSkikoTypefaceFontProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.posix.memccpy
import platform.posix.memcpy
import resources

// Extremely simple Cache interface which is enough for ui.text needs
internal interface Cache<K, V> {
    // get a value for [key] or load it by [loader] if doesn't exist
    fun get(key: K, loader: (K) -> V): V
}

class FontLoadResult(val typeface: TIOSKHSkikoTypeface?, val aliases: List<String>)

internal class FontCache {
    internal val fonts = TIOSKHSkikoFontCollection()
    private val fontProvider = TIOSKHSkikoTypefaceFontProvider()

    init {
        fonts.setDefaultFontManagerFontMgr(TIOSKHSkikoFontMgr.companion().default())
        fonts.setAssetFontManagerFontMgr(fontProvider)
    }

    private fun mapGenericFontFamily(generic: GenericFontFamily): List<String> {
        return GenericFontFamiliesMapping[generic.name]
            ?: error("Unknown generic font family ${generic.name}")
    }

    private val registered = HashSet<String>()

    internal fun load(font: PlatformFont): FontLoadResult {
        val typeface = loadFromTypefacesCache(font)
        ensureRegistered(typeface, font.cacheKey)
        return FontLoadResult(typeface, listOf(font.cacheKey))
    }

    internal fun loadPlatformTypes(
        fontFamily: FontFamily,
        fontWeight: FontWeight = FontWeight.Normal,
        fontStyle: FontStyle = FontStyle.Normal
    ): FontLoadResult {
        val aliases = ensureRegistered(fontFamily)
        val style = fontStyle.toSkFontStyle().withWeightWeight(fontWeight.weight)
        return FontLoadResult(fonts.findTypefacesFamilyNames(aliases.toTypedArray().toTIOSKHKotlinArray(), style).getIndex(0) as TIOSKHSkikoTypeface, aliases)
    }

    private fun ensureRegistered(typeface: TIOSKHSkikoTypeface, key: String) {
        if (!registered.contains(key)) {
            fontProvider.registerTypefaceTypeface(typeface, key)
            registered.add(key)
        }
    }

    private fun ensureRegistered(fontFamily: FontFamily): List<String> =
        when (fontFamily) {
            is FontListFontFamily -> {
                // not supported
                throw IllegalArgumentException(
                    "Don't load FontListFontFamily through ensureRegistered: $fontFamily"
                )
            }
            is LoadedFontFamily -> {
                val typeface = fontFamily.typeface as SkiaBackedTypeface
                val alias = typeface.alias ?: typeface.nativeTypeface.familyName()
                if (!registered.contains(alias)) {
                    fontProvider.registerTypefaceTypeface(typeface.nativeTypeface, alias)
                    registered.add(alias)
                }
                listOf(alias)
            }
            is GenericFontFamily -> mapGenericFontFamily(fontFamily)
            FontFamily.Default -> mapGenericFontFamily(FontFamily.SansSerif)
            else -> throw IllegalArgumentException("Unknown font family type: $fontFamily")
        }
}

abstract class PlatformFont : Font {
    abstract val identity: String
    internal val cacheKey: String
        get() = "${this::class.qualifiedName}|$identity"
}

internal val GenericFontFamiliesMapping by lazy {
    mapOf(
        FontFamily.SansSerif.name to listOf(
            "Helvetica Neue",
            "Helvetica"
        ),
        FontFamily.Serif.name to listOf("Times"),
        FontFamily.Monospace.name to listOf("Courier"),
        FontFamily.Cursive.name to listOf("Apple Chancery")
    )
}

/**
 * Defines a Font using resource name.
 *
 * @param name The resource name in classpath.
 * @param weight The weight of the font. The system uses this to match a font to a font request
 * that is given in a [androidx.compose.ui.text.SpanStyle].
 * @param style The style of the font, normal or italic. The system uses this to match a font to a
 * font request that is given in a [androidx.compose.ui.text.SpanStyle].
 *
 * @see FontFamily
 */

class ResourceFont internal constructor(
    val name: String,
    override val weight: FontWeight = FontWeight.Normal,
    override val style: FontStyle = FontStyle.Normal
) : PlatformFont() {
    override val identity
        get() = name

    @ExperimentalTextApi
    override val loadingStrategy: FontLoadingStrategy = FontLoadingStrategy.Blocking

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        other as ResourceFont

        if (name != other.name) return false
        if (weight != other.weight) return false
        if (style != other.style) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + weight.hashCode()
        result = 31 * result + style.hashCode()
        return result
    }

    override fun toString(): String {
        return "ResourceFont(name='$name', weight=$weight, style=$style)"
    }
}

/**
 * Defines a Font using byte array with loaded font data.
 *
 * @param identity Unique identity for a font. Used internally to distinguish fonts.
 * @param data Byte array with loaded font data.
 * @param weight The weight of the font. The system uses this to match a font to a font request
 * that is given in a [androidx.compose.ui.text.SpanStyle].
 * @param style The style of the font, normal or italic. The system uses this to match a font to a
 * font request that is given in a [androidx.compose.ui.text.SpanStyle].
 *
 * @see FontFamily
 */
class LoadedFont internal constructor(
    override val identity: String,
    val data: ByteArray,
    override val weight: FontWeight,
    override val style: FontStyle
) : PlatformFont() {
    @ExperimentalTextApi
    override val loadingStrategy: FontLoadingStrategy = FontLoadingStrategy.Blocking

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoadedFont) return false
        if (identity != other.identity) return false
        if (!data.contentEquals(other.data)) return false
        if (weight != other.weight) return false
        if (style != other.style) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identity.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + weight.hashCode()
        result = 31 * result + style.hashCode()
        return result
    }

    override fun toString(): String {
        return "LoadedFont(identity='$identity', weight=$weight, style=$style)"
    }
}

/**
 * Creates a Font using resource name.
 *
 * @param resource The resource name in classpath.
 * @param weight The weight of the font. The system uses this to match a font to a font request
 * that is given in a [androidx.compose.ui.text.SpanStyle].
 * @param style The style of the font, normal or italic. The system uses this to match a font to a
 * font request that is given in a [androidx.compose.ui.text.SpanStyle].
 *
 * @see FontFamily
 */
fun Font(
    resource: String,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal
): Font = ResourceFont(resource, weight, style)

internal class SkiaBackedTypeface(
    val alias: String?,
    val nativeTypeface: TIOSKHSkikoTypeface
) : Typeface {
    override val fontFamily: FontFamily? = null
}

/**
 * Defines a Font using file path.
 *
 * @param file File path to font.
 * @param weight The weight of the font. The system uses this to match a font to a font request
 * that is given in a [androidx.compose.ui.text.SpanStyle].
 * @param style The style of the font, normal or italic. The system uses this to match a font to a
 * font request that is given in a [androidx.compose.ui.text.SpanStyle].
 *
 * @see FontFamily
 */
class FileFont internal constructor(
    val file: File,
    override val weight: FontWeight = FontWeight.Normal,
    override val style: FontStyle = FontStyle.Normal,
) : PlatformFont() {
    override val identity
        get() = file.toString()

    @ExperimentalTextApi
    override val loadingStrategy: FontLoadingStrategy = FontLoadingStrategy.Blocking

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        other as FileFont

        if (file != other.file) return false
        if (weight != other.weight) return false
        if (style != other.style) return false

        return true
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + weight.hashCode()
        result = 31 * result + style.hashCode()
        return result
    }

    override fun toString(): String {
        return "FileFont(file=$file, weight=$weight, style=$style)"
    }
}

/**
 * Creates a Font using file path.
 *
 * @param file File path to font.
 * @param weight The weight of the font. The system uses this to match a font to a font request
 * that is given in a [androidx.compose.ui.text.SpanStyle].
 * @param style The style of the font, normal or italic. The system uses this to match a font to a
 * font request that is given in a [androidx.compose.ui.text.SpanStyle].
 *
 * @see FontFamily
 */
fun Font(
    file: File,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal
): Font = FileFont(file, weight, style)

@OptIn(ExperimentalForeignApi::class)
internal fun FontListFontFamily.makeAlias(): String {
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    fonts.fastForEach { font ->
        when (font) {
            is PlatformFont -> {
                digest.usePinned { digestPinned ->
                    font.identity.usePinned { inputPinned ->
                        CC_SHA256(inputPinned.addressOf(0), font.identity.length.convert(), digestPinned.addressOf(0))
                    }
                }
            }
        }
    }
    val str = digest.joinToString(separator = "") { it -> it.toString(16) }
    return "-compose-${str}"
}

internal fun loadFromTypefacesCache(font: Font): TIOSKHSkikoTypeface {
    if (font !is PlatformFont) {
        throw IllegalArgumentException("Unsupported font type: $font")
    }
    return typefacesCache.get(font.cacheKey) {
        when (font) {
            is ResourceFont -> typefaceResource(font.name)
            //is FileFont -> TIOSKHSkikoTypeface.makeFromFile(font.file.toString())
            is LoadedFont -> TIOSKHSkikoTypeface.companion().makeFromDataData(TIOSKHSkikoData.companion().makeFromBytesBytes(font.data.toTIOSKotlinByteArray(), 0, font.data.size), 0)
            else -> throw IllegalArgumentException("Unsupported font type: $font")
        }
    }
}

internal val typefacesCache: Cache<String, TIOSKHSkikoTypeface> =
    ExpireAfterAccessCache<String, TIOSKHSkikoTypeface>(
        60_000_000_000 // 1 minute
    )

@OptIn(ExperimentalForeignApi::class)
private fun typefaceResource(resourceName: String): TIOSKHSkikoTypeface {
    val resArr = resourceName.split("/").toMutableList()
    val resFile = resArr.last()
    resArr.removeLast()
    val path = resArr.joinToString("/")
    val stream = LuaResource.getResource(path, resFile)
    if(stream != null)
    {
        val data = stream.getData()
        if(data != null) {
            val bytes = ByteArray(data.length.toInt()).apply {
                usePinned {
                    memcpy(it.addressOf(0), data.bytes, data.length)
                }
            }
            return TIOSKHSkikoTypeface.companion().makeFromDataData(TIOSKHSkikoData.companion().makeFromBytesBytes(bytes.toTIOSKotlinByteArray(), 0, bytes.size), 0)
        }
    }
    return TIOSKHSkikoTypeface.companion().makeDefault()
}
