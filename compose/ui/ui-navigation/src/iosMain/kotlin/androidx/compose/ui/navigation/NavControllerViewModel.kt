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

import cocoapods.Topping.LuaViewModel
import cocoapods.Topping.NavViewModelStoreProviderProtocol
import cocoapods.Topping.ViewModelProvider
import cocoapods.Topping.ViewModelProviderFactoryProtocol
import cocoapods.Topping.ViewModelStore
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCClass
import platform.darwin.NSObject

@ExperimentalForeignApi
private val NavControllerViewModelFACTORY: ViewModelProviderFactoryProtocol = object : NSObject(), ViewModelProviderFactoryProtocol {
    override fun create(): LuaViewModel {
        return NavControllerViewModel()
    }

    override fun createWithCls(cls: ObjCClass): NSObject {
        return NavControllerViewModel()
    }

    override fun createWithPtr(ptr: COpaquePointer?): COpaquePointer? {
        return null
    }
}

@OptIn(ExperimentalForeignApi::class)
fun NavControllerViewModelgetInstance(viewModelStore: ViewModelStore): NavControllerViewModel {
    val viewModelProvider = ViewModelProvider(viewModelStore, NavControllerViewModelFACTORY)
    return viewModelProvider.get() as NavControllerViewModel
}

/**
 * NavControllerViewModel is the always up to date view of the NavController's
 * non configuration state
 */
class NavControllerViewModel : cocoapods.Topping.NavControllerViewModel(), NavViewModelStoreProviderProtocol {
    private val viewModelStores = mutableMapOf<String, ViewModelStore>()

    fun clear(backStackEntryId: String) {
        // Clear and remove the NavGraph's ViewModelStore
        val viewModelStore = viewModelStores.remove(backStackEntryId)
        viewModelStore?.clear()
    }

    override fun onCleared() {
        for (store in viewModelStores.values) {
            store.clear()
        }
        viewModelStores.clear()
    }

    override fun getViewModelStoreWithBackStackEntryId(backStackEntryId: String): ViewModelStore {
        var viewModelStore = viewModelStores[backStackEntryId]
        if (viewModelStore == null) {
            viewModelStore = ViewModelStore()
            viewModelStores[backStackEntryId] = viewModelStore
        }
        return viewModelStore
    }

    override fun description(): String? {
        val sb = StringBuilder("NavControllerViewModel{")
        //sb.append(Integer.toHexString(System.identityHashCode(this)))
        sb.append("} ViewModelStores (")
        val viewModelStoreIterator: Iterator<String> = viewModelStores.keys.iterator()
        while (viewModelStoreIterator.hasNext()) {
            sb.append(viewModelStoreIterator.next())
            if (viewModelStoreIterator.hasNext()) {
                sb.append(", ")
            }
        }
        sb.append(')')
        return sb.toString()
    }
}