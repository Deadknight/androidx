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

import Activity
import AttributeSet
import Context
import JvmOverloads
import ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.UiComposable
import androidx.compose.ui.node.InternalCoreApi
import androidx.compose.ui.node.Owner
import cocoapods.Topping.LGView
import cocoapods.Topping.LuaForm
import cocoapods.Topping.MeasureSpec
import cocoapods.Topping.TIOSKHTViewProtocol
import cocoapods.Topping.TIOSKHViewGroupLayoutParams
import cocoapods.Topping.WRAP_CONTENT
import findViewTreeSavedStateRegistryOwner
import findViewTreeViewModelStoreOwner
import getChildAt
import init
import kotlin.native.ref.WeakReference
import measuredHeight
import measuredWidth
import paddingBottom
import paddingLeft
import paddingRight
import paddingTop
import platform.darwin.NSInteger
import setViewTreeSavedStateRegistryOwner
import setViewTreeViewModelStoreOwner

abstract class AbstractComposeView constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) {
    var self: ViewGroup

    inner class AbstractComposeViewViewGroup : ViewGroup() {

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()

        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()

            previousAttachedWindowToken = _view?.window

            if (shouldCreateCompositionOnAttachedToWindow) {
                ensureCompositionCreated()
            }
        }

        fun superOnMeasure(widthMeasureSpec: Int, _1: Int) {
            super.onMeasure(widthMeasureSpec, _1)
        }

        final override fun onMeasure(widthMeasureSpec: Int, _1: Int) {
            val heightMeasureSpec = _1
            ensureCompositionCreated()
            internalOnMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        // Below: enforce restrictions on adding child views to this ViewGroup
        override fun addSubview(`val`: LGView?) {
            checkAddView()
            super.addSubview(`val`)
        }

        override fun addSubview(`val`: LGView?, _1: NSInteger) {
            checkAddView()
            super.addSubview(`val`, _1)
        }

        override fun addViewView(view: TIOSKHTViewProtocol, param: TIOSKHViewGroupLayoutParams) {
            checkAddView()
            super.addViewView(view, param)
        }

        final override fun layout(l: Int, _1: Int, _2: Int, _3: Int) {
            val left = l
            val top = _1
            val right = _2
            val bottom = _3
            internalOnLayout(true, left, top, right, bottom)
        }

        override fun onRtlPropertiesChanged(layoutDirection: Int) {
            // Force the single child for our composition to have the same LayoutDirection
            // that we do. We will get onRtlPropertiesChanged eagerly as the value changes,
            // but the composition child view won't until it measures. This can be too late
            // to catch the composition pass for that frame, so propagate it eagerly.
            //getChildAt(0)?.layoutDirection = layoutDirection
            getChildAt(0)?.resolveLayoutDirection()
        }
    }

    init {
        self = AbstractComposeViewViewGroup()
        self.kParentType = this
        self.init(context, attrs)
    }

    private var cachedViewTreeCompositionContext: WeakReference<CompositionContext>? = null

    protected var previousAttachedWindowToken: Any? = null
        set(value) {
            if (field !== value) {
                field = value
                cachedViewTreeCompositionContext = null
            }
        }

    private var composition: Composition? = null

    /**
     * The explicitly set [CompositionContext] to use as the parent of compositions created
     * for this view. Set by [setParentCompositionContext].
     *
     * If set to a non-null value [cachedViewTreeCompositionContext] will be cleared.
     */
    private var parentContext: CompositionContext? = null
        set(value) {
            if (field !== value) {
                field = value
                if (value != null) {
                    cachedViewTreeCompositionContext = null
                }
                val old = composition
                if (old !== null) {
                    old.dispose()
                    composition = null

                    // Recreate the composition now if we are attached.
                    if (self.isAttachedToWindow()) {
                        ensureCompositionCreated()
                    }
                }
            }
        }

    /**
     * Set the [CompositionContext] that should be the parent of this view's composition.
     * If [parent] is `null` it will be determined automatically from the window the view is
     * attached to.
     */
    fun setParentCompositionContext(parent: CompositionContext?) {
        parentContext = parent
    }

    // Leaking `this` during init is generally dangerous, but we know that the implementation of
    // this particular ViewCompositionStrategy is not going to do something harmful with it.
    @Suppress("LeakingThis")
    private var disposeViewCompositionStrategy: (() -> Unit)? =
        ViewCompositionStrategy.Default.installFor(this)

    /**
     * Set the strategy for managing disposal of this View's internal composition.
     * Defaults to [ViewCompositionStrategy.Default].
     *
     * This View's composition is a live resource that must be disposed to ensure that
     * long-lived references to it do not persist
     *
     * See [ViewCompositionStrategy] for more information.
     */
    fun setViewCompositionStrategy(strategy: ViewCompositionStrategy) {
        disposeViewCompositionStrategy?.invoke()
        disposeViewCompositionStrategy = strategy.installFor(this)
    }

    /**
     * If `true`, this View's composition will be created when it becomes attached to a
     * window for the first time. Defaults to `true`.
     *
     * Subclasses may choose to override this property to prevent this eager initial composition
     * in cases where the view's content is not yet ready. Initial composition will still occur
     * when this view is first measured.
     */
    protected open val shouldCreateCompositionOnAttachedToWindow: Boolean
        get() = true

    /**
     * Enables the display of visual layout bounds for the Compose UI content of this view.
     * This is typically managed
     */
    @OptIn(InternalCoreApi::class)
    @InternalComposeUiApi
    @Suppress("GetterSetterNames")
    @get:Suppress("GetterSetterNames")
    var showLayoutBounds: Boolean = false
        set(value) {
            field = value
            self.getChildAt(0)?.let {
                (it as Owner).showLayoutBounds = value
            }
        }

    /**
     * The Jetpack Compose UI content for this view.
     * Subclasses must implement this method to provide content. Initial composition will
     * occur when the view becomes attached to a window or when [createComposition] is called,
     * whichever comes first.
     */
    @Composable
    @UiComposable
    abstract fun Content()

    fun createComposition() {
        check(parentContext != null || self.isAttachedToWindow()) {
            "createComposition requires either a parent reference or the View to be attached" +
                "to a window. Attach the View or call setParentCompositionReference."
        }
        ensureCompositionCreated()
    }

    private var creatingComposition = false
    protected fun checkAddView() {
        if (!creatingComposition) {
            throw UnsupportedOperationException(
                "Cannot add views to " +
                    "${self}; only Compose content is supported"
            )
        }
    }

    /**
     * `true` if the [CompositionContext] can be considered to be "alive" for the purposes
     * of locally caching it in case the view is placed into a ViewOverlay.
     * [Recomposer]s that are in the [Recomposer.State.ShuttingDown] state or lower should
     * not be cached or reusedif currently cached, as they will never recompose content.
     */
    private val CompositionContext.isAlive: Boolean
        get() = this !is Recomposer || currentState.value > Recomposer.State.ShuttingDown

    /**
     * Cache this [CompositionContext] in [cachedViewTreeCompositionContext] if it [isAlive]
     * and return the [CompositionContext] itself either way.
     */
    private fun CompositionContext.cacheIfAlive(): CompositionContext = also { context ->
        context.takeIf { it.isAlive }
            ?.let { cachedViewTreeCompositionContext = WeakReference(it) }
    }

    private fun resolveParentCompositionContext() = parentContext
        ?: self.findViewTreeCompositionContext()?.cacheIfAlive()
        ?: cachedViewTreeCompositionContext?.get()?.takeIf { it.isAlive }
        ?: self.windowRecomposer.cacheIfAlive()

    @Suppress("DEPRECATION") // Still using ViewGroup.setContent for now
    protected fun ensureCompositionCreated() {
        if (composition == null) {
            try {
                creatingComposition = true
                composition = setContent(resolveParentCompositionContext()) {
                    Content()
                }
            } finally {
                creatingComposition = false
            }
        }
    }

    /**
     * Dispose of the underlying composition and [requestLayout].
     * A new composition will be created if [createComposition] is called or when needed to
     * lay out this view.
     */
    fun disposeComposition() {
        composition?.dispose()
        composition = null
        self.requestLayout()
    }

    /**
     * `true` if this View is host to an active Compose UI composition.
     * An active composition may consume resources.
     */
    val hasComposition: Boolean get() = composition != null

    @Suppress("WrongCall")
    open fun internalOnMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val child = self.getChildAt(0)
        if (child == null) {
            (self as AbstractComposeViewViewGroup).superOnMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val width = maxOf(0, MeasureSpec.getSize(widthMeasureSpec) - self.paddingLeft - self.paddingRight)
        val height = maxOf(0, MeasureSpec.getSize(heightMeasureSpec) - self.paddingTop - self.paddingBottom)
        child.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.getMode(widthMeasureSpec)),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.getMode(heightMeasureSpec)),
        )
        self.setMeasuredDimension(
            child.measuredWidth + self.paddingLeft + self.paddingRight,
            child.measuredHeight + self.paddingTop + self.paddingBottom
        )
    }

    open fun internalOnLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        self.getChildAt(0)?.layout(
            self.paddingLeft,
            self.paddingTop,
            right - left - self.paddingRight,
            bottom - top - self.paddingBottom
        )
    }

    // Transition group handling:
    // Both the framework and androidx transition APIs use isTransitionGroup as a signal for
    // determining view properties to capture during a transition. As AbstractComposeView uses
    // a view subhierarchy to perform its work but operates as a single unit, mark instances as
    // transition groups by default.
    // This is implemented as overridden methods instead of setting isTransitionGroup = true in
    // the constructor so that values set explicitly by xml inflation performed by the ViewGroup
    // constructor will take precedence. As of this writing all known framework implementations
    // use the public isTransitionGroup method rather than checking the internal ViewGroup flag
    // to determine behavior, making this implementation a slight compatibility risk for a
    // tradeoff of cleaner View-consumer API behavior without the overhead of performing an
    // additional obtainStyledAttributes call to determine a value potentially overridden from xml.

    private var isTransitionGroupSet = false

    fun isTransitionGroup(): Boolean = !isTransitionGroupSet

    fun setTransitionGroup(isTransitionGroup: Boolean) {
        isTransitionGroupSet = true
    }

    fun shouldDelayChildPressedState(): Boolean = false
}

actual class CommonContext actual constructor(nativeContext: Any, nativeAttributeSet: Any?) {
    val nContext: Context
    val nAttributeSet: AttributeSet?
    init {
        this.nContext = nativeContext as Context
        this.nAttributeSet = nativeAttributeSet as AttributeSet?
    }
    actual fun getNativeContext(): Any {
        return nContext
    }

    actual fun getNativeAttributeSet(): Any? {
        return nAttributeSet
    }
}

actual class ComposeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    actual constructor(commonContext: CommonContext, defStyleAttr: Int)
        : this(commonContext.getNativeContext() as Context, commonContext.nAttributeSet, defStyleAttr)

    actual fun addSelfToActivity(activity: Any,
        parent: CompositionContext?,
        content: @Composable () -> Unit) {
        val act = activity as Activity

        setParentCompositionContext(parent)
        setContent(content)

        if (act.lgview!!.findViewTreeLifecycleOwner() == null) {
            act.lgview!!.setViewTreeLifecycleOwner(act)
        }
        if (act.lgview!!.findViewTreeViewModelStoreOwner() == null) {
            act.lgview!!.setViewTreeViewModelStoreOwner(act)
        }
        if (act.lgview!!.findViewTreeSavedStateRegistryOwner() == null) {
            act.lgview!!.setViewTreeSavedStateRegistryOwner(act)
        }

        self.dWidthDimension = WRAP_CONTENT
        self.dHeightDimension = WRAP_CONTENT
        (act.lgview as ViewGroup).addSubview(self)
        act.lgview!!.componentAddMethod(act.lgview!!._view, self._view)

        //act.setLuaView(self)
    }

    private val content = mutableStateOf<(@Composable () -> Unit)?>(null)

    @Suppress("RedundantVisibilityModifier")
    protected override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    @Composable
    override fun Content() {
        content.value?.invoke()
    }

    fun getAccessibilityClassName(): CharSequence {
        return self.toString() ?: ""
    }

    /**
     * Set the Jetpack Compose UI content for this view.
     * Initial composition will occur when the view becomes attached to a window or when
     * [createComposition] is called, whichever comes first.
     */
    fun setContent(content: @Composable () -> Unit) {
        shouldCreateCompositionOnAttachedToWindow = true
        this.content.value = content
        if (self.isAttachedToWindow()) {
            createComposition()
        }
    }
}
