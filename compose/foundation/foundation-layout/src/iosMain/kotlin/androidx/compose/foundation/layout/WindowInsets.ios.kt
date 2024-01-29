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

@file:OptIn(ExperimentalForeignApi::class)

package androidx.compose.foundation.layout

import View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalKeyboardOverlapHeight
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIApplication
import platform.UIKit.UIEdgeInsets
import platform.UIKit.UIInterfaceOrientationLandscapeLeft
import platform.UIKit.UIInterfaceOrientationLandscapeRight
import platform.UIKit.UIInterfaceOrientationPortrait
import platform.UIKit.UIInterfaceOrientationPortraitUpsideDown

private val ZeroInsets = WindowInsets(0, 0, 0, 0)

fun UIEdgeInsets.toWindowInsets(): WindowInsets {
    return WindowInsets(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
}

/**
 * This insets represents iOS SafeAreas.
 */
private val WindowInsets.Companion.iosSafeArea: WindowInsets
    @Composable
    get() = LocalView.current._view!!.window!!.safeAreaInsets.useContents { toWindowInsets() }

/**
 * This insets represents iOS layoutMargins.
 */
private val WindowInsets.Companion.layoutMargins: WindowInsets
    @Composable
    get() {
        val view = LocalView.current
        return WindowInsets(view.dMarginLeft, view.dMarginTop, view.dMarginRight, view.dMarginBottom)
    }

/**
 * An insets type representing the window of a caption bar.
 * It is useless for iOS.
 */
actual val WindowInsets.Companion.captionBar: WindowInsets
    get() = ZeroInsets

/**
 * This [WindowInsets] represents the area with the display cutout (e.g. for camera).
 */
actual val WindowInsets.Companion.displayCutout: WindowInsets
    @Composable
    get() {
        return when (UIApplication.sharedApplication.statusBarOrientation)
        {
            UIInterfaceOrientationPortrait -> iosSafeArea.only(WindowInsetsSides.Top)
            UIInterfaceOrientationPortraitUpsideDown -> iosSafeArea.only(WindowInsetsSides.Bottom)
            UIInterfaceOrientationLandscapeLeft -> iosSafeArea.only(WindowInsetsSides.Right)
            UIInterfaceOrientationLandscapeRight -> iosSafeArea.only(WindowInsetsSides.Left)
            else -> iosSafeArea.only(WindowInsetsSides.Top)
        }
    }

/**
 * An insets type representing the window of an "input method",
 * for iOS IME representing the software keyboard.
 *
 * TODO: Animation doesn't work on iOS yet
 */
actual val WindowInsets.Companion.ime: WindowInsets
    @Composable
    get() = WindowInsets(bottom = LocalKeyboardOverlapHeight.current.keyBoardHeight.dp)

/**
 * These insets represent the space where system gestures have priority over application gestures.
 */
actual val WindowInsets.Companion.mandatorySystemGestures: WindowInsets
    @Composable
    get() = iosSafeArea.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom)

/**
 * These insets represent where system UI places navigation bars.
 * Interactive UI should avoid the navigation bars area.
 */
actual val WindowInsets.Companion.navigationBars: WindowInsets
    @Composable
    get() = iosSafeArea.only(WindowInsetsSides.Bottom)

/**
 * These insets represent status bar.
 */
actual val WindowInsets.Companion.statusBars: WindowInsets
    @Composable
    get() = when (UIApplication.sharedApplication.statusBarOrientation)
    {
        UIInterfaceOrientationPortrait -> iosSafeArea.only(WindowInsetsSides.Top)
        else -> ZeroInsets
    }

/**
 * These insets represent all system bars.
 * Includes [statusBars], [captionBar] as well as [navigationBars], but not [ime].
 */
actual val WindowInsets.Companion.systemBars: WindowInsets
    @Composable
    get() = iosSafeArea

/**
 * The [systemGestures] insets represent the area of a window where system gestures have
 * priority and may consume some or all touch input, e.g. due to the system bar
 * occupying it, or it being reserved for touch-only gestures.
 */
actual val WindowInsets.Companion.systemGestures: WindowInsets
    @Composable
    get() = layoutMargins // the same as iosSafeArea.add(WindowInsets(left = 16.dp, right = 16.dp))

/**
 * Returns the tappable element insets.
 */
actual val WindowInsets.Companion.tappableElement: WindowInsets
    @Composable
    get() = iosSafeArea.only(WindowInsetsSides.Top)

/**
 * The insets for the curved areas in a waterfall display.
 * It is useless for iOS.
 */
actual val WindowInsets.Companion.waterfall: WindowInsets get() = ZeroInsets

/**
 * The insets that include areas where content may be covered by other drawn content.
 * This includes all [systemBars], [displayCutout], and [ime].
 */
actual val WindowInsets.Companion.safeDrawing: WindowInsets
    @Composable
    get() = systemBars.union(ime).union(displayCutout)

/**
 * The insets that include areas where gestures may be confused with other input,
 * including [systemGestures], [mandatorySystemGestures], [waterfall], and [tappableElement].
 */
actual val WindowInsets.Companion.safeGestures: WindowInsets
    @Composable
    get() = tappableElement.union(mandatorySystemGestures).union(systemGestures).union(waterfall)

/**
 * The insets that include all areas that may be drawn over or have gesture confusion,
 * including everything in [safeDrawing] and [safeGestures].
 */
actual val WindowInsets.Companion.safeContent: WindowInsets
    @Composable
    get() = safeDrawing.union(safeGestures)