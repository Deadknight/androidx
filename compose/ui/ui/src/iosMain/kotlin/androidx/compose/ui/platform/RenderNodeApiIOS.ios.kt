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

package androidx.compose.ui.platform

import android.graphics.inverse
import android.graphics.setFrom
import android.graphics.toSkiaImage
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.CanvasHolder
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.drawImage
import androidx.compose.ui.graphics.toSkiaImage
import cocoapods.Topping.TIOSKHMetalRedrawer
import cocoapods.Topping.TIOSKHSkiaCanvas
import cocoapods.Topping.TIOSKHSkikoBitmap
import cocoapods.Topping.TIOSKHSkikoPicture
import cocoapods.Topping.TIOSKHSkikoPictureRecorder
import cocoapods.Topping.TIOSKHSkikoRect
import cocoapods.Topping.setValueForKeyPath
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Color
import platform.CoreGraphics.CGColorCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSNumber
import platform.Foundation.valueForKeyPath
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLDeviceProtocol
import platform.Metal.MTLPixelFormatBGRA8Unorm
import platform.QuartzCore.CALayer
import platform.QuartzCore.CAMetalLayer
import platform.UIKit.UIGraphicsBeginImageContext
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage

@OptIn(ExperimentalForeignApi::class)
class RenderNode : CALayer() {
    var img: UIImage? = null
    var canvas: android.graphics.Canvas? = null
    var width = -1.0
    var height = -1.0

    override fun initWithLayer(layer: Any): CALayer {
        val self = CALayer(layer)
        if(layer is RenderNode && self is RenderNode) {
            self.img = layer.img
            self.canvas = layer.canvas
        }
        return self
    }

    override fun setFrame(frame: CValue<CGRect>) {
        super.setFrame(frame)
        frame.useContents {
            changeRenderNodeWidthHeight(size.width, size.height)
        }
    }

    fun changeRenderNodeWidthHeight(width: CGFloat, height: CGFloat) {
        if(canvas != null && this.width == width && this.height == height)
            return
        this.width = width
        this.height = height
        UIGraphicsBeginImageContext(CGSizeMake(width, height))
        img = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        if(img == null)
            return

        val skiaImage = img!!.toSkiaImage() ?: return

        canvas = android.graphics.Canvas(TIOSKHSkikoBitmap.companion().makeFromImageImage(skiaImage))

        /*UIGraphicsBeginImageContext(CGSizeMake(width.toDouble(), height.toDouble()))
        val img = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        if(img == null)
            return

        val skiaImage = img.toSkiaImage() ?: return

        val canvas = SkiaBackedCanvas(android.graphics.Canvas(Bitmap.Companion.makeFromImage(skiaImage)))
        canvasHolder.drawInto(canvas) {
            if (clipPath != null) {
                save()
                clipPath(clipPath)
            }
            drawBlock(this)
            if (clipPath != null) {
                restore()
            }
        }

        renderNode.contents = skiaImage.toUIImage()*/
    }

    /*@ExperimentalForeignApi
    override fun drawInContext(ctx: CGContextRef?) {
        super.drawInContext(ctx)

        UIGraphicsPushContext(ctx)



        UIGraphicsPopContext()
    }*/
}

/**
 * RenderNode on M-O devices, where RenderNode isn't officially supported. This class uses
 * a hidden android.view.RenderNode API that we have stubs for in the ui-android-stubs module.
 * This implementation has higher performance than the View implementation by both avoiding
 * reflection and using the lower overhead RenderNode instead of Views.
 */
@OptIn(ExperimentalForeignApi::class)
internal class RenderNodeApiIOS(val ownerView: AndroidComposeView) : DeviceRenderNode {
    private val redrawer: TIOSKHMetalRedrawer
    val pictureRecorder = TIOSKHSkikoPictureRecorder()
    var lastPicture: TIOSKHSkikoPicture? = null
    private val renderNode = CAMetalLayer().also {
        @Suppress("USELESS_CAST")
        val _device: MTLDeviceProtocol =
            MTLCreateSystemDefaultDevice() ?: throw IllegalStateException("Metal is not supported on this system")
        it.device = _device as objcnames.protocols.MTLDeviceProtocol?

        it.pixelFormat = MTLPixelFormatBGRA8Unorm
        doubleArrayOf(0.0, 0.0, 0.0, 0.0).usePinned { pinned ->
            it.backgroundColor = CGColorCreate(CGColorSpaceCreateDeviceRGB(), pinned.addressOf(0))
        }
        it.framebufferOnly = false

        redrawer = TIOSKHMetalRedrawer(it,
            drawCallback = { surface ->
                surface?.let {
                    lastPicture?.playbackCanvas(surface.canvas(), null)
                }
            },
            addDisplayLinkToRunLoop = null,
            disposeCallback = {

            },
            tickCallback = {

            })
        ownerView.self._view!!.layer.addSublayer(it)
    }

    private var internalCompositingStrategy = CompositingStrategy.Auto

    override val uniqueId: Long get() = 0

    override var left: Int = 0
    override var top: Int = 0
    override var right: Int = 0
    override var bottom: Int = 0
    override val width: Int get() = right - left
    override val height: Int get() = bottom - top

    // API level 23 does not support RenderEffect so keep the field around for consistency
    // however, it will not be applied to the rendered result. Consumers are encouraged
    // to use the RenderEffect.isSupported API before consuming a [RenderEffect] instance.
    // If RenderEffect is used on an unsupported API level, it should act as a no-op and not
    // crash the compose application
    override var renderEffect: RenderEffect? = null

    override var scaleX: Float
        get() = (renderNode.valueForKeyPath("transform.scale.x") as NSNumber).floatValue
        set(value) {
            renderNode.setValueForKeyPath(value, "transform.scale.x")
        }

    override var scaleY: Float
        get() = (renderNode.valueForKeyPath("transform.scale.y") as NSNumber).floatValue
        set(value) {
            renderNode.setValueForKeyPath(value, "transform.scale.y")
        }

    override var translationX: Float
        get() = (renderNode.valueForKeyPath("transform.translation.x") as NSNumber).floatValue
        set(value) {
            renderNode.setValueForKeyPath(value, "transform.translation.x")
        }

    override var translationY: Float
        get() = (renderNode.valueForKeyPath("transform.translation.y") as NSNumber).floatValue
        set(value) {
            renderNode.setValueForKeyPath(value, "transform.translation.y")
        }

    override var elevation: Float
        get() = 0f//(renderNode.valueForKeyPath("translation.z") as NSNumber).floatValue
        set(value) {
            //renderNode.setValueForKeyPath(value, "translation.z")
        }

    override var ambientShadowColor: Int
        get() {
            return Color.BLACK
        }
        set(value) {

        }

    override var spotShadowColor: Int
        get() {
            return Color.BLACK
        }
        set(value) {

        }

    override var rotationZ: Float
        get() = (renderNode.valueForKeyPath("transform.rotation.z") as NSNumber).floatValue
        set(value) {
            renderNode.setValueForKeyPath(value, "transform.rotation.z")
        }

    override var rotationX: Float
        get() = (renderNode.valueForKeyPath("transform.rotation.x") as NSNumber).floatValue
        set(value) {
            renderNode.setValueForKeyPath(value, "transform.rotation.x")
        }

    override var rotationY: Float
        get() = (renderNode.valueForKeyPath("transform.rotation.y") as NSNumber).floatValue
        set(value) {
            renderNode.setValueForKeyPath(value, "transform.rotation.y")
        }

    override var cameraDistance: Float
        // Camera distance was negated in older API levels. Maintain the same input parameters
        // and negate the given camera distance before it is applied and also negate it when
        // it is queried
        get() = 0f
        set(value) {
        }

    override var pivotX: Float
        get() {
            renderNode.anchorPoint.useContents {
                return x.toFloat()
            }
        }
        set(value) {
            renderNode.anchorPoint.useContents {
                x = value.toDouble()
            }
        }

    override var pivotY: Float
        get() {
            renderNode.anchorPoint.useContents {
                return y.toFloat()
            }
        }
        set(value) {
            renderNode.anchorPoint.useContents {
                y = value.toDouble()
            }
        }

    override var clipToOutline: Boolean
        get() = false
        set(value) {
        }

    override var clipToBounds: Boolean = false
        set(value) {
         }

    override var alpha: Float
        get() = renderNode.opacity
        set(value) {
            renderNode.opacity = value
        }

    override var compositingStrategy: CompositingStrategy
        get() = internalCompositingStrategy
        set(value) {
            /*when (value) {
                CompositingStrategy.Offscreen -> {
                    renderNode.setLayerType(View.LAYER_TYPE_HARDWARE)
                    renderNode.setHasOverlappingRendering(true)
                }
                CompositingStrategy.ModulateAlpha -> {
                    renderNode.setLayerType(View.LAYER_TYPE_NONE)
                    renderNode.setHasOverlappingRendering(false)
                }
                else -> { // CompositingStrategy.Auto
                    renderNode.setLayerType(View.LAYER_TYPE_NONE)
                    renderNode.setHasOverlappingRendering(true)
                }
            }*/
            internalCompositingStrategy = value
        }

    internal fun getLayerType(): Int = when (internalCompositingStrategy) {
        CompositingStrategy.Offscreen -> 1
        else -> 0
    }

    internal fun hasOverlappingRendering(): Boolean = false

    override val hasDisplayList: Boolean
        get() = renderNode.superlayer != null

    override fun setOutline(outline: Outline?) {
        //renderNode.setOutline(outline)
    }

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
        //TODO:IOS frame or bounds?
        renderNode.setFrame(CGRectMake(left.toDouble(), top.toDouble(), (right - left).toDouble(), (bottom - top).toDouble()))
        return true
    }

    override fun offsetLeftAndRight(offset: Int) {
        left += offset
        right += offset
        renderNode.setFrame(CGRectMake(left.toDouble(), top.toDouble(), (right - left).toDouble(), (bottom - top).toDouble()))
    }

    override fun offsetTopAndBottom(offset: Int) {
        top += offset
        bottom += offset
        renderNode.setFrame(CGRectMake(left.toDouble(), top.toDouble(), (right - left).toDouble(), (bottom - top).toDouble()))
    }

    override fun record(
        canvasHolder: CanvasHolder,
        clipPath: Path?,
        drawBlock: (Canvas) -> Unit
    ) {
        val canvas = pictureRecorder.beginRecordingBounds(TIOSKHSkikoRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat()))
        canvasHolder.drawInto(TIOSKHSkiaCanvas(canvas)) {
            if (clipPath != null) {
                save()
                clipPath(clipPath)
            }
            drawBlock(this)
            if (clipPath != null) {
                restore()
            }
        }
        lastPicture = pictureRecorder.finishRecordingAsPicture()
        /*if(renderNode.canvas == null)
            return
        canvasHolder.drawInto(renderNode.canvas!!) {
            if (clipPath != null) {
                save()
                clipPath(clipPath)
            }
            drawBlock(this)
            if (clipPath != null) {
                restore()
            }
        }

        renderNode.contents = renderNode.img*/
    }

    override fun getMatrix(matrix: android.graphics.Matrix) {
        renderNode.transform.useContents {
            matrix.setFrom(this)
        }
    }

    override fun getInverseMatrix(matrix: android.graphics.Matrix) {
        renderNode.transform.useContents {
            matrix.setFrom(this)
        }
        matrix.inverse()
    }

    override fun drawInto(canvas: android.graphics.Canvas) {
        canvas.drawImage(renderNode.toSkiaImage(), 0f, 0f)
    }

    override fun setHasOverlappingRendering(hasOverlappingRendering: Boolean): Boolean = false
        //renderNode.setHasOverlappingRendering(hasOverlappingRendering)

    override fun dumpRenderNodeData(): DeviceRenderNodeData =
        DeviceRenderNodeData(
            // Platform RenderNode for API level 23-29 does not provide bounds/dimension properties
            uniqueId = 0,
            left = 0,
            top = 0,
            right = 0,
            bottom = 0,
            width = 0,
            height = 0,
            scaleX = scaleX,
            scaleY = scaleY,
            translationX = translationX,
            translationY = translationY,
            elevation = elevation,
            ambientShadowColor = ambientShadowColor,
            spotShadowColor = spotShadowColor,
            rotationZ = rotationZ,
            rotationX = rotationX,
            rotationY = rotationY,
            cameraDistance = cameraDistance,
            pivotX = pivotX,
            pivotY = pivotY,
            clipToOutline = clipToOutline,
            // No getter on RenderNode for clipToBounds, always return the value we have configured
            // on it since this is a write only field
            clipToBounds = clipToBounds,
            alpha = alpha,
            renderEffect = renderEffect,
            compositingStrategy = internalCompositingStrategy
        )

    override fun discardDisplayList() {

    }

    companion object {
        // Used by tests to force failing creating a RenderNode to simulate a device that
        // doesn't support RenderNodes before Q.
        internal var testFailCreateRenderNode = false

        // We need to validate that RenderNodes can be accessed before using the RenderNode
        // stub implementation, but we only need to validate it once. This flag indicates that
        // validation is still needed.
        private var needToValidateAccess = true
    }
}
