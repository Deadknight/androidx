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

package androidx.compose.ui.graphics

import bytebuffer.ByteOrder
import bytebuffer.PlatformBuffer
import bytebuffer.wrap
import cocoapods.Topping.TIOSKHByte
import cocoapods.Topping.TIOSKHKotlinByteArray
import cocoapods.Topping.TIOSKHKotlinIntArray
import cocoapods.Topping.TIOSKHSkikoColorAlphaType
import cocoapods.Topping.TIOSKHSkikoColorType
import cocoapods.Topping.TIOSKHSkikoImage
import cocoapods.Topping.TIOSKHSkikoImageInfo
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGColorRenderingIntent
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGDataProviderCopyData
import platform.CoreGraphics.CGDataProviderCreateWithCFData
import platform.CoreGraphics.CGDataProviderRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageCreate
import platform.CoreGraphics.CGImageCreateCopyWithColorSpace
import platform.CoreGraphics.CGImageGetAlphaInfo
import platform.CoreGraphics.CGImageGetBytesPerRow
import platform.CoreGraphics.CGImageGetDataProvider
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGSizeMake
import platform.CoreGraphics.kCGBitmapByteOrder32Little
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.dataWithBytesNoCopy
import platform.UIKit.UIGraphicsBeginImageContext
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIView

internal actual fun ByteArray.putBytesInto(array: IntArray, offset: Int, length: Int) {
    val buffer = PlatformBuffer.wrap(this, ByteOrder.LITTLE_ENDIAN)
    for(i in offset until length)
    {
        array[i] = buffer[i].toInt()
    }
}

@OptIn(ExperimentalForeignApi::class)
fun UIView.toUIImage(): UIImage {
    UIGraphicsBeginImageContext(
        CGSizeMake(
            bounds.useContents { size.width },
            bounds.useContents { size.height })
    )
    layer.renderInContext(UIGraphicsGetCurrentContext())
    val image = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return image ?: UIImage()
}

@OptIn(ExperimentalForeignApi::class)
fun UIImage.toSkiaImage(): TIOSKHSkikoImage? {
    val imageRef = CGImageCreateCopyWithColorSpace(this.CGImage, CGColorSpaceCreateDeviceRGB()) ?: return null

    val width = CGImageGetWidth(imageRef).toInt()
    val height = CGImageGetHeight(imageRef).toInt()

    val bytesPerRow = CGImageGetBytesPerRow(imageRef)
    val data = CGDataProviderCopyData(CGImageGetDataProvider(imageRef))
    val bytePointer = CFDataGetBytePtr(data)
    val length = CFDataGetLength(data)
    val alphaInfo = CGImageGetAlphaInfo(imageRef)

    val alphaType = when (alphaInfo) {
        CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst, CGImageAlphaInfo.kCGImageAlphaPremultipliedLast -> TIOSKHSkikoColorAlphaType.premul()
        CGImageAlphaInfo.kCGImageAlphaFirst, CGImageAlphaInfo.kCGImageAlphaLast -> TIOSKHSkikoColorAlphaType.unpremul()
        CGImageAlphaInfo.kCGImageAlphaNone, CGImageAlphaInfo.kCGImageAlphaNoneSkipFirst, CGImageAlphaInfo.kCGImageAlphaNoneSkipLast -> TIOSKHSkikoColorAlphaType.opaque()
        else -> TIOSKHSkikoColorAlphaType.unknown()
    }

    val byteArray = TIOSKHKotlinByteArray.arrayWithSize(length.toInt())  {
        TIOSKHByte(bytePointer!![it!!.intValue].toByte())
    }
    CFRelease(data)
    CFRelease(imageRef)

    return TIOSKHSkikoImage.companion().makeRasterImageInfo(
        imageInfo = TIOSKHSkikoImageInfo(width = width, height = height, colorType = TIOSKHSkikoColorType.rgba8888(), alphaType = alphaType),
        bytes = byteArray,
        rowBytes = bytesPerRow.toInt(),
    )
}

fun UIImage.toImageBitmap(): ImageBitmap {
    return this.toSkiaImage()!!.toComposeImageBitmap()
}

@OptIn(ExperimentalForeignApi::class)
private fun <T> ByteArray.useNSDataRef(block: (NSData) -> T): T {
    return usePinned { pin ->
        val bytesPointer = when {
            isNotEmpty() -> pin.addressOf(0)
            else -> null
        }
        val nsData = NSData.dataWithBytesNoCopy(
            bytes = bytesPointer,
            length = size.convert(),
            freeWhenDone = false
        )

        @Suppress("UNCHECKED_CAST")
        val typeRef = CFBridgingRetain(nsData) as CFDataRef

        try {
            block(nsData)
        } finally {
            CFBridgingRelease(typeRef)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toImageBytes() : NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toImageBytes),
        length = this@toImageBytes.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
fun TIOSKHKotlinByteArray.toImageBytes() : NSData = memScoped {
    val byteArr = ByteArray(size()) {
        getIndex(it)
    }
    NSData.create(bytes = allocArrayOf(byteArr),
        length = byteArr.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
fun TIOSKHSkikoImage.toUIImage(): UIImage {
    val byteArr = peekPixels()?.buffer()?.bytes() ?: TIOSKHKotlinByteArray()

    val data = byteArr.toImageBytes()
    val cfData = CFBridgingRetain(data) as? CFDataRef?

    //NSLog(@"%@,", str);
    val colorSpaceRef = CGColorSpaceCreateDeviceRGB()
    val dataProviderRef = CGDataProviderCreateWithCFData(cfData)
    val imageRef = CGImageCreate(
        imageInfo().width().toULong(),
        imageInfo().height().toULong(),
        8u,
        (imageInfo().bytesPerPixel() * 8).toULong(),
        imageInfo().minRowBytes().toULong(),
        colorSpaceRef,
        CGImageAlphaInfo.kCGImageAlphaFirst.value or kCGBitmapByteOrder32Little, dataProviderRef, null, false,
        CGColorRenderingIntent.kCGRenderingIntentDefault
    )
    CFBridgingRelease(cfData)
    CGColorSpaceRelease(colorSpaceRef)
    CGDataProviderRelease(dataProviderRef)

    return UIImage.imageWithCGImage(imageRef)
}