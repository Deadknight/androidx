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

import Build
import DoNotInline
import GONE
import RequiresApi
import SuppressLint
import VISIBLE
import View
import addView
import alpha
import android.graphics.set
import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.CanvasHolder
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Fields
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.ReusableGraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.GraphicLayerInfo
import androidx.compose.ui.node.OwnedLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.ID
import clipToOutline
import cocoapods.ToppingCompose.TIOSKHTCanvasProtocol
import context
import drawingTime
import elevation
import height
import init
import left
import offsetLeftAndRight
import offsetTopAndBottom
import pivotX
import pivotY
import platform.Foundation.NSUUID
import removeViewInLayout
import rotation
import rotationX
import rotationY
import scaleX
import scaleY
import setWillNotDraw
import top
import translationX
import translationY
import visibility
import width

/**
 * View implementation of OwnedLayer.
 */
internal class ViewLayer(
    val ownerView: AndroidComposeView,
    val container: DrawChildContainer,
    drawBlock: (Canvas) -> Unit,
    invalidateParentLayer: () -> Unit
) : OwnedLayer, GraphicLayerInfo {

    val self: View

    inner class ViewLayerView : View() {
        override fun layout(l: Int, _1: Int, _2: Int, _3: Int) {
            super.layout(l, _1, _2, _3)
            self.dWidth = _2 - l
            self.dHeight = _3 - _1
        }

        override fun dispatchDrawCanvas(canvas: TIOSKHTCanvasProtocol) {
            canvasHolder.drawInto(canvas) {
                var didClip = false
                val clipPath = manualClipPath
                if (clipPath != null/* || !canvas.isHardwareAccelerated*/) {
                    didClip = true
                    save()
                    outlineResolver.clipToOutline(this)
                }
                drawBlock?.invoke(this)
                if (didClip) {
                    restore()
                }
            }
            isInvalidated = false
        }

        /*override fun layout(l: Int, _1: Int, _2: Int, _3: Int) {

        }*/

        override fun forceLayout() {
            // Don't do anything. These Views are treated as RenderNodes, so a forced layout
            // should not do anything. If we keep this, we get more redrawing than is necessary.
        }
    }

    init {
        self = ViewLayerView()
        self.kParentType = this
        self.init(ownerView.self.context, null)
        self.lc = ownerView.self.context
    }
    private var drawBlock: ((Canvas) -> Unit)? = drawBlock
    private var invalidateParentLayer: (() -> Unit)? = invalidateParentLayer

    private val outlineResolver = OutlineResolver(ownerView.density)
    // Value of the layerModifier's clipToBounds property
    private var clipToBounds = false
    private var clipBoundsCache: android.graphics.Rect? = null
    private val manualClipPath: Path? get() =
        if (!self.clipToOutline || outlineResolver.outlineClipSupported) {
            null
        } else {
            outlineResolver.clipPath
        }
    var isInvalidated = false
        private set(value) {
            if (value != field) {
                field = value
                ownerView.notifyLayerIsDirty(this, value)
            }
        }
    private var drawnWithZ = false
    private val canvasHolder = CanvasHolder()

    private val matrixCache = LayerMatrixCache(getMatrix)

    /**
     * Local copy of the transform origin as GraphicsLayerModifier can be implemented
     * as a model object. Update this field within [updateLayerProperties] and use it
     * in [resize] or other methods
     */
    private var mTransformOrigin: TransformOrigin = TransformOrigin.Center

    private var mHasOverlappingRendering = true

    init {
        self.setWillNotDraw(false) // we WILL draw
        container.self.addView(self)
    }

    override val layerId: ID = NSUUID.UUID().UUIDString

    override val ownerViewId: ID
        get() = ""

    /**
     * Configure the camera distance on the View in pixels. View already has a get/setCameraDistance
     * API however, that operates in Dp values.
     */
    var cameraDistancePx: Float
        get() {
            // View internally converts distance to dp so divide by density here to have
            // consistent usage of pixels with RenderNode that is backing the View
            //return cameraDistance / resources.displayMetrics.densityDpi
            return 0f
        }
        set(value) {
            // View internally converts distance to dp so multiply by density here to have
            // consistent usage of pixels with RenderNode that is backing the View
            //cameraDistance = value * resources.displayMetrics.densityDpi
        }

    private var mutatedFields: Int = 0

    override fun updateLayerProperties(
        scope: ReusableGraphicsLayerScope,
        layoutDirection: LayoutDirection,
        density: Density,
    ) {
        val maybeChangedFields = scope.mutatedFields or mutatedFields
        if (maybeChangedFields and Fields.TransformOrigin != 0) {
            this.mTransformOrigin = scope.transformOrigin
            this.self.pivotX = mTransformOrigin.pivotFractionX * self.width
            this.self.pivotY = mTransformOrigin.pivotFractionY * self.height
        }
        if (maybeChangedFields and Fields.ScaleY != 0) {
            this.self.scaleX = scope.scaleX
        }
        if (maybeChangedFields and Fields.ScaleY != 0) {
            this.self.scaleY = scope.scaleY
        }
        if (maybeChangedFields and Fields.Alpha != 0) {
            this.self.alpha = scope.alpha
        }
        if (maybeChangedFields and Fields.TranslationX != 0) {
            this.self.translationX = scope.translationX
        }
        if (maybeChangedFields and Fields.TranslationY != 0) {
            this.self.translationY = scope.translationY
        }
        if (maybeChangedFields and Fields.ShadowElevation != 0) {
            this.self.elevation = scope.shadowElevation
        }
        if (maybeChangedFields and Fields.RotationZ != 0) {
            this.self.rotation = scope.rotationZ
        }
        if (maybeChangedFields and Fields.RotationX != 0) {
            this.self.rotationX = scope.rotationX
        }
        if (maybeChangedFields and Fields.RotationY != 0) {
            this.self.rotationY = scope.rotationY
        }
        if (maybeChangedFields and Fields.CameraDistance != 0) {
            this.cameraDistancePx = scope.cameraDistance
        }
        val wasClippingManually = manualClipPath != null
        if (maybeChangedFields and (Fields.Clip or Fields.Shape) != 0) {
            this.clipToBounds = scope.clip && scope.shape === RectangleShape
            resetClipBounds()
            this.self.clipToOutline = scope.clip && scope.shape !== RectangleShape
        }
        val shapeChanged = if (maybeChangedFields and Fields.OutlineAffectingFields != 0) {
            outlineResolver.update(
                scope.shape,
                this.self.alpha,
                this.self.clipToOutline,
                this.self.elevation,
                layoutDirection,
                density
            ).also {
                updateOutlineResolver()
            }
        } else false
        val isClippingManually = manualClipPath != null
        if (wasClippingManually != isClippingManually || (isClippingManually && shapeChanged)) {
            invalidate() // have to redraw the content
        }
        if (!drawnWithZ && self.elevation > 0) {
            invalidateParentLayer?.invoke()
        }
        if (maybeChangedFields and Fields.MatrixAffectingFields != 0) {
            matrixCache.invalidate()
        }

        if (maybeChangedFields and Fields.CompositingStrategy != 0) {
            mHasOverlappingRendering = when (scope.compositingStrategy) {
                CompositingStrategy.Offscreen -> {
                    //setLayerType(LAYER_TYPE_HARDWARE, null)
                    true
                }

                CompositingStrategy.ModulateAlpha -> {
                    //setLayerType(LAYER_TYPE_NONE, null)
                    false
                }

                else -> { // CompositingStrategy.Auto
                    //setLayerType(LAYER_TYPE_NONE, null)
                    true
                }
            }
        }
        mutatedFields = scope.mutatedFields
    }

    override fun isInLayer(position: Offset): Boolean {
        val x = position.x
        val y = position.y
        if (clipToBounds) {
            return 0f <= x && x < self.width && 0f <= y && y < self.height
        }

        if (self.clipToOutline) {
            return outlineResolver.isInOutline(position)
        }

        return true
    }

    private fun updateOutlineResolver() {
        /*this.outlineProvider = if (outlineResolver.outline != null) {
            OutlineProvider
        } else {
            null
        }*/
    }

    private fun resetClipBounds() {
        /*this.clipBounds = if (clipToBounds) {
            if (clipBoundsCache == null) {
                clipBoundsCache = android.graphics.Rect(0, 0, width, height)
            } else {
                clipBoundsCache!!.set(0, 0, width, height)
            }
            clipBoundsCache
        } else {
            null
        }*/
    }

    override fun resize(size: IntSize) {
        val width = size.width
        val height = size.height
        if (width != this.self.width || height != this.self.height) {
            self.pivotX = mTransformOrigin.pivotFractionX * width
            self.pivotY = mTransformOrigin.pivotFractionY * height
            outlineResolver.update(Size(width.toFloat(), height.toFloat()))
            updateOutlineResolver()
            self.layout(self.left, self.top, self.left + width, self.top + height)
            resetClipBounds()
            matrixCache.invalidate()
        }
    }

    override fun move(position: IntOffset) {
        val left = position.x

        if (left != this.self.left) {
            self.offsetLeftAndRight(left - this.self.left)
            matrixCache.invalidate()
        }
        val top = position.y
        if (top != this.self.top) {
            self.offsetTopAndBottom(top - this.self.top)
            matrixCache.invalidate()
        }
    }

    override fun drawLayer(canvas: Canvas) {
        drawnWithZ = self.elevation > 0f
        if (drawnWithZ) {
            canvas.enableZ()
        }
        container.drawChild(canvas, self, self.drawingTime)
        if (drawnWithZ) {
            canvas.disableZ()
        }
    }

    override fun invalidate() {
        if (!isInvalidated) {
            isInvalidated = true
            self.invalidate()
            ownerView.self.invalidate()
        }
    }

    override fun destroy() {
        isInvalidated = false
        ownerView.requestClearInvalidObservations()
        drawBlock = null
        invalidateParentLayer = null

        // L throws during RenderThread when reusing the Views. The stack trace
        // wasn't easy to decode, so this work-around keeps up to 10 Views active
        // only for L. On other versions, it uses the WeakHashMap to retain as many
        // as are convenient.

        val recycle = ownerView.recycle(this@ViewLayer)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || shouldUseDispatchDraw || !recycle) {
            container.self.removeViewInLayout(self)
        } else {
            self.visibility = View.GONE
        }
    }

    override fun updateDisplayList() {
        if (isInvalidated && !shouldUseDispatchDraw) {
            updateDisplayList(self)
            isInvalidated = false
        }
    }

    override fun mapOffset(point: Offset, inverse: Boolean): Offset {
        return if (inverse) {
            matrixCache.calculateInverseMatrix(self)?.map(point) ?: Offset.Infinite
        } else {
            matrixCache.calculateMatrix(self).map(point)
        }
    }

    override fun mapBounds(rect: MutableRect, inverse: Boolean) {
        if (inverse) {
            val matrix = matrixCache.calculateInverseMatrix(self)
            if (matrix != null) {
                matrix.map(rect)
            } else {
                rect.set(0f, 0f, 0f, 0f)
            }
        } else {
            matrixCache.calculateMatrix(self).map(rect)
        }
    }

    override fun reuseLayer(drawBlock: (Canvas) -> Unit, invalidateParentLayer: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || shouldUseDispatchDraw) {
            container.self.addView(self)
        } else {
            self.visibility = View.VISIBLE
        }
        clipToBounds = false
        drawnWithZ = false
        mTransformOrigin = TransformOrigin.Center
        this.drawBlock = drawBlock
        this.invalidateParentLayer = invalidateParentLayer
    }

    override fun transform(matrix: Matrix) {
        matrix.timesAssign(matrixCache.calculateMatrix(self))
    }

    override fun inverseTransform(matrix: Matrix) {
        val inverse = matrixCache.calculateInverseMatrix(self)
        if (inverse != null) {
            matrix.timesAssign(inverse)
        }
    }

    companion object {
        private val getMatrix: (View, android.graphics.Matrix) -> Unit = { view, matrix ->
            val newMatrix = view.matrix

            matrix.set(newMatrix)
        }

        /*val OutlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: android.graphics.Outline) {
                view as ViewLayer
                outline.set(view.outlineResolver.outline!!)
            }
        }*/
        var hasRetrievedMethod = false
            private set
        var shouldUseDispatchDraw = true
            internal set // internal so that tests can use it.

        @SuppressLint("BanUncheckedReflection")
        fun updateDisplayList(view: View) {
        }
    }
}
