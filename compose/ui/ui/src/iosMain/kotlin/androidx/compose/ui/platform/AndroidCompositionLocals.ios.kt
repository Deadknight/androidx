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

import Context
import LifecycleOwner
import SavedStateRegistryOwner
import View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.ImageVectorCache
import applicationContext
import cocoapods.Topping.ComponentCallbacksProtocol
import cocoapods.Topping.Configuration
import cocoapods.Topping.ViewModelStoreOwnerProtocol
import configuration
import context
import findViewTreeViewModelStoreOwner
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSValue
import platform.UIKit.CGRectValue
import platform.UIKit.UIKeyboardFrameEndUserInfoKey
import platform.UIKit.UIKeyboardWillShowNotification
import platform.darwin.NSObject
import registerComponentCallbacks
import resources
import unregisterComponentCallbacks

/**
 * The Android [Configuration]. The [Configuration] is useful for determining how to organize the
 * UI.
 */
val LocalConfiguration = compositionLocalOf<Configuration> {
    noLocalProvidedFor("LocalConfiguration")
}

/**
 * Provides a [Context] that can be used by Android applications.
 */
val LocalContext = staticCompositionLocalOf<Context> {
    noLocalProvidedFor("LocalContext")
}

internal val LocalImageVectorCache = staticCompositionLocalOf<ImageVectorCache> {
    noLocalProvidedFor("LocalImageVectorCache")
}

/**
 * The CompositionLocal containing the current [LifecycleOwner].
 */
val LocalLifecycleOwner = staticCompositionLocalOf<LifecycleOwner> {
    noLocalProvidedFor("LocalLifecycleOwner")
}

/**
 * The CompositionLocal containing the current [SavedStateRegistryOwner].
 */
val LocalSavedStateRegistryOwner = staticCompositionLocalOf<SavedStateRegistryOwner> {
    noLocalProvidedFor("LocalSavedStateRegistryOwner")
}

/**
 * The CompositionLocal containing the current Compose [View].
 */
val LocalView = staticCompositionLocalOf<View> {
    noLocalProvidedFor("LocalView")
}

val LocalKeyboardOverlapHeight = staticCompositionLocalOf<KeyboardObserver> {
    noLocalProvidedFor("LocalKeyboardOverlapHeight")
}

public object LocalViewModelStoreOwner {
    private val LocalViewModelStoreOwner =
        compositionLocalOf<ViewModelStoreOwnerProtocol?> { null }

    /**
     * Returns current composition local value for the owner or `null` if one has not
     * been provided nor is one available via [findViewTreeViewModelStoreOwner] on the
     * current [LocalView].
     */
    public val current: ViewModelStoreOwnerProtocol?
        @Composable
        get() = LocalViewModelStoreOwner.current
            ?: LocalView.current.findViewTreeViewModelStoreOwner()

    /**
     * Associates a [LocalViewModelStoreOwner] key to a value in a call to
     * [CompositionLocalProvider].
     */
    public infix fun provides(viewModelStoreOwner: ViewModelStoreOwnerProtocol):
        ProvidedValue<ViewModelStoreOwnerProtocol?> {
        return LocalViewModelStoreOwner.provides(viewModelStoreOwner)
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal fun ProvideAndroidCompositionLocals(
    owner: AndroidComposeView,
    content: @Composable () -> Unit
) {
    val view = owner
    val context = view.self.context
    // Make a deep copy to compare to later, since the same configuration object will be mutated
    // as part of configuration changes
    var configuration by remember {
        mutableStateOf(Configuration(context.resources.configuration))
    }

    owner.configurationChangeObserver = { configuration = Configuration(it) }

    val uriHandler = remember { AndroidUriHandler(context) }
    val viewTreeOwners = owner.viewTreeOwners ?: throw IllegalStateException(
        "Called when the ViewTreeOwnersAvailability is not yet in Available state"
    )

    val saveableStateRegistry = remember {
        DisposableSaveableStateRegistry(view.self, viewTreeOwners.savedStateRegistryOwner)
    }
    DisposableEffect(Unit) {
        onDispose {
            saveableStateRegistry.dispose()
        }
    }

    val imageVectorCache = obtainImageVectorCache(context, configuration)
    val keyboardHeight = obtainKeyboardHeight()
    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalContext provides context,
        LocalLifecycleOwner provides viewTreeOwners.lifecycleOwner,
        LocalSavedStateRegistryOwner provides viewTreeOwners.savedStateRegistryOwner,
        LocalSaveableStateRegistry provides saveableStateRegistry,
        LocalView provides owner.view,
        LocalImageVectorCache provides imageVectorCache,
        LocalKeyboardOverlapHeight provides keyboardHeight
    ) {
        ProvideCommonCompositionLocals(
            owner = owner,
            uriHandler = uriHandler,
            content = content
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
class KeyboardObserver() : NSObject() {
    var keyBoardHeight : Double = 0.0

    fun addObserver() {
        val sel = NSSelectorFromString(::keyboardWillShow.name)
        NSNotificationCenter.defaultCenter.addObserver(this, sel, UIKeyboardWillShowNotification, null)
    }

    fun removeObserver() {
        NSNotificationCenter.defaultCenter.removeObserver(this, UIKeyboardWillShowNotification, null)
    }

    @ObjCAction
    fun keyboardWillShow(notification: NSNotification) {
        val keyboardFrame: NSValue? = notification.userInfo?.get(UIKeyboardFrameEndUserInfoKey) as? NSValue
        keyboardFrame?.let {
            it.CGRectValue.useContents {
                keyBoardHeight = size.height
            }
        }
    }
}

@Stable
@Composable
private fun obtainKeyboardHeight() : KeyboardObserver {
    val keyboardObserver = remember { KeyboardObserver() }

    DisposableEffect(keyboardObserver) {
        keyboardObserver.addObserver()
        onDispose {
            keyboardObserver.removeObserver()
        }
    }
    return keyboardObserver
}

@Stable
@Composable
private fun obtainImageVectorCache(
    context: Context,
    configuration: Configuration?
): ImageVectorCache {
    val imageVectorCache = remember { ImageVectorCache() }
    val currentConfiguration: Configuration = remember {
        Configuration().apply { configuration?.let { this.setTo(it) } }
    }
    val callbacks = remember {
        object : NSObject(), ComponentCallbacksProtocol {
            override fun onConfigurationChanged(configuration: Configuration?) {
                val changedFlags = currentConfiguration.updateFrom(configuration)
                imageVectorCache.prune(changedFlags)
                currentConfiguration.setTo(configuration)
            }

            override fun onLowMemory() {
                imageVectorCache.clear()
            }

            override fun onTrimMemory(level: Int) {
                imageVectorCache.clear()
            }
        }
    }
    DisposableEffect(imageVectorCache) {
        context.applicationContext.registerComponentCallbacks(callbacks)
        onDispose {
            context.applicationContext.unregisterComponentCallbacks(callbacks)
        }
    }
    return imageVectorCache
}

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
