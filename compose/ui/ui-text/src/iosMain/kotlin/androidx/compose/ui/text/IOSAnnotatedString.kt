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

package androidx.compose.ui.text

import androidx.compose.ui.util.fastFold
import androidx.compose.ui.util.fastMap

internal actual fun AnnotatedString.transform(
    transform: (String, Int, Int) -> String
): AnnotatedString {
    val transitions = mutableSetOf(0, text.length)
    collectRangeTransitions(spanStylesOrNull, transitions)
    collectRangeTransitions(paragraphStylesOrNull, transitions)
    collectRangeTransitions(annotations, transitions)

    var resultStr = ""
    val offsetMap = mutableMapOf(0 to 0)
    transitions.windowed(size = 2) { (start, end) ->
        resultStr += transform(text, start, end)
        offsetMap.put(end, resultStr.length)
    }

    val newSpanStyles = spanStylesOrNull?.fastMap {
        // The offset map must have mapping entry from all style start, end position.
        AnnotatedString.Range(it.item, offsetMap[it.start]!!, offsetMap[it.end]!!)
    }
    val newParaStyles = paragraphStylesOrNull?.fastMap {
        AnnotatedString.Range(it.item, offsetMap[it.start]!!, offsetMap[it.end]!!)
    }
    val newAnnotations = annotations?.fastMap {
        AnnotatedString.Range(it.item, offsetMap[it.start]!!, offsetMap[it.end]!!)
    }

    return AnnotatedString(
        text = resultStr,
        spanStylesOrNull = newSpanStyles,
        paragraphStylesOrNull = newParaStyles,
        annotations = newAnnotations
    )
}

/**
 * Adds all [AnnotatedString.Range] transition points
 *
 * @param ranges The list of AnnotatedString.Range
 * @param target The output list
 */
private fun collectRangeTransitions(
    ranges: List<AnnotatedString.Range<*>>?,
    target: MutableSet<Int>
) {
    ranges?.fastFold(target) { acc, range ->
        acc.apply {
            add(range.start)
            add(range.end)
        }
    }
}