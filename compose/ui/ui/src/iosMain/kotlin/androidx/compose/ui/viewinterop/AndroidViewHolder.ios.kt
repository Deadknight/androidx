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

package androidx.compose.ui.viewinterop

import androidx.compose.runtime.ComposeNodeLifecycleCallback
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.LayoutNode
import androidx.compose.ui.node.Owner
import androidx.compose.ui.node.OwnerScope
import androidx.compose.ui.node.OwnerSnapshotObserver
import AT_MOST
import androidx.compose.ui.platform.AndroidComposeView
import Context
import EXACTLY
import GONE
import LayoutParams
import LifecycleOwner
import MATCH_PARENT
import SavedStateRegistryOwner
import UNSPECIFIED
import View
import ViewGroup
import WRAP_CONTENT
import addView
import childCount
import androidx.compose.ui.platform.compositionContext
import getChildAt
import height
import isAttachedToWindow
import layoutParams
import measuredHeight
import measuredWidth
import removeAllViewsInLayout
import setViewTreeLifecycleOwner
import setViewTreeSavedStateRegistryOwner
import visibility
import width
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import cocoapods.ToppingCompose.MATCH_PARENT
import cocoapods.ToppingCompose.MeasureSpec
import cocoapods.ToppingCompose.TIOSKHTRunnableProtocol
import cocoapods.ToppingCompose.TIOSKHViewGroupLayoutParams
import init
import kotlin.math.roundToInt
import minimumHeight
import minimumWidth
import platform.darwin.NSObject
import platform.posix.int32_t

/**
 * A base class used to host a [View] inside Compose.
 * This API is not designed to be used directly, but rather using the [AndroidView] and
 * `AndroidViewBinding` APIs, which are built on top of [AndroidViewHolder].
 *
 * @param view The view hosted by this holder.
 * @param owner The [Owner] of the composition that this holder lives in.
 */
@OptIn(ExperimentalComposeUiApi::class)
internal open class AndroidViewHolder(
    context: Context,
    parentContext: CompositionContext?,
    private val compositeKeyHash: Int,
    private val dispatcher: NestedScrollDispatcher,
    val view: View,
    private val owner: Owner,
) : /*NestedScrollingParent3,*/ ComposeNodeLifecycleCallback, OwnerScope {

    var self: ViewGroup
    inner class AndroidViewHolderViewGroup : ViewGroup() {
        override fun onMeasure(widthMeasureSpec: Int, _1: Int) {
            val heightMeasureSpec = _1
            if (view.parent !== this) {
                setMeasuredDimension(
                    MeasureSpec.getSize(widthMeasureSpec),
                    MeasureSpec.getSize(heightMeasureSpec)
                )
                return
            }
            if (view.visibility == View.GONE) {
                setMeasuredDimension(0, 0)
                return
            }

            view.measure(widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(view.measuredWidth, view.measuredHeight)
            lastWidthMeasureSpec = widthMeasureSpec
            lastHeightMeasureSpec = heightMeasureSpec
        }

        override fun layoutL(l: int32_t, t: int32_t, r: int32_t, b: int32_t) {
            view.layout(0, 0, r - l, b - t)
        }

        override fun getLayoutParams(): TIOSKHViewGroupLayoutParams? {
            return (view.layoutParams
                ?: LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }

        //TODO:Topping
        /*override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            onRequestDisallowInterceptTouchEvent?.invoke(disallowIntercept)
            super.requestDisallowInterceptTouchEvent(disallowIntercept)
        }*/

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            runUpdate()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            // remove all observations:
            snapshotObserver.clear(this)
        }

        // When there is no hardware acceleration invalidates are intercepted using this method,
        // otherwise using onDescendantInvalidated. Return null to avoid invalidating the
        // AndroidComposeView or the handler.
        //@Suppress("Deprecation")
        //TODO:Topping
        /*override fun invalidateChildInParent(location: IntArray?, dirty: Rect?): LGView? {
            super.invalidateChildInParent(location, dirty)
            invalidateOrDefer()
            return null
        }*/

        //TODO:Topping
        /*override fun onDescendantInvalidated(child: View, target: View) {
            // We need to call super here in order to correctly update the dirty flags of the holder.
            super.onDescendantInvalidated(child, target)
            invalidateOrDefer()
        }*/
    }

    init {
        self = AndroidViewHolderViewGroup()
        self.kParentType = this
        self.init(context, null)
        self.lc = context
        // Any [Abstract]ComposeViews that are descendants of this view will host
        // subcompositions of the host composition.
        // UiApplier doesn't supply this, only AndroidView.
        parentContext?.let {
            self.compositionContext = it
        }
        // We save state ourselves, depending on composition.
        //isSaveFromParentEnabled = false

        @Suppress("LeakingThis")
        self.addView(view)
    }

    // Keep nullable to match the `expect` declaration of InteropViewFactoryHolder
    @Suppress("RedundantNullableReturnType")
    fun getInteropView(): InteropView? = view

    /**
     * The update logic of the [View].
     */
    var update: () -> Unit = {}
        protected set(value) {
            field = value
            hasUpdateBlock = true
            runUpdate()
        }
    private var hasUpdateBlock = false

    var reset: () -> Unit = {}
        protected set

    var release: () -> Unit = {}
        protected set

    /**
     * The modifier of the `LayoutNode` corresponding to this [View].
     */
    var modifier: Modifier = Modifier
        set(value) {
            if (value !== field) {
                field = value
                onModifierChanged?.invoke(value)
            }
        }

    internal var onModifierChanged: ((Modifier) -> Unit)? = null

    /**
     * The screen density of the layout.
     */
    var density: Density = Density(1f)
        set(value) {
            if (value !== field) {
                field = value
                onDensityChanged?.invoke(value)
            }
        }

    internal var onDensityChanged: ((Density) -> Unit)? = null

    /** Sets the ViewTreeLifecycleOwner for this view. */
    var lifecycleOwner: LifecycleOwner? = null
        set(value) {
            if (value !== field) {
                field = value
                self.setViewTreeLifecycleOwner(value)
            }
        }

    /** Sets the ViewTreeSavedStateRegistryOwner for this view. */
    var savedStateRegistryOwner: SavedStateRegistryOwner? = null
        set(value) {
            if (value !== field) {
                field = value
                self.setViewTreeSavedStateRegistryOwner(value)
            }
        }

    /**
     * The [OwnerSnapshotObserver] of this holder's [Owner]. Will be null when this view is not
     * attached, since the observer is not valid unless the view is attached.
     */
    private val snapshotObserver: OwnerSnapshotObserver
        get() {
            check(self.isAttachedToWindow) {
                "Expected AndroidViewHolder to be attached when observing reads."
            }
            return owner.snapshotObserver
        }

    private val runUpdate: () -> Unit = {
        // If we're not attached, the observer isn't started, so don't bother running it.
        // onAttachedToWindow will run an update the next time the view is attached.
        if (hasUpdateBlock && self.isAttachedToWindow) {
            snapshotObserver.observeReads(this, OnCommitAffectingUpdate, update)
        }
    }

    private val runInvalidate: () -> Unit = {
        layoutNode.invalidateLayer()
    }

    internal var onRequestDisallowInterceptTouchEvent: ((Boolean) -> Unit)? = null

    private val location = IntArray(2)

    private var lastWidthMeasureSpec: Int = Unmeasured
    private var lastHeightMeasureSpec: Int = Unmeasured

    /*private val nestedScrollingParentHelper: NestedScrollingParentHelper =
        NestedScrollingParentHelper(this)*/

    private var isDrawing = false

    override val isValidOwnerScope: Boolean
        get() = self.isAttachedToWindow

    override fun onReuse() {
        // We reset at the same time we remove the view. So if the view was removed, we can just
        // re-add it and it's ready to go. If it's already attached, we didn't reset it and need
        // to do so for it to be reused correctly.
        if (view.parent !== this) {
            self.addView(view)
        } else {
            reset()
        }
    }

    override fun onDeactivate() {
        reset()
        self.removeAllViewsInLayout()
    }

    override fun onRelease() {
        release()
    }

    fun remeasure() {
        if (lastWidthMeasureSpec == Unmeasured || lastHeightMeasureSpec == Unmeasured) {
            // This should never happen: it means that the views handler was measured without
            // the AndroidComposeView having been measured.
            return
        }
        self.measure(lastWidthMeasureSpec, lastHeightMeasureSpec)
    }

    fun invalidateOrDefer() {
        if (isDrawing) {
            // If an invalidation occurs while drawing invalidate until next frame to avoid
            // redrawing multiple times during the same frame the same content.
            view.postOnAnimation(runInvalidate)
        } else {
            // when not drawing, we can invalidate any time and not risk multiple draws, we don't
            // defer to avoid waiting a full frame to draw content.
            layoutNode.invalidateLayer()
        }
    }

    /**
     * A [LayoutNode] tree representation for this Android [View] holder.
     * The [LayoutNode] will proxy the Compose core calls to the [View].
     */
    val layoutNode: LayoutNode = run {
        // Prepare layout node that proxies measure and layout passes to the View.
        val layoutNode = LayoutNode()
        @OptIn(InternalComposeUiApi::class)
        layoutNode.interopViewFactoryHolder = this@AndroidViewHolder

        val coreModifier = Modifier
            .nestedScroll(NoOpScrollConnection, dispatcher)
            .semantics(true) {}
            .pointerInteropFilter(this)
            .drawBehind {
                drawIntoCanvas { canvas ->
                    if (view.visibility != View.GONE) {
                        isDrawing = true
                        (layoutNode.owner as? AndroidComposeView)
                            ?.drawAndroidView(this@AndroidViewHolder, canvas.nativeCanvas)
                        isDrawing = false
                    }
                }
            }
            .onGloballyPositioned {
                // The global position of this LayoutNode can change with it being replaced. For
                // these cases, we need to inform the View.
                self.layoutAccordingTo(layoutNode)
            }
        layoutNode.compositeKeyHash = compositeKeyHash
        layoutNode.modifier = modifier.then(coreModifier)
        onModifierChanged = { layoutNode.modifier = it.then(coreModifier) }

        layoutNode.density = density
        onDensityChanged = { layoutNode.density = it }

        layoutNode.onAttach = { owner ->
            (owner as? AndroidComposeView)?.addAndroidView(this, layoutNode)
            if (view.parent !== this) self.addView(view)
        }
        layoutNode.onDetach = { owner ->
            (owner as? AndroidComposeView)?.removeAndroidView(this)
            self.removeAllViewsInLayout()
        }

        layoutNode.measurePolicy = object : MeasurePolicy {
            override fun MeasureScope.measure(
                measurables: List<Measurable>,
                constraints: Constraints
            ): MeasureResult {
                if (self.childCount == 0) {
                    return layout(constraints.minWidth, constraints.minHeight) {}
                }

                if (constraints.minWidth != 0) {
                    self.getChildAt(0)!!.minimumWidth = constraints.minWidth
                }
                if (constraints.minHeight != 0) {
                    self.getChildAt(0)!!.minimumHeight = constraints.minHeight
                }

                self.measure(
                    obtainMeasureSpec(
                        constraints.minWidth,
                        constraints.maxWidth,
                        self.layoutParams!!.width
                    ),
                    obtainMeasureSpec(
                        constraints.minHeight,
                        constraints.maxHeight,
                        self.layoutParams!!.height
                    )
                )
                return layout(self.measuredWidth, self.measuredHeight) {
                    self.layoutAccordingTo(layoutNode)
                }
            }

            override fun IntrinsicMeasureScope.minIntrinsicWidth(
                measurables: List<IntrinsicMeasurable>,
                height: Int
            ) = intrinsicWidth(height)

            override fun IntrinsicMeasureScope.maxIntrinsicWidth(
                measurables: List<IntrinsicMeasurable>,
                height: Int
            ) = intrinsicWidth(height)

            private fun intrinsicWidth(height: Int): Int {
                self.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    obtainMeasureSpec(0, height, self.layoutParams!!.height)
                )
                return self.measuredWidth
            }

            override fun IntrinsicMeasureScope.minIntrinsicHeight(
                measurables: List<IntrinsicMeasurable>,
                width: Int
            ) = intrinsicHeight(width)

            override fun IntrinsicMeasureScope.maxIntrinsicHeight(
                measurables: List<IntrinsicMeasurable>,
                width: Int
            ) = intrinsicHeight(width)

            private fun intrinsicHeight(width: Int): Int {
                self.measure(
                    obtainMeasureSpec(0, width, self.layoutParams!!.width),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                return self.measuredHeight
            }
        }
        layoutNode
    }

    /**
     * Intersects [Constraints] and [View] LayoutParams to obtain the suitable [View.MeasureSpec]
     * for measuring the [View].
     */
    private fun obtainMeasureSpec(
        min: Int,
        max: Int,
        preferred: Int
    ): Int = when {
        preferred >= 0 || min == max -> {
            // Fixed size due to fixed size layout param or fixed constraints.
            MeasureSpec.makeMeasureSpec(preferred.coerceIn(min, max), MeasureSpec.EXACTLY)
        }
        preferred == LayoutParams.WRAP_CONTENT && max != Constraints.Infinity -> {
            // Wrap content layout param with finite max constraint. If max constraint is infinite,
            // we will measure the child with UNSPECIFIED.
            MeasureSpec.makeMeasureSpec(max, MeasureSpec.AT_MOST)
        }
        preferred == LayoutParams.MATCH_PARENT && max != Constraints.Infinity -> {
            // Match parent layout param, so we force the child to fill the available space.
            MeasureSpec.makeMeasureSpec(max, MeasureSpec.EXACTLY)
        }
        else -> {
            // max constraint is infinite and layout param is WRAP_CONTENT or MATCH_PARENT.
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        }
    }

    // TODO: b/203141462 - consume whether the AndroidView() is inside a scrollable container, and
    //  use that to set this. In the meantime set true as the defensive default.
    fun shouldDelayChildPressedState(): Boolean = true

    // NestedScrollingParent3
    //TODO ?
    /*override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0 ||
            (axes and ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0
    }

    override fun getNestedScrollAxes(): Int {
        return nestedScrollingParentHelper.nestedScrollAxes
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        nestedScrollingParentHelper.onStopNestedScroll(target, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (!isNestedScrollingEnabled) return
        val consumedByParent =
            dispatcher.dispatchPostScroll(
                consumed = Offset(dxConsumed.toComposeOffset(), dyConsumed.toComposeOffset()),
                available = Offset(dxUnconsumed.toComposeOffset(), dyUnconsumed.toComposeOffset()),
                source = toNestedScrollSource(type)
            )
        consumed[0] = composeToViewOffset(consumedByParent.x)
        consumed[1] = composeToViewOffset(consumedByParent.y)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        if (!isNestedScrollingEnabled) return
        dispatcher.dispatchPostScroll(
            consumed = Offset(dxConsumed.toComposeOffset(), dyConsumed.toComposeOffset()),
            available = Offset(dxUnconsumed.toComposeOffset(), dyUnconsumed.toComposeOffset()),
            source = toNestedScrollSource(type)
        )
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (!isNestedScrollingEnabled) return
        val consumedByParent =
            dispatcher.dispatchPreScroll(
                available = Offset(dx.toComposeOffset(), dy.toComposeOffset()),
                source = toNestedScrollSource(type)
            )
        consumed[0] = composeToViewOffset(consumedByParent.x)
        consumed[1] = composeToViewOffset(consumedByParent.y)
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        if (!isNestedScrollingEnabled) return false
        val viewVelocity = Velocity(velocityX.toComposeVelocity(), velocityY.toComposeVelocity())
        dispatcher.coroutineScope.launch {
            if (!consumed) {
                dispatcher.dispatchPostFling(
                    consumed = Velocity.Zero,
                    available = viewVelocity
                )
            } else {
                dispatcher.dispatchPostFling(
                    consumed = viewVelocity,
                    available = Velocity.Zero
                )
            }
        }
        return false
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        if (!isNestedScrollingEnabled) return false
        val toBeConsumed = Velocity(velocityX.toComposeVelocity(), velocityY.toComposeVelocity())
        dispatcher.coroutineScope.launch {
            dispatcher.dispatchPreFling(toBeConsumed)
        }
        return false
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return view.isNestedScrollingEnabled
    }*/

    companion object {
        private val OnCommitAffectingUpdate: (AndroidViewHolder) -> Unit = {
            it.self.postRunnable(object : NSObject(), TIOSKHTRunnableProtocol {
                override fun run() {
                    it.runUpdate.invoke()
                }
            })
            //it.handler.post(it.runUpdate)
        }
    }
}

private fun View.layoutAccordingTo(layoutNode: LayoutNode) {
    val position = layoutNode.coordinates.positionInRoot()
    val x = position.x.roundToInt()
    val y = position.y.roundToInt()
    layout(x, y, x + measuredWidth, y + measuredHeight)
}

private const val Unmeasured = Int.MIN_VALUE

/**
 * No-op Connection required by nested scroll modifier. This is No-op because we don't want
 * to influence nested scrolling with it and it is required by [Modifier.nestedScroll].
 */
private val NoOpScrollConnection = object : NestedScrollConnection {}

/*private fun Int.toComposeOffset() = toFloat() * -1

private fun Float.toComposeVelocity(): Float = this * -1f

private fun toNestedScrollSource(type: Int): NestedScrollSource = when (type) {
    ViewCompat.TYPE_TOUCH -> NestedScrollSource.Drag
    else -> NestedScrollSource.Fling
}*/
