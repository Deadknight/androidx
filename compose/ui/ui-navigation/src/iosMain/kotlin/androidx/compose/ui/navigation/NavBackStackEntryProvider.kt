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

package androidx.compose.ui.navigation

import UUID
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.platform.LocalViewModelStoreOwner
import cocoapods.Topping.HasDefaultViewModelProviderFactoryProtocol
import cocoapods.Topping.LuaViewModel
import cocoapods.Topping.SavedStateHandle
import cocoapods.Topping.ViewModelProvider
import cocoapods.Topping.ViewModelProviderFactoryProtocol
import cocoapods.Topping.ViewModelStoreOwnerProtocol
import kotlin.native.ref.WeakReference
import kotlin.reflect.KClass
import randomUUID

/**
 * Provides [this] [PlatformNavBackStackEntry] as [LocalViewModelStoreOwner], [LocalLifecycleOwner] and
 * [LocalSavedStateRegistryOwner] to the [content] and saves the [content]'s saveable states with
 * the given [saveableStateHolder].
 *
 * @param saveableStateHolder The [SaveableStateHolder] that holds the saved states. The same
 * holder should be used for all [PlatformNavBackStackEntry]s in the encapsulating [Composable] and the
 * holder should be hoisted.
 * @param content The content [Composable]
 */
@Composable
public fun PlatformNavBackStackEntry.LocalOwnersProvider(
    saveableStateHolder: SaveableStateHolder,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides this,
        LocalLifecycleOwner provides this,
        LocalSavedStateRegistryOwner provides this
    ) {
        saveableStateHolder.SaveableStateProvider(content)
    }
}

@Composable
private fun SaveableStateHolder.SaveableStateProvider(content: @Composable () -> Unit) {
    val viewModel = viewModel<BackStackEntryIdViewModel>()
    // Stash a reference to the SaveableStateHolder in the ViewModel so that
    // it is available when the ViewModel is cleared, marking the permanent removal of this
    // NavBackStackEntry from the back stack. Which, because of animations,
    // only happens after this leaves composition. Which means we can't rely on
    // DisposableEffect to clean up this reference (as it'll be cleaned up too early)
    viewModel.saveableStateHolderRef = WeakReference(this)
    SaveableStateProvider(viewModel.id, content)
}

internal class BackStackEntryIdViewModel(handle: SavedStateHandle) : LuaViewModel() {

    private val IdKey = "SaveableStateHolder_BackStackEntryKey"

    // we create our own id for each back stack entry to support multiple entries of the same
    // destination. this id will be restored by SavedStateHandle
    val id: UUID = handle.getWithKey(IdKey) as UUID? ?: UUID.randomUUID().also { handle.setWithKey(IdKey, it) }

    lateinit var saveableStateHolderRef: WeakReference<SaveableStateHolder>

    // onCleared will be called on the entries removed from the back stack. here we notify
    // SaveableStateProvider that we should remove any state is had associated with this
    // destination as it is no longer needed.
    override fun onCleared() {
        super.onCleared()
        saveableStateHolderRef.get()?.removeState(id)
        saveableStateHolderRef.clear()
    }
}

@Deprecated(
    "Superseded by viewModel that takes CreationExtras",
    level = DeprecationLevel.HIDDEN
)
@Suppress("MissingJvmstatic")
@Composable
public inline fun <reified VM : LuaViewModel> viewModel(
    viewModelStoreOwner: ViewModelStoreOwnerProtocol = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwnerProtocol was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
    factory: ViewModelProviderFactoryProtocol? = null
): VM = viewModel(VM::class, viewModelStoreOwner, key, factory)

@Suppress("MissingJvmstatic")
@Composable
public fun <VM : LuaViewModel> viewModel(
    modelClass: KClass<VM>,
    viewModelStoreOwner: ViewModelStoreOwnerProtocol = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwnerProtocol was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
    factory: ViewModelProviderFactoryProtocol? = null
): VM = viewModelStoreOwner.get(modelClass, key, factory)

@Composable
public inline fun <reified VM : LuaViewModel> viewModel(
    viewModelStoreOwner: ViewModelStoreOwnerProtocol = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwnerProtocol was provided via LocalViewModelStoreOwner"
    },
    key: String? = null
): VM = viewModel(
    VM::class,
    viewModelStoreOwner,
    key
)

private fun <VM : LuaViewModel> ViewModelStoreOwnerProtocol.get(
    javaClass: KClass<VM>,
    key: String? = null,
    factory: ViewModelProviderFactoryProtocol? = null,
): VM {
    val provider = if (factory != null) {
        ViewModelProvider(this.getViewModelStore()!!, factory)
    } else if (this is HasDefaultViewModelProviderFactoryProtocol) {
        ViewModelProvider(this.getViewModelStore()!!, this.getDefaultViewModelProviderFactory())
    } else {
        ViewModelProvider(this)
    }
    return if (key != null) {
        provider.getWithKey(key) as VM
    } else {
        provider.get() as VM
    }
}