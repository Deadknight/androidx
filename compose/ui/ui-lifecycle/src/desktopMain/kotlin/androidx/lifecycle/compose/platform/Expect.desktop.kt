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

actual enum class PlatformLifecycleState(val value: Int) {
    DESTROYED(0),
    INITIALIZED(1),
    CREATED(2),
    STARTED(3),
    RESUMED(4)
}

actual object PlatformLifecycleOwnerCompose {
    @Composable
    actual fun current(): PlatformLifecycleOwner {
        return PlatformLifecycleOwner()
    }
}

actual class PlatformLifecycleOwner {
    actual val lifecycle: PlatformLifecycle
        get() = PlatformLifecycle()
}

actual enum class PlatformLifecycleEvent(val value: Int = 0) {
    ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTROY, ON_ANY
}

actual class PlatformLifecycleStartStopEffectScope
actual interface PlatformLifecycleStopOrDisposeEffectResult
actual class PlatformLifecycleResumePauseEffectScope
actual interface PlatformLifecyclePauseOrDisposeEffectResult

actual class PlatformLifecycle