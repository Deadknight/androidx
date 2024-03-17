/*
 * Copyright 2020 The Android Open Source Project
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
package androidx.compose.ui.platform

import View
import addView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.CompositionServiceKey
import androidx.compose.runtime.CompositionServices
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReusableComposition
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.LocalInspectionTables
import androidx.compose.ui.node.LayoutNode
import androidx.compose.ui.node.UiApplier
import childCount
import cocoapods.ToppingCompose.Lifecycle
import cocoapods.ToppingCompose.LifecycleEvent
import cocoapods.ToppingCompose.LifecycleEventObserverProtocol
import cocoapods.ToppingCompose.LifecycleOwnerProtocol
import cocoapods.ToppingCompose.LifecycleState
import cocoapods.ToppingCompose.TIOSKHViewGroupLayoutParams
import context
import currentState
import getChildAt
import layoutParams
import lifecycle
import platform.Foundation.NSUUID
import platform.darwin.NSObject
import removeAllViews

// TODO(chuckj): This is a temporary work-around until subframes exist so that
// nextFrame() inside recompose() doesn't really start a new frame, but a new subframe
// instead.
internal actual fun createSubcomposition(
    container: LayoutNode,
    parent: CompositionContext
): ReusableComposition = ReusableComposition(
    UiApplier(container),
    parent
)

internal fun AbstractComposeView.setContent(
    parent: CompositionContext,
    content: @Composable () -> Unit
): Composition {
    GlobalSnapshotManager.ensureStarted()
    val composeView =
        if (self.childCount > 0) {
            self.getChildAt(0) as? AndroidComposeView
        } else {
            self.removeAllViews(); null
        } ?: AndroidComposeView(self.context, parent.effectCoroutineContext).also {
            self.addViewView(it.view, DefaultLayoutParams)
            self.componentAddMethod(self._view, it.view._view)
        }
    return doSetContent(composeView, parent, content)
}

private fun doSetContent(
    owner: AndroidComposeView,
    parent: CompositionContext,
    content: @Composable () -> Unit
): Composition {
    val original = Composition(UiApplier(owner.root), parent)
    val wrapped = owner.view.getTag("R.id.wrapped_composition_tag")
        as? WrappedCompositionWrapper
        ?: WrappedCompositionWrapper(WrappedComposition(owner, original)).also {
            owner.view.setTag("R.id.wrapped_composition_tag", it)
        }
    wrapped.obj.setContent(content)
    return wrapped.obj
}

private class WrappedCompositionWrapper(val obj: WrappedComposition) : NSObject()

private class WrappedComposition(
    val owner: AndroidComposeView,
    val original: Composition
) : Composition, CompositionServices {

    private var lifecycleEventObserver = LifecycleEventObserver()
    inner class LifecycleEventObserver : NSObject(), LifecycleEventObserverProtocol {
        private val key = NSUUID.UUID().UUIDString

        override fun getKey(): String? {
            return key
        }

        override fun onStateChanged(source: LifecycleOwnerProtocol?, _1: LifecycleEvent) {
            val event = _1
            if (event == LifecycleEvent.LIFECYCLEEVENT_ON_DESTROY) {
                dispose()
            } else if (event == LifecycleEvent.LIFECYCLEEVENT_ON_CREATE) {
                if (!disposed) {
                    setContent(lastContent)
                }
            }
        }
    }

    private var disposed = false
    private var addedToLifecycle: Lifecycle? = null
    private var lastContent: @Composable () -> Unit = {}

    override fun setContent(content: @Composable () -> Unit) {
        owner.setOnViewTreeOwnersAvailable {
            if (!disposed) {
                val lifecycle = it.lifecycleOwner.lifecycle
                lastContent = content
                if (addedToLifecycle == null) {
                    addedToLifecycle = lifecycle
                    // this will call ON_CREATE synchronously if we already created
                    lifecycle!!.addObserver(lifecycleEventObserver)
                } else if (Lifecycle.isAtLeast(lifecycle!!.currentState, LifecycleState.LIFECYCLESTATE_CREATED)) {
                    original.setContent {

                        @Suppress("UNCHECKED_CAST")
                        val inspectionTable =
                            owner.self.getTag("R.id.inspection_slot_table_set") as?
                                MutableSet<CompositionData>
                                ?: (owner.self.parent as? View)?.getTag("R.id.inspection_slot_table_set")
                                    as? MutableSet<CompositionData>
                        if (inspectionTable != null) {
                            inspectionTable.add(currentComposer.compositionData)
                            currentComposer.collectParameterInformation()
                        }

                        LaunchedEffect(owner) { owner.boundsUpdatesEventLoop() }

                        CompositionLocalProvider(LocalInspectionTables provides inspectionTable) {
                            ProvideAndroidCompositionLocals(owner, content)
                        }
                    }
                }
            }
        }
    }

    override fun dispose() {
        if (!disposed) {
            disposed = true
            owner.view.setTag("R.id.wrapped_composition_tag", null)
            addedToLifecycle?.removeObserver(lifecycleEventObserver)
        }
        original.dispose()
        Choreographer.getInstance().dispose()
    }

    override val hasInvalidations get() = original.hasInvalidations
    override val isDisposed: Boolean get() = original.isDisposed

    override fun <T> getCompositionService(key: CompositionServiceKey<T>): T? =
        (original as? CompositionServices)?.getCompositionService(key)
}

private val DefaultLayoutParams = TIOSKHViewGroupLayoutParams()
