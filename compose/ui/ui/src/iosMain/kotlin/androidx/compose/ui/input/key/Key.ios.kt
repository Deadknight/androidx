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

import androidx.compose.ui.input.key.Key.Companion.Number
import androidx.compose.ui.util.packInts
import androidx.compose.ui.util.unpackInt1

/**
 * Actual implementation of [Key] for Android.
 *
 * @param keyCode an integer code representing the key pressed.
 *
 * @sample androidx.compose.ui.samples.KeyEventIsAltPressedSample
 */
actual value class Key(val keyCode: Long) {
    actual companion object {
        /** Unknown key. */
        actual val Unknown = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Soft Left key.
         *
         * Usually situated below the display on phones and used as a multi-function
         * feature key for selecting a software defined function shown on the bottom left
         * of the display.
         */
        actual val SoftLeft = Key(CGKeyCodeList.kVK_RightOption)

        /**
         * Soft Right key.
         *
         * Usually situated below the display on phones and used as a multi-function
         * feature key for selecting a software defined function shown on the bottom right
         * of the display.
         */
        actual val SoftRight = Key(CGKeyCodeList.kVK_RightCommand)

        /**
         * Home key.
         *
         * This key is handled by the framework and is never delivered to applications.
         */
        actual val Home = Key(CGKeyCodeList.kVK_Home)

        /** Back key. */
        actual val Back = Key(CGKeyCodeList.kVK_Escape)

        /** Help key. */
        actual val Help = Key(CGKeyCodeList.kVK_Help)

        /**
         * Navigate to previous key.
         *
         * Goes backward by one item in an ordered collection of items.
         */
        actual val NavigatePrevious = Key(CGKeyCodeList.kVK_LeftArrow)

        /**
         * Navigate to next key.
         *
         * Advances to the next item in an ordered collection of items.
         */
        actual val NavigateNext = Key(CGKeyCodeList.kVK_RightArrow)

        /**
         * Navigate in key.
         *
         * Activates the item that currently has focus or expands to the next level of a navigation
         * hierarchy.
         */
        actual val NavigateIn = Key(CGKeyCodeList.kVK_Tab)

        /**
         * Navigate out key.
         *
         * Backs out one level of a navigation hierarchy or collapses the item that currently has
         * focus.
         */
        actual val NavigateOut = Key(CGKeyCodeList.kVK_Tab)

        /** Consumed by the system for navigation up. */
        actual val SystemNavigationUp = Key(CGKeyCodeList.kVK_Unknown)

        /** Consumed by the system for navigation down. */
        actual val SystemNavigationDown = Key(CGKeyCodeList.kVK_Unknown)

        /** Consumed by the system for navigation left. */
        actual val SystemNavigationLeft = Key(CGKeyCodeList.kVK_Unknown)

        /** Consumed by the system for navigation right. */
        actual val SystemNavigationRight = Key(CGKeyCodeList.kVK_Unknown)

        /** Call key. */
        actual val Call = Key(CGKeyCodeList.kVK_Unknown)

        /** End Call key. */
        actual val EndCall = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Up Arrow Key / Directional Pad Up key.
         *
         * May also be synthesized from trackball motions.
         */
        actual val DirectionUp = Key(CGKeyCodeList.kVK_UpArrow)

        /**
         * Down Arrow Key / Directional Pad Down key.
         *
         * May also be synthesized from trackball motions.
         */
        actual val DirectionDown = Key(CGKeyCodeList.kVK_DownArrow)

        /**
         * Left Arrow Key / Directional Pad Left key.
         *
         * May also be synthesized from trackball motions.
         */
        actual val DirectionLeft = Key(CGKeyCodeList.kVK_LeftArrow)

        /**
         * Right Arrow Key / Directional Pad Right key.
         *
         * May also be synthesized from trackball motions.
         */
        actual val DirectionRight = Key(CGKeyCodeList.kVK_RightArrow)

        /**
         * Center Arrow Key / Directional Pad Center key.
         *
         * May also be synthesized from trackball motions.
         */
        actual val DirectionCenter = Key(CGKeyCodeList.kVK_Unknown)

        /** Directional Pad Up-Left. */
        actual val DirectionUpLeft = Key(CGKeyCodeList.kVK_Unknown)

        /** Directional Pad Down-Left. */
        actual val DirectionDownLeft = Key(CGKeyCodeList.kVK_Unknown)

        /** Directional Pad Up-Right. */
        actual val DirectionUpRight = Key(CGKeyCodeList.kVK_Unknown)

        /** Directional Pad Down-Right. */
        actual val DirectionDownRight = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Volume Up key.
         *
         * Adjusts the speaker volume up.
         */
        actual val VolumeUp = Key(CGKeyCodeList.kVK_VolumeUp)

        /**
         * Volume Down key.
         *
         * Adjusts the speaker volume down.
         */
        actual val VolumeDown = Key(CGKeyCodeList.kVK_VolumeDown)

        /** Power key.  */
        actual val Power = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Camera key.
         *
         * Used to launch a camera application or take pictures.
         */
        actual val Camera = Key(CGKeyCodeList.kVK_Unknown)

        /** Clear key. */
        actual val Clear = Key(CGKeyCodeList.kVK_Unknown)

        /** '0' key. */
        actual val Zero = Key(CGKeyCodeList.kVK_ANSI_0)

        /** '1' key. */
        actual val One = Key(CGKeyCodeList.kVK_ANSI_1)

        /** '2' key. */
        actual val Two = Key(CGKeyCodeList.kVK_ANSI_2)

        /** '3' key. */
        actual val Three = Key(CGKeyCodeList.kVK_ANSI_3)

        /** '4' key. */
        actual val Four = Key(CGKeyCodeList.kVK_ANSI_4)

        /** '5' key. */
        actual val Five = Key(CGKeyCodeList.kVK_ANSI_5)

        /** '6' key. */
        actual val Six = Key(CGKeyCodeList.kVK_ANSI_6)

        /** '7' key. */
        actual val Seven = Key(CGKeyCodeList.kVK_ANSI_7)

        /** '8' key. */
        actual val Eight = Key(CGKeyCodeList.kVK_ANSI_8)

        /** '9' key. */
        actual val Nine = Key(CGKeyCodeList.kVK_ANSI_9)

        /** '+' key. */
        actual val Plus = Key(CGKeyCodeList.kVK_ANSI_KeypadPlus)

        /** '-' key. */
        actual val Minus = Key(CGKeyCodeList.kVK_ANSI_KeypadMinus)

        /** '*' key. */
        actual val Multiply = Key(CGKeyCodeList.kVK_ANSI_KeypadMultiply)

        /** '=' key. */
        actual val Equals = Key(CGKeyCodeList.kVK_ANSI_KeypadEquals)

        /** '#' key. */
        actual val Pound = Key(CGKeyCodeList.kVK_ANSI_KeypadDecimal)

        /** 'A' key. */
        actual val A = Key(CGKeyCodeList.kVK_ANSI_A)

        /** 'B' key. */
        actual val B = Key(CGKeyCodeList.kVK_ANSI_B)

        /** 'C' key. */
        actual val C = Key(CGKeyCodeList.kVK_ANSI_C)

        /** 'D' key. */
        actual val D = Key(CGKeyCodeList.kVK_ANSI_D)

        /** 'E' key. */
        actual val E = Key(CGKeyCodeList.kVK_ANSI_E)

        /** 'F' key. */
        actual val F = Key(CGKeyCodeList.kVK_ANSI_F)

        /** 'G' key. */
        actual val G = Key(CGKeyCodeList.kVK_ANSI_G)

        /** 'H' key. */
        actual val H = Key(CGKeyCodeList.kVK_ANSI_H)

        /** 'I' key. */
        actual val I = Key(CGKeyCodeList.kVK_ANSI_I)

        /** 'J' key. */
        actual val J = Key(CGKeyCodeList.kVK_ANSI_J)

        /** 'K' key. */
        actual val K = Key(CGKeyCodeList.kVK_ANSI_K)

        /** 'L' key. */
        actual val L = Key(CGKeyCodeList.kVK_ANSI_L)

        /** 'M' key. */
        actual val M = Key(CGKeyCodeList.kVK_ANSI_M)

        /** 'N' key. */
        actual val N = Key(CGKeyCodeList.kVK_ANSI_N)

        /** 'O' key. */
        actual val O = Key(CGKeyCodeList.kVK_ANSI_O)

        /** 'P' key. */
        actual val P = Key(CGKeyCodeList.kVK_ANSI_P)

        /** 'Q' key. */
        actual val Q = Key(CGKeyCodeList.kVK_ANSI_Q)

        /** 'R' key. */
        actual val R = Key(CGKeyCodeList.kVK_ANSI_R)

        /** 'S' key. */
        actual val S = Key(CGKeyCodeList.kVK_ANSI_S)

        /** 'T' key. */
        actual val T = Key(CGKeyCodeList.kVK_ANSI_T)

        /** 'U' key. */
        actual val U = Key(CGKeyCodeList.kVK_ANSI_U)

        /** 'V' key. */
        actual val V = Key(CGKeyCodeList.kVK_ANSI_V)

        /** 'W' key. */
        actual val W = Key(CGKeyCodeList.kVK_ANSI_W)

        /** 'X' key. */
        actual val X = Key(CGKeyCodeList.kVK_ANSI_X)

        /** 'Y' key. */
        actual val Y = Key(CGKeyCodeList.kVK_ANSI_Y)

        /** 'Z' key. */
        actual val Z = Key(CGKeyCodeList.kVK_ANSI_Z)

        /** ',' key. */
        actual val Comma = Key(CGKeyCodeList.kVK_ANSI_Comma)

        /** '.' key. */
        actual val Period = Key(CGKeyCodeList.kVK_ANSI_Period)

        /** Left Alt modifier key. */
        actual val AltLeft = Key(CGKeyCodeList.kVK_Option)

        /** Right Alt modifier key. */
        actual val AltRight = Key(CGKeyCodeList.kVK_RightOption)

        /** Left Shift modifier key. */
        actual val ShiftLeft = Key(CGKeyCodeList.kVK_Shift)

        /** Right Shift modifier key. */
        actual val ShiftRight = Key(CGKeyCodeList.kVK_RightShift)

        /** Tab key. */
        actual val Tab = Key(CGKeyCodeList.kVK_Tab)

        /** Space key. */
        actual val Spacebar = Key(CGKeyCodeList.kVK_Space)

        /**
         * Symbol modifier key.
         *
         * Used to enter alternate symbols.
         */
        actual val Symbol = Key(CGKeyCodeList.kVK_Command)

        /**
         * Browser special function key.
         *
         * Used to launch a browser application.
         */
        actual val Browser = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Envelope special function key.
         *
         * Used to launch a mail application.
         */
        actual val Envelope = Key(CGKeyCodeList.kVK_Unknown)

        /** Enter key. */
        actual val Enter = Key(CGKeyCodeList.kVK_ANSI_KeypadEnter)

        /**
         * Backspace key.
         *
         * Deletes characters before the insertion point, unlike [Delete].
         */
        actual val Backspace = Key(CGKeyCodeList.kVK_Delete)

        /**
         * Delete key.
         *
         * Deletes characters ahead of the insertion point, unlike [Backspace].
         */
        actual val Delete = Key(CGKeyCodeList.kVK_ForwardDelete)

        /** Escape key. */
        actual val Escape = Key(CGKeyCodeList.kVK_Escape)

        /** Left Control modifier key. */
        actual val CtrlLeft = Key(CGKeyCodeList.kVK_Control)

        /** Right Control modifier key. */
        actual val CtrlRight = Key(CGKeyCodeList.kVK_RightControl)

        /** Caps Lock key. */
        actual val CapsLock = Key(CGKeyCodeList.kVK_CapsLock)

        /** Scroll Lock key. */
        actual val ScrollLock = Key(CGKeyCodeList.kVK_Unknown)

        /** Left Meta modifier key. */
        actual val MetaLeft = Key(CGKeyCodeList.kVK_Option)

        /** Right Meta modifier key. */
        actual val MetaRight = Key(CGKeyCodeList.kVK_RightOption)

        /** Function modifier key. */
        actual val Function = Key(CGKeyCodeList.kVK_Function)

        /** System Request / Print Screen key. */
        actual val PrintScreen = Key(CGKeyCodeList.kVK_Unknown)

        /** Break / Pause key. */
        actual val Break = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Home Movement key.
         *
         * Used for scrolling or moving the cursor around to the start of a line
         * or to the top of a list.
         */
        actual val MoveHome = Key(CGKeyCodeList.kVK_Home)

        /**
         * End Movement key.
         *
         * Used for scrolling or moving the cursor around to the end of a line
         * or to the bottom of a list.
         */
        actual val MoveEnd = Key(CGKeyCodeList.kVK_End)

        /**
         * Insert key.
         *
         * Toggles insert / overwrite edit mode.
         */
        actual val Insert = Key(CGKeyCodeList.kVK_Unknown)

        /** Cut key. */
        actual val Cut = Key(CGKeyCodeList.kVK_Unknown)

        /** Copy key. */
        actual val Copy = Key(CGKeyCodeList.kVK_Unknown)

        /** Paste key. */
        actual val Paste = Key(CGKeyCodeList.kVK_Unknown)

        /** '`' (backtick) key. */
        actual val Grave = Key(CGKeyCodeList.kVK_Unknown)

        /** '[' key. */
        actual val LeftBracket = Key(CGKeyCodeList.kVK_ANSI_LeftBracket)

        /** ']' key. */
        actual val RightBracket = Key(CGKeyCodeList.kVK_ANSI_RightBracket)

        /** '/' key. */
        actual val Slash = Key(CGKeyCodeList.kVK_ANSI_Slash)

        /** '\' key. */
        actual val Backslash = Key(CGKeyCodeList.kVK_Unknown)

        /** ';' key. */
        actual val Semicolon = Key(CGKeyCodeList.kVK_ANSI_Semicolon)

        /** ''' (apostrophe) key. */
        actual val Apostrophe = Key(CGKeyCodeList.kVK_Unknown)

        /** '@' key. */
        actual val At = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Number modifier key.
         *
         * Used to enter numeric symbols.
         * This key is not Num Lock; it is more like  [AltLeft].
         */
        actual val Number = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Headset Hook key.
         *
         * Used to hang up calls and stop media.
         */
        actual val HeadsetHook = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Camera Focus key.
         *
         * Used to focus the camera.
         */
        actual val Focus = Key(CGKeyCodeList.kVK_Unknown)

        /** Menu key. */
        actual val Menu = Key(CGKeyCodeList.kVK_Unknown)

        /** Notification key. */
        actual val Notification = Key(CGKeyCodeList.kVK_Unknown)

        /** Search key. */
        actual val Search = Key(CGKeyCodeList.kVK_Unknown)

        /** Page Up key. */
        actual val PageUp = Key(CGKeyCodeList.kVK_PageUp)

        /** Page Down key. */
        actual val PageDown = Key(CGKeyCodeList.kVK_PageDown)

        /**
         * Picture Symbols modifier key.
         *
         * Used to switch symbol sets (Emoji, Kao-moji).
         */
        actual val PictureSymbols = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Switch Charset modifier key.
         *
         * Used to switch character sets (Kanji, Katakana).
         */
        actual val SwitchCharset = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * A Button key.
         *
         * On a game controller, the A button should be either the button labeled A
         * or the first button on the bottom row of controller buttons.
         */
        actual val ButtonA = Key(CGKeyCodeList.kVK_ANSI_A)

        /**
         * B Button key.
         *
         * On a game controller, the B button should be either the button labeled B
         * or the second button on the bottom row of controller buttons.
         */
        actual val ButtonB = Key(CGKeyCodeList.kVK_ANSI_B)

        /**
         * C Button key.
         *
         * On a game controller, the C button should be either the button labeled C
         * or the third button on the bottom row of controller buttons.
         */
        actual val ButtonC = Key(CGKeyCodeList.kVK_ANSI_C)

        /**
         * X Button key.
         *
         * On a game controller, the X button should be either the button labeled X
         * or the first button on the upper row of controller buttons.
         */
        actual val ButtonX = Key(CGKeyCodeList.kVK_ANSI_X)

        /**
         * Y Button key.
         *
         * On a game controller, the Y button should be either the button labeled Y
         * or the second button on the upper row of controller buttons.
         */
        actual val ButtonY = Key(CGKeyCodeList.kVK_ANSI_Y)

        /**
         * Z Button key.
         *
         * On a game controller, the Z button should be either the button labeled Z
         * or the third button on the upper row of controller buttons.
         */
        actual val ButtonZ = Key(CGKeyCodeList.kVK_ANSI_Z)

        /**
         * L1 Button key.
         *
         * On a game controller, the L1 button should be either the button labeled L1 (or L)
         * or the top left trigger button.
         */
        actual val ButtonL1 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * R1 Button key.
         *
         * On a game controller, the R1 button should be either the button labeled R1 (or R)
         * or the top right trigger button.
         */
        actual val ButtonR1 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * L2 Button key.
         *
         * On a game controller, the L2 button should be either the button labeled L2
         * or the bottom left trigger button.
         */
        actual val ButtonL2 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * R2 Button key.
         *
         * On a game controller, the R2 button should be either the button labeled R2
         * or the bottom right trigger button.
         */
        actual val ButtonR2 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Left Thumb Button key.
         *
         * On a game controller, the left thumb button indicates that the left (or only)
         * joystick is pressed.
         */
        actual val ButtonThumbLeft = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Right Thumb Button key.
         *
         * On a game controller, the right thumb button indicates that the right
         * joystick is pressed.
         */
        actual val ButtonThumbRight = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Start Button key.
         *
         * On a game controller, the button labeled Start.
         */
        actual val ButtonStart = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Select Button key.
         *
         * On a game controller, the button labeled Select.
         */
        actual val ButtonSelect = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Mode Button key.
         *
         * On a game controller, the button labeled Mode.
         */
        actual val ButtonMode = Key(CGKeyCodeList.kVK_Unknown)

        /** Generic Game Pad Button #1. */
        actual val Button1 = Key(CGKeyCodeList.kVK_ANSI_1)

        /** Generic Game Pad Button #2. */
        actual val Button2 = Key(CGKeyCodeList.kVK_ANSI_2)

        /** Generic Game Pad Button #3. */
        actual val Button3 = Key(CGKeyCodeList.kVK_ANSI_3)

        /** Generic Game Pad Button #4. */
        actual val Button4 = Key(CGKeyCodeList.kVK_ANSI_4)

        /** Generic Game Pad Button #5. */
        actual val Button5 = Key(CGKeyCodeList.kVK_ANSI_5)

        /** Generic Game Pad Button #6. */
        actual val Button6 = Key(CGKeyCodeList.kVK_ANSI_6)

        /** Generic Game Pad Button #7. */
        actual val Button7 = Key(CGKeyCodeList.kVK_ANSI_7)

        /** Generic Game Pad Button #8. */
        actual val Button8 = Key(CGKeyCodeList.kVK_ANSI_8)

        /** Generic Game Pad Button #9. */
        actual val Button9 = Key(CGKeyCodeList.kVK_ANSI_9)

        /** Generic Game Pad Button #10. */
        actual val Button10 = Key(CGKeyCodeList.kVK_Unknown)

        /** Generic Game Pad Button #11. */
        actual val Button11 = Key(CGKeyCodeList.kVK_Unknown)

        /** Generic Game Pad Button #12. */
        actual val Button12 = Key(CGKeyCodeList.kVK_Unknown)

        /** Generic Game Pad Button #13. */
        actual val Button13 = Key(CGKeyCodeList.kVK_Unknown)

        /** Generic Game Pad Button #14. */
        actual val Button14 = Key(CGKeyCodeList.kVK_Unknown)

        /** Generic Game Pad Button #15. */
        actual val Button15 = Key(CGKeyCodeList.kVK_Unknown)

        /** Generic Game Pad Button #16. */
        actual val Button16 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Forward key.
         *
         * Navigates forward in the history stack. Complement of [Back].
         */
        actual val Forward = Key(CGKeyCodeList.kVK_Unknown)

        /** F1 key. */
        actual val F1 = Key(CGKeyCodeList.kVK_F1)

        /** F2 key. */
        actual val F2 = Key(CGKeyCodeList.kVK_F2)

        /** F3 key. */
        actual val F3 = Key(CGKeyCodeList.kVK_F3)

        /** F4 key. */
        actual val F4 = Key(CGKeyCodeList.kVK_F4)

        /** F5 key. */
        actual val F5 = Key(CGKeyCodeList.kVK_F5)

        /** F6 key. */
        actual val F6 = Key(CGKeyCodeList.kVK_F6)

        /** F7 key. */
        actual val F7 = Key(CGKeyCodeList.kVK_F7)

        /** F8 key. */
        actual val F8 = Key(CGKeyCodeList.kVK_F8)

        /** F9 key. */
        actual val F9 = Key(CGKeyCodeList.kVK_F9)

        /** F10 key. */
        actual val F10 = Key(CGKeyCodeList.kVK_F10)

        /** F11 key. */
        actual val F11 = Key(CGKeyCodeList.kVK_F11)

        /** F12 key. */
        actual val F12 = Key(CGKeyCodeList.kVK_F12)

        /**
         * Num Lock key.
         *
         * This is the Num Lock key; it is different from [Number].
         * This key alters the behavior of other keys on the numeric keypad.
         */
        actual val NumLock = Key(CGKeyCodeList.kVK_Unknown)

        /** Numeric keypad '0' key. */
        actual val NumPad0 = Key(CGKeyCodeList.kVK_ANSI_Keypad0)

        /** Numeric keypad '1' key. */
        actual val NumPad1 = Key(CGKeyCodeList.kVK_ANSI_Keypad1)

        /** Numeric keypad '2' key. */
        actual val NumPad2 = Key(CGKeyCodeList.kVK_ANSI_Keypad2)

        /** Numeric keypad '3' key. */
        actual val NumPad3 = Key(CGKeyCodeList.kVK_ANSI_Keypad3)

        /** Numeric keypad '4' key. */
        actual val NumPad4 = Key(CGKeyCodeList.kVK_ANSI_Keypad4)

        /** Numeric keypad '5' key. */
        actual val NumPad5 = Key(CGKeyCodeList.kVK_ANSI_Keypad5)

        /** Numeric keypad '6' key. */
        actual val NumPad6 = Key(CGKeyCodeList.kVK_ANSI_Keypad6)

        /** Numeric keypad '7' key. */
        actual val NumPad7 = Key(CGKeyCodeList.kVK_ANSI_Keypad7)

        /** Numeric keypad '8' key. */
        actual val NumPad8 = Key(CGKeyCodeList.kVK_ANSI_Keypad8)

        /** Numeric keypad '9' key. */
        actual val NumPad9 = Key(CGKeyCodeList.kVK_ANSI_Keypad9)

        /** Numeric keypad '/' key (for division). */
        actual val NumPadDivide = Key(CGKeyCodeList.kVK_ANSI_KeypadDivide)

        /** Numeric keypad '*' key (for multiplication). */
        actual val NumPadMultiply = Key(CGKeyCodeList.kVK_ANSI_KeypadMultiply)

        /** Numeric keypad '-' key (for subtraction). */
        actual val NumPadSubtract = Key(CGKeyCodeList.kVK_ANSI_KeypadMinus)

        /** Numeric keypad '+' key (for addition). */
        actual val NumPadAdd = Key(CGKeyCodeList.kVK_ANSI_KeypadPlus)

        /** Numeric keypad '.' key (for decimals or digit grouping). */
        actual val NumPadDot = Key(CGKeyCodeList.kVK_ANSI_KeypadDecimal)

        /** Numeric keypad ',' key (for decimals or digit grouping). */
        actual val NumPadComma = Key(CGKeyCodeList.kVK_JIS_KeypadComma)

        /** Numeric keypad Enter key. */
        actual val NumPadEnter = Key(CGKeyCodeList.kVK_ANSI_KeypadEnter)

        /** Numeric keypad '=' key. */
        actual val NumPadEquals = Key(CGKeyCodeList.kVK_ANSI_KeypadEquals)

        /** Numeric keypad '(' key. */
        actual val NumPadLeftParenthesis = Key(CGKeyCodeList.kVK_Unknown)

        /** Numeric keypad ')' key. */
        actual val NumPadRightParenthesis = Key(CGKeyCodeList.kVK_Unknown)

        /** Play media key. */
        actual val MediaPlay = Key(CGKeyCodeList.kVK_Unknown)

        /** Pause media key. */
        actual val MediaPause = Key(CGKeyCodeList.kVK_Unknown)

        /** Play/Pause media key. */
        actual val MediaPlayPause = Key(CGKeyCodeList.kVK_Unknown)

        /** Stop media key. */
        actual val MediaStop = Key(CGKeyCodeList.kVK_Unknown)

        /** Record media key. */
        actual val MediaRecord = Key(CGKeyCodeList.kVK_Unknown)

        /** Play Next media key. */
        actual val MediaNext = Key(CGKeyCodeList.kVK_Unknown)

        /** Play Previous media key. */
        actual val MediaPrevious = Key(CGKeyCodeList.kVK_Unknown)

        /** Rewind media key. */
        actual val MediaRewind = Key(CGKeyCodeList.kVK_Unknown)

        /** Fast Forward media key. */
        actual val MediaFastForward = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Close media key.
         *
         * May be used to close a CD tray, for example.
         */
        actual val MediaClose = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Audio Track key.
         *
         * Switches the audio tracks.
         */
        actual val MediaAudioTrack = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Eject media key.
         *
         * May be used to eject a CD tray, for example.
         */
        actual val MediaEject = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Media Top Menu key.
         *
         * Goes to the top of media menu.
         */
        actual val MediaTopMenu = Key(CGKeyCodeList.kVK_Unknown)

        /** Skip forward media key. */
        actual val MediaSkipForward = Key(CGKeyCodeList.kVK_Unknown)

        /** Skip backward media key. */
        actual val MediaSkipBackward = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Step forward media key.
         *
         * Steps media forward, one frame at a time.
         */
        actual val MediaStepForward = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Step backward media key.
         *
         * Steps media backward, one frame at a time.
         */
        actual val MediaStepBackward = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Mute key.
         *
         * Mutes the microphone, unlike [VolumeMute].
         */
        actual val MicrophoneMute = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Volume Mute key.
         *
         * Mutes the speaker, unlike [MicrophoneMute].
         *
         * This key should normally be implemented as a toggle such that the first press
         * mutes the speaker and the second press restores the original volume.
         */
        actual val VolumeMute = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Info key.
         *
         * Common on TV remotes to show additional information related to what is
         * currently being viewed.
         */
        actual val Info = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Channel up key.
         *
         * On TV remotes, increments the television channel.
         */
        actual val ChannelUp = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Channel down key.
         *
         * On TV remotes, decrements the television channel.
         */
        actual val ChannelDown = Key(CGKeyCodeList.kVK_Unknown)

        /** Zoom in key. */
        actual val ZoomIn = Key(CGKeyCodeList.kVK_Unknown)

        /** Zoom out key. */
        actual val ZoomOut = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * TV key.
         *
         * On TV remotes, switches to viewing live TV.
         */
        actual val Tv = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Window key.
         *
         * On TV remotes, toggles picture-in-picture mode or other windowing functions.
         * On Android Wear devices, triggers a display offset.
         */
        actual val Window = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Guide key.
         *
         * On TV remotes, shows a programming guide.
         */
        actual val Guide = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * DVR key.
         *
         * On some TV remotes, switches to a DVR mode for recorded shows.
         */
        actual val Dvr = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Bookmark key.
         *
         * On some TV remotes, bookmarks content or web pages.
         */
        actual val Bookmark = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Toggle captions key.
         *
         * Switches the mode for closed-captioning text, for example during television shows.
         */
        actual val Captions = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Settings key.
         *
         * Starts the system settings activity.
         */
        actual val Settings = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * TV power key.
         *
         * On TV remotes, toggles the power on a television screen.
         */
        actual val TvPower = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * TV input key.
         *
         * On TV remotes, switches the input on a television screen.
         */
        actual val TvInput = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Set-top-box power key.
         *
         * On TV remotes, toggles the power on an external Set-top-box.
         */
        actual val SetTopBoxPower = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Set-top-box input key.
         *
         * On TV remotes, switches the input mode on an external Set-top-box.
         */
        actual val SetTopBoxInput = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * A/V Receiver power key.
         *
         * On TV remotes, toggles the power on an external A/V Receiver.
         */
        actual val AvReceiverPower = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * A/V Receiver input key.
         *
         * On TV remotes, switches the input mode on an external A/V Receiver.
         */
        actual val AvReceiverInput = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Red "programmable" key.
         *
         * On TV remotes, acts as a contextual/programmable key.
         */
        actual val ProgramRed = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Green "programmable" key.
         *
         * On TV remotes, acts as a contextual/programmable key.
         */
        actual val ProgramGreen = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Yellow "programmable" key.
         *
         * On TV remotes, acts as a contextual/programmable key.
         */
        actual val ProgramYellow = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Blue "programmable" key.
         *
         * On TV remotes, acts as a contextual/programmable key.
         */
        actual val ProgramBlue = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * App switch key.
         *
         * Should bring up the application switcher dialog.
         */
        actual val AppSwitch = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Language Switch key.
         *
         * Toggles the current input language such as switching between English and Japanese on
         * a QWERTY keyboard.  On some devices, the same function may be performed by
         * pressing Shift+Space.
         */
        actual val LanguageSwitch = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Manner Mode key.
         *
         * Toggles silent or vibrate mode on and off to make the device behave more politely
         * in certain settings such as on a crowded train.  On some devices, the key may only
         * operate when long-pressed.
         */
        actual val MannerMode = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * 3D Mode key.
         *
         * Toggles the display between 2D and 3D mode.
         */
        actual val Toggle2D3D = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Contacts special function key.
         *
         * Used to launch an address book application.
         */
        actual val Contacts = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Calendar special function key.
         *
         * Used to launch a calendar application.
         */
        actual val Calendar = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Music special function key.
         *
         * Used to launch a music player application.
         */
        actual val Music = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Calculator special function key.
         *
         * Used to launch a calculator application.
         */
        actual val Calculator = Key(CGKeyCodeList.kVK_Unknown)

        /** Japanese full-width / half-width key. */
        actual val ZenkakuHankaru = Key(CGKeyCodeList.kVK_Unknown)

        /** Japanese alphanumeric key. */
        actual val Eisu = Key(CGKeyCodeList.kVK_Unknown)

        /** Japanese non-conversion key. */
        actual val Muhenkan = Key(CGKeyCodeList.kVK_Unknown)

        /** Japanese conversion key. */
        actual val Henkan = Key(CGKeyCodeList.kVK_Unknown)

        /** Japanese katakana / hiragana key. */
        actual val KatakanaHiragana = Key(CGKeyCodeList.kVK_Unknown)

        /** Japanese Yen key. */
        actual val Yen = Key(CGKeyCodeList.kVK_Unknown)

        /** Japanese Ro key. */
        actual val Ro = Key(CGKeyCodeList.kVK_Unknown)

        /** Japanese kana key. */
        actual val Kana = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Assist key.
         *
         * Launches the global assist activity.  Not delivered to applications.
         */
        actual val Assist = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Brightness Down key.
         *
         * Adjusts the screen brightness down.
         */
        actual val BrightnessDown = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Brightness Up key.
         *
         * Adjusts the screen brightness up.
         */
        actual val BrightnessUp = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Sleep key.
         *
         * Puts the device to sleep. Behaves somewhat like [Power] but it
         * has no effect if the device is already asleep.
         */
        actual val Sleep = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Wakeup key.
         *
         * Wakes up the device.  Behaves somewhat like [Power] but it
         * has no effect if the device is already awake.
         */
        actual val WakeUp = Key(CGKeyCodeList.kVK_Unknown)

        /** Put device to sleep unless a wakelock is held.  */
        actual val SoftSleep = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Pairing key.
         *
         * Initiates peripheral pairing mode. Useful for pairing remote control
         * devices or game controllers, especially if no other input mode is
         * available.
         */
        actual val Pairing = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Last Channel key.
         *
         * Goes to the last viewed channel.
         */
        actual val LastChannel = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * TV data service key.
         *
         * Displays data services like weather, sports.
         */
        actual val TvDataService = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Voice Assist key.
         *
         * Launches the global voice assist activity. Not delivered to applications.
         */
        actual val VoiceAssist = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Radio key.
         *
         * Toggles TV service / Radio service.
         */
        actual val TvRadioService = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Teletext key.
         *
         * Displays Teletext service.
         */
        actual val TvTeletext = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Number entry key.
         *
         * Initiates to enter multi-digit channel number when each digit key is assigned
         * for selecting separate channel. Corresponds to Number Entry Mode (0x1D) of CEC
         * User Control Code.
         */
        actual val TvNumberEntry = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Analog Terrestrial key.
         *
         * Switches to analog terrestrial broadcast service.
         */
        actual val TvTerrestrialAnalog = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Digital Terrestrial key.
         *
         * Switches to digital terrestrial broadcast service.
         */
        actual val TvTerrestrialDigital = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Satellite key.
         *
         * Switches to digital satellite broadcast service.
         */
        actual val TvSatellite = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * BS key.
         *
         * Switches to BS digital satellite broadcasting service available in Japan.
         */
        actual val TvSatelliteBs = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * CS key.
         *
         * Switches to CS digital satellite broadcasting service available in Japan.
         */
        actual val TvSatelliteCs = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * BS/CS key.
         *
         * Toggles between BS and CS digital satellite services.
         */
        actual val TvSatelliteService = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Toggle Network key.
         *
         * Toggles selecting broadcast services.
         */
        actual val TvNetwork = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Antenna/Cable key.
         *
         * Toggles broadcast input source between antenna and cable.
         */
        actual val TvAntennaCable = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * HDMI #1 key.
         *
         * Switches to HDMI input #1.
         */
        actual val TvInputHdmi1 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * HDMI #2 key.
         *
         * Switches to HDMI input #2.
         */
        actual val TvInputHdmi2 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * HDMI #3 key.
         *
         * Switches to HDMI input #3.
         */
        actual val TvInputHdmi3 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * HDMI #4 key.
         *
         * Switches to HDMI input #4.
         */
        actual val TvInputHdmi4 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Composite #1 key.
         *
         * Switches to composite video input #1.
         */
        actual val TvInputComposite1 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Composite #2 key.
         *
         * Switches to composite video input #2.
         */
        actual val TvInputComposite2 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Component #1 key.
         *
         * Switches to component video input #1.
         */
        actual val TvInputComponent1 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Component #2 key.
         *
         * Switches to component video input #2.
         */
        actual val TvInputComponent2 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * VGA #1 key.
         *
         * Switches to VGA (analog RGB) input #1.
         */
        actual val TvInputVga1 = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Audio description key.
         *
         * Toggles audio description off / on.
         */
        actual val TvAudioDescription = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Audio description mixing volume up key.
         *
         * Increase the audio description volume as compared with normal audio volume.
         */
        actual val TvAudioDescriptionMixingVolumeUp = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Audio description mixing volume down key.
         *
         * Lessen audio description volume as compared with normal audio volume.
         */
        actual val TvAudioDescriptionMixingVolumeDown = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Zoom mode key.
         *
         * Changes Zoom mode (Normal, Full, Zoom, Wide-zoom, etc.)
         */
        actual val TvZoomMode = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Contents menu key.
         *
         * Goes to the title list. Corresponds to Contents Menu (0x0B) of CEC User Control Code
         */
        actual val TvContentsMenu = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Media context menu key.
         *
         * Goes to the context menu of media contents. Corresponds to Media Context-sensitive
         * Menu (0x11) of CEC User Control Code.
         */
        actual val TvMediaContextMenu = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Timer programming key.
         *
         * Goes to the timer recording menu. Corresponds to Timer Programming (0x54) of
         * CEC User Control Code.
         */
        actual val TvTimerProgramming = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Primary stem key for Wearables.
         *
         * Main power/reset button.
         */
        actual val StemPrimary = Key(CGKeyCodeList.kVK_Unknown)

        /** Generic stem key 1 for Wearables. */
        actual val Stem1 = Key(CGKeyCodeList.kVK_Unknown)

        /** Generic stem key 2 for Wearables. */
        actual val Stem2 = Key(CGKeyCodeList.kVK_Unknown)

        /** Generic stem key 3 for Wearables. */
        actual val Stem3 = Key(CGKeyCodeList.kVK_Unknown)

        /** Show all apps. */
        actual val AllApps = Key(CGKeyCodeList.kVK_Unknown)

        /** Refresh key. */
        actual val Refresh = Key(CGKeyCodeList.kVK_Unknown)

        /** Thumbs up key. Apps can use this to let user up-vote content. */
        actual val ThumbsUp = Key(CGKeyCodeList.kVK_Unknown)

        /** Thumbs down key. Apps can use this to let user down-vote content. */
        actual val ThumbsDown = Key(CGKeyCodeList.kVK_Unknown)

        /**
         * Used to switch current that is
         * consuming content. May be consumed by system to set account globally.
         */
        actual val ProfileSwitch = Key(CGKeyCodeList.kVK_Unknown)
    }

    actual override fun toString(): String = "Key code: $keyCode"
}

/**
 * The native keycode corresponding to this [Key].
 */
val Key.nativeKeyCode: Int
    get() = unpackInt1(keyCode)

fun Key(nativeKeyCode: Int): Key = Key(packInts(nativeKeyCode, 0))
