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

package androidx.compose.ui.text.platform

/*import Typeface
import androidx.compose.runtime.State
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.EmojiSupportMatch
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.TypefaceResult
import androidx.compose.ui.text.intl.AndroidLocale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.util.PriorityQueue
import cocoapods.ToppingCompose.NSQueue
import kotlin.math.ceil
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import org.jetbrains.skia.BreakIterator
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSString
import platform.Foundation.languageCode
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSStringDrawingUsesLineFragmentOrigin
import platform.UIKit.boundingRectWithSize

/**
 * Computes and caches the text layout intrinsic values such as min/max width.
 */
internal class LayoutIntrinsics(
    private val charSequence: CharSequence,
    private val textPaint: AndroidTextPaint,
    private val textDirectionHeuristic: TextDirection
) {

    private var _maxIntrinsicWidth: Float = Float.NaN
    private var _minIntrinsicWidth: Float = Float.NaN

    /**
     * Calculate minimum intrinsic width of the CharSequence.
     *
     * @see androidx.compose.ui.text.android.minIntrinsicWidth
     */
    val minIntrinsicWidth: Float
        get() = if (!_minIntrinsicWidth.isNaN()) {
            _minIntrinsicWidth
        } else {
            _minIntrinsicWidth = minIntrinsicWidth(charSequence, textPaint)
            _minIntrinsicWidth
        }

    /**
     * Calculate maximum intrinsic width for the CharSequence. Maximum intrinsic width is the width
     * of text where no soft line breaks are applied.
     */
    @OptIn(ExperimentalForeignApi::class)
    val maxIntrinsicWidth: Float
        get() = if (!_maxIntrinsicWidth.isNaN()) {
            _maxIntrinsicWidth
        } else {
            val str = charSequence.toString() as NSString
            var desiredWidth = 0f
            str.boundingRectWithSize(CGSizeMake(Double.MAX_VALUE, Double.MAX_VALUE), NSStringDrawingUsesLineFragmentOrigin,
                mapOf(NSFontAttributeName to textPaint.font), null).useContents {
                    desiredWidth = this.size.width.toFloat()
            }
            _maxIntrinsicWidth = desiredWidth
            _maxIntrinsicWidth
        }
}

/**
 * Returns the word with the longest length. To calculate it in a performant way, it applies a heuristics where
 *  - it first finds a set of words with the longest length
 *  - finds the word with maximum width in that set
 */
@OptIn(ExperimentalForeignApi::class)
internal fun minIntrinsicWidth(text: CharSequence, paint: AndroidTextPaint): Float {
    val iterator = BreakIterator.makeLineInstance(paint.locale.languageCode)
    iterator.setText(text.toString())

    // 10 is just a random number that limits the size of the candidate list
    val heapSize = 10
    // min heap that will hold [heapSize] many words with max length
    val longestWordCandidates = PriorityQueue<Pair<Int, Int>>(
        Comparator<Pair<Int, Int>> { left, right ->
            (left.second - left.first) - (right.second - right.first)
        }
    )

    var start = 0
    var end = iterator.next()
    while (end != BreakIterator.DONE) {
        if (longestWordCandidates.size < heapSize) {
            longestWordCandidates.add(Pair(start, end))
        } else {
            longestWordCandidates.peek()?.let { minPair ->
                if ((minPair.second - minPair.first) < (end - start)) {
                    longestWordCandidates.poll()
                    longestWordCandidates.add(Pair(start, end))
                }
            }
        }

        start = end
        end = iterator.next()
    }

    var minWidth = 0f

    longestWordCandidates.forEach { (start, end) ->
        var width = 0f
        val textNs = text as NSString
        textNs.boundingRectWithSize(CGSizeMake(Double.MAX_VALUE, Double.MAX_VALUE), NSStringDrawingUsesLineFragmentOrigin,
            mapOf(NSFontAttributeName to paint.font), null).useContents {
            width = this.size.width.toFloat()
        }
        minWidth = maxOf(minWidth, width)
    }

    return minWidth
}

internal class AndroidParagraphIntrinsics constructor(
    val text: String,
    val style: TextStyle,
    val spanStyles: List<AnnotatedString.Range<SpanStyle>>,
    val placeholders: List<AnnotatedString.Range<Placeholder>>,
    val fontFamilyResolver: FontFamily.Resolver,
    val density: Density
) : ParagraphIntrinsics {

    internal val textPaint: AndroidTextPaint

    internal val charSequence: CharSequence

    internal val layoutIntrinsics: LayoutIntrinsics

    override val maxIntrinsicWidth: Float
        get() = layoutIntrinsics.maxIntrinsicWidth

    override val minIntrinsicWidth: Float
        get() = layoutIntrinsics.minIntrinsicWidth

    private var resolvedTypefaces: TypefaceDirtyTrackerLinkedList? = null

    override val hasStaleResolvedFonts: Boolean
        get() = (resolvedTypefaces?.isStaleResolvedFont ?: false)

    init {
        val resolveTypeface: (FontFamily?, FontWeight, FontStyle, FontSynthesis) -> Typeface =
            { fontFamily, fontWeight, fontStyle, fontSynthesis ->
                val result = fontFamilyResolver.resolve(
                    fontFamily,
                    fontWeight,
                    fontStyle,
                    fontSynthesis
                )
                if (result !is TypefaceResult.Immutable) {
                    val newHead = TypefaceDirtyTrackerLinkedList(result, resolvedTypefaces)
                    resolvedTypefaces = newHead
                    newHead.typeface
                } else {
                    result.value as Typeface
                }
            }

        textPaint = AndroidTextPaint(resolvedTypefaces!!.typeface)

        //textPaint.setTextMotion(style.textMotion)

        /*val notAppliedStyle = textPaint.applySpanStyle(
            style = style.toSpanStyle(),
            resolveTypeface = resolveTypeface,
            density = density,
            requiresLetterSpacing = spanStyles.isNotEmpty(),
        )

        val finalSpanStyles = if (notAppliedStyle != null) {
            // This is just a prepend operation, written in a lower alloc way
            // equivalent to: `AnnotatedString.Range(...) + spanStyles`
            List(spanStyles.size + 1) { position ->
                when (position) {
                    0 -> AnnotatedString.Range(
                        item = notAppliedStyle,
                        start = 0,
                        end = text.length
                    )

                    else -> spanStyles[position - 1]
                }
            }
        } else {
            spanStyles
        }*/
        charSequence = text

        layoutIntrinsics = LayoutIntrinsics(charSequence, textPaint, style.textDirection ?: TextDirection.Ltr)
    }
}

internal actual fun ActualParagraphIntrinsics(
    text: String,
    style: TextStyle,
    spanStyles: List<AnnotatedString.Range<SpanStyle>>,
    placeholders: List<AnnotatedString.Range<Placeholder>>,
    density: Density,
    fontFamilyResolver: FontFamily.Resolver
): ParagraphIntrinsics = AndroidParagraphIntrinsics(
    text = text,
    style = style,
    placeholders = placeholders,
    fontFamilyResolver = fontFamilyResolver,
    spanStyles = spanStyles,
    density = density
)

private class TypefaceDirtyTrackerLinkedList(
    private val resolveResult: State<Any>,
    private val next: TypefaceDirtyTrackerLinkedList? = null
) {
    val initial = resolveResult.value
    val typeface: Typeface
        get() = initial as Typeface

    val isStaleResolvedFont: Boolean
        get() = resolveResult.value !== initial || (next != null && next.isStaleResolvedFont)
}

private val TextStyle.hasEmojiCompat: Boolean
    get() = platformStyle?.paragraphStyle?.emojiSupportMatch != EmojiSupportMatch.None*/