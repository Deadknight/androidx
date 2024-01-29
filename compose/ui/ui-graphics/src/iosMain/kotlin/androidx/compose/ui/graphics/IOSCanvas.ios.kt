/*
 * Copyright 2019 The Android Open Source Project
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

import SuppressWarnings
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toSkia
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toSkiaRect
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinFloatArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinIntArray
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKotlinShortArray
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastForEach
import cocoapods.Topping.TIOSKHSize
import cocoapods.Topping.TIOSKHSkiaCanvas
import cocoapods.Topping.TIOSKHSkikoBitmap
import cocoapods.Topping.TIOSKHSkikoClipMode
import cocoapods.Topping.TIOSKHSkikoFilterMode
import cocoapods.Topping.TIOSKHSkikoMatrix33
import cocoapods.Topping.TIOSKHSkikoMatrix44
import cocoapods.Topping.TIOSKHSkikoPoint
import cocoapods.Topping.TIOSKHSkikoSamplingModeProtocol
import cocoapods.Topping.TIOSKHTCanvasProtocol

fun ClipOp.toSkia() : TIOSKHSkikoClipMode {
    return when(this) {
        ClipOp.Difference -> {
            TIOSKHSkikoClipMode.difference()
        }
        ClipOp.Intersect -> {
            TIOSKHSkikoClipMode.intersect()
        }

        else -> {
            return TIOSKHSkikoClipMode.difference()
        }
    }
}

fun Offset.toSkia() : TIOSKHSkikoPoint {
    return TIOSKHSkikoPoint(x, y)
}

fun Size.toSkia(): TIOSKHSize {
    return TIOSKHSize(width, height)
}

//fun Canvas(c: SkiaBackedCanvas): Canvas =
fun Canvas(c: TIOSKHSkiaCanvas): Canvas =
    IOSCanvas().apply { internalCanvas = c }

/**
 * Holder class that is used to issue scoped calls to a [Canvas] from the framework
 * equivalent canvas without having to allocate an object on each draw call
 */
class CanvasHolder() {
    @PublishedApi internal val mIOSCanvas = IOSCanvas()

    inline fun drawInto(targetCanvas: TIOSKHTCanvasProtocol, block: Canvas.() -> Unit) {
        val previousCanvas = mIOSCanvas.internalCanvas
        mIOSCanvas.internalCanvas = targetCanvas as TIOSKHSkiaCanvas
        mIOSCanvas.block()
        mIOSCanvas.internalCanvas = previousCanvas
    }
}

// Stub canvas instance used to keep the internal canvas parameter non-null during its
// scoped usage and prevent unnecessary byte code null checks from being generated
//private val EmptyCanvas = SkiaBackedCanvas(org.jetbrains.skia.Canvas(Bitmap()))
private val EmptyCanvas = TIOSKHSkiaCanvas(TIOSKHSkikoBitmap())

@PublishedApi internal class IOSCanvas() : Canvas {

    // Keep the internal canvas as a var prevent having to allocate an AndroidCanvas
    // instance on each draw call
    @PublishedApi internal var internalCanvas: TIOSKHSkiaCanvas = EmptyCanvas

    /**
     * @see Canvas.save
     */
    override fun save() {
        internalCanvas.save()
    }

    /**
     * @see Canvas.restore
     */
    override fun restore() {
        internalCanvas.restore()
    }

    /**
     * @see Canvas.saveLayer
     */
    @SuppressWarnings("deprecation")
    override fun saveLayer(bounds: Rect, paint: Paint) {
        @Suppress("DEPRECATION")
        //internalCanvas.saveLayer(
        internalCanvas.saveLayerBounds(
            bounds.toSkiaRect(),
            paint.asFrameworkPaint()
        )
    }

    /**
     * @see Canvas.translate
     */
    override fun translate(dx: Float, dy: Float) {
        internalCanvas.translateDx(dx, dy)
    }

    /**
     * @see Canvas.scale
     */
    override fun scale(sx: Float, sy: Float) {
        internalCanvas.scaleSx(sx, sy)
    }

    /**
     * @see Canvas.rotate
     */
    override fun rotate(degrees: Float) {
        internalCanvas.rotateDegrees(degrees)
    }

    /**
     * @see Canvas.skew
     */
    override fun skew(sx: Float, sy: Float) {
        internalCanvas.skewSx(sx, sy)
    }

    /**
     * @throws IllegalStateException if an arbitrary transform is provided
     */
    override fun concat(matrix: Matrix) {
        if (!matrix.isIdentity()) {
            internalCanvas.concatMatrix(matrix.toSkia33())
        }
    }

    @SuppressWarnings("deprecation")
    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float, clipOp: ClipOp) {
        @Suppress("DEPRECATION")
        internalCanvas.clipRectLeft(left, top, right, bottom, clipOp.toSkia())
    }

    /**
     * @see Canvas.clipPath
     */
    override fun clipPath(path: Path, clipOp: ClipOp) {
        @Suppress("DEPRECATION")
        internalCanvas.clipPathPath(path.asSkiaPath(), clipOp.toSkia())
    }

    /**
     * @see Canvas.drawLine
     */
    override fun drawLine(p1: Offset, p2: Offset, paint: Paint) {
        internalCanvas.drawLineP1(p1.toSkia(), p2.toSkia(), paint.asFrameworkPaint())
    }

    override fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        internalCanvas.drawRectLeft(left, top, right, bottom, paint.asFrameworkPaint())
    }

    override fun drawRoundRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radiusX: Float,
        radiusY: Float,
        paint: Paint
    ) {
        internalCanvas.drawRoundRectLeft(
            left,
            top,
            right,
            bottom,
            radiusX,
            radiusY,
            paint.asFrameworkPaint()
        )
    }

    override fun drawOval(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        internalCanvas.drawOvalLeft(left, top, right, bottom, paint.asFrameworkPaint())
    }

    /**
     * @see Canvas.drawCircle
     */
    override fun drawCircle(center: Offset, radius: Float, paint: Paint) {
        internalCanvas.drawCircleCenter(
            center.toSkia(),
            radius,
            paint.asFrameworkPaint()
        )
    }

    override fun drawArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        useCenter: Boolean,
        paint: Paint
    ) {
        internalCanvas.drawArcLeft(
            left,
            top,
            right,
            bottom,
            startAngle,
            sweepAngle,
            useCenter,
            paint.asFrameworkPaint()
        )
    }

    /**
     * @see Canvas.drawPath
     */
    override fun drawPath(path: Path, paint: Paint) {
        internalCanvas.drawPathPath(path.asSkiaPath(), paint.asFrameworkPaint())
    }

    /**
     * @see Canvas.drawImage
     */
    override fun drawImage(image: ImageBitmap, topLeftOffset: Offset, paint: Paint) {
        val size = Size(image.width.toFloat(), image.height.toFloat())
        drawImageRect(image, Offset.Zero, size, topLeftOffset, size, paint)
    }

    /**
     * @See Canvas.drawImageRect
     */
    override fun drawImageRect(
        image: ImageBitmap,
        srcOffset: IntOffset,
        srcSize: IntSize,
        dstOffset: IntOffset,
        dstSize: IntSize,
        paint: Paint
    ) {
        drawImageRect(image, srcOffset, srcSize, dstOffset, dstSize, paint)
    }

    // TODO(demin): probably this method should be in the common Canvas
    private fun drawImageRect(
        image: ImageBitmap,
        srcOffset: Offset,
        srcSize: Size,
        dstOffset: Offset,
        dstSize: Size,
        paint: Paint
    ) {
        // TODO(gorshenev): need to use skiko's .use() rather than jvm one here.
        // But can't do that as skiko is jvmTarget=11 for now, so can't inline
        // into jvmTarget=8 compose.
        // After this issue is resolved use:
        //     import org.jetbrains.skia.impl.use
        internalCanvas.drawImageRectImage(
            image.asSkiaBitmap(),
            srcOffset.toSkia(),
            srcSize.toSkia(),
            dstOffset.toSkia(),
            dstSize.toSkia(),
            paint.asFrameworkPaint()
        )
    }

    /**
     * @see Canvas.drawPoints
     */
    override fun drawPoints(pointMode: PointMode, points: List<Offset>, paint: Paint) {
        when (pointMode) {
            // Draw a line between each pair of points, each point has at most one line
            // If the number of points is odd, then the last point is ignored.
            PointMode.Lines -> drawLines(points, paint, 2)

            // Connect each adjacent point with a line
            PointMode.Polygon -> drawLines(points, paint, 1)

            // Draw a point at each provided coordinate
            PointMode.Points -> drawPoints(points, paint)
        }
    }

    override fun enableZ() {
        internalCanvas.enableZ()
    }

    override fun disableZ() {
        internalCanvas.disableZ()
    }

    private fun drawPoints(points: List<Offset>, paint: Paint) {
        points.fastForEach { point ->
            internalCanvas.skia().drawPointX(
                point.x,
                point.y,
                paint.asFrameworkPaint().skia()
            )
        }
    }

    /**
     * Draw lines connecting points based on the corresponding step.
     *
     * ex. 3 points with a step of 1 would draw 2 lines between the first and second points
     * and another between the second and third
     *
     * ex. 4 points with a step of 2 would draw 2 lines between the first and second and another
     * between the third and fourth. If there is an odd number of points, the last point is
     * ignored
     *
     * @see drawRawLines
     */
    private fun drawLines(points: List<Offset>, paint: Paint, stepBy: Int) {
        if (points.size >= 2) {
            for (i in 0 until points.size - 1 step stepBy) {
                val p1 = points[i]
                val p2 = points[i + 1]
                internalCanvas.skia().drawLineX0(
                    p1.x,
                    p1.y,
                    p2.x,
                    p2.y,
                    paint.asFrameworkPaint().skia()
                )
            }
        }
    }

    /**
     * @throws IllegalArgumentException if a non even number of points is provided
     */
    override fun drawRawPoints(pointMode: PointMode, points: FloatArray, paint: Paint) {
        if (points.size % 2 != 0) {
            throw IllegalArgumentException("points must have an even number of values")
        }
        when (pointMode) {
            PointMode.Lines -> drawRawLines(points, paint, 2)
            PointMode.Polygon -> drawRawLines(points, paint, 1)
            PointMode.Points -> drawRawPoints(points, paint, 2)
        }
    }

    private fun drawRawPoints(points: FloatArray, paint: Paint, stepBy: Int) {
        if (points.size % 2 == 0) {
            for (i in 0 until points.size - 1 step stepBy) {
                val x = points[i]
                val y = points[i + 1]
                internalCanvas.skia().drawPointX(x, y, paint.asFrameworkPaint().skia())
            }
        }
    }

    /**
     * Draw lines connecting points based on the corresponding step. The points are interpreted
     * as x, y coordinate pairs in alternating index positions
     *
     * ex. 3 points with a step of 1 would draw 2 lines between the first and second points
     * and another between the second and third
     *
     * ex. 4 points with a step of 2 would draw 2 lines between the first and second and another
     * between the third and fourth. If there is an odd number of points, the last point is
     * ignored
     *
     * @see drawLines
     */
    private fun drawRawLines(points: FloatArray, paint: Paint, stepBy: Int) {
        // Float array is treated as alternative set of x and y coordinates
        // x1, y1, x2, y2, x3, y3, ... etc.
        if (points.size >= 4 && points.size % 2 == 0) {
            for (i in 0 until points.size - 3 step stepBy * 2) {
                val x1 = points[i]
                val y1 = points[i + 1]
                val x2 = points[i + 2]
                val y2 = points[i + 3]
                internalCanvas.skia().drawLineX0(
                    x1,
                    y1,
                    x2,
                    y2,
                    paint.asFrameworkPaint().skia()
                )
            }
        }
    }

    override fun drawVertices(vertices: Vertices, blendMode: BlendMode, paint: Paint) {
        internalCanvas.skia().drawVerticesVertexMode(
            vertices.vertexMode.toSkiaVertexMode(),
            vertices.positions.toTIOSKotlinFloatArray(),
            vertices.colors.toTIOSKotlinIntArray(),
            vertices.textureCoordinates.toTIOSKotlinFloatArray(),
            vertices.indices.toTIOSKotlinShortArray(),
            blendMode.toSkia(),
            paint.asFrameworkPaint().skia()
        )
    }

    private fun Matrix.toSkia33() = TIOSKHSkikoMatrix33(
        floatArrayOf(
            this[0, 0],
            this[1, 0],
            this[2, 0],

            this[0, 1],
            this[1, 1],
            this[2, 1],

            this[0, 2],
            this[1, 2],
            this[2, 2],

            this[0, 3],
            this[1, 3],
            this[2, 3]
        ).toTIOSKotlinFloatArray()
    )

    private fun Matrix.toSkia() = TIOSKHSkikoMatrix44(
        floatArrayOf(
            this[0, 0],
            this[1, 0],
            this[2, 0],
            this[3, 0],

            this[0, 1],
            this[1, 1],
            this[2, 1],
            this[3, 1],

            this[0, 2],
            this[1, 2],
            this[2, 2],
            this[3, 2],

            this[0, 3],
            this[1, 3],
            this[2, 3],
            this[3, 3]
        ).toTIOSKotlinFloatArray()
    )

    // These constants are chosen to correspond the old implementation of SkFilterQuality:
    // https://github.com/google/skia/blob/1f193df9b393d50da39570dab77a0bb5d28ec8ef/src/image/SkImage.cpp#L809
    // https://github.com/google/skia/blob/1f193df9b393d50da39570dab77a0bb5d28ec8ef/include/core/SkSamplingOptions.h#L86
    private fun FilterQuality.toSkia(): TIOSKHSkikoSamplingModeProtocol = when (this) {
        FilterQuality.Low -> FilterMipmap(TIOSKHSkikoFilterMode.linear(), MipmapMode.NONE)
        FilterQuality.Medium -> FilterMipmap(TIOSKHSkikoFilterMode.linear(), MipmapMode.NEAREST)
        FilterQuality.High -> CubicResampler(1 / 3.0f, 1 / 3.0f)
        else -> FilterMipmap(TIOSKHSkikoFilterMode.nearest(), MipmapMode.NONE)
    }
}
