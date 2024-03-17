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
package androidx.compose.ui.platform

import Context
import SuppressLint
import View
import ViewGroup
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toSkiaImage
import androidx.compose.ui.graphics.toUIImage
import androidx.compose.ui.node.LayoutNode
import androidx.compose.ui.viewinterop.AndroidViewHolder
import bottom
import childCount
import cocoapods.ToppingCompose.EXACTLY
import cocoapods.ToppingCompose.MeasureSpec
import cocoapods.ToppingCompose.MeasureSpec.Companion.getMode
import cocoapods.ToppingCompose.TIOSKHMotionEvent
import cocoapods.ToppingCompose.TIOSKHSkiaCanvas
import cocoapods.ToppingCompose.TIOSKHSkikoImage
import cocoapods.ToppingCompose.TIOSKHSkikoImageInfo
import cocoapods.ToppingCompose.TIOSKHTCanvasProtocol
import getChildAt
import init
import isLayoutRequested
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import left
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGSizeZero
import platform.UIKit.UIGestureRecognizerState
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import right
import top

/**
 * Used by [AndroidComposeView] to handle the Android [View]s attached to its hierarchy.
 * The [AndroidComposeView] has one direct [AndroidViewsHandler], which is responsible
 * of intercepting [requestLayout]s, [onMeasure]s, [invalidate]s, etc. sent from or towards
 * children.
 */
@OptIn(ExperimentalForeignApi::class)
internal class AndroidViewsHandler(context: Context) : ViewGroup() {
    init {
        //clipChildren = false
        init(context, null)
        lc = context
    }

    val holderToLayoutNode = hashMapOf<AndroidViewHolder, LayoutNode>()
    val layoutNodeToHolder = hashMapOf<LayoutNode, AndroidViewHolder>()

    override fun onMeasure(widthMeasureSpec: Int, _1: Int) {
        val heightMeasureSpec = _1
        // Layout will be handled by component nodes. However, we act like proper measurement
        // here in case ViewRootImpl did forceLayout().
        require(getMode(widthMeasureSpec).toLong() == EXACTLY) { "widthMeasureSpec should be EXACTLY" }
        require(getMode(heightMeasureSpec).toLong() == EXACTLY) { "heightMeasureSpec should be EXACTLY" }
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        // Remeasure children, such that, if ViewRootImpl did forceLayout(), the holders
        // will be set PFLAG_LAYOUT_REQUIRED and they will be relaid out during the next layout.
        // This will ensure that the need relayout flags will be cleared correctly.
        holderToLayoutNode.keys.forEach { it.remeasure() }
    }

    override fun layout(l: Int, _1: Int, _2: Int, _3: Int) {
        // Layout was already handled by component nodes, but replace here because
        // the View system has forced relayout on children. This method will only be called
        // when forceLayout is called on the Views hierarchy.
        holderToLayoutNode.keys.forEach { it.self.layout(it.self.left, it.self.top, it.self.right, it.self.bottom) }
    }

    fun drawView(view: AndroidViewHolder, canvas: android.graphics.Canvas) {
        val nativeImage = view.self._view!!.toUIImage()

        val skImage = nativeImage.toSkiaImage()!!
        canvas.drawImage(skImage, 0f, 0f)
        //view.draw(canvas)
    }

    // Touch events forwarding will be handled by component nodes.
    override fun dispatchTouchEvent(event: TIOSKHMotionEvent?): Boolean {
        return true
    }

    // No call to super to avoid invalidating the AndroidComposeView and rely on
    // component nodes logic.
    @SuppressLint("MissingSuperCall")
    override fun requestLayout() {
        // Hack to cleanup the dirty layout flag on ourselves, such that this method continues
        // to be called for further children requestLayout().
        //cleanupLayoutState(this)
        // requestLayout() was called by a child, so we have to request remeasurement for
        // their corresponding layout node.
        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            val node = holderToLayoutNode[child.kParentType]
            if (child.isLayoutRequested && node != null) {
                node.requestRemeasure()
            }
        }
    }

    //override fun shouldDelayChildPressedState(): Boolean = false

    // We don't want the AndroidComposeView drawing the holder and its children. All draw
    // calls should come through AndroidViewHolder or ViewLayer.
    override fun dispatchDrawCanvas(canvas: TIOSKHTCanvasProtocol) {

    }
}
