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

package androidx.compose.foundation

import View
import ViewGroup
import androidx.compose.ui.input.key.CGKeyCodeList
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.type
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewConfiguration
import shouldDelayChildPressedState

internal actual fun CompositionLocalConsumerModifierNode
    .isComposeRootInScrollableContainer(): Boolean {
    return currentValueOf(LocalView).isInScrollableViewGroup()
}

private fun View.isInScrollableViewGroup(): Boolean {
    var p = parent
    while (p != null && p is ViewGroup) {
        if (p.shouldDelayChildPressedState()) {
            return true
        }
        p = p.parent
    }
    return false
}

internal actual val TapIndicationDelay: Long = 0L

/**
 * Whether the specified [KeyEvent] should trigger a press for a clickable component, i.e. whether
 * it is associated with a press of an enter key or dpad centre.
 */
internal actual val KeyEvent.isPress: Boolean
    get() = type == KeyDown && isEnter

/**
 * Whether the specified [KeyEvent] should trigger a click for a clickable component, i.e. whether
 * it is associated with a release of an enter key or dpad centre.
 */
internal actual val KeyEvent.isClick: Boolean
    get() = type == KeyUp && isEnter

private val KeyEvent.isEnter: Boolean
    get() = when (key.nativeKeyCode) {
        CGKeyCodeList.kVK_ANSI_KeypadEnter, CGKeyCodeList.kVK_Return -> true
        else -> false
    }
