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
import View
import ViewGroup
import androidx.compose.ui.graphics.Canvas
import cocoapods.ToppingCompose.TIOSKHTCanvasProtocol
import getChildAt
import init
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNumber
import platform.Foundation.numberWithBool

/**
 * The container we will use for [ViewLayer]s.
 */
internal class ViewLayerContainer(context: Context) : DrawChildContainer(context) {

    inner class ViewLayerContainerViewGroup : ViewGroup() {
        override fun layout(l: Int, _1: Int, _2: Int, _3: Int) {
            super.layout(l, _1, _2, _3)
            self.dWidth = _2 - l
            self.dHeight = _3 - _1
        }

        override fun onMeasure(widthMeasureSpec: Int, _1: Int) {
            // we don't measure our children
            setMeasuredDimension(0, 0)
        }

        /**
         * We don't want to advertise children to the transition system. ViewLayers shouldn't be
         * watched for add/remove for transitions purposes.
         */
        override fun getChildCount(): Int = if (isDrawing) super.getChildCount() else 0

        override fun dispatchDrawCanvas(canvas: TIOSKHTCanvasProtocol) {
            // we draw our children as part of AndroidComposeView.dispatchDraw
        }
    }

    init {
        self = ViewLayerContainerViewGroup()
        self.kParentType = this
        self.init(context, null)
        self.lc = context
    }

    /**
     * We control our own child Views and we don't want the View system to force updating
     * the display lists.
     * We override hidden protected method from ViewGroup
     */
    protected fun dispatchGetDisplayList() {
    }
}

/**
 * The container we will use for [ViewLayer]s when [ViewLayer.shouldUseDispatchDraw] is true.
 */
internal open class DrawChildContainer(context: Context) {
    protected var isDrawing = false

    inner class DrawChildContainerViewGroup : ViewGroup() {
        override fun layout(l: Int, _1: Int, _2: Int, _3: Int) {
            super.layout(l, _1, _2, _3)
            self.dWidth = _2 - l
            self.dHeight = _3 - _1
        }

        override fun onMeasure(widthMeasureSpec: Int, _1: Int) {
            // we don't measure our children
            setMeasuredDimension(0, 0)
        }

        override fun dispatchDrawCanvas(canvas: TIOSKHTCanvasProtocol) {
            // We must updateDisplayListIfDirty for all invalidated Views.

            // We only want to call super.dispatchDraw() if there is an invalidated layer
            var doDispatch = false
            for (i in 0 until super.getChildCount()) {
                val child = getChildAt(i) as ViewLayer
                if (child.isInvalidated) {
                    doDispatch = true
                    break
                }
            }

            if (doDispatch) {
                isDrawing = true
                try {
                    super.dispatchDrawCanvas(canvas)
                } finally {
                    isDrawing = false
                }
            }
        }

        /**
         * We don't want to advertise children to the transition system. ViewLayers shouldn't be
         * watched for add/remove for transitions purposes.
         */
        override fun getChildCount(): Int = if (isDrawing) super.getChildCount() else 0
    }

    var self: ViewGroup

    init {
        self = DrawChildContainerViewGroup()
        self.kParentType = this
        self.init(context, null)
        self.lc = context
        //clipChildren = false

        // Hide this view and its children in tools:
        self.setTag("R.id.hide_in_inspector_tag", NSNumber.numberWithBool(true))
    }

    // we change visibility for this method so ViewLayer can use it for drawing
    @OptIn(ExperimentalForeignApi::class)
    internal fun drawChild(canvas: Canvas, view: View, drawingTime: Long) {
        //super.drawChild(canvas.nativeCanvas, view, drawingTime)
        /*var width = view.dWidth
        var height = view.dHeight
        if(width == 0 || height == 0)
            return

        UIGraphicsBeginImageContextWithOptions(CGSizeMake(width.toDouble(), height.toDouble()), view._view!!.opaque, 0.0)
        view._view!!.drawViewHierarchyInRect(view._view!!.bounds, false)
        val snapshotImageFromView = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        val img = snapshotImageFromView?.toSkiaImage()
        canvas.drawImage(ImageBi)
        canvas.drawImage()
        val skImage = TIOSKHSkiaCanvasKt.toSkiaImage(snapshotImageFromView)*/

        /*var sizeP: CGSize = CGSizeZero
        self._view!!.bounds.useContents {
            sizeP = size
        }
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(sizeP.width, sizeP.height), self._view!!.opaque, 0.0)
        view.
        [self._view drawViewHierarchyInRect:self._view.bounds afterScreenUpdates:NO];
        UIImage *snapshotImageFromMyView = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();

        TIOSKHSkikoImage *skImage = [TIOSKHSkiaCanvasKt toSkiaImage:snapshotImageFromMyView];
        TIOSKHSkikoBitmap *skBitmap = [skImage toBitmap];
        TIOSKHSkikoPoint *point = [[TIOSKHSkikoPoint alloc] initWithX:0 y:0];
        [canvas drawImageImage:skBitmap topLeftOffset:point paint:[self.lc createPaint]];*/
    }
}
