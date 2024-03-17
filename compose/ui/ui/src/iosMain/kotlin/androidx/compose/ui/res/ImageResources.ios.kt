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

package androidx.compose.ui.res

import DrawableRes
import Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toImageBitmap
import androidx.compose.ui.platform.LocalContext
import cocoapods.ToppingCompose.TIOSKHSkikoBitmap
import org.jetbrains.skia.Bitmap

/**
 * Load an ImageBitmap from an image resource.
 *
 * This function is intended to be used for when low-level ImageBitmap-specific
 * functionality is required.  For simply displaying onscreen, the vector/bitmap-agnostic
 * [painterResource] is recommended instead.
 *
 * @return Loaded image file represented as an [ImageBitmap]
 */
fun ImageBitmap.Companion.imageResource(res: Resources, @DrawableRes id: String): ImageBitmap {
    val result = res.getDrawable(id, null)
    if(result == null)
        return TIOSKHSkikoBitmap().asComposeImageBitmap()

    return result.getImage()!!.toImageBitmap()
}

/**
 * Load an ImageBitmap from an image resource.
 *
 * This function is intended to be used for when low-level ImageBitmap-specific
 * functionality is required.  For simply displaying onscreen, the vector/bitmap-agnostic
 * [painterResource] is recommended instead.
 *
 * @param id the resource identifier
 * @return the decoded image data associated with the resource
 */
@Composable
fun ImageBitmap.Companion.imageResource(@DrawableRes id: String): ImageBitmap {
    val context = LocalContext.current
    return remember(id) { imageResource(Resources(context), id) }
}
