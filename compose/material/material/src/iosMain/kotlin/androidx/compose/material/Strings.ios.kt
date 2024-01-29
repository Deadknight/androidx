/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.compose.material

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import resources

@Composable
internal actual fun getString(string: Strings): String {
    LocalConfiguration.current
    val resources = LocalContext.current.resources
    return when (string) {
        Strings.NavigationMenu -> resources.getStringKey(null, "R.string.navigation_menu")
        Strings.CloseDrawer -> resources.getStringKey(null, "R.string.close_drawer")
        Strings.CloseSheet -> resources.getStringKey(null, "R.string.close_sheet")
        Strings.DefaultErrorMessage -> resources.getStringKey(null, "R.string.default_error_message")
        Strings.ExposedDropdownMenu -> resources.getStringKey(null, "R.string.dropdown_menu")
        Strings.SliderRangeStart -> resources.getStringKey(null, "R.string.range_start")
        Strings.SliderRangeEnd -> resources.getStringKey(null, "R.string.range_end")
        else -> ""
    }
}
