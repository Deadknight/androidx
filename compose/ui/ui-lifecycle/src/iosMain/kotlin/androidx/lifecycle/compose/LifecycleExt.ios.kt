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
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.platform.PlatformLifecycle
import androidx.lifecycle.compose.platform.PlatformLifecycleState
import cocoapods.ToppingCompose.LifecycleState
import currentState
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
actual fun PlatformLifecycle.currentStateAsState(): State<PlatformLifecycleState> = MutableStateFlow(
    when(lifecycle.currentState) {
        LifecycleState.LIFECYCLESTATE_DESTROYED -> PlatformLifecycleState.DESTROYED
        LifecycleState.LIFECYCLESTATE_INITIALIZED -> PlatformLifecycleState.INITIALIZED
        LifecycleState.LIFECYCLESTATE_CREATED -> PlatformLifecycleState.CREATED
        LifecycleState.LIFECYCLESTATE_STARTED -> PlatformLifecycleState.STARTED
        LifecycleState.LIFECYCLESTATE_RESUMED -> PlatformLifecycleState.RESUMED
        else -> PlatformLifecycleState.INITIALIZED
    }
).collectAsState()