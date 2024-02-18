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

package androidx.lifecycle.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.platform.PlatformLifecycle
import androidx.lifecycle.compose.platform.PlatformLifecycleState
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow

/**
 * Collects values from this [Flow] and represents its latest value via [State] in a
 * lifecycle-aware manner.
 *
 * Every time there would be new value posted into the [Flow] the returned [State] will be updated
 * causing recomposition of every [State.value] usage whenever the [lifecycle] is at
 * least [minActiveState].
 *
 * This [Flow] is collected every time [lifecycle] reaches the [minActiveState] Lifecycle
 * state. The collection stops when [lifecycle] falls below [minActiveState].
 *
 * @sample androidx.lifecycle.compose.samples.FlowCollectAsStateWithLifecycle
 *
 * Warning: [Lifecycle.State.INITIALIZED] is not allowed in this API. Passing it as a
 * parameter will throw an [IllegalArgumentException].
 *
 * @param initialValue The initial value given to the returned [State.value].
 * @param lifecycle [Lifecycle] used to restart collecting `this` flow.
 * @param minActiveState [Lifecycle.State] in which the upstream flow gets collected. The
 * collection will stop if the lifecycle falls below that state, and will restart if it's in that
 * state again.
 * @param context [CoroutineContext] to use for collecting.
 */
@Composable
actual fun <T> Flow<T>.collectAsStateWithLifecycle(
    initialValue: T,
    lifecycle: PlatformLifecycle /*Lifecycle*/,
    minActiveState: PlatformLifecycleState,
    context: CoroutineContext
): State<T> {
    TODO("Not yet implemented")
}