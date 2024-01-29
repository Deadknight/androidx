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

package android.graphics

import androidx.compose.ui.graphics.Matrix
import cocoapods.Topping.LGView
import cocoapods.Topping.TIOSKHByte
import cocoapods.Topping.TIOSKHKotlinByteArray
import cocoapods.Topping.TIOSKHKotlinFloatArray
import cocoapods.Topping.TIOSKHSkiaCanvas
import cocoapods.Topping.TIOSKHSkikoColorAlphaType
import cocoapods.Topping.TIOSKHSkikoColorType
import cocoapods.Topping.TIOSKHSkikoImage
import cocoapods.Topping.TIOSKHSkikoImageInfo
import cocoapods.Topping.TIOSKHSkikoMatrix33
import cocoapods.Topping.TIOSKHSkikoRect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreGraphics.CGDataProviderCopyData
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetAlphaInfo
import platform.CoreGraphics.CGImageGetBytesPerRow
import platform.CoreGraphics.CGImageGetDataProvider
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.QuartzCore.CALayer
import platform.QuartzCore.CATransform3D

typealias Matrix = TIOSKHSkikoMatrix33
typealias Canvas = TIOSKHSkiaCanvas
typealias Rect = TIOSKHSkikoRect

var LGView.preferKeepClearRects: MutableList<Rect>
    get() {
        return mutableListOf()
    }
    set(value) {

    }

var LGView.systemGestureExclusionRects: MutableList<Rect>
    get() {
        return mutableListOf()
    }
    set(value) {

    }

@OptIn(ExperimentalForeignApi::class)
fun CALayer.toSkiaImage(): TIOSKHSkikoImage {
    val imageRef = contents as CGImageRef?
    /*val dataProviderRef = CGImageGetDataProvider(imageRef)
    val dataRef = CGDataProviderCopyData(dataProviderRef)
    val dataBytePointer = CFDataGetBytePtr(dataRef)
    val colorSpace = CGImageGetColorSpace(imageRef)
    val context = CGBitmapContextCreateWithData(dataBytePointer, CGImageGetWidth(imageRef), CGImageGetHeight(imageRef), CGImageGetBitsPerComponent(imageRef), CGImageGetBytesPerRow(imageRef), colorSpace, CGImageGetBitmapInfo(imageRef), null, null)*/

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

    val byteArray = TIOSKHKotlinByteArray.arrayWithSize(length.toInt()) {
        TIOSKHByte(bytePointer!![it!!.intValue].toByte())
    }

    val skiaImage = TIOSKHSkikoImage.companion().makeRasterImageInfo(
        imageInfo = TIOSKHSkikoImageInfo(width = width, height = height, colorType = TIOSKHSkikoColorType.rgba8888(), alphaType = alphaType),
        bytes = byteArray,
        rowBytes = bytesPerRow.toInt(),
    )

    return skiaImage
}

/** SkMatrix organizes its values in row-major order. These members correspond to
each value in SkMatrix.
 */
const val kMScaleX = 0 //!< horizontal scale factor
const val kMSkewX  = 1 //!< horizontal skew factor
const val kMTransX = 2 //!< horizontal translation
const val kMSkewY  = 3 //!< vertical skew factor
const val kMScaleY = 4 //!< vertical scale factor
const val kMTransY = 5 //!< vertical translation
const val kMPersp0 = 6 //!< input x perspective factor
const val kMPersp1 = 7 //!< input y perspective factor
const val kMPersp2 = 8 //!< perspective bias

/** Affine arrays are in column-major order to match the matrix used by
PDF and XPS.
 */
const val kAScaleX = 0 //!< horizontal scale factor
const val kASkewY  = 1 //!< vertical skew factor
const val kASkewX  = 2 //!< horizontal skew factor
const val kAScaleY = 3 //!< vertical scale factor
const val kATransX = 4 //!< horizontal translation
const val kATransY = 5 //!< vertical translation

fun TIOSKHSkikoMatrix33.preTranslate(dx: Float, dy: Float): TIOSKHSkikoMatrix33 {
    mat().setIndex(kMTransX, mat().getIndex(kMTransX) + dx)
    mat().setIndex(kMTransY, mat().getIndex(kMTransY) + dy)
    return this
}

/////////////////

/**
 * Sets this [Matrix] to be the result of this * [other]
 */
private fun android.graphics.Matrix.preTransform(other: android.graphics.Matrix) {
    val v00 = dot(other, 0, this, 0)
    val v01 = dot(other, 0, this, 1)
    val v02 = dot(other, 0, this, 2)
    val v03 = dot(other, 0, this, 3)
    val v10 = dot(other, 1, this, 0)
    val v11 = dot(other, 1, this, 1)
    val v12 = dot(other, 1, this, 2)
    val v13 = dot(other, 1, this, 3)
    val v20 = dot(other, 2, this, 0)
    val v21 = dot(other, 2, this, 1)
    val v22 = dot(other, 2, this, 2)
    val v23 = dot(other, 2, this, 3)
    val v30 = dot(other, 3, this, 0)
    val v31 = dot(other, 3, this, 1)
    val v32 = dot(other, 3, this, 2)
    val v33 = dot(other, 3, this, 3)
    this[0, 0] = v00
    this[0, 1] = v01
    this[0, 2] = v02
    this[0, 3] = v03
    this[1, 0] = v10
    this[1, 1] = v11
    this[1, 2] = v12
    this[1, 3] = v13
    this[2, 0] = v20
    this[2, 1] = v21
    this[2, 2] = v22
    this[2, 3] = v23
    this[3, 0] = v30
    this[3, 1] = v31
    this[3, 2] = v32
    this[3, 3] = v33
}

fun TIOSKHSkikoMatrix33.isIdentity() : Boolean {
    val idM = TIOSKHSkikoMatrix33.companion().IDENTITY()
    for(i in 0 until mat().size()) {
        if(mat().getIndex(i) != idM.mat().getIndex(i))
            return false
    }
    return true
}

fun TIOSKHSkikoMatrix33.inverse() : TIOSKHSkikoMatrix33 {
    val newMatrix = TIOSKHSkikoMatrix33.companion().IDENTITY()

    var determinant = determinant()

    var matrix = this

    newMatrix[0,0] = ((matrix[1,1]*matrix[2,2]) - (matrix[1,2]*matrix[2,1]));
    newMatrix[0,1] = -((matrix[0,1]*matrix[2,2]) - (matrix[0,2]*matrix[2,1]));
    newMatrix[0,2] = ((matrix[0,1]*matrix[1,2]) - (matrix[0,2]*matrix[1,1]));

    newMatrix[1,0] = -((matrix[1,0]*matrix[2,2]) - (matrix[1,2]*matrix[2,0]));
    newMatrix[1,1] = ((matrix[0,0]*matrix[2,2]) - (matrix[0,2]*matrix[2,0]));
    newMatrix[1,2] = -((matrix[0,0]*matrix[1,2]) - (matrix[0,2]*matrix[1,0]));

    newMatrix[2,0] = ((matrix[1,0]*matrix[2,1]) - (matrix[1,1]*matrix[2,0]));
    newMatrix[2,1] = -((matrix[0,0]*matrix[2,1]) - (matrix[0,1]*matrix[2,0]));
    newMatrix[2,2] = ((matrix[0,0]*matrix[1,1]) - (matrix[0,1]*matrix[1,0]));

    for (i in 0..2) {
        for (j in 0..2) {
            newMatrix[i,j] *= 1 / determinant
            if (newMatrix[i,j] == 0f) newMatrix[i,j] = 0f //to fix -0.0 showing in output
        }
    }

    return newMatrix
}

fun TIOSKHSkikoMatrix33.determinant(): Float {
    var determinant = 0f
    determinant += this[0,0] * (this[1,1] * this[2,2] - this[1,2] * this[2,1])
    determinant -= this[0,1] * (this[1,0] * this[2,2] - this[1,2] * this[2,0])
    determinant += this[0,2] * (this[1,0] * this[2,1] - this[1,1] * this[2,0])
    return determinant
}

fun TIOSKHSkikoMatrix33.reset() {
    var count = 0
    val identity = TIOSKHSkikoMatrix33.companion().IDENTITY()
    for(i in 0 until identity.mat().size()) {
        mat().setIndex(i, identity.mat().getIndex(i))
    }
}

fun TIOSKHSkikoMatrix33.translate(dx: Float, dy: Float) {
    mat().setIndex(kMTransX, dx)
    mat().setIndex(kMTransY, dy)
}

val TIOSKHSkikoMatrix33.Companion.IDENTITY : TIOSKHSkikoMatrix33
    get() = TIOSKHSkikoMatrix33.companion().IDENTITY()

operator fun TIOSKHKotlinFloatArray.get(i: Int) : Float {
    return getIndex(i)
}

operator fun TIOSKHKotlinFloatArray.set(i: Int, value: Float) {
    setIndex(i, value)
}

operator fun TIOSKHSkikoMatrix33.get(rowIndex: Int, colIndex: Int) : Float {
    if (rowIndex < 0 || colIndex < 0 || rowIndex >= 3 || colIndex >= 3) {
        throw IllegalArgumentException("Matrix.get: Index out of bound")
    } else {
        return mat().getIndex(rowIndex * 3 + colIndex)
    }
}

operator fun TIOSKHSkikoMatrix33.set(rowIndex: Int, colIndex: Int, value: Number) {
    if (rowIndex < 0 || colIndex < 0 || rowIndex >= 3 || colIndex >= 3) {
        throw IllegalArgumentException("Matrix.set: Index out of bound")
    } else {
        mat().setIndex(rowIndex * 3 + colIndex, value.toFloat())
    }
}

fun TIOSKHSkikoMatrix33.postTranslate(dx: Float, dy: Float): android.graphics.Matrix {
    mat().setIndex(kMTransX, mat().getIndex(kMTransX) + dx)
    mat().setIndex(kMTransY, mat().getIndex(kMTransY) + dy)
    return this
}

private fun TIOSKHSkikoMatrix33.preTranslate(x: Float, y: Float, tmpMatrix: android.graphics.Matrix) {
    tmpMatrix.reset()
    tmpMatrix.translate(x, y)
    preTransform(tmpMatrix)
}

// Taken from Exts.kt
private fun TIOSKHSkikoMatrix33.dot(m1: android.graphics.Matrix, row: Int, m2: android.graphics.Matrix, column: Int): Float {
    return m1[row, 0] * m2[0, column] +
        m1[row, 1] * m2[1, column] +
        m1[row, 2] * m2[2, column] +
        m1[row, 3] * m2[3, column]
}

fun TIOSKHSkikoMatrix33.set(matrix: TIOSKHSkikoMatrix33) {
    for(i in 0 until matrix.mat().size())
    {
        mat().setIndex(i, matrix.mat().getIndex(i))
    }
}

/**
 * Set the matrix values the native [android.graphics.Matrix].
 */
fun TIOSKHSkikoMatrix33.setFrom(matrix: android.graphics.Matrix) {
    for(i in 0 until mat().size()) {
        mat().setIndex(i, matrix.mat().getIndex(i))
    }
    /*val v = values
    val scaleX = matrix.mat[kMScaleX]
    val skewX = matrix.mat[kMSkewX]
    val translateX = matrix.mat[kMTransX]
    val skewY = matrix.mat[kMSkewY]
    val scaleY = matrix.mat[kMScaleY]
    val translateY = matrix.mat[kMTransY]
    val persp0 = matrix.mat[kMPersp0]
    val persp1 = matrix.mat[kMPersp1]
    val persp2 = matrix.mat[kMPersp2]

    v[Matrix.ScaleX] = scaleX // 0
    v[Matrix.SkewY] = skewY // 1
    v[2] = 0f // 2
    v[Matrix.Perspective0] = persp0 // 3
    v[Matrix.SkewX] = skewX // 4
    v[Matrix.ScaleY] = scaleY // 5
    v[6] = 0f // 6
    v[Matrix.Perspective1] = persp1 // 7
    v[8] = 0f // 8
    v[9] = 0f // 9
    v[Matrix.ScaleZ] = 1.0f // 10
    v[11] = 0f // 11
    v[Matrix.TranslateX] = translateX // 12
    v[Matrix.TranslateY] = translateY // 13
    v[14] = 0f // 14
    v[Matrix.Perspective2] = persp2 // 15*/
}

fun TIOSKHSkikoMatrix33.setFrom(matrix: CATransform3D) {
    val iden = TIOSKHSkikoMatrix33.companion().IDENTITY()
    for(i in 0 until mat().size())
    {
        mat().setIndex(i, iden.mat().getIndex(i))
    }
    mat().setIndex(kMScaleX, matrix.m11.toFloat())
    mat().setIndex(kMSkewX, matrix.m21.toFloat())
    mat().setIndex(kMTransX, matrix.m41.toFloat())
    mat().setIndex(kMPersp0, matrix.m14.toFloat())
    mat().setIndex(kMSkewY, matrix.m12.toFloat())
    mat().setIndex(kMScaleY, matrix.m22.toFloat())
    mat().setIndex(kMTransY, matrix.m42.toFloat())
    mat().setIndex(kMPersp1, matrix.m24.toFloat())
}
