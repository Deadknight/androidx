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
import androidx.lifecycle.compose.platform.PlatformLifecycleEvent
import androidx.lifecycle.compose.platform.PlatformLifecycleOwner
import androidx.lifecycle.compose.platform.PlatformLifecyclePauseOrDisposeEffectResult
import androidx.lifecycle.compose.platform.PlatformLifecycleResumePauseEffectScope
import androidx.lifecycle.compose.platform.PlatformLifecycleStartStopEffectScope
import androidx.lifecycle.compose.platform.PlatformLifecycleStopOrDisposeEffectResult

/**
 * Schedule an effect to run when the [Lifecycle] receives a specific [Lifecycle.Event].
 *
 * Using a [LifecycleEventObserver] to listen for when [LifecycleEventEffect] enters
 * the composition, [onEvent] will be launched when receiving the specified [event].
 *
 * This function should **not** be used to listen for [Lifecycle.Event.ON_DESTROY] because
 * Compose stops recomposing after receiving a [Lifecycle.Event.ON_STOP] and will never be
 * aware of an ON_DESTROY to launch [onEvent].
 *
 * This function should also **not** be used to launch tasks in response to callback
 * events by way of storing callback data as a [Lifecycle.State] in a [MutableState].
 * Instead, see [currentStateAsState] to obtain a [State<Lifecycle.State>][State]
 * that may be used to launch jobs in response to state changes.
 *
 * @sample androidx.lifecycle.compose.samples.lifecycleEventEffectSample
 *
 * @param event The [Lifecycle.Event] to listen for
 * @param lifecycleOwner The lifecycle owner to attach an observer
 * @param onEvent The effect to be launched when we receive an [event] callback
 *
 * @throws IllegalArgumentException if attempting to listen for [Lifecycle.Event.ON_DESTROY]
 */
@Composable
actual fun LifecycleEventEffect(
    event: PlatformLifecycleEvent,
    lifecycleOwner: PlatformLifecycleOwner,
    onEvent: () -> Unit
) {
}

/**
 * Schedule a pair of effects to run when the [Lifecycle] receives either a
 * [Lifecycle.Event.ON_START] or [Lifecycle.Event.ON_STOP] (or any new unique
 * value of [key1]). The ON_START effect will be the body of the [effects]
 * block and the ON_STOP effect will be within the
 * (onStopOrDispose clause)[LifecycleStartStopEffectScope.onStopOrDispose]:
 *
 * LifecycleStartEffect(lifecycleOwner) {
 *     // add ON_START effect here
 *
 *     onStopOrDispose {
 *         // add clean up for work kicked off in the ON_START effect here
 *     }
 * }
 *
 * @sample androidx.lifecycle.compose.samples.lifecycleStartEffectSample
 *
 * A [LifecycleStartEffect] **must** include an
 * [onStopOrDispose][LifecycleStartStopEffectScope.onStopOrDispose] clause as the final
 * statement in its [effects] block. If your operation does not require an effect for
 * _both_ [Lifecycle.Event.ON_START] and [Lifecycle.Event.ON_STOP], a [LifecycleEventEffect]
 * should be used instead.
 *
 * A [LifecycleStartEffect]'s _key_ is a value that defines the identity of the effect.
 * If the key changes, the [LifecycleStartEffect] must
 * [dispose][LifecycleStartStopEffectScope.onStopOrDispose] its current [effects] and
 * reset by calling [effects] again. Examples of keys include:
 *
 * * Observable objects that the effect subscribes to
 * * Unique request parameters to an operation that must cancel and retry if those parameters change
 *
 * This function uses a [LifecycleEventObserver] to listen for when [LifecycleStartEffect]
 * enters the composition and the effects will be launched when receiving a
 * [Lifecycle.Event.ON_START] or [Lifecycle.Event.ON_STOP] event, respectively. If the
 * [LifecycleStartEffect] leaves the composition prior to receiving an [Lifecycle.Event.ON_STOP]
 * event, [onStopOrDispose][LifecycleStartStopEffectScope.onStopOrDispose] will be called to
 * clean up the work that was kicked off in the ON_START effect.
 *
 * This function should **not** be used to launch tasks in response to callback
 * events by way of storing callback data as a [Lifecycle.State] in a [MutableState].
 * Instead, see [currentStateAsState] to obtain a [State<Lifecycle.State>][State]
 * that may be used to launch jobs in response to state changes.
 *
 * @param key1 The unique value to trigger recomposition upon change
 * @param lifecycleOwner The lifecycle owner to attach an observer
 * @param effects The effects to be launched when we receive the respective event callbacks
 */
@Composable
actual fun LifecycleStartEffect(
    key1: Any?,
    lifecycleOwner: PlatformLifecycleOwner,
    effects: PlatformLifecycleStartStopEffectScope.() -> PlatformLifecycleStopOrDisposeEffectResult
) {
}

/**
 * Schedule a pair of effects to run when the [Lifecycle] receives either a
 * [Lifecycle.Event.ON_START] or [Lifecycle.Event.ON_STOP] (or any new unique
 * value of [key1] or [key2]). The ON_START effect will be the body of the
 * [effects] block and the ON_STOP effect will be within the
 * (onStopOrDispose clause)[LifecycleStartStopEffectScope.onStopOrDispose]:
 *
 * LifecycleStartEffect(lifecycleOwner) {
 *     // add ON_START effect here
 *
 *     onStopOrDispose {
 *         // add clean up for work kicked off in the ON_START effect here
 *     }
 * }
 *
 * @sample androidx.lifecycle.compose.samples.lifecycleStartEffectSample
 *
 * A [LifecycleStartEffect] **must** include an
 * [onStopOrDispose][LifecycleStartStopEffectScope.onStopOrDispose] clause as the final
 * statement in its [effects] block. If your operation does not require an effect for
 * _both_ [Lifecycle.Event.ON_START] and [Lifecycle.Event.ON_STOP], a [LifecycleEventEffect]
 * should be used instead.
 *
 * A [LifecycleStartEffect]'s _key_ is a value that defines the identity of the effect.
 * If a key changes, the [LifecycleStartEffect] must
 * [dispose][LifecycleStartStopEffectScope.onStopOrDispose] its current [effects] and
 * reset by calling [effects] again. Examples of keys include:
 *
 * * Observable objects that the effect subscribes to
 * * Unique request parameters to an operation that must cancel and retry if those parameters change
 *
 * This function uses a [LifecycleEventObserver] to listen for when [LifecycleStartEffect]
 * enters the composition and the effects will be launched when receiving a
 * [Lifecycle.Event.ON_START] or [Lifecycle.Event.ON_STOP] event, respectively. If the
 * [LifecycleStartEffect] leaves the composition prior to receiving an [Lifecycle.Event.ON_STOP]
 * event, [onStopOrDispose][LifecycleStartStopEffectScope.onStopOrDispose] will be called to
 * clean up the work that was kicked off in the ON_START effect.
 *
 * This function should **not** be used to launch tasks in response to callback
 * events by way of storing callback data as a [Lifecycle.State] in a [MutableState].
 * Instead, see [currentStateAsState] to obtain a [State<Lifecycle.State>][State]
 * that may be used to launch jobs in response to state changes.
 *
 * @param key1 A unique value to trigger recomposition upon change
 * @param key2 A unique value to trigger recomposition upon change
 * @param lifecycleOwner The lifecycle owner to attach an observer
 * @param effects The effects to be launched when we receive the respective event callbacks
 */
@Composable
actual fun LifecycleStartEffect(
    key1: Any?,
    key2: Any?,
    lifecycleOwner: PlatformLifecycleOwner,
    effects: PlatformLifecycleStartStopEffectScope.() -> PlatformLifecycleStopOrDisposeEffectResult
) {
}

/**
 * Schedule a pair of effects to run when the [Lifecycle] receives either a
 * [Lifecycle.Event.ON_START] or [Lifecycle.Event.ON_STOP] (or any new unique
 * value of [key1] or [key2] or [key3]). The ON_START effect will be the body
 * of the [effects] block and the ON_STOP effect will be within the
 * (onStopOrDispose clause)[LifecycleStartStopEffectScope.onStopOrDispose]:
 *
 * LifecycleStartEffect(lifecycleOwner) {
 *     // add ON_START effect here
 *
 *     onStopOrDispose {
 *         // add clean up for work kicked off in the ON_START effect here
 *     }
 * }
 *
 * @sample androidx.lifecycle.compose.samples.lifecycleStartEffectSample
 *
 * A [LifecycleStartEffect] **must** include an
 * [onStopOrDispose][LifecycleStartStopEffectScope.onStopOrDispose] clause as the final
 * statement in its [effects] block. If your operation does not require an effect for
 * _both_ [Lifecycle.Event.ON_START] and [Lifecycle.Event.ON_STOP], a [LifecycleEventEffect]
 * should be used instead.
 *
 * A [LifecycleStartEffect]'s _key_ is a value that defines the identity of the effect.
 * If a key changes, the [LifecycleStartEffect] must
 * [dispose][LifecycleStartStopEffectScope.onStopOrDispose] its current [effects] and
 * reset by calling [effects] again. Examples of keys include:
 *
 * * Observable objects that the effect subscribes to
 * * Unique request parameters to an operation that must cancel and retry if those parameters change
 *
 * This function uses a [LifecycleEventObserver] to listen for when [LifecycleStartEffect]
 * enters the composition and the effects will be launched when receiving a
 * [Lifecycle.Event.ON_START] or [Lifecycle.Event.ON_STOP] event, respectively. If the
 * [LifecycleStartEffect] leaves the composition prior to receiving an [Lifecycle.Event.ON_STOP]
 * event, [onStopOrDispose][LifecycleStartStopEffectScope.onStopOrDispose] will be called to
 * clean up the work that was kicked off in the ON_START effect.
 *
 * This function should **not** be used to launch tasks in response to callback
 * events by way of storing callback data as a [Lifecycle.State] in a [MutableState].
 * Instead, see [currentStateAsState] to obtain a [State<Lifecycle.State>][State]
 * that may be used to launch jobs in response to state changes.
 *
 * @param key1 The unique value to trigger recomposition upon change
 * @param key2 The unique value to trigger recomposition upon change
 * @param key3 The unique value to trigger recomposition upon change
 * @param lifecycleOwner The lifecycle owner to attach an observer
 * @param effects The effects to be launched when we receive the respective event callbacks
 */
@Composable
actual fun LifecycleStartEffect(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    lifecycleOwner: PlatformLifecycleOwner,
    effects: PlatformLifecycleStartStopEffectScope.() -> PlatformLifecycleStopOrDisposeEffectResult
) {
}

/**
 * Schedule a pair of effects to run when the [Lifecycle] receives either a
 * [Lifecycle.Event.ON_START] or [Lifecycle.Event.ON_STOP] (or any new unique
 * value of [keys]). The ON_START effect will be the body of the [effects]
 * block and the ON_STOP effect will be within the
 * (onStopOrDispose clause)[LifecycleStartStopEffectScope.onStopOrDispose]:
 *
 * LifecycleStartEffect(lifecycleOwner) {
 *     // add ON_START effect here
 *
 *     onStopOrDispose {
 *         // add clean up for work kicked off in the ON_START effect here
 *     }
 * }
 *
 * @sample androidx.lifecycle.compose.samples.lifecycleStartEffectSample
 *
 * A [LifecycleStartEffect] **must** include an
 * [onStopOrDispose][LifecycleStartStopEffectScope.onStopOrDispose] clause as the final
 * statement in its [effects] block. If your operation does not require an effect for
 * _both_ [Lifecycle.Event.ON_START] and [Lifecycle.Event.ON_STOP], a [LifecycleEventEffect]
 * should be used instead.
 *
 * A [LifecycleStartEffect]'s _key_ is a value that defines the identity of the effect.
 * If a key changes, the [LifecycleStartEffect] must
 * [dispose][LifecycleStartStopEffectScope.onStopOrDispose] its current [effects] and
 * reset by calling [effects] again. Examples of keys include:
 *
 * * Observable objects that the effect subscribes to
 * * Unique request parameters to an operation that must cancel and retry if those parameters change
 *
 * This function uses a [LifecycleEventObserver] to listen for when [LifecycleStartEffect]
 * enters the composition and the effects will be launched when receiving a
 * [Lifecycle.Event.ON_START] or [Lifecycle.Event.ON_STOP] event, respectively. If the
 * [LifecycleStartEffect] leaves the composition prior to receiving an [Lifecycle.Event.ON_STOP]
 * event, [onStopOrDispose][LifecycleStartStopEffectScope.onStopOrDispose] will be called to
 * clean up the work that was kicked off in the ON_START effect.
 *
 * This function should **not** be used to launch tasks in response to callback
 * events by way of storing callback data as a [Lifecycle.State] in a [MutableState].
 * Instead, see [currentStateAsState] to obtain a [State<Lifecycle.State>][State]
 * that may be used to launch jobs in response to state changes.
 *
 * @param keys The unique values to trigger recomposition upon changes
 * @param lifecycleOwner The lifecycle owner to attach an observer
 * @param effects The effects to be launched when we receive the respective event callbacks
 */
@Composable
actual fun LifecycleStartEffect(
    vararg keys: Any?,
    lifecycleOwner: PlatformLifecycleOwner,
    effects: PlatformLifecycleStartStopEffectScope.() -> PlatformLifecycleStopOrDisposeEffectResult
) {
}

/**
 * Schedule a pair of effects to run when the [Lifecycle] receives either a
 * [Lifecycle.Event.ON_RESUME] or [Lifecycle.Event.ON_PAUSE] (or any new unique
 * value of [key1]). The ON_RESUME effect will be the body of the [effects]
 * block and the ON_PAUSE effect will be within the
 * (onPauseOrDispose clause)[LifecycleResumePauseEffectScope.onPauseOrDispose]:
 *
 * LifecycleResumeEffect(lifecycleOwner) {
 *     // add ON_RESUME effect here
 *
 *     onPauseOrDispose {
 *         // add clean up for work kicked off in the ON_RESUME effect here
 *     }
 * }
 *
 * @sample androidx.lifecycle.compose.samples.lifecycleResumeEffectSample
 *
 * A [LifecycleResumeEffect] **must** include an
 * [onPauseOrDispose][LifecycleResumePauseEffectScope.onPauseOrDispose] clause as
 * the final statement in its [effects] block. If your operation does not require
 * an effect for _both_ [Lifecycle.Event.ON_RESUME] and [Lifecycle.Event.ON_PAUSE],
 * a [LifecycleEventEffect] should be used instead.
 *
 * A [LifecycleResumeEffect]'s _key_ is a value that defines the identity of the effect.
 * If a key changes, the [LifecycleResumeEffect] must
 * [dispose][LifecycleResumePauseEffectScope.onPauseOrDispose] its current [effects] and
 * reset by calling [effects] again. Examples of keys include:
 *
 * * Observable objects that the effect subscribes to
 * * Unique request parameters to an operation that must cancel and retry if those parameters change
 *
 * This function uses a [LifecycleEventObserver] to listen for when [LifecycleResumeEffect]
 * enters the composition and the effects will be launched when receiving a
 * [Lifecycle.Event.ON_RESUME] or [Lifecycle.Event.ON_PAUSE] event, respectively. If the
 * [LifecycleResumeEffect] leaves the composition prior to receiving an [Lifecycle.Event.ON_PAUSE]
 * event, [onPauseOrDispose][LifecycleResumePauseEffectScope.onPauseOrDispose] will be called
 * to clean up the work that was kicked off in the ON_RESUME effect.
 *
 * This function should **not** be used to launch tasks in response to callback
 * events by way of storing callback data as a [Lifecycle.State] in a [MutableState].
 * Instead, see [currentStateAsState] to obtain a [State<Lifecycle.State>][State]
 * that may be used to launch jobs in response to state changes.
 *
 * @param key1 The unique value to trigger recomposition upon change
 * @param lifecycleOwner The lifecycle owner to attach an observer
 * @param effects The effects to be launched when we receive the respective event callbacks
 */
@Composable
actual fun LifecycleResumeEffect(
    key1: Any?,
    lifecycleOwner: PlatformLifecycleOwner,
    effects: PlatformLifecycleResumePauseEffectScope.() -> PlatformLifecyclePauseOrDisposeEffectResult
) {
}

/**
 * Schedule a pair of effects to run when the [Lifecycle] receives either a
 * [Lifecycle.Event.ON_RESUME] or [Lifecycle.Event.ON_PAUSE] (or any new unique
 * value of [key1] or [key2]). The ON_RESUME effect will be the body of the
 * [effects] block and the ON_PAUSE effect will be within the
 * (onPauseOrDispose clause)[LifecycleResumePauseEffectScope.onPauseOrDispose]:
 *
 * LifecycleResumeEffect(lifecycleOwner) {
 *     // add ON_RESUME effect here
 *
 *     onPauseOrDispose {
 *         // add clean up for work kicked off in the ON_RESUME effect here
 *     }
 * }
 *
 * @sample androidx.lifecycle.compose.samples.lifecycleResumeEffectSample
 *
 * A [LifecycleResumeEffect] **must** include an
 * [onPauseOrDispose][LifecycleResumePauseEffectScope.onPauseOrDispose] clause as
 * the final statement in its [effects] block. If your operation does not require
 * an effect for _both_ [Lifecycle.Event.ON_RESUME] and [Lifecycle.Event.ON_PAUSE],
 * a [LifecycleEventEffect] should be used instead.
 *
 * A [LifecycleResumeEffect]'s _key_ is a value that defines the identity of the effect.
 * If a key changes, the [LifecycleResumeEffect] must
 * [dispose][LifecycleResumePauseEffectScope.onPauseOrDispose] its current [effects] and
 * reset by calling [effects] again. Examples of keys include:
 *
 * * Observable objects that the effect subscribes to
 * * Unique request parameters to an operation that must cancel and retry if those parameters change
 *
 * This function uses a [LifecycleEventObserver] to listen for when [LifecycleResumeEffect]
 * enters the composition and the effects will be launched when receiving a
 * [Lifecycle.Event.ON_RESUME] or [Lifecycle.Event.ON_PAUSE] event, respectively. If the
 * [LifecycleResumeEffect] leaves the composition prior to receiving an [Lifecycle.Event.ON_PAUSE]
 * event, [onPauseOrDispose][LifecycleResumePauseEffectScope.onPauseOrDispose] will be called
 * to clean up the work that was kicked off in the ON_RESUME effect.
 *
 * This function should **not** be used to launch tasks in response to callback
 * events by way of storing callback data as a [Lifecycle.State] in a [MutableState].
 * Instead, see [currentStateAsState] to obtain a [State<Lifecycle.State>][State]
 * that may be used to launch jobs in response to state changes.
 *
 * @param key1 A unique value to trigger recomposition upon change
 * @param key2 A unique value to trigger recomposition upon change
 * @param lifecycleOwner The lifecycle owner to attach an observer
 * @param effects The effects to be launched when we receive the respective event callbacks
 */
@Composable
actual fun LifecycleResumeEffect(
    key1: Any?,
    key2: Any?,
    lifecycleOwner: PlatformLifecycleOwner,
    effects: PlatformLifecycleResumePauseEffectScope.() -> PlatformLifecyclePauseOrDisposeEffectResult
) {
}

/**
 * Schedule a pair of effects to run when the [Lifecycle] receives either a
 * [Lifecycle.Event.ON_RESUME] or [Lifecycle.Event.ON_PAUSE] (or any new unique
 * value of [key1] or [key2] or [key3]). The ON_RESUME effect will be the body
 * of the [effects] block and the ON_PAUSE effect will be within the
 * (onPauseOrDispose clause)[LifecycleResumePauseEffectScope.onPauseOrDispose]:
 *
 * LifecycleResumeEffect(lifecycleOwner) {
 *     // add ON_RESUME effect here
 *
 *     onPauseOrDispose {
 *         // add clean up for work kicked off in the ON_RESUME effect here
 *     }
 * }
 *
 * @sample androidx.lifecycle.compose.samples.lifecycleResumeEffectSample
 *
 * A [LifecycleResumeEffect] **must** include an
 * [onPauseOrDispose][LifecycleResumePauseEffectScope.onPauseOrDispose] clause as
 * the final statement in its [effects] block. If your operation does not require
 * an effect for _both_ [Lifecycle.Event.ON_RESUME] and [Lifecycle.Event.ON_PAUSE],
 * a [LifecycleEventEffect] should be used instead.
 *
 * A [LifecycleResumeEffect]'s _key_ is a value that defines the identity of the effect.
 * If a key changes, the [LifecycleResumeEffect] must
 * [dispose][LifecycleResumePauseEffectScope.onPauseOrDispose] its current [effects] and
 * reset by calling [effects] again. Examples of keys include:
 *
 * * Observable objects that the effect subscribes to
 * * Unique request parameters to an operation that must cancel and retry if those parameters change
 *
 * This function uses a [LifecycleEventObserver] to listen for when [LifecycleResumeEffect]
 * enters the composition and the effects will be launched when receiving a
 * [Lifecycle.Event.ON_RESUME] or [Lifecycle.Event.ON_PAUSE] event, respectively. If the
 * [LifecycleResumeEffect] leaves the composition prior to receiving an [Lifecycle.Event.ON_PAUSE]
 * event, [onPauseOrDispose][LifecycleResumePauseEffectScope.onPauseOrDispose] will be called
 * to clean up the work that was kicked off in the ON_RESUME effect.
 *
 * This function should **not** be used to launch tasks in response to callback
 * events by way of storing callback data as a [Lifecycle.State] in a [MutableState].
 * Instead, see [currentStateAsState] to obtain a [State<Lifecycle.State>][State]
 * that may be used to launch jobs in response to state changes.
 *
 * @param key1 A unique value to trigger recomposition upon change
 * @param key2 A unique value to trigger recomposition upon change
 * @param key3 A unique value to trigger recomposition upon change
 * @param lifecycleOwner The lifecycle owner to attach an observer
 * @param effects The effects to be launched when we receive the respective event callbacks
 */
@Composable
actual fun LifecycleResumeEffect(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    lifecycleOwner: PlatformLifecycleOwner,
    effects: PlatformLifecycleResumePauseEffectScope.() -> PlatformLifecyclePauseOrDisposeEffectResult
) {
}

/**
 * Schedule a pair of effects to run when the [Lifecycle] receives either a
 * [Lifecycle.Event.ON_RESUME] or [Lifecycle.Event.ON_PAUSE] (or any new unique
 * value of [keys]). The ON_RESUME effect will be the body of the [effects]
 * block and the ON_PAUSE effect will be within the
 * (onPauseOrDispose clause)[LifecycleResumePauseEffectScope.onPauseOrDispose]:
 *
 * LifecycleResumeEffect(lifecycleOwner) {
 *     // add ON_RESUME effect here
 *
 *     onPauseOrDispose {
 *         // add clean up for work kicked off in the ON_RESUME effect here
 *     }
 * }
 *
 * @sample androidx.lifecycle.compose.samples.lifecycleResumeEffectSample
 *
 * A [LifecycleResumeEffect] **must** include an
 * [onPauseOrDispose][LifecycleResumePauseEffectScope.onPauseOrDispose] clause as
 * the final statement in its [effects] block. If your operation does not require
 * an effect for _both_ [Lifecycle.Event.ON_RESUME] and [Lifecycle.Event.ON_PAUSE],
 * a [LifecycleEventEffect] should be used instead.
 *
 * A [LifecycleResumeEffect]'s _key_ is a value that defines the identity of the effect.
 * If a key changes, the [LifecycleResumeEffect] must
 * [dispose][LifecycleResumePauseEffectScope.onPauseOrDispose] its current [effects] and
 * reset by calling [effects] again. Examples of keys include:
 *
 * * Observable objects that the effect subscribes to
 * * Unique request parameters to an operation that must cancel and retry if those parameters change
 *
 * This function uses a [LifecycleEventObserver] to listen for when [LifecycleResumeEffect]
 * enters the composition and the effects will be launched when receiving a
 * [Lifecycle.Event.ON_RESUME] or [Lifecycle.Event.ON_PAUSE] event, respectively. If the
 * [LifecycleResumeEffect] leaves the composition prior to receiving an [Lifecycle.Event.ON_PAUSE]
 * event, [onPauseOrDispose][LifecycleResumePauseEffectScope.onPauseOrDispose] will be called
 * to clean up the work that was kicked off in the ON_RESUME effect.
 *
 * This function should **not** be used to launch tasks in response to callback
 * events by way of storing callback data as a [Lifecycle.State] in a [MutableState].
 * Instead, see [currentStateAsState] to obtain a [State<Lifecycle.State>][State]
 * that may be used to launch jobs in response to state changes.
 *
 * @param keys The unique values to trigger recomposition upon changes
 * @param lifecycleOwner The lifecycle owner to attach an observer
 * @param effects The effects to be launched when we receive the respective event callbacks
 */
@Composable
actual fun LifecycleResumeEffect(
    vararg keys: Any?,
    lifecycleOwner: PlatformLifecycleOwner,
    effects: PlatformLifecycleResumePauseEffectScope.() -> PlatformLifecyclePauseOrDisposeEffectResult
) {
}