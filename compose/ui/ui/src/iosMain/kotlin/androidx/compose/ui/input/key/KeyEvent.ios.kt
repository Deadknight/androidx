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

package androidx.compose.ui.input.key

import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.KeyEventType.Companion.Unknown
import platform.UIKit.UIEvent
import platform.UIKit.UIKey

//TODO:IOS
/**
 * The native Android [KeyEvent][NativeKeyEvent].
 */
actual typealias NativeKeyEvent = Int

/**
 * The key that was pressed.
 *
 * @sample androidx.compose.ui.samples.KeyEventIsAltPressedSample
 */
actual val KeyEvent.key: Key
    get() = Key(nativeKeyEvent)

/**
 * The UTF16 value corresponding to the key event that was pressed. The unicode character
 * takes into account any meta keys that are pressed (eg. Pressing shift results in capital
 * alphabets). The UTF16 value uses the
 * [U+n notation][http://www.unicode.org/reports/tr27/#notation] of the Unicode Standard.
 *
 * An [Int] is used instead of a [Char] so that we can support supplementary characters. The
 * Unicode Standard allows for characters whose representation requires more than 16 bits.
 * The range of legal code points is U+0000 to U+10FFFF, known as Unicode scalar value.
 *
 * The set of characters from U+0000 to U+FFFF is sometimes referred to as the Basic
 * Multilingual Plane (BMP). Characters whose code points are greater than U+FFFF are called
 * supplementary characters. In this representation, supplementary characters are represented
 * as a pair of char values, the first from the high-surrogates range, (\uD800-\uDBFF), the
 * second from the low-surrogates range (\uDC00-\uDFFF).
 *
 * If the return value has bit [KeyCharacterMap.COMBINING_ACCENT] set, the key is a "dead key"
 * that should be combined with another to actually produce a character -- see
 * [KeyCharacterMap.getDeadChar] -- after masking with [KeyCharacterMap.COMBINING_ACCENT_MASK].
 */
actual val KeyEvent.utf16CodePoint: Int
    get() = 0//nativeKeyEvent.unicodeChar

/**
 * The [type][KeyEventType] of key event.
 *
 * @sample androidx.compose.ui.samples.KeyEventTypeSample
 */
actual val KeyEvent.type: KeyEventType
    get() = Unknown
    /*get() = when (nativeKeyEvent.action) {
        ACTION_DOWN -> KeyDown
        ACTION_UP -> KeyUp
        else -> Unknown
    }*/

/**
 * Indicates whether the Alt key is pressed.
 *
 * @sample androidx.compose.ui.samples.KeyEventIsAltPressedSample
 */
actual val KeyEvent.isAltPressed: Boolean
    get() = false//nativeKeyEvent.isAltPressed

/**
 * Indicates whether the Ctrl key is pressed.
 *
 * @sample androidx.compose.ui.samples.KeyEventIsCtrlPressedSample
 */
actual val KeyEvent.isCtrlPressed: Boolean
    get() = false//nativeKeyEvent.isCtrlPressed

/**
 * Indicates whether the Meta key is pressed.
 *
 * @sample androidx.compose.ui.samples.KeyEventIsMetaPressedSample
 */
actual val KeyEvent.isMetaPressed: Boolean
    get() = false//nativeKeyEvent.isMetaPressed

/**
 * Indicates whether the Shift key is pressed.
 *
 * @sample androidx.compose.ui.samples.KeyEventIsShiftPressedSample
 */
actual val KeyEvent.isShiftPressed: Boolean
    get() = false//nativeKeyEvent.isShiftPressed

typealias CGKeyCode = Int
object CGKeyCodeList
{
    /*
    * From Events.h in Carbon.framework
    *  Summary:
    *    Virtual keycodes
    *
    *  Discussion:
    *    These constants are the virtual keycodes defined originally in
    *    Inside Mac Volume V, pg. V-191. They identify physical keys on a
    *    keyboard. Those constants with "ANSI" in the name are labeled
    *    according to the key position on an ANSI-standard US keyboard.
    *    For example, kVK_ANSI_A indicates the virtual keycode for the key
    *    with the letter 'A' in the US keyboard layout. Other keyboard
    *    layouts may have the 'A' key label on a different physical key;
    *    in this case, pressing 'A' will generate a different virtual
    *    keycode.
    */
    const val kVK_Unknown                   : CGKeyCode = 0xFF
    const val kVK_ANSI_A                    : CGKeyCode = 0x00
    const val kVK_ANSI_S                    : CGKeyCode = 0x01
    const val kVK_ANSI_D                    : CGKeyCode = 0x02
    const val kVK_ANSI_F                    : CGKeyCode = 0x03
    const val kVK_ANSI_H                    : CGKeyCode = 0x04
    const val kVK_ANSI_G                    : CGKeyCode = 0x05
    const val kVK_ANSI_Z                    : CGKeyCode = 0x06
    const val kVK_ANSI_X                    : CGKeyCode = 0x07
    const val kVK_ANSI_C                    : CGKeyCode = 0x08
    const val kVK_ANSI_V                    : CGKeyCode = 0x09
    const val kVK_ANSI_B                    : CGKeyCode = 0x0B
    const val kVK_ANSI_Q                    : CGKeyCode = 0x0C
    const val kVK_ANSI_W                    : CGKeyCode = 0x0D
    const val kVK_ANSI_E                    : CGKeyCode = 0x0E
    const val kVK_ANSI_R                    : CGKeyCode = 0x0F
    const val kVK_ANSI_Y                    : CGKeyCode = 0x10
    const val kVK_ANSI_T                    : CGKeyCode = 0x11
    const val kVK_ANSI_1                    : CGKeyCode = 0x12
    const val kVK_ANSI_2                    : CGKeyCode = 0x13
    const val kVK_ANSI_3                    : CGKeyCode = 0x14
    const val kVK_ANSI_4                    : CGKeyCode = 0x15
    const val kVK_ANSI_6                    : CGKeyCode = 0x16
    const val kVK_ANSI_5                    : CGKeyCode = 0x17
    const val kVK_ANSI_Equal                : CGKeyCode = 0x18
    const val kVK_ANSI_9                    : CGKeyCode = 0x19
    const val kVK_ANSI_7                    : CGKeyCode = 0x1A
    const val kVK_ANSI_Minus                : CGKeyCode = 0x1B
    const val kVK_ANSI_8                    : CGKeyCode = 0x1C
    const val kVK_ANSI_0                    : CGKeyCode = 0x1D
    const val kVK_ANSI_RightBracket         : CGKeyCode = 0x1E
    const val kVK_ANSI_O                    : CGKeyCode = 0x1F
    const val kVK_ANSI_U                    : CGKeyCode = 0x20
    const val kVK_ANSI_LeftBracket          : CGKeyCode = 0x21
    const val kVK_ANSI_I                    : CGKeyCode = 0x22
    const val kVK_ANSI_P                    : CGKeyCode = 0x23
    const val kVK_ANSI_L                    : CGKeyCode = 0x25
    const val kVK_ANSI_J                    : CGKeyCode = 0x26
    const val kVK_ANSI_Quote                : CGKeyCode = 0x27
    const val kVK_ANSI_K                    : CGKeyCode = 0x28
    const val kVK_ANSI_Semicolon            : CGKeyCode = 0x29
    const val kVK_ANSI_Backslash            : CGKeyCode = 0x2A
    const val kVK_ANSI_Comma                : CGKeyCode = 0x2B
    const val kVK_ANSI_Slash                : CGKeyCode = 0x2C
    const val kVK_ANSI_N                    : CGKeyCode = 0x2D
    const val kVK_ANSI_M                    : CGKeyCode = 0x2E
    const val kVK_ANSI_Period               : CGKeyCode = 0x2F
    const val kVK_ANSI_Grave                : CGKeyCode = 0x32
    const val kVK_ANSI_KeypadDecimal        : CGKeyCode = 0x41
    const val kVK_ANSI_KeypadMultiply       : CGKeyCode = 0x43
    const val kVK_ANSI_KeypadPlus           : CGKeyCode = 0x45
    const val kVK_ANSI_KeypadClear          : CGKeyCode = 0x47
    const val kVK_ANSI_KeypadDivide         : CGKeyCode = 0x4B
    const val kVK_ANSI_KeypadEnter          : CGKeyCode = 0x4C
    const val kVK_ANSI_KeypadMinus          : CGKeyCode = 0x4E
    const val kVK_ANSI_KeypadEquals         : CGKeyCode = 0x51
    const val kVK_ANSI_Keypad0              : CGKeyCode = 0x52
    const val kVK_ANSI_Keypad1              : CGKeyCode = 0x53
    const val kVK_ANSI_Keypad2              : CGKeyCode = 0x54
    const val kVK_ANSI_Keypad3              : CGKeyCode = 0x55
    const val kVK_ANSI_Keypad4              : CGKeyCode = 0x56
    const val kVK_ANSI_Keypad5              : CGKeyCode = 0x57
    const val kVK_ANSI_Keypad6              : CGKeyCode = 0x58
    const val kVK_ANSI_Keypad7              : CGKeyCode = 0x59
    const val kVK_ANSI_Keypad8              : CGKeyCode = 0x5B
    const val kVK_ANSI_Keypad9              : CGKeyCode = 0x5C

    // keycodes for keys that are independent of keyboard layout
    const val kVK_Return                    : CGKeyCode = 0x24
    const val kVK_Tab                       : CGKeyCode = 0x30
    const val kVK_Space                     : CGKeyCode = 0x31
    const val kVK_Delete                    : CGKeyCode = 0x33
    const val kVK_Escape                    : CGKeyCode = 0x35
    const val kVK_Command                   : CGKeyCode = 0x37
    const val kVK_Shift                     : CGKeyCode = 0x38
    const val kVK_CapsLock                  : CGKeyCode = 0x39
    const val kVK_Option                    : CGKeyCode = 0x3A
    const val kVK_Control                   : CGKeyCode = 0x3B
    const val kVK_RightCommand              : CGKeyCode = 0x36 // Out of order
    const val kVK_RightShift                : CGKeyCode = 0x3C
    const val kVK_RightOption               : CGKeyCode = 0x3D
    const val kVK_RightControl              : CGKeyCode = 0x3E
    const val kVK_Function                  : CGKeyCode = 0x3F
    const val kVK_F17                       : CGKeyCode = 0x40
    const val kVK_VolumeUp                  : CGKeyCode = 0x48
    const val kVK_VolumeDown                : CGKeyCode = 0x49
    const val kVK_Mute                      : CGKeyCode = 0x4A
    const val kVK_F18                       : CGKeyCode = 0x4F
    const val kVK_F19                       : CGKeyCode = 0x50
    const val kVK_F20                       : CGKeyCode = 0x5A
    const val kVK_F5                        : CGKeyCode = 0x60
    const val kVK_F6                        : CGKeyCode = 0x61
    const val kVK_F7                        : CGKeyCode = 0x62
    const val kVK_F3                        : CGKeyCode = 0x63
    const val kVK_F8                        : CGKeyCode = 0x64
    const val kVK_F9                        : CGKeyCode = 0x65
    const val kVK_F11                       : CGKeyCode = 0x67
    const val kVK_F13                       : CGKeyCode = 0x69
    const val kVK_F16                       : CGKeyCode = 0x6A
    const val kVK_F14                       : CGKeyCode = 0x6B
    const val kVK_F10                       : CGKeyCode = 0x6D
    const val kVK_F12                       : CGKeyCode = 0x6F
    const val kVK_F15                       : CGKeyCode = 0x71
    const val kVK_Help                      : CGKeyCode = 0x72
    const val kVK_Home                      : CGKeyCode = 0x73
    const val kVK_PageUp                    : CGKeyCode = 0x74
    const val kVK_ForwardDelete             : CGKeyCode = 0x75
    const val kVK_F4                        : CGKeyCode = 0x76
    const val kVK_End                       : CGKeyCode = 0x77
    const val kVK_F2                        : CGKeyCode = 0x78
    const val kVK_PageDown                  : CGKeyCode = 0x79
    const val kVK_F1                        : CGKeyCode = 0x7A
    const val kVK_LeftArrow                 : CGKeyCode = 0x7B
    const val kVK_RightArrow                : CGKeyCode = 0x7C
    const val kVK_DownArrow                 : CGKeyCode = 0x7D
    const val kVK_UpArrow                   : CGKeyCode = 0x7E

    // ISO keyboards only
    const val kVK_ISO_Section               : CGKeyCode = 0x0A

    // JIS keyboards only
    const val kVK_JIS_Yen                   : CGKeyCode = 0x5D
    const val kVK_JIS_Underscore            : CGKeyCode = 0x5E
    const val kVK_JIS_KeypadComma           : CGKeyCode = 0x5F
    const val kVK_JIS_Eisu                  : CGKeyCode = 0x66
    const val kVK_JIS_Kana                  : CGKeyCode = 0x68

    fun isModifier(key: CGKeyCode): Boolean {
        return (kVK_RightCommand..kVK_Function).contains(key)
    }

    fun isPressed(key: CGKeyCode): Boolean {
        //TODO
        return false
    }

    /*val isModifier: Boolean
        get() {
            return (.kVK_RightCommand...(.kVK_Function)).contains(self)
        }

    var baseModifier: CGKeyCode?
    {
        if (.kVK_Command...(.kVK_Control)).contains(self)
        || self == .kVK_Function
        {
            return self
        }

        switch self
            {
                case .kVK_RightShift: return .kVK_Shift
                case .kVK_RightCommand: return .kVK_Command
                case .kVK_RightOption: return .kVK_Option
                case .kVK_RightControl: return .kVK_Control

                default: return nil
            }
    }

    var isPressed: Bool {
    CGEventSource.keyState(.combinedSessionState, key: self)
    }*/
}
