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

package androidx.compose.ui.graphics.vector.compat

/**
 * Constants used to resolve VectorDrawable attributes during xml inflation
 */
internal object AndroidVectorResources {

    // Resources ID generated in the latest R.java for framework.
    val STYLEABLE_VECTOR_DRAWABLE_TYPE_ARRAY = intArrayOf()
    val STYLEABLE_VECTOR_DRAWABLE_ALPHA = Pair<String?, String>(null, "alpha")
    val STYLEABLE_VECTOR_DRAWABLE_AUTO_MIRRORED = Pair<String?, String>(null, "autoMirrored")
    val STYLEABLE_VECTOR_DRAWABLE_HEIGHT = Pair<String?, String>(null, "height")
    val STYLEABLE_VECTOR_DRAWABLE_NAME = Pair<String?, String>(null, "name")
    val STYLEABLE_VECTOR_DRAWABLE_TINT = Pair<String?, String>(null, "tint")
    val STYLEABLE_VECTOR_DRAWABLE_TINT_MODE = Pair<String?, String>(null, "tintMode")
    val STYLEABLE_VECTOR_DRAWABLE_VIEWPORT_HEIGHT = Pair<String?, String>(null, "viewportHeight")
    val STYLEABLE_VECTOR_DRAWABLE_VIEWPORT_WIDTH = Pair<String?, String>(null, "viewportWidth")
    val STYLEABLE_VECTOR_DRAWABLE_WIDTH = Pair<String?, String>(null, "width")
    val STYLEABLE_VECTOR_DRAWABLE_GROUP = intArrayOf()
    val STYLEABLE_VECTOR_DRAWABLE_GROUP_NAME = Pair<String?, String>(null, "name")
    val STYLEABLE_VECTOR_DRAWABLE_GROUP_PIVOT_X = Pair<String?, String>(null, "pivotX")
    val STYLEABLE_VECTOR_DRAWABLE_GROUP_PIVOT_Y = Pair<String?, String>(null, "pivotY")
    val STYLEABLE_VECTOR_DRAWABLE_GROUP_ROTATION = Pair<String?, String>(null, "rotation")
    val STYLEABLE_VECTOR_DRAWABLE_GROUP_SCALE_X = Pair<String?, String>(null, "scaleX")
    val STYLEABLE_VECTOR_DRAWABLE_GROUP_SCALE_Y = Pair<String?, String>(null, "scaleY")
    val STYLEABLE_VECTOR_DRAWABLE_GROUP_TRANSLATE_X = Pair<String?, String>(null, "translateX")
    val STYLEABLE_VECTOR_DRAWABLE_GROUP_TRANSLATE_Y = Pair<String?, String>(null, "translateY")
    val STYLEABLE_VECTOR_DRAWABLE_PATH = intArrayOf()
    val STYLEABLE_VECTOR_DRAWABLE_PATH_FILL_ALPHA = Pair<String?, String>(null, "fillAlpha")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_FILL_COLOR = Pair<String?, String>(null, "fillColor")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_NAME = Pair<String?, String>(null, "name")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_PATH_DATA = Pair<String?, String>(null, "pathData")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_STROKE_ALPHA = Pair<String?, String>(null, "strokeAlpha")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_STROKE_COLOR = Pair<String?, String>(null, "strokeColor")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_STROKE_LINE_CAP = Pair<String?, String>(null, "strokeLineCap")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_STROKE_LINE_JOIN = Pair<String?, String>(null, "strokeLineJoin")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_STROKE_MITER_LIMIT = Pair<String?, String>(null, "strokeMiterLimit")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_STROKE_WIDTH = Pair<String?, String>(null, "strokeWidth")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_TRIM_PATH_END = Pair<String?, String>(null, "trimPathEnd")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_TRIM_PATH_OFFSET = Pair<String?, String>(null, "trimePathOffset")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_TRIM_PATH_START = Pair<String?, String>(null, "trimPathStart")
    val STYLEABLE_VECTOR_DRAWABLE_PATH_TRIM_PATH_FILLTYPE = Pair<String?, String>(null, "trimPathFillType")
    val STYLEABLE_VECTOR_DRAWABLE_CLIP_PATH =
        intArrayOf()
    val STYLEABLE_VECTOR_DRAWABLE_CLIP_PATH_NAME = Pair<String?, String>(null, "pathName")
    val STYLEABLE_VECTOR_DRAWABLE_CLIP_PATH_PATH_DATA = Pair<String?, String>(null, "pathData")
}
