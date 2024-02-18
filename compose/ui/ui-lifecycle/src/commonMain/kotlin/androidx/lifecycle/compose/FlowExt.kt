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
import androidx.lifecycle.compose.platform.PlatformLifecycleOwner
import androidx.lifecycle.compose.platform.PlatformLifecycleOwnerCompose
import androidx.lifecycle.compose.platform.PlatformLifecycleState
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Collects values from this [StateFlow] and represents its latest value via [State] in a
 * lifecycle-aware manner.
 *
 * The [StateFlow.value] is used as an initial value. Every time there would be new value posted
 * into the [StateFlow] the returned [State] will be updated causing recomposition of every
 * [State.value] usage whenever the [lifecycleOwner]'s lifecycle is at least [minActiveState].
 *
 * This [StateFlow] is collected every time the [lifecycleOwner]'s lifecycle reaches the
 * [minActiveState] Lifecycle state. The collection stops when the [lifecycleOwner]'s lifecycle
 * falls below [minActiveState].
 *
 * @sample androidx.lifecycle.compose.samples.StateFlowCollectAsStateWithLifecycle
 *
 * Warning: [Lifecycle.State.INITIALIZED] is not allowed in this API. Passing it as a
 * parameter will throw an [IllegalArgumentException].
 *
 * @param lifecycleOwner [LifecycleOwner] whose `lifecycle` is used to restart collecting `this`
 * flow.
 * @param minActiveState [Lifecycle.State] in which the upstream flow gets collected. The
 * collection will stop if the lifecycle falls below that state, and will restart if it's in that
 * state again.
 * @param context [CoroutineContext] to use for collecting.
 */
@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycle(
    lifecycleOwner: PlatformLifecycleOwner = PlatformLifecycleOwnerCompose.current(),
    minActiveState: PlatformLifecycleState = PlatformLifecycleState.STARTED,
    context: CoroutineContext = kotlin.coroutines.EmptyCoroutineContext
): State<T> = collectAsStateWithLifecycle(
    initialValue = this.value,
    lifecycle = lifecycleOwner.lifecycle,
    minActiveState = minActiveState,
    context = context
)

/**
 * Collects values from this [StateFlow] and represents its latest value via [State] in a
 * lifecycle-aware manner.
 *
 * The [StateFlow.value] is used as an initial value. Every time there would be new value posted
 * into the [StateFlow] the returned [State] will be updated causing recomposition of every
 * [State.value] usage whenever the [lifecycle] is at least [minActiveState].
 *
 * This [StateFlow] is collected every time [lifecycle] reaches the [minActiveState] Lifecycle
 * state. The collection stops when [lifecycle] falls below [minActiveState].
 *
 * @sample androidx.lifecycle.compose.samples.StateFlowCollectAsStateWithLifecycle
 *
 * Warning: [Lifecycle.State.INITIALIZED] is not allowed in this API. Passing it as a
 * parameter will throw an [IllegalArgumentException].
 *
 * @param lifecycle [Lifecycle] used to restart collecting `this` flow.
 * @param minActiveState [Lifecycle.State] in which the upstream flow gets collected. The
 * collection will stop if the lifecycle falls below that state, and will restart if it's in that
 * state again.
 * @param context [CoroutineContext] to use for collecting.
 */
@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycle(
    lifecycle: PlatformLifecycle,
    minActiveState: PlatformLifecycleState = PlatformLifecycleState.STARTED,
    context: CoroutineContext = kotlin.coroutines.EmptyCoroutineContext
): State<T> = collectAsStateWithLifecycle(
    initialValue = this.value,
    lifecycle = lifecycle,
    minActiveState = minActiveState,
    context = context
)

/**
 * Collects values from this [Flow] and represents its latest value via [State] in a
 * lifecycle-aware manner.
 *
 * Every time there would be new value posted into the [Flow] the returned [State] will be updated
 * causing recomposition of every [State.value] usage whenever the [lifecycleOwner]'s lifecycle is
 * at least [minActiveState].
 *
 * This [Flow] is collected every time the [lifecycleOwner]'s lifecycle reaches the [minActiveState]
 * Lifecycle state. The collection stops when the [lifecycleOwner]'s lifecycle falls below
 * [minActiveState].
 *
 * @sample androidx.lifecycle.compose.samples.FlowCollectAsStateWithLifecycle
 *
 * Warning: [Lifecycle.State.INITIALIZED] is not allowed in this API. Passing it as a
 * parameter will throw an [IllegalArgumentException].
 *
 * @param initialValue The initial value given to the returned [State.value].
 * @param lifecycleOwner [LifecycleOwner] whose `lifecycle` is used to restart collecting `this`
 * flow.
 * @param minActiveState [Lifecycle.State] in which the upstream flow gets collected. The
 * collection will stop if the lifecycle falls below that state, and will restart if it's in that
 * state again.
 * @param context [CoroutineContext] to use for collecting.
 */
@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(
    initialValue: T,
    lifecycleOwner: PlatformLifecycleOwner = PlatformLifecycleOwnerCompose.current(),
    minActiveState: PlatformLifecycleState = PlatformLifecycleState.STARTED,
    context: CoroutineContext = kotlin.coroutines.EmptyCoroutineContext
): State<T> = collectAsStateWithLifecycle(
    initialValue = initialValue,
    lifecycle = lifecycleOwner.lifecycle,
    minActiveState = minActiveState,
    context = context
)

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
expect fun <T> Flow<T>.collectAsStateWithLifecycle(
    initialValue: T,
    lifecycle: PlatformLifecycle,
    minActiveState: PlatformLifecycleState = PlatformLifecycleState.STARTED,
    context: CoroutineContext = kotlin.coroutines.EmptyCoroutineContext
): State<T>