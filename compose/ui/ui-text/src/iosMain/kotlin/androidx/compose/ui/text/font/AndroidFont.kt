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

package androidx.compose.ui.text.font

import AssetManager
import Typeface
import Context
import File
import ParcelFileDescriptor
import RequiresApi
import androidx.compose.runtime.Stable

/**
 * Create a Font declaration from a file in the assets directory. The content of the [File] is
 * read during construction.
 *
 * @param path full path starting from the assets directory (i.e. dir/myfont.ttf for
 * assets/dir/myfont.ttf).
 * @param assetManager Android AssetManager
 * @param weight The weight of the font. The system uses this to match a font to a font request
 * that is given in a [androidx.compose.ui.text.SpanStyle].
 * @param style The style of the font, normal or italic. The system uses this to match a font to a
 * font request that is given in a [androidx.compose.ui.text.SpanStyle].
 * @param variationSettings on API 26 and above these settings are applied to a variable font when
 * the font is loaded
 */
@Stable
fun Font(
    path: String,
    assetManager: AssetManager,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
    variationSettings: FontVariation.Settings = FontVariation.Settings(weight, style)
): Font = AndroidAssetFont(assetManager, path, weight, style, variationSettings)

/**
 * Create a Font declaration from a file. The content of the [File] is read during construction.
 *
 * @param file the font file.
 * @param weight The weight of the font. The system uses this to match a font to a font request
 * that is given in a [androidx.compose.ui.text.SpanStyle].
 * @param style The style of the font, normal or italic. The system uses this to match a font to a
 * font request that is given in a [androidx.compose.ui.text.SpanStyle].
 * @param variationSettings on API 26 and above these settings are applied to a variable font when
 * the font is loaded
 */
@Stable
@Suppress("StreamFiles")
fun Font(
    file: File,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
    variationSettings: FontVariation.Settings = FontVariation.Settings(weight, style)
): Font = AndroidFileFont(file, weight, style, variationSettings)

/**
 * Create a Font declaration from a [ParcelFileDescriptor]. The content of the
 * [ParcelFileDescriptor] is read during construction.
 *
 * @param fileDescriptor the file descriptor for the font file.
 * @param weight The weight of the font. The system uses this to match a font to a font request
 * that is given in a [androidx.compose.ui.text.SpanStyle].
 * @param style The style of the font, normal or italic. The system uses this to match a font to a
 * font request that is given in a [androidx.compose.ui.text.SpanStyle].
 * @param variationSettings these settings are applied to a variable font when the font is loaded
 */
@RequiresApi(26)
@Stable
fun Font(
    fileDescriptor: ParcelFileDescriptor,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
    variationSettings: FontVariation.Settings = FontVariation.Settings(weight, style)
): Font = AndroidFileDescriptorFont(fileDescriptor, weight, style, variationSettings)

abstract class AndroidFont constructor(
    final override val loadingStrategy: FontLoadingStrategy,
    val typefaceLoader: TypefaceLoader,
    variationSettings: FontVariation.Settings,
) : Font {

    @Deprecated(
        "Replaced with fontVariation constructor",
        ReplaceWith(
            "AndroidFont(loadingStrategy, typefaceLoader, FontVariation.Settings())"
        )
    )
    constructor(
        loadingStrategy: FontLoadingStrategy,
        typefaceLoader: TypefaceLoader,
    ) : this(loadingStrategy, typefaceLoader, FontVariation.Settings())

    val variationSettings: FontVariation.Settings = variationSettings

    interface TypefaceLoader {
        fun loadBlocking(context: Context, font: AndroidFont): Typeface?

        suspend fun awaitLoad(context: Context, font: AndroidFont): Typeface?
    }
}

// keep generating AndroidFontKt to avoid API change
private fun generateAndroidFontKtForApiCompatibility() {}
