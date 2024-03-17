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

import cocoapods.ToppingCompose.TIOSKHSkikoBitmap as Bitmap
import cocoapods.ToppingCompose.TIOSKHSkikoColorAlphaType as ColorAlphaType
import cocoapods.ToppingCompose.TIOSKHSkikoColorInfo as ColorInfo
import cocoapods.ToppingCompose.TIOSKHSkikoColorType as ColorType
import cocoapods.ToppingCompose.TIOSKHSkikoImage as Image
import cocoapods.ToppingCompose.TIOSKHSkikoImageInfo as ImageInfo
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import cocoapods.ToppingCompose.TIOSKHRect
import cocoapods.ToppingCompose.TIOSKHSkiaCanvas
import cocoapods.ToppingCompose.TIOSKHSkikoColorAlphaType
import cocoapods.ToppingCompose.TIOSKHSkikoRect as Rect
import cocoapods.ToppingCompose.TIOSKHSkikoColorSpace
import cocoapods.ToppingCompose.TIOSKHSkikoColorType
import cocoapods.ToppingCompose.TIOSKHSkikoFilterMode
import cocoapods.ToppingCompose.TIOSKHSkikoSamplingModeProtocol
import kotlin.math.abs
import platform.darwin.NSObject
import platform.darwin.NSUInteger

class CubicResampler(val b: Float, val c: Float) : NSObject(), TIOSKHSkikoSamplingModeProtocol {

    override fun _pack(): Long = (0x8L shl 60) or ((b.toBits().toULong() shl 32) or c.toBits().toULong()).toLong()

    override fun _packedInt1(): Int = b.toBits() or (0x8 shl 28)
    override fun _packedInt2(): Int = c.toBits()

    override fun isEqual(`object`: Any?): Boolean {
        val other = `object`
        if (other === this) return true
        if (other !is CubicResampler) return false
        if (b.compareTo(other.b) != 0) return false
        return c.compareTo(other.c) == 0
    }

    override fun hash(): NSUInteger {
        val PRIME = 59
        var result = 1
        result = result * PRIME + b.toBits()
        result = result * PRIME + c.toBits()
        return result.toULong()
    }

    override fun description(): String? {
        return "CubicResampler(_B=$b, _C=$c)"
    }
}

enum class MipmapMode {
    /**
     * ignore mipmap levels, sample from the "base"
     */
    NONE,

    /**
     * sample from the nearest level
     */
    NEAREST,

    /**
     * interpolate between the two nearest levels
     */
    LINEAR;
}

class FilterMipmap constructor(
    internal val filterMode: TIOSKHSkikoFilterMode,
    internal val mipmapMode: MipmapMode = MipmapMode.NONE
) : NSObject(), TIOSKHSkikoSamplingModeProtocol {

    override fun _pack() = filterMode.ordinal().toLong() shl 32 or mipmapMode.ordinal.toLong()

    override fun _packedInt1(): Int = filterMode.ordinal()
    override fun _packedInt2(): Int = mipmapMode.ordinal

    override fun isEqual(`object`: Any?): Boolean {
        val other = `object`
        if (other === this) return true
        if (other !is FilterMipmap) return false
        if (this.filterMode != other.filterMode) return false
        return this.mipmapMode == other.mipmapMode
    }

    override fun hash(): NSUInteger {
        val PRIME = 59
        var result = 1
        result = result * PRIME + (filterMode.hashCode())
        result = result * PRIME + (mipmapMode.hashCode())
        return result.toULong()
    }

    override fun description(): String? {
        return "FilterMipmap(_filterMode=$filterMode, _mipmapMode=$mipmapMode)"
    }
}

class SamplingMode {
    companion object {
        val DEFAULT: TIOSKHSkikoSamplingModeProtocol = FilterMipmap(TIOSKHSkikoFilterMode.nearest(), MipmapMode.NONE)
        val LINEAR: TIOSKHSkikoSamplingModeProtocol = FilterMipmap(TIOSKHSkikoFilterMode.linear(), MipmapMode.NONE)
        val MITCHELL: TIOSKHSkikoSamplingModeProtocol = CubicResampler(0.33333334f, 0.33333334f)
        val CATMULL_ROM: TIOSKHSkikoSamplingModeProtocol = CubicResampler(0f, 0.5f)
    }
}

fun TIOSKHSkiaCanvas.drawImage(image: Image, left: Float, top: Float) {
    skia().drawImageRectImage(
        image,
        Rect.companion().makeWHW(image.width_().toFloat(), image.height_().toFloat()),
        Rect.companion().makeXYWHL(left, top, image.width_().toFloat(), image.height_().toFloat()),
        SamplingMode.DEFAULT,
        null,
        true
    )
}

/**
 * Create an [ImageBitmap] from the given [Bitmap]. Note this does
 * not create a copy of the original [Bitmap] and changes to it
 * will modify the returned [ImageBitmap]
 */
fun Bitmap.asComposeImageBitmap(): ImageBitmap = SkiaBackedImageBitmap(this)

/**
 * Create an [ImageBitmap] from the given [Image].
 */
fun Image.toComposeImageBitmap(): ImageBitmap = SkiaBackedImageBitmap(toBitmap())

private fun Image.toBitmap(): Bitmap {
    val bitmap = Bitmap()
    bitmap.doAllocPixelsImageInfo(ImageInfo.companion().makeN32Width(width_(), height_(), TIOSKHSkikoColorAlphaType.premul()))
    val canvas = TIOSKHSkiaCanvas(bitmap)
    canvas.drawImage(this, 0f, 0f)
    bitmap.setImmutable()
    return bitmap
}

internal actual fun ActualImageBitmap(
    width: Int,
    height: Int,
    config: ImageBitmapConfig,
    hasAlpha: Boolean,
    colorSpace: ColorSpace
): ImageBitmap {
    val colorType = config.toSkiaColorType()
    val alphaType = if (hasAlpha) TIOSKHSkikoColorAlphaType.premul() else TIOSKHSkikoColorAlphaType.opaque()
    val skiaColorSpace = colorSpace.toSkiaColorSpace()
    val colorInfo = ColorInfo(colorType, alphaType, skiaColorSpace)
    val imageInfo = ImageInfo(colorInfo, width, height)
    val bitmap = Bitmap()
    bitmap.doAllocPixelsImageInfo(imageInfo)
    return SkiaBackedImageBitmap(bitmap)
}

/**
 * Obtain a reference to the [org.jetbrains.skia.Bitmap]
 *
 * @Throws UnsupportedOperationException if this [ImageBitmap] is not backed by an
 * org.jetbrains.skia.Image
 */
fun ImageBitmap.asSkiaBitmap(): Bitmap =
    when (this) {
        is SkiaBackedImageBitmap -> bitmap
        else -> throw UnsupportedOperationException("Unable to obtain org.jetbrains.skia.Image")
    }

private class SkiaBackedImageBitmap(val bitmap: Bitmap) : ImageBitmap {
    override val colorSpace = bitmap.colorSpace().toComposeColorSpace()
    override val config = bitmap.colorType().toComposeConfig()
    override val hasAlpha = !bitmap.isOpaque()
    override val height get() = bitmap.height_()
    override val width get() = bitmap.width_()
    override fun prepareToDraw() = Unit

    override fun readPixels(
        buffer: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        bufferOffset: Int,
        stride: Int
    ) {
        // similar to https://cs.android.com/android/platform/superproject/+/42c50042d1f05d92ecc57baebe3326a57aeecf77:frameworks/base/graphics/java/android/graphics/Bitmap.java;l=2007
        val lastScanline: Int = bufferOffset + (height - 1) * stride
        require(startX >= 0 && startY >= 0)
        require(width > 0 && startX + width <= this.width)
        require(height > 0 && startY + height <= this.height)
        require(abs(stride) >= width)
        require(bufferOffset >= 0 && bufferOffset + width <= buffer.size)
        require(lastScanline >= 0 && lastScanline + width <= buffer.size)

        // similar to https://cs.android.com/android/platform/superproject/+/9054ca2b342b2ea902839f629e820546d8a2458b:frameworks/base/libs/hwui/jni/Bitmap.cpp;l=898;bpv=1
        val colorInfo = ColorInfo(
            TIOSKHSkikoColorType.bgra8888(),
            TIOSKHSkikoColorAlphaType.premul(),
            TIOSKHSkikoColorSpace.companion().sRGB()
        )
        val imageInfo = ImageInfo(colorInfo, width, height)
        val bytesPerPixel = 4
        val bytes = bitmap.readPixelsDstInfo(imageInfo, stride * bytesPerPixel, startX, startY)!!
        val bytesPlatform = ByteArray(bytes.size()) {
            bytes.getIndex(it)
        }
        bytesPlatform.putBytesInto(buffer, bufferOffset, bytesPlatform.size / bytesPerPixel)
    }
}

internal expect fun ByteArray.putBytesInto(array: IntArray, offset: Int, length: Int)

// TODO(demin): [API] maybe we should use:
//  `else -> throw UnsupportedOperationException()`
//  in toSkiaColorType/toComposeConfig/toComposeColorSpace/toSkiaColorSpace
//  see [https://android-review.googlesource.com/c/platform/frameworks/support/+/1429835/comment/c219501b_63c3d1fe/]

private fun ImageBitmapConfig.toSkiaColorType() = when (this) {
    ImageBitmapConfig.Argb8888 -> TIOSKHSkikoColorType.companion().N32()
    ImageBitmapConfig.Alpha8 -> TIOSKHSkikoColorType.alpha8()
    ImageBitmapConfig.Rgb565 -> TIOSKHSkikoColorType.rgb565()
    ImageBitmapConfig.F16 -> TIOSKHSkikoColorType.rgbaF16()
    else -> TIOSKHSkikoColorType.companion().N32()
}

private fun ColorType.toComposeConfig() = when (this) {
    TIOSKHSkikoColorType.companion().N32() -> androidx.compose.ui.graphics.ImageBitmapConfig.Argb8888
    TIOSKHSkikoColorType.alpha8() -> androidx.compose.ui.graphics.ImageBitmapConfig.Alpha8
    TIOSKHSkikoColorType.rgb565() -> androidx.compose.ui.graphics.ImageBitmapConfig.Rgb565
    TIOSKHSkikoColorType.rgbaF16() -> androidx.compose.ui.graphics.ImageBitmapConfig.F16
    else -> androidx.compose.ui.graphics.ImageBitmapConfig.Argb8888
}

private fun TIOSKHSkikoColorSpace?.toComposeColorSpace(): ColorSpace {
    return when (this) {
        TIOSKHSkikoColorSpace.companion().sRGB() -> androidx.compose.ui.graphics.colorspace.ColorSpaces.Srgb
        TIOSKHSkikoColorSpace.companion().sRGBLinear() -> androidx.compose.ui.graphics.colorspace.ColorSpaces.LinearSrgb
        TIOSKHSkikoColorSpace.companion().displayP3() -> androidx.compose.ui.graphics.colorspace.ColorSpaces.DisplayP3
        else -> androidx.compose.ui.graphics.colorspace.ColorSpaces.Srgb
    }
}

// TODO(demin): support all color spaces.
//  to do this we need to implement SkColorSpace::MakeRGB in skia
private fun ColorSpace.toSkiaColorSpace(): TIOSKHSkikoColorSpace {
    return when (this) {
        ColorSpaces.Srgb -> TIOSKHSkikoColorSpace.companion().sRGB()
        ColorSpaces.LinearSrgb -> TIOSKHSkikoColorSpace.companion().sRGBLinear()
        ColorSpaces.DisplayP3 -> TIOSKHSkikoColorSpace.companion().displayP3()
        else -> TIOSKHSkikoColorSpace.companion().sRGB()
    }
}
