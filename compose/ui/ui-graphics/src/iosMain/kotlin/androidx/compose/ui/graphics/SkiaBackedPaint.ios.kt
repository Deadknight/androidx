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

package androidx.compose.ui.graphics

import cocoapods.ToppingCompose.TIOSKHSkikoPaintMode as SkPaintMode
import cocoapods.ToppingCompose.TIOSKHSkikoPaintStrokeCap as SkPaintStrokeCap
import cocoapods.ToppingCompose.TIOSKHSkikoPaintStrokeJoin as SkPaintStrokeJoin
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toSkia

actual typealias NativePaint = cocoapods.ToppingCompose.ToppingPaint

actual fun Paint(): Paint = SkiaBackedPaint()

fun cocoapods.ToppingCompose.ToppingPaint.asComposePaint(): Paint = SkiaBackedPaint(this)

internal class SkiaBackedPaint(
    val skia: cocoapods.ToppingCompose.ToppingPaint = cocoapods.ToppingCompose.ToppingPaint(null)
) : Paint {
    override fun asFrameworkPaint(): NativePaint = skia

    private var mAlphaMultiplier = 1.0f
    private var mColor: Color = Color.Black

    var alphaMultiplier: Float
        get() = mAlphaMultiplier
        set(value) {
            val multiplier = value.coerceIn(0f, 1f)
            updateAlpha(multiplier = multiplier)
            mAlphaMultiplier = multiplier
        }

    private fun updateAlpha(alpha: Float = this.alpha, multiplier: Float = this.mAlphaMultiplier) {
        skia.setColor(mColor.copy(alpha = alpha * multiplier).toArgb())
    }

    override var alpha: Float
        get() = mColor.alpha
        set(value) {
            mColor = mColor.copy(alpha = value)
            updateAlpha(alpha = value)
        }

    override var isAntiAlias: Boolean
        get() = skia.isAntiAlias()
        set(value) {
            skia.setIsAntiAlias(value)
        }

    override var color: Color
        get() = mColor
        set(color) {
            mColor = color
            skia.setColor(color.toArgb())
        }

    override var blendMode: BlendMode = BlendMode.SrcOver
        set(value) {
            skia.setBlendMode(value.toSkia())
            field = value
        }

    override var style: PaintingStyle = PaintingStyle.Fill
        set(value) {
            skia.skia().setMode(value.toSkia())
            field = value
        }

    override var strokeWidth: Float
        get() = skia.strokeWidth()
        set(value) {
            skia.setStrokeWidth(value)
        }

    override var strokeCap: StrokeCap = StrokeCap.Butt
        set(value) {
            skia.setStrokeCap(value.toSkia())
            field = value
        }

    override var strokeJoin: StrokeJoin = StrokeJoin.Round
        set(value) {
            skia.setStrokeJoin(value.toSkia())
            field = value
        }

    override var strokeMiterLimit: Float = 0f
        set(value) {
            skia.skia().setStrokeMiter(value)
            field = value
        }

    override var filterQuality: FilterQuality = FilterQuality.Medium

    override var shader: Shader? = null
        set(value) {
            skia.setShader(value)
            field = value
        }

    override var colorFilter: ColorFilter? = null
        set(value) {
            skia.setColorFilter(value?.asSkiaColorFilter())
            field = value
        }

    override var pathEffect: PathEffect? = null
        set(value) {
            skia.setPathEffect((value as SkiaBackedPathEffect?)?.asSkiaPathEffect())
            field = value
        }

    private fun PaintingStyle.toSkia() = when (this) {
        PaintingStyle.Fill -> SkPaintMode.fill()
        PaintingStyle.Stroke -> SkPaintMode.stroke()
        else -> SkPaintMode.fill()
    }

    private fun StrokeCap.toSkia() = when (this) {
        StrokeCap.Butt -> SkPaintStrokeCap.butt()
        StrokeCap.Round -> SkPaintStrokeCap.round()
        StrokeCap.Square -> SkPaintStrokeCap.square()
        else -> SkPaintStrokeCap.butt()
    }

    private fun StrokeJoin.toSkia() = when (this) {
        StrokeJoin.Miter -> SkPaintStrokeJoin.miter()
        StrokeJoin.Round -> SkPaintStrokeJoin.round()
        StrokeJoin.Bevel -> SkPaintStrokeJoin.bevel()
        else -> SkPaintStrokeJoin.miter()
    }
}

actual fun BlendMode.isSupported(): Boolean = true
