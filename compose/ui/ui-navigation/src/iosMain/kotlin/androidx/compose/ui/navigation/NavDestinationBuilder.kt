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

import cocoapods.ToppingCompose.NavAction
import cocoapods.ToppingCompose.NavArgument
import cocoapods.ToppingCompose.NavBackStackEntry
import cocoapods.ToppingCompose.NavController
import cocoapods.ToppingCompose.NavDestination
import cocoapods.ToppingCompose.NavOptions
import cocoapods.ToppingCompose.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSString

public open class NavDestinationBuilder internal constructor(
    /**
     * The navigator the destination was created from
     */
    protected val navigator: Navigator,
    /**
     * The destination's unique ID.
     */
    public val id: String,
    /**
     * The destination's unique route.
     */
    public val route: String?
) {

    /**
     * DSL for constructing a new [NavDestination] with a unique id.
     *
     * This sets the destination's [route] to `null`.
     *
     * @param navigator navigator used to create the destination
     * @param id the destination's unique id
     *
     * @return the newly constructed [NavDestination]
     */
    @Deprecated(
        "Use routes to build your NavDestination instead",
        ReplaceWith("NavDestinationBuilder(navigator, route = id.toString())")
    )
    public constructor(navigator: Navigator, id: String) :
        this(navigator, id, null)

    /**
     * DSL for constructing a new [NavDestination] with a unique route.
     *
     * This will also update the [id] of the destination based on route.
     *
     * @param navigator navigator used to create the destination
     * @param route the destination's unique route
     *
     * @return the newly constructed [NavDestination]
     */
    public constructor(navigator: Navigator, route: String?) :
        this(navigator, "-1", route)

    /**
     * The descriptive label of the destination
     */
    public var label: CharSequence? = null

    private var arguments = mutableMapOf<String, NavArgument>()

    /**
     * Add a [NavArgument] to this destination.
     */
    public fun argument(name: String, argumentBuilder: NavArgumentBuilder.() -> Unit) {
        arguments[name] = NavArgumentBuilder().apply(argumentBuilder).build()
    }

    //private var deepLinks = mutableListOf<NavDeepLink>()
    private var deepLinks = mutableListOf<NavDeepLink>()

    /**
     * Add a deep link to this destination.
     *
     * In addition to a direct Uri match, the following features are supported:
     *
     * *    Uris without a scheme are assumed as http and https. For example,
     *      `www.example.com` will match `http://www.example.com` and
     *      `https://www.example.com`.
     * *    Placeholders in the form of `{placeholder_name}` matches 1 or more
     *      characters. The String value of the placeholder will be available in the arguments
     *      [Bundle] with a key of the same name. For example,
     *      `http://www.example.com/users/{id}` will match
     *      `http://www.example.com/users/4`.
     * *    The `.*` wildcard can be used to match 0 or more characters.
     *
     * @param uriPattern The uri pattern to add as a deep link
     * @see deepLink
     */
    public fun deepLink(uriPattern: String) {
        deepLinks.add(NavDeepLink(uriPattern))
    }

    /**
     * Add a deep link to this destination.
     *
     * In addition to a direct Uri match, the following features are supported:
     *
     * *    Uris without a scheme are assumed as http and https. For example,
     *      `www.example.com` will match `http://www.example.com` and
     *      `https://www.example.com`.
     * *    Placeholders in the form of `{placeholder_name}` matches 1 or more
     *      characters. The String value of the placeholder will be available in the arguments
     *      [Bundle] with a key of the same name. For example,
     *      `http://www.example.com/users/{id}` will match
     *      `http://www.example.com/users/4`.
     * *    The `.*` wildcard can be used to match 0 or more characters.
     *
     * @param navDeepLink the NavDeepLink to be added to this destination
     */
    public fun deepLink(navDeepLink: NavDeepLinkDslBuilder.() -> Unit) {
        deepLinks.add(NavDeepLinkDslBuilder().apply(navDeepLink).build())
    }

    private var actions = mutableMapOf<String, NavAction>()

    /**
     * Adds a new [NavAction] to the destination
     */
    @Deprecated(
        "Building NavDestinations using IDs with the Kotlin DSL has been deprecated in " +
            "favor of using routes. When using routes there is no need for actions."
    )
    public fun action(actionId: String, actionBuilder: NavActionBuilder.() -> Unit) {
        actions[actionId] = NavActionBuilder().apply(actionBuilder).build()
    }

    /**
     * Build the NavDestination by calling [Navigator.createDestination].
     */
    public open fun build(): NavDestination {
        return navigator.createDestination().also { destination ->
            destination.mLabel = label.toString()
            arguments.forEach { (name, argument) ->
                destination.arguments[name] = argument
            }
            deepLinks.forEach { deepLink ->
                destination.deepLinks?.add(deepLink)
            }
            actions.forEach { (actionId, action) ->
                destination.mActions?.setObject(action, actionId as NSString)
            }
            if (route != null) {
                destination.setRouteInternal(route)
            }
            if (id != "-1") {
                destination.idVal = id
            }
        }
    }
}

fun NavDestination.setRouteInternal(route: String?) {
    if (route == null) {
        this.idVal = "0"
    } else {
        require(route.isNotBlank()) { "Cannot have an empty route" }
        val internalRoute = createRoute(route)
        this.idVal = internalRoute
        deepLinks?.add(NavDeepLink.Builder().setUriPattern(internalRoute ?: "").build())
        //addDeepLink(internalRoute)
    }
    deepLinks?.remove(deepLinks?.firstOrNull { (it as NavDeepLink).uriPattern == createRoute(this.route) })
    this.route = route
}

public class NavActionBuilder {
    /**
     * The ID of the destination that should be navigated to when this action is used
     */
    public var destinationId: String = "0"

    /**
     * The set of default arguments that should be passed to the destination. The keys
     * used here should be the same as those used on the [NavDestinationBuilder.argument]
     * for the destination.
     *
     * All values added here should be able to be added to a [android.os.Bundle].
     *
     * @see NavAction.getDefaultArguments
     */
    public val defaultArguments: MutableMap<String, Any?> = mutableMapOf()

    private var navOptions: NavOptions? = null

    /**
     * Sets the [NavOptions] for this action that should be used by default
     */
    public fun navOptions(optionsBuilder: NavOptionsBuilder.() -> Unit) {
        navOptions = NavOptionsBuilder().apply(optionsBuilder).build()
    }

    internal fun build() = NavAction(
        destinationId, navOptions,
        if (defaultArguments.isEmpty())
            null
        else
            bundleOf(*defaultArguments.toList().toTypedArray())
    )
}

public class NavArgumentBuilder {
    private val argument = NavArgument()
    private var _type: NavType<*>? = null

    /**
     * The NavType for this argument.
     *
     * If you don't set a type explicitly, it will be inferred
     * from the default value of this argument.
     */
    public var type: NavType<*>
        set(value) {
            _type = value
            argument.setTypeObject(value)
        }
        get() {
            return _type ?: throw IllegalStateException("NavType has not been set on this builder.")
        }

    /**
     * Controls if this argument allows null values.
     */
    public var nullable: Boolean = false
        set(value) {
            field = value
            argument.mIsNullable = value
        }

    /**
     * An optional default value for this argument.
     *
     * Any object that you set here must be compatible with [type], if it was specified.
     */
    public var defaultValue: Any? = null
        set(value) {
            field = value
            argument.mDefaultValue = value
        }

    /**
     * Builds the NavArgument by calling [NavArgument.Builder.build].
     */
    public fun build(): NavArgument {
        return argument
    }
}