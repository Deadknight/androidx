/*
 * Copyright 2024 The Android Open Source Project
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

package androidx.lifecycle.compose.platform

import androidx.compose.runtime.Composable

expect class PlatformLifecycle

expect class PlatformLifecycleOwner {
    val lifecycle: PlatformLifecycle
}

expect object PlatformLifecycleOwnerCompose
{
    @Composable
    fun current() : PlatformLifecycleOwner
}

expect enum class PlatformLifecycleState {
    DESTROYED,
    INITIALIZED,
    CREATED,
    STARTED,
    RESUMED
}

expect enum class PlatformLifecycleEvent {
    ON_CREATE,
    ON_START,
    ON_RESUME,
    ON_PAUSE,
    ON_STOP,
    ON_DESTROY,
    ON_ANY
}

expect class PlatformLifecycleStartStopEffectScope
expect interface PlatformLifecycleStopOrDisposeEffectResult

expect class PlatformLifecycleResumePauseEffectScope
expect interface PlatformLifecyclePauseOrDisposeEffectResult

expect open class PlatformViewModel
expect interface PlatformViewModelStoreOwner
expect object PlatformLocalViewModelStoreOwner {
    val current: PlatformViewModelStoreOwner?
}

@Composable
expect inline fun <reified VM : PlatformViewModel> viewModel(
    viewModelStoreOwner: PlatformViewModelStoreOwner = checkNotNull(PlatformLocalViewModelStoreOwner.current) {
        "No PlatformViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    key: String,
    crossinline init: () -> VM
): VM