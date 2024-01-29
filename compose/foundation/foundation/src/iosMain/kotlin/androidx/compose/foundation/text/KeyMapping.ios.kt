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

package androidx.compose.foundation.text

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import platform.UIKit.*

internal actual val platformDefaultKeyMapping = object : KeyMapping {
    override fun map(event: KeyEvent): KeyCommand? = when {
        event.isShiftPressed && event.isAltPressed ->
            when (event.key) {
                MappedKeys.DirectionLeft -> KeyCommand.SELECT_LINE_LEFT
                MappedKeys.DirectionRight -> KeyCommand.SELECT_LINE_RIGHT
                MappedKeys.DirectionUp -> KeyCommand.SELECT_HOME
                MappedKeys.DirectionDown -> KeyCommand.SELECT_END
                else -> null
            }

        event.isAltPressed ->
            when (event.key) {
                MappedKeys.DirectionLeft -> KeyCommand.LINE_LEFT
                MappedKeys.DirectionRight -> KeyCommand.LINE_RIGHT
                MappedKeys.DirectionUp -> KeyCommand.HOME
                MappedKeys.DirectionDown -> KeyCommand.END
                else -> null
            }

        else -> null
    } ?: defaultKeyMapping.map(event)
}

internal actual object MappedKeys {
    actual val A: Key = Key(UIKeyboardHIDUsageKeyboardA)
    actual val C: Key = Key(UIKeyboardHIDUsageKeyboardC)
    actual val H: Key = Key(UIKeyboardHIDUsageKeyboardH)
    actual val V: Key = Key(UIKeyboardHIDUsageKeyboardV)
    actual val Y: Key = Key(UIKeyboardHIDUsageKeyboardY)
    actual val X: Key = Key(UIKeyboardHIDUsageKeyboardX)
    actual val Z: Key = Key(UIKeyboardHIDUsageKeyboardZ)
    actual val Backslash: Key = Key(UIKeyboardHIDUsageKeyboardBackslash)
    actual val DirectionLeft: Key = Key(UIKeyboardHIDUsageKeyboardLeftArrow)
    actual val DirectionRight: Key = Key(UIKeyboardHIDUsageKeyboardRightArrow)
    actual val DirectionUp: Key = Key(UIKeyboardHIDUsageKeyboardUpArrow)
    actual val DirectionDown: Key = Key(UIKeyboardHIDUsageKeyboardDownArrow)
    actual val PageUp: Key = Key(UIKeyboardHIDUsageKeyboardPageUp)
    actual val PageDown: Key = Key(UIKeyboardHIDUsageKeyboardPageDown)
    actual val MoveHome: Key = Key(UIKeyboardHIDUsageKeyboardHome)
    actual val MoveEnd: Key = Key(UIKeyboardHIDUsageKeyboardEnd)
    actual val Insert: Key = Key(UIKeyboardHIDUsageKeyboardInsert)
    actual val Enter: Key = Key(UIKeyboardHIDUsageKeyboardReturnOrEnter)
    actual val Backspace: Key = Key(UIKeyboardHIDUsageKeyboardDeleteOrBackspace)
    actual val Delete: Key = Key(UIKeyboardHIDUsageKeyboardDeleteForward)
    actual val Paste: Key = Key(UIKeyboardHIDUsageKeyboardPaste)
    actual val Cut: Key = Key(UIKeyboardHIDUsageKeyboardCut)
    actual val Copy: Key = Key(UIKeyboardHIDUsageKeyboardCopy)
    actual val Tab: Key = Key(UIKeyboardHIDUsageKeyboardTab)
}
