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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecyclePauseOrDisposeEffectResult
import androidx.lifecycle.compose.LifecycleResumePauseEffectScope
import androidx.lifecycle.compose.LifecycleStartStopEffectScope
import androidx.lifecycle.compose.LifecycleStopOrDisposeEffectResult

actual enum class PlatformLifecycleState(val value: androidx.lifecycle.Lifecycle.State) {
    DESTROYED(androidx.lifecycle.Lifecycle.State.DESTROYED),
    INITIALIZED(androidx.lifecycle.Lifecycle.State.INITIALIZED),
    CREATED(androidx.lifecycle.Lifecycle.State.CREATED),
    STARTED(androidx.lifecycle.Lifecycle.State.STARTED),
    RESUMED(androidx.lifecycle.Lifecycle.State.RESUMED)
}

actual class PlatformLifecycleOwner(val lifecycleOwner: LifecycleOwner) {
    actual val lifecycle: PlatformLifecycle
        get() = PlatformLifecycle(lifecycleOwner.lifecycle)
}

actual object PlatformLifecycleOwnerCompose {
    @Composable
    actual fun current(): PlatformLifecycleOwner {
        return PlatformLifecycleOwner(LocalLifecycleOwner.current)
    }
}

actual enum class PlatformLifecycleEvent(val value: Lifecycle.Event) {
    ON_CREATE(Lifecycle.Event.ON_CREATE),
    ON_START(Lifecycle.Event.ON_START),
    ON_RESUME(Lifecycle.Event.ON_RESUME),
    ON_PAUSE(Lifecycle.Event.ON_PAUSE),
    ON_STOP(Lifecycle.Event.ON_STOP),
    ON_DESTROY(Lifecycle.Event.ON_DESTROY),
    ON_ANY(Lifecycle.Event.ON_ANY)
}

actual typealias PlatformLifecycleStartStopEffectScope = LifecycleStartStopEffectScope
actual typealias PlatformLifecycleStopOrDisposeEffectResult = LifecycleStopOrDisposeEffectResult
actual typealias PlatformLifecycleResumePauseEffectScope = LifecycleResumePauseEffectScope
actual typealias PlatformLifecyclePauseOrDisposeEffectResult = LifecyclePauseOrDisposeEffectResult

actual class PlatformLifecycle(val lifecycle: Lifecycle)