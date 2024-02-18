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

import cocoapods.Topping.NavDestination
import cocoapods.Topping.NavGraph
import cocoapods.Topping.NavGraphNavigator
import cocoapods.Topping.NavigationProvider
import platform.Foundation.NSString

/**
 * Construct a new [NavGraph]
 *
 * @param id the destination's unique id
 * @param startDestination the starting destination for this NavGraph
 * @param builder the builder used to construct the graph
 *
 * @return the newly constructed NavGraph
 */
@Suppress("Deprecation")
@Deprecated(
    "Use routes to build your NavGraph instead",
    ReplaceWith(
        "navigation(startDestination = startDestination.toString(), route = id.toString()) " +
            "{ builder.invoke() }"
    )
)
public inline fun NavigationProvider.navigationId(
    id: String = "0",
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit
): NavGraph = NavGraphBuilder(this, id, startDestination).apply(builder).build()

/**
 * Construct a new [NavGraph]
 *
 * @param startDestination the starting destination's route for this NavGraph
 * @param route the destination's unique route
 * @param builder the builder used to construct the graph
 *
 * @return the newly constructed NavGraph
 */
public inline fun NavigationProvider.navigation(
    startDestination: String,
    route: String? = null,
    builder: NavGraphBuilder.() -> Unit
): NavGraph = NavGraphBuilder(this, startDestination, route).apply(builder)
    .build()

/**
 * Construct a nested [NavGraph]
 *
 * @param id the destination's unique id
 * @param startDestination the starting destination for this NavGraph
 * @param builder the builder used to construct the graph
 *
 * @return the newly constructed nested NavGraph
 */
@Suppress("Deprecation")
@Deprecated(
    "Use routes to build your nested NavGraph instead",
    ReplaceWith(
        "navigation(startDestination = startDestination.toString(), route = id.toString()) " +
            "{ builder.invoke() }"
    )
)
public inline fun NavGraphBuilder.navigationId(
    id: String = "0",
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit
): Unit = destination(NavGraphBuilder(provider, id, startDestination).apply(builder))

/**
 * Construct a nested [NavGraph]
 *
 * @param startDestination the starting destination's route for this NavGraph
 * @param route the destination's unique route
 * @param builder the builder used to construct the graph
 *
 * @return the newly constructed nested NavGraph
 */
public inline fun NavGraphBuilder.navigation(
    startDestination: String,
    route: String,
    builder: NavGraphBuilder.() -> Unit
): Unit = destination(NavGraphBuilder(provider, startDestination, route).apply(builder))

public open class NavGraphBuilder : NavDestinationBuilder {
    /**
     * The [NavGraphBuilder]'s [NavigatorProvider].
     */
    public val provider: NavigationProvider
    private var startDestinationId: String = "0"
    private var startDestinationRoute: String? = null

    /**
     * DSL for constructing a new [NavGraph]
     *
     * @param provider navigator used to create the destination
     * @param id the graph's unique id
     * @param startDestination the starting destination for this NavGraph
     *
     * @return the newly created NavGraph
     */
    @Suppress("Deprecation")
    @Deprecated(
        "Use routes to build your NavGraph instead",
        ReplaceWith(
            "NavGraphBuilder(provider, startDestination = startDestination.toString(), " +
                "route = id.toString())"
        )
    )
    public constructor(
        provider: NavigationProvider,
        id: String,
        startDestination: String
    ) : super(provider.getNavigatorWithNavigator(NavGraphNavigator()), id = id) {
        this.provider = provider
        this.startDestinationId = startDestination
    }

    /**
     * DSL for constructing a new [NavGraph]
     *
     * @param provider navigator used to create the destination
     * @param startDestination the starting destination's route for this NavGraph
     * @param route the graph's unique route
     *
     * @return the newly created NavGraph
     */
    public constructor(
        provider: NavigationProvider,
        startDestination: String,
        route: String?
    ) : super(provider.getNavigatorWithNavigator(NavGraphNavigator()), route = route) {
        this.provider = provider
        this.startDestinationRoute = startDestination
    }

    private val destinations = mutableListOf<NavDestination>()

    /**
     * Build and add a new destination to the [NavGraphBuilder]
     */
    public fun destination(navDestination: NavDestinationBuilder) {
        destinations += navDestination.build()
    }

    /**
     * Adds this destination to the [NavGraphBuilder]
     */
    public operator fun NavDestination.unaryPlus() {
        addDestination(this)
    }

    /**
     * Add the destination to the [NavGraphBuilder]
     */
    public fun addDestination(destination: NavDestination) {
        destinations += destination
    }

    override fun build(): NavGraph = super.build().also { navGraph ->
        navGraph as NavGraph
        navGraph.addDestinations(destinations)
        if (startDestinationId == "0" && startDestinationRoute == null) {
            if (route != null) {
                throw IllegalStateException("You must set a start destination route")
            } else {
                throw IllegalStateException("You must set a start destination id")
            }
        }
        if (startDestinationRoute != null) {
            navGraph.mStartDestinationRoute = startDestinationRoute!!
        } else {
            navGraph.mStartDestinationId = startDestinationId
        }
    } as NavGraph
}

fun NavGraph.addDestinations(nodes: Collection<NavDestination?>) {
    for (node in nodes) {
        if (node == null) {
            continue
        }
        addDestination(node)
    }
}

fun NavGraph.addDestination(node: NavDestination) {
    val id = node.idVal
    val innerRoute = node.route
    require(idVal != "0" || innerRoute != null) {
        "Destinations must have an id or route. Call setId(), setRoute(), or include an " +
            "android:id or app:route in your navigation XML."
    }
    if (route != null) {
        require(innerRoute != route) {
            "Destination $node cannot have the same route as graph $this"
        }
    }
    require(id != this.idVal) { "Destination $node cannot have the same id as graph $this" }
    val existingDestination = mNodes?.objectForKey(idVal) as NavDestination?
    if (existingDestination === node) {
        return
    }
    check(node.mParent == null) {
        "Destination already has a parent set. Call NavGraph.remove() to remove the previous " +
            "parent."
    }
    if (existingDestination != null) {
        existingDestination.mParent = null
    }
    node.mParent = this
    mNodes?.setObject(node, node.idVal!! as NSString)
}