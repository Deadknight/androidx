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

package androidx.compose.ui.hapticfeedback

import View
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UISelectionFeedbackGenerator

/**
 * IOS implementation for [HapticFeedback]
 * Move this to topping?
 */
internal class PlatformHapticFeedback(private val view: View) :
    HapticFeedback {

    override fun performHapticFeedback(
        hapticFeedbackType: HapticFeedbackType
    ) {
        when (hapticFeedbackType) {
            HapticFeedbackType.LongPress ->
            {
                val gen = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
                gen.prepare()
                gen.impactOccurred()
            }
            HapticFeedbackType.TextHandleMove -> {
                val gen = UISelectionFeedbackGenerator()
                gen.prepare()
                gen.selectionChanged()
            }
        }
    }
}

internal actual object PlatformHapticFeedbackType {
    actual val LongPress: HapticFeedbackType = HapticFeedbackType(
        0
    )
    actual val TextHandleMove: HapticFeedbackType =
        HapticFeedbackType(1)
}
