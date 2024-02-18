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

import LifecycleOwner
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.LifecyclePauseOrDisposeEffectResult
import androidx.lifecycle.compose.LifecycleResumePauseEffectScope
import androidx.lifecycle.compose.LifecycleStartStopEffectScope
import androidx.lifecycle.compose.LifecycleStopOrDisposeEffectResult
import cocoapods.Topping.Lifecycle
import cocoapods.Topping.LifecycleEvent
import cocoapods.Topping.LifecycleState
import lifecycle

actual enum class PlatformLifecycleState(val value: LifecycleState) {
    DESTROYED(LifecycleState.LIFECYCLESTATE_DESTROYED),
    INITIALIZED(LifecycleState.LIFECYCLESTATE_INITIALIZED),
    CREATED(LifecycleState.LIFECYCLESTATE_CREATED),
    STARTED(LifecycleState.LIFECYCLESTATE_STARTED),
    RESUMED(LifecycleState.LIFECYCLESTATE_RESUMED)
}

actual class PlatformLifecycleOwner(val lifecycleOwner: LifecycleOwner) {
    actual val lifecycle: PlatformLifecycle
        get() = PlatformLifecycle(lifecycleOwner.lifecycle!!)
}

actual object PlatformLifecycleOwnerCompose {
    @Composable
    actual fun current(): PlatformLifecycleOwner {
        return PlatformLifecycleOwner(LocalLifecycleOwner.current)
    }
}

actual enum class PlatformLifecycleEvent(val value: LifecycleEvent) {
    ON_CREATE(LifecycleEvent.LIFECYCLEEVENT_ON_CREATE),
    ON_START(LifecycleEvent.LIFECYCLEEVENT_ON_START),
    ON_RESUME(LifecycleEvent.LIFECYCLEEVENT_ON_RESUME),
    ON_PAUSE(LifecycleEvent.LIFECYCLEEVENT_ON_PAUSE),
    ON_STOP(LifecycleEvent.LIFECYCLEEVENT_ON_STOP),
    ON_DESTROY(LifecycleEvent.LIFECYCLEEVENT_ON_DESTROY),
    ON_ANY(LifecycleEvent.LIFECYCLEEVENT_ON_ANY)
}

actual typealias PlatformLifecycleStartStopEffectScope = LifecycleStartStopEffectScope
actual typealias PlatformLifecycleStopOrDisposeEffectResult = LifecycleStopOrDisposeEffectResult
actual typealias PlatformLifecycleResumePauseEffectScope = LifecycleResumePauseEffectScope
actual typealias PlatformLifecyclePauseOrDisposeEffectResult = LifecyclePauseOrDisposeEffectResult

actual class PlatformLifecycle(val lifecycle: Lifecycle)