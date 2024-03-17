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

import Bundle
import Context
import androidx.compose.ui.graphics.androidx.compose.ui.graphics.toTIOSKHKotlinArray
import cocoapods.ToppingCompose.LGNavigationParser
import cocoapods.ToppingCompose.LuaFormIntent
import cocoapods.ToppingCompose.NavDestination
import cocoapods.ToppingCompose.NavGraph
import cocoapods.ToppingCompose.NavGraphNavigator
import cocoapods.ToppingCompose.NavOptions
import cocoapods.ToppingCompose.NavigationProvider
import cocoapods.ToppingCompose.Navigator
import cocoapods.ToppingCompose.NavigatorExtrasProtocol
import platform.Foundation.allKeys

public class NavDeepLinkBuilder(private val context: Context) {
    private class DeepLinkDestination constructor(
        val destinationId: String,
        val arguments: Bundle?
    )

    private val intent: Intent = LuaFormIntent()
    private var graph: NavGraph? = null
    private val destinations = mutableListOf<DeepLinkDestination>()
    private var globalArgs: Bundle? = null

    /**
     * @see NavController.createDeepLink
     */
    internal constructor(navController: NavController) : this(navController.context) {
        graph = navController.graph
    }

    public fun setGraphId(navGraphId: String): NavDeepLinkBuilder {
        return setGraph(LGNavigationParser.getInstance()!!.getNavigationProvider(navGraphId, PermissiveNavigatorProvider())!!)
    }

    public fun setGraph(navGraph: NavGraph): NavDeepLinkBuilder {
        graph = navGraph
        verifyAllDestinations()
        return this
    }

    /**
     * Sets the destination id to deep link to. Any destinations previous added via
     * [addDestination] are cleared, effectively resetting this object
     * back to only this single destination.
     *
     * @param destId destination ID to deep link to.
     * @param args Arguments to pass to this destination and any synthetic back stack created
     * due to this destination being added.
     * @return this object for chaining
     */

    public fun setDestinationId(destId: String, args: Bundle? = null): NavDeepLinkBuilder {
        destinations.clear()
        destinations.add(DeepLinkDestination(destId, args))
        if (graph != null) {
            verifyAllDestinations()
        }
        return this
    }

    /**
     * Sets the destination route to deep link to. Any destinations previous added via
     * [.addDestination] are cleared, effectively resetting this object
     * back to only this single destination.
     *
     * @param destRoute destination route to deep link to.
     * @param args Arguments to pass to this destination and any synthetic back stack created
     * due to this destination being added.
     * @return this object for chaining
     */

    public fun setDestination(destRoute: String, args: Bundle? = null): NavDeepLinkBuilder {
        destinations.clear()
        destinations.add(DeepLinkDestination(NavDestination.createRoute(destRoute), args))
        if (graph != null) {
            verifyAllDestinations()
        }
        return this
    }

    /**
     * Add a new destination id to deep link to. This builds off any previous calls to this method
     * or calls to [setDestination], building the minimal synthetic back stack of
     * start destinations between the previous deep link destination and the newly added
     * deep link destination.
     *
     * This means that if R.navigation.nav_graph has startDestination= R.id.start_destination,
     *
     * ```
     * navDeepLinkBuilder
     *    .setGraph(R.navigation.nav_graph)
     *    .addDestination(R.id.second_destination, null)
     * ```
     * is equivalent to
     * ```
     * navDeepLinkBuilder
     *    .setGraph(R.navigation.nav_graph)
     *    .addDestination(R.id.start_destination, null)
     *    .addDestination(R.id.second_destination, null)
     * ```
     *
     * Use the second form to assign specific arguments to the start destination.
     *
     * @param destId destination ID to deep link to.
     * @param args Arguments to pass to this destination and any synthetic back stack created
     * due to this destination being added.
     * @return this object for chaining
     */

    public fun addDestinationId(destId: String, args: Bundle? = null): NavDeepLinkBuilder {
        destinations.add(DeepLinkDestination(destId, args))
        if (graph != null) {
            verifyAllDestinations()
        }
        return this
    }

    /**
     * Add a new destination route to deep link to. This builds off any previous calls to this
     * method or calls to [.setDestination], building the minimal synthetic back stack of
     * start destinations between the previous deep link destination and the newly added
     * deep link destination.
     *
     * @param route destination route to deep link to.
     * @param args Arguments to pass to this destination and any synthetic back stack created
     * due to this destination being added.
     * @return this object for chaining
     */

    public fun addDestination(route: String, args: Bundle? = null): NavDeepLinkBuilder {
        destinations.add(DeepLinkDestination(NavDestination.createRoute(route), args))
        if (graph != null) {
            verifyAllDestinations()
        }
        return this
    }

    private fun findDestinationId(destId: String): NavDestination? {
        val possibleDestinations = ArrayDeque<NavDestination>()
        possibleDestinations.add(graph!!)
        while (!possibleDestinations.isEmpty()) {
            val destination = possibleDestinations.removeFirst()
            if (destination.idVal == destId) {
                return destination
            } else if (destination is NavGraph) {
                for (key in destination.mNodes?.allKeys() ?: listOf<String>()) {
                    val child = destination.mNodes?.objectForKey(key) as NavDestination?
                    if(child != null)
                        possibleDestinations.add(child)
                }
            }
        }
        return null
    }

    private fun verifyAllDestinations() {
        for (destination in destinations) {
            val destId = destination.destinationId
            val node = findDestinationId(destId)
            if (node == null) {
                throw IllegalArgumentException(
                    "Navigation destination $destId cannot be found in the navigation graph $graph"
                )
            }
        }
    }

    private fun fillInIntent() {
        val deepLinkIds = mutableListOf<String>()
        val deepLinkArgs = ArrayList<Bundle?>()
        var previousDestination: NavDestination? = null
        for (destination in destinations) {
            val destId = destination.destinationId
            val arguments = destination.arguments
            val node = findDestinationId(destId)
            if (node == null) {
                throw IllegalArgumentException(
                    "Navigation destination $destId cannot be found in the navigation graph $graph"
                )
            }
            for (id in node.buildDeepLinkIds(previousDestination)) {
                deepLinkIds.add(id)
                deepLinkArgs.add(arguments)
            }
            previousDestination = node
        }
        val idArray = deepLinkIds.toTypedArray()
        intent.bundle?.putStringArray(KEY_DEEP_LINK_IDS, idArray.toTIOSKHKotlinArray())
        intent.bundle?.putObject(KEY_DEEP_LINK_ARGS, deepLinkArgs)
    }

    /**
     * Set optional arguments to send onto every destination created by this deep link.
     * @param args arguments to pass to each destination
     * @return this object for chaining
     */
    public fun setArguments(args: Bundle?): NavDeepLinkBuilder {
        globalArgs = args
        intent.bundle?.putBundle(KEY_DEEP_LINK_EXTRAS, args)
        return this
    }

    /*public fun createTaskStackBuilder(): TaskStackBuilder {
        checkNotNull(graph) {
            "You must call setGraph() before constructing the deep link"
        }
        check(destinations.isNotEmpty()) {
            "You must call setDestination() or addDestination() before constructing the deep link"
        }
        fillInIntent()
        // We create a copy of the Intent to ensure the Intent does not have itself
        // as an extra. This also prevents developers from modifying the internal Intent
        // via taskStackBuilder.editIntentAt()
        val taskStackBuilder = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(Intent(intent))
        for (index in 0 until taskStackBuilder.intentCount) {
            // Attach the original Intent to each Activity so that they can know
            // they were constructed in response to a deep link
            taskStackBuilder.editIntentAt(index)
                ?.putExtra(NavController.KEY_DEEP_LINK_INTENT, intent)
        }
        return taskStackBuilder
    }*/

    private class PermissiveNavigatorProvider : NavigationProvider() {
        /**
         * A Navigator that only parses the [NavDestination] attributes.
         */
        private val mDestNavigator: Navigator =
            object : Navigator() {
                override fun createDestination(): NavDestination {
                    return NavDestination("permissive")
                }

                override fun navigateWithDestination(
                    destination: NavDestination,
                    args: Bundle?,
                    navOptions: NavOptions?,
                    navigatorExtras: NavigatorExtrasProtocol?
                ): NavDestination? {
                    throw IllegalStateException("navigate is not supported")
                }

                override fun popBackStack(): Boolean {
                    throw IllegalStateException("popBackStack is not supported")
                }
            }

        override fun getNavigatorWithName(name: String): Navigator {
            return try {
                super.getNavigatorWithName(name)
            } catch (e: IllegalStateException) {
                mDestNavigator
            }
        }

        init {
            addNavigatorWithNavigator(NavGraphNavigator(this))
        }
    }
}