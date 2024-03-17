/*
 * Copyright 2023 The Android Open Source Project
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

import android.os.Handler
import android.os.Looper
import androidx.collection.SparseArrayCompat
import androidx.collection.forEach
import androidx.collection.set
import androidx.compose.ui.util.ID
import cocoapods.ToppingCompose.ComponentCallbacksProtocol
import cocoapods.ToppingCompose.DisplayMetrics
import cocoapods.ToppingCompose.GRAVITY_BOTTOM
import cocoapods.ToppingCompose.GRAVITY_END
import cocoapods.ToppingCompose.GRAVITY_START
import cocoapods.ToppingCompose.GRAVITY_TOP
import cocoapods.ToppingCompose.Gravity
import cocoapods.ToppingCompose.LGColorParser
import cocoapods.ToppingCompose.LGColorState
import cocoapods.ToppingCompose.LGDrawableParser
import cocoapods.ToppingCompose.LGDrawableReturn
import cocoapods.ToppingCompose.LGFontParser
import cocoapods.ToppingCompose.LGFontReturn
import cocoapods.ToppingCompose.LGValueParser
import cocoapods.ToppingCompose.LGView
import cocoapods.ToppingCompose.LGViewGroup
import cocoapods.ToppingCompose.LGXmlParser
import cocoapods.ToppingCompose.Lifecycle
import cocoapods.ToppingCompose.LifecycleOwnerProtocol
import cocoapods.ToppingCompose.LuaBundle
import cocoapods.ToppingCompose.LuaColor
import cocoapods.ToppingCompose.LuaComponentDialog
import cocoapods.ToppingCompose.LuaContext
import cocoapods.ToppingCompose.LuaForm
import cocoapods.ToppingCompose.LuaMenu
import cocoapods.ToppingCompose.LuaResource
import cocoapods.ToppingCompose.MeasureSpec
import cocoapods.ToppingCompose.SavedStateProviderProtocol
import cocoapods.ToppingCompose.SavedStateRegistry
import cocoapods.ToppingCompose.SavedStateRegistryOwnerProtocol
import cocoapods.ToppingCompose.TIOSKHKotlinArray
import cocoapods.ToppingCompose.TIOSKHKotlinIntArray
import cocoapods.ToppingCompose.TIOSKHMotionEvent
import cocoapods.ToppingCompose.TIOSKHMotionEventCompanion
import cocoapods.ToppingCompose.TIOSKHMotionEventPointerCoords
import cocoapods.ToppingCompose.TIOSKHMotionEventPointerProperties
import cocoapods.ToppingCompose.TIOSKHSkikoClipMode
import cocoapods.ToppingCompose.TIOSKHTCanvasProtocol
import cocoapods.ToppingCompose.TIOSKHTDisplayMetrics
import cocoapods.ToppingCompose.TIOSKHViewGroupLayoutParams
import cocoapods.ToppingCompose.ToppingResources
import cocoapods.ToppingCompose.ViewModelStoreOwnerProtocol
import cocoapods.ToppingCompose.TIOSKHCoreXmlBufferedReader
import cocoapods.ToppingCompose.TIOSKHPoint
import cocoapods.ToppingCompose.TIOSKHRect
import cocoapods.ToppingCompose.TIOSKHTColorCompanion
import cocoapods.ToppingCompose.TIOSKHTResourcesProtocol
import cocoapods.ToppingCompose.TIOSKHTViewCompanion
import cocoapods.ToppingCompose.TIOSKHTypedValueCompanion
import cocoapods.ToppingCompose.TIOSKHXmlCompanion
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGDataProviderCreateWithCFData
import platform.CoreGraphics.CGFontCopyFullName
import platform.CoreGraphics.CGFontCreateWithDataProvider
import platform.CoreText.CTFontManagerRegisterGraphicsFont
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSArray
import platform.Foundation.NSAttributedString
import platform.Foundation.NSCoder
import platform.Foundation.NSCodingProtocol
import platform.Foundation.NSCopyingProtocol
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSJSONReadingMutableContainers
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSLocale
import platform.Foundation.NSMutableArray
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSRunLoop
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.allKeys
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.date
import platform.Foundation.stringWithFormat
import platform.Foundation.timeIntervalSince1970
import platform.QuartzCore.CADisplayLink
import platform.UIKit.NSTextRange
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIFontDescriptorTraitBold
import platform.UIKit.UIFontDescriptorTraitItalic
import platform.UIKit.UIKey
import platform.UIKit.UIScreen
import platform.UIKit.UIWindow
import platform.UIKit.systemFontSize
import platform.darwin.NSObject
import platform.posix.int32_t
import platform.posix.int64_t
import threading.ThreadLocal

object Build {
    object VERSION {
        const val SDK_INT = Int.MAX_VALUE
    }
    object VERSION_CODES {
        const val M = 21
        const val O = 26
        const val P = 27
        const val Q = 29
        const val R = 29
        const val S = 29
        const val TIRAMISU = 29
    }
}

annotation class RequiresApi(val api: Int)
annotation class JvmOverloads
annotation class SuppressWarnings(val value: String)
annotation class StyleableRes
annotation class DrawableRes
annotation class ColorRes
annotation class IntegerRes
annotation class ArrayRes
annotation class PluralsRes
annotation class BoolRes
annotation class DimenRes
annotation class StringRes
annotation class DoNotInline
annotation class JvmStatic
annotation class SuppressLint(vararg val value: String)

typealias AttributeSet = NSMutableDictionary
typealias Activity = LuaForm
typealias View = LGView
typealias Context = LuaContext
typealias ViewGroup = LGViewGroup
typealias LifecycleOwner = LifecycleOwnerProtocol
typealias SavedStateRegistryOwner = SavedStateRegistryOwnerProtocol
typealias ExtractedText = NSTextRange
typealias Outline = Rect
typealias Bundle = LuaBundle
typealias AndroidKeyEvent = UIKey

//////////////////////

fun LuaBundle.containsKey(key: String) : Boolean {
    return bundle?.objectForKey(key) != null
}

fun LuaBundle.putAll(key: LuaBundle) : Boolean {
    key.bundle?.allKeys()?.forEach { keyP ->
        val value = key.bundle?.objectForKey(keyP)
        value?.let {
            bundle?.setObject(value, keyP as NSCopyingProtocol)
        }
    }
    return bundle?.objectForKey(key) != null
}

operator fun LuaBundle.get(index: String): Any? {
    return bundle?.objectForKey(index)
}

//////////////////////

object FoldingFeature {
    var orientation = Orientation.VERTICAL
    var occlusionType = 1
    var isSeparating = false
    var state = 1
    var bounds = TIOSKHRect(0, 0, 0, 0)
    object Orientation {
        val VERTICAL = 0
        val HORIZONTAL = 1
    }

    object State {
        val HALF_OPENED = 0

    }

    object OcclusionType {
        val FULL = 0
    }
}

//////////////////////

class WindowMetricsCalculator {
    @OptIn(ExperimentalForeignApi::class)
    fun computeCurrentWindowMetrics(context: Context): TIOSKHRect {
        return context.form!!.view.frame.useContents {
            TIOSKHRect(this.origin.x.toInt(), this.origin.y.toInt(), (this.origin.x + this.size.width).toInt(), (this.origin.y + this.size.height).toInt())
        }
    }

    companion object {
        fun getOrCreate(): WindowMetricsCalculator {
            return WindowMetricsCalculator()
        }
    }
}

class DisplayFeature(val bounds: TIOSKHRect)

class WindowLayoutInfo constructor(
    val displayFeatures: List<DisplayFeature>
)

class WindowInfoTracker(val context: Context) {
    fun windowLayoutInfo(context: Context): Flow<WindowLayoutInfo> {
        return flow {
            emit(WindowLayoutInfo(listOf()))
        }
        /*val windowLayoutInfoFlow: Flow<WindowLayoutInfo>? = (context as? Activity)
            ?.let { activity -> windowLayoutInfo(activity.context!!) }
        return windowLayoutInfoFlow
            ?: throw NotImplementedError(
                message = "Must override windowLayoutInfo(context) and provide an implementation.")*/
    }

    companion object {
        fun getOrCreate(context: Context): WindowInfoTracker {
            return WindowInfoTracker(context)
        }
    }
}

//////////////////////

fun String.Companion.formatCommon(locale: NSLocale, raw: String, args: Array<out Any>): String {
    return raw
}

//////////////////////

val UIKey.metaState
    get() = modifierFlags.toInt()

//////////////////////

val LifecycleOwner.lifecycle
    get() = getLifecycle()

//////////////////////

val Lifecycle.currentState
    get() = getCurrentState()

///////////////////////

interface Serializable

///////////////////////

interface Binder

///////////////////////

class Magnifier(view: View)
{
    class Builder(val view: View) {
        fun setSize(roundToInt: Int, roundToInt1: Int) {

        }

        fun setCornerRadius(pixelCornerRadius: Float) {

        }

        fun setElevation(pixelElevation: Float) {

        }

        fun setInitialZoom(initialZoom: Float) {

        }

        fun setClippingEnabled(clippingEnabled: Boolean) {

        }

        fun build() : Magnifier {
            return Magnifier(view)
        }

    }

    val width = 0
    val height = 0
    var zoom = 0f

    fun update() {}
    fun show(x: Float, y: Float) {}
    fun dismiss() {}
    fun show(x: Float, y: Float, x1: Float, y1: Float) {}
}

///////////////////////

val SavedStateRegistryOwnerProtocol.savedStateRegistry
    get() = getSavedStateRegistry()

fun SavedStateRegistry.consumeRestoredStateForKey(key: String): Map<Any?, *>? {
    val bundle = consumeRestoredStateForKeyWithKey(key)
    if(bundle == null)
        return null
    return bundle.getObject("savedStateProvider") as Map<Any?, *>?
}

fun SavedStateRegistry.registerSavedStateProvider(key: String, provider: SavedStateProviderProtocol) {
    registerSavedStateProviderWithKey(key, provider)
}

fun SavedStateRegistry.registerSavedStateProvider(key: String, block: () -> Map<Any?, *>) {
    val sspp = object : NSObject(), SavedStateProviderProtocol {
        override fun saveState(): LuaBundle {
            val res = block()
            val bundle = LuaBundle()
            bundle.putObject("savedStateProvider", res)
            return bundle
        }
    }
    registerSavedStateProviderWithKey(key, sspp)
}

fun SavedStateRegistry.unregisterSavedStateProvider(key: String)
{
    unregisterSavedStateProvider(key)
}

///////////////////////

object AssetManager {

}

///////////////////////

class Typeface {
    var lfr: LGFontReturn? = null
    var font: UIFont? = null

    constructor(lfr: LGFontReturn) {
        this.lfr = lfr
    }

    constructor(font: UIFont?) {
        this.font = font
    }

    class Builder {

        var fontName = ""

        constructor() {

        }

        constructor(assetManager: AssetManager, path: String) {
            fontName = getFontFromPath(path)
        }

        constructor(file: File) {

        }

        fun setFontVariationSettings(variation: Any): Builder {
            return this
        }

        fun build(): Typeface? {
            return Typeface(UIFont.fontWithName(fontName, UIFont.systemFontSize))
        }
    }

    companion object {
        @OptIn(ExperimentalForeignApi::class)
        fun getFontFromPath(path: String) : String {
            var fontName = ""
            val arr = path.split("/")
            val sb = StringBuilder()
            for(i in 0 until arr.size - 1)
                sb.append(arr[i])
            val data = LuaResource.getResourceAsset(sb.toString(), arr.last()) as? NSData
            if(data != null) {
                val provider = CGDataProviderCreateWithCFData(data as CFDataRef)
                val fontRef = CGFontCreateWithDataProvider(provider)
                val cfFontName = CGFontCopyFullName(fontRef)
                fontName = CFBridgingRelease(cfFontName) as String
                CFRelease(cfFontName)
                CTFontManagerRegisterGraphicsFont(fontRef, null)
                CFRelease(provider)
                CFRelease(fontRef)
            }

            return fontName
        }

        fun create(fontFamily: String, type: Int): Typeface {
            return Typeface(UIFont.fontWithName(fontFamily, UIFont.systemFontSize))
        }

        fun create(typeface: Typeface, style: Int): Typeface {
            val font = typeface.font
            if(style == ITALIC) {
                val descriptor = font!!.fontDescriptor.fontDescriptorWithSymbolicTraits(
                    UIFontDescriptorTraitItalic)!!
                return Typeface(UIFont.fontWithDescriptor(descriptor, font.pointSize))
            } else if(style == BOLD) {
                val descriptor = font!!.fontDescriptor.fontDescriptorWithSymbolicTraits(
                    UIFontDescriptorTraitBold)!!
                return Typeface(UIFont.fontWithDescriptor(descriptor, font.pointSize))
            } else if(style == BOLD_ITALIC) {
                val descriptor = font!!.fontDescriptor.fontDescriptorWithSymbolicTraits(
                    UIFontDescriptorTraitBold or UIFontDescriptorTraitItalic)!!
                return Typeface(UIFont.fontWithDescriptor(descriptor, font.pointSize))
            }
            return typeface
        }

        fun create(typeface: Typeface, fontweight: Int, final: Boolean): Typeface {
            return typeface
        }

        fun createFromAsset(assetManager: AssetManager, path: String): Typeface? {
            return Builder(assetManager, path).build()
        }

        fun createFromFile(file: File): Typeface? {
            //TODO
            return Typeface(UIFont.systemFontOfSize(UIFont.systemFontSize))
        }

        fun defaultFromStyle(style: Int): Typeface {
            return create(Typeface(UIFont.systemFontOfSize(UIFont.systemFontSize)), style)
        }

        val NORMAL = 1
        val ITALIC = 2
        val BOLD = 4
        val BOLD_ITALIC = ITALIC and BOLD
        val DEFAULT = Typeface(UIFont.systemFontOfSize(UIFont.systemFontSize))
    }

}

///////////////////////

typealias File = NSData

///////////////////////

class DecodedValue(
    val value: Any
) : NSObject(), NSCodingProtocol {
    override fun encodeWithCoder(coder: NSCoder) {
        // no-op
    }

    override fun initWithCoder(coder: NSCoder): NSCodingProtocol? = null
}

interface Parcelable {
    fun coding(): NSCodingProtocol
}

class ParcelFileDescriptor {

    val fileDescriptor: File = NSData()
}

///////////////////////

object TimeUnit {
    object MILLISECONDS {
        fun toNanos(value: Long) : Long {
            return value * 1000000L
        }
    }
}

object System {
    fun nanoTime() : Long {
        return (NSDate.date().timeIntervalSince1970 * 1000000000).toLong()
    }
    fun timeInMillis() : Long {
        return (NSDate.date().timeIntervalSince1970 * 1000).toLong()
    }
}

object SystemClock {
    fun uptimeMillis() : Long {
        return (NSDate.date().timeIntervalSince1970 * 1000).toLong()
    }
}

///////////////////////

@OptIn(ExperimentalForeignApi::class)
fun <R> Function<R>.toLuaTranslator(obj: R?): cocoapods.ToppingCompose.LuaTranslator {
    val kt = KTWrap()
    val lt: cocoapods.ToppingCompose.LuaTranslator = cocoapods.ToppingCompose.LuaTranslator()
    lt.nobj = StableRef.create(kt).asCPointer()
    lt.kFRetF = kt.Init(obj, this)
    return lt
}

@OptIn(ExperimentalForeignApi::class)
fun <R> Function<R>?.toLuaTranslator(obj: R?): cocoapods.ToppingCompose.LuaTranslator? {
    if (this == null)
        return null
    val kt = KTWrap()
    val lt: cocoapods.ToppingCompose.LuaTranslator = cocoapods.ToppingCompose.LuaTranslator()
    lt.nobj = StableRef.create(kt).asCPointer()
    lt.kFRetF = kt.Init(obj, this)
    return lt
}

///////////////////////

object Log {
    fun d(tag: String, value: String) {
        println("$tag : $value")
    }

    fun w(tag: String, value: String) {
        println("$tag : $value")
    }

    fun w(tag: String, value: String, e: Exception) {
        println("$tag : $value + $e")
    }

    fun v(tag: String, value: String) {
        println("$tag : $value")
    }
}

///////////////////////

typealias Rect = TIOSKHRect

///////////////////////

class WindowManager(val window: UIWindow) {
    fun updateViewLayout(popupView: View, params: LayoutParams) {

    }

    fun addView(view: LGView, params: LayoutParams) {
        window.addView(view, params)
    }

    fun removeViewImmediate(view: LGView) {
        if(view.parent != null && view.parent is LGViewGroup)
            (view.parent!! as LGViewGroup).removeSubview(view)
        else
            view._view?.removeFromSuperview()
    }

    class LayoutParams : TIOSKHViewGroupLayoutParams() {
        var gravity = 0
        var x = 0
        var y = 0
    }
}

///////////////////////

val Gravity.Companion.START
    get() = GRAVITY_START.toInt()

val Gravity.Companion.TOP
    get() = GRAVITY_TOP.toInt()

val Gravity.Companion.BOTTOM
    get() = GRAVITY_BOTTOM.toInt()

val Gravity.Companion.END
    get() = GRAVITY_END.toInt()

///////////////////////

typealias ClipData = NSString

///////////////////////

val ToppingResources.configuration
    get() = getConfiguration()

val ToppingResources.displayMetrics
    get() = getDisplayMetrics()

val TIOSKHTDisplayMetrics.density
    get() = 1f//UIScreen.mainScreen().nativeScale.toFloat()

fun TIOSKHTResourcesProtocol.getIntArray(key: ID): IntArray {
    val v =  LGValueParser.getInstance()!!.getValue(key)
    if(v is NSArray) {
        val vOut = IntArray(v.count.toInt())
        for(i in 0 until v.count.toInt()) {
            vOut[i] = v.objectAtIndex(i.toULong()) as Int
        }
        return vOut
    }
    return IntArray(0)
}

val LuaContext.resources
    get() = getResources() as ToppingResources

val LuaContext.theme
    get() = Resources.Theme

val LuaContext.applicationContext
    get() = this

fun LuaContext.registerComponentCallbacks(callback: ComponentCallbacksProtocol) {
    this.componentCallbacks = callback
}

fun LuaContext.unregisterComponentCallbacks(callback: ComponentCallbacksProtocol) {
    this.componentCallbacks = null
}

///////////////////////

fun UIWindow.addView(view: LGView) {
    addSubview(view._view!!)
}

fun UIWindow.addView(view: LGView, params: WindowManager.LayoutParams) {
    //TODO: Params?
    addSubview(view._view!!)
}

///////////////////////

/*interface Runnable {
    fun run()
}*/

///////////////////////

object Display {
    val refreshRate
        get() = UIScreen.mainScreen.maximumFramesPerSecond.toFloat()
}

//////////////////////

val TIOSKHTResourcesProtocol.displayMetrics
    get() = DisplayMetrics

fun TIOSKHTResourcesProtocol.getValue(id: String, typedValue: TypedValue, resolveRefs: Boolean) {
    LGValueParser.getInstance()!!.getValue(id)
}

///////////////////////

typealias Point = TIOSKHPoint

fun TIOSKHPoint.set(x: Int, y: Int) {
    this.setX(x)
    this.setY(y)
}

///////////////////////

fun String.Companion.format(format: String, vararg args: Any?) : String {
    var returnString = ""
    val regEx = "%[\\d|.]*[sdfx]|[%]".toRegex()
    val singleFormats = regEx.findAll(format).map {
        it.groupValues.first()
    }.asSequence().toList()
    val newStrings = format.split(regEx)
    for (i in 0 until args.count()) {
        val arg = args[i]
        returnString += if(singleFormats[i] == "%s")
                NSString.stringWithFormat(newStrings[i] + "%@", args[i])
            else
                NSString.stringWithFormat(newStrings[i] + singleFormats[i], args[i])
    }

    return returnString
}

///////////////////////

fun LGViewGroup.createLayoutParams(): WindowManager.LayoutParams {
    return WindowManager.LayoutParams()
}

fun LGViewGroup.addView(view: View?) {
    if(view == null)
        return
    this.addSubview(view)
    view.componentAddMethod(_view, view._view)
}

fun LGViewGroup.addView(view: View?, params: TIOSKHViewGroupLayoutParams) {
    view?.layoutParams = params
    this.addView(view)
}

fun LGViewGroup.removeViewInLayout(view: LGView) {
    removeSubview(view)
}

fun LGViewGroup.removeAllViewsInLayout() {
    this.removeAllSubViews()
}

fun LGViewGroup.getChildAt(index: Int): LGView? {
    return getChildAtIndex(index) as LGView?
}

fun LGViewGroup.removeAllViews() {
    removeAllSubViews()
}

fun LGViewGroup.shouldDelayChildPressedState(): Boolean {
    return false
}

inline fun LGViewGroup.forEach(action: (view: View) -> Unit) {
    for (index in 0 until childCount) {
        val view = getChildAt(index)
        if(view != null)
            action(view)
    }
}

val LGViewGroup.children
    get() = subviews!!

val LGViewGroup.childCount
    get() = getChildCount()

val LGViewGroup.descendants: Sequence<View>
    get() = sequence {
        forEach { child ->
            yield(child)
            if (child is ViewGroup) {
                yieldAll(child.descendants)
            }
        }
    }

///////////////////////

fun LGView.init(context: LuaContext, attrs: AttributeSet?) {
    initProperties()
    attrs?.let { map ->
        map.allKeys.forEach { key ->
            val value = map.objectForKey(key)
            this.setAttributeValue(key.toString(), value.toString())
        }
    }
    applyStyles()
    beforeInitSubviews()
    beforeInitComponent()
    resize()
    if(parent != null)
        this.addSelfToParent(parent!!._view, lc!!.form)
    else {
        val view = this.createComponent()
        this.initComponent(view, context)
        this.setupComponent(view)
    }
}

fun LGView.setLayoutDirection(direction: Int) {

}

fun LGView.requestFocusFromTouch() : Boolean {
    return true
}

fun LGView.clearFocus() {
    _view?.resignFirstResponder()
}

fun LGView.setWillNotDraw(value: Boolean) {

}

fun LGView.getWindowVisibleDisplayFrame(outRect: Rect) {

}

fun LGView.getChildAt(index: Int) : LGView? {
    return getChildAtIndex(index) as LGView?
}

fun LGView.getLocationOnScreen(arr: IntArray) {
    val arrC = TIOSKHKotlinIntArray.arrayWithSize(arr.size)
    getLocationOnScreenTempLoc(arrC)
    for(i in 0 until arr.size)
    {
        arr[i] = arrC.getIndex(i)
    }
}

fun LGView.getLocationOnWindow(arr: IntArray) {
    val arrC = TIOSKHKotlinIntArray.arrayWithSize(arr.size)
    getLocationOnScreenTempLoc(arrC)
    for(i in 0 until arr.size)
    {
        arr[i] = arrC.getIndex(i)
    }
}

fun LGView.setViewTreeLifecycleOwner(owner: LifecycleOwner?) {
    setTag("R.id.view_tree_lifecycle_owner", owner as NSObject?)
}

fun LGView.findViewTreeSavedStateRegistryOwner(): SavedStateRegistryOwner? {
    return generateSequence(this) { view ->
        view.parent as? View
    }.mapNotNull { view ->
        view.getTag("R.id.view_tree_saved_state_registry_owner") as? SavedStateRegistryOwner
    }.firstOrNull()
}

fun LGView.setViewTreeSavedStateRegistryOwner(owner: SavedStateRegistryOwner?) {
    setTag("R.id.view_tree_saved_state_registry_owner", owner as NSObject?)
}

fun LGView.setViewTreeViewModelStoreOwner(viewModelStoreOwner: ViewModelStoreOwnerProtocol?) {
    setTag("R.id.view_tree_view_model_store_owner", viewModelStoreOwner as NSObject?)
}

fun LGView.findViewTreeViewModelStoreOwner(): ViewModelStoreOwnerProtocol? {
    return generateSequence(this) { view ->
        view.parent as? View
    }.mapNotNull { view ->
        view.getTag("R.id.view_tree_view_model_store_owner") as? ViewModelStoreOwnerProtocol
    }.firstOrNull()
}

fun LGView.post(runnable: Runnable) {
    handler.post(runnable)
}

fun LGView.removeCallbacks(runnable: Runnable) {
    handler.removeCallbacks(runnable)
}

fun LGView.offsetLeftAndRight(value: Int) {
    //TODO:Check this
    dX += value
    dWidth += value
    _view?.setNeedsLayout()
}

fun LGView.offsetTopAndBottom(value: Int) {
    //TODO:Check this
    dY += value
    dHeight += value
    dHeight += value
    _view?.setNeedsLayout()
}

val LGView.rootView: LGView
    get() {
        var parentView = this
        while (parentView.parent != null && parentView.parent is View) {
            parentView = parentView.parent!!
        }

        return parentView
    }

val LGView.drawingTime
    get() = 0L

val LGView.ancestors: Sequence<LGView>
    get() = generateSequence(parent) {
        it.parent
    }

val LGView.allViews: Sequence<View>
    get() = sequence {
        yield(this@allViews)
        if (this@allViews is ViewGroup) {
            yieldAll(this@allViews.descendants)
        }
    }

val LGView.isHardwareAccelerated: Boolean
    get() = true

//TODO(ios fix
var LGView.isFocusable: Boolean
    get() {
        return _view!!.canBecomeFocused()
    }
    set(value) {}

var LGView.clipToOutline: Boolean
    get() = false
    set(value) {}

var LGView.isLayoutRequested: Boolean
    get() = layoutRequested
    set(value) {
        layoutRequested = value
    }

val LGView.handler: Handler
    get() {
        if(lc?.form?.lgview == null)
            return Handler(Looper.getMainLooper())

        var handler: Handler? = lc!!.form!!.lgview!!.getTag("topping_handler") as Handler?
        if(handler == null)
        {
            handler = Handler(Looper.getMainLooper())
            lc!!.form!!.lgview!!.setTag("topping_handler", handler)
        }

        return handler
    }

val LGView.left
    get() = getLeft()

val LGView.top
    get() = getTop()

val LGView.right
    get() = getRight()

val LGView.bottom
    get() = getBottom()

var LGView.pivotX
    get() = getPivotX()
    set(value) {
        setPivotXValue(value)
    }

var LGView.pivotY
    get() = getPivotY()
    set(value) {
        setPivotYValue(value)
    }

var LGView.scaleX
    get() = getScaleX()
    set(value) {
        setScaleXValue(value)
    }

var LGView.scaleY
    get() = getScaleY()
    set(value) {
        setScaleYValue(value)
    }

var LGView.translationX
    get() = getTranslationX()
    set(value) {
        setTranslationXValue(value)
    }

var LGView.translationY
    get() = getTranslationY()
    set(value) {
        setTranslationYValue(value)
    }

var LGView.elevation
    get() = getTranslationZ()
    set(value) {
        setTranslationZValue(value)
    }

var LGView.alpha
    get() = getAlpha()
    set(value) {
        setAlphaValue(value)
    }

var LGView.rotation
    get() = getRotation_()
    set(value) {
        setRotationValue(value)
    }

var LGView.rotationX
    get() = getRotationX()
    set(value) {
        setRotationXValue(value)
    }

var LGView.rotationY
    get() = getRotationY()
    set(value) {
        setRotationYValue(value)
    }

val LGView.display: Display
    get() {
        return Display
    }

val LGView.windowVisibility: Int
    get() {
        return visibility
    }

val LGView.isInEditMode
    get() = isInEditMode()

val LGView.isInTouchMode
    get() = true

var LGView.id
    get() = GetId()
    set(value) {
        lua_id = value
    }

val LGView.windowManager: WindowManager
    get() {
        return WindowManager(_view!!.window!!)
    }

var LGView.context
    get() = lc!!
    set(value) {
        lc = value
    }

var LGView.paddingLeft
    get() = dPaddingLeft
    set(value) {
        dPaddingLeft = value
    }

var LGView.paddingRight
    get() = dPaddingRight
    set(value) {
        dPaddingRight = value
    }

var LGView.paddingTop
    get() = dPaddingTop
    set(value) {
        dPaddingTop = value
    }

var LGView.paddingBottom
    get() = dPaddingBottom
    set(value) {
        dPaddingBottom = value
    }

var LGView.measuredWidth
    get() = dWidth
    set(value) {
        dWidth = value
    }

var LGView.measuredHeight
    get() = dHeight
    set(value) {
        dHeight = value
    }

val LGView.isAttachedToWindow
    get() = isAttachedToWindow()

var LGView.visibility : Int
    get() = getVisibility().toInt()
    set(value) {
        setVisibility(value.toLong())
    }

var LGView.layoutParams
    get() = kLayoutParams
    set(value) {
        kLayoutParams = value
    }

var LGView.layoutDirection
    get() = context.resources.configuration.getLayoutDirection()
    set(value) {

    }

var LGView.width
    get() = dWidth
    set(value) {
        dWidth = value
    }

var LGView.height
    get() = dHeight
    set(value) {
        dHeight = value
    }

var LGView.minimumWidth
    get() = dWidthMin
    set(value) {
        dWidthMin = value
    }

var LGView.minimumHeight
    get() = dHeightMin
    set(value) {
        dHeightMin = value
    }

var LGView.scrollX
    get() = mScrollX
    set(value) {
        mScrollX = value
    }

var LGView.scrollY
    get() = mScrollY
    set(value) {
        mScrollY = value
    }

val LGView.Companion.VISIBLE
    get() = TIOSKHTViewCompanion.shared().VISIBLE()

val LGView.Companion.INVISIBLE
    get() = TIOSKHTViewCompanion.shared().INVISIBLE()

val LGView.Companion.GONE
    get() = TIOSKHTViewCompanion.shared().GONE()

//////////////////////

val LuaComponentDialog.context
    get() = this.controller!!.context()!!

//////////////////////

val MeasureSpec.Companion.MODE_SHIFT
    get() = 30
val MeasureSpec.Companion.MODE_MASK
    get() = 0x3 shl MODE_SHIFT
val MeasureSpec.Companion.UNSPECIFIED
    get() = 0 shl MODE_SHIFT
val MeasureSpec.Companion.EXACTLY
    get() = 1 shl MODE_SHIFT
val MeasureSpec.Companion.AT_MOST
    get() = 2 shl MODE_SHIFT
val MeasureSpec.Companion.MEASURED_STATE_TOO_SMALL
    get() = 0x01000000
val MeasureSpec.Companion.MEASURED_SIZE_MASK
    get() = 0x00ffffff
val MeasureSpec.Companion.MEASURED_STATE_MASK
    get() = 0xff000000
val MeasureSpec.Companion.MEASURED_HEIGHT_STATE_SHIFT
    get() = 16

//////////////////////

typealias LayoutParams = TIOSKHViewGroupLayoutParams

//TODO Remove the hardcode
val TIOSKHViewGroupLayoutParams.Companion.WRAP_CONTENT: Int
    get() {
        return -2
    }
val TIOSKHViewGroupLayoutParams.Companion.MATCH_PARENT: Int
    get() {
        return -1
    }

var LayoutParams.width
    get() = width()
    set(value) {
        setWidth(value)
    }

var LayoutParams.height
    get() = height()
    set(value) {
        setHeight(value)
    }

//////////////////////

typealias XmlPullParser = TIOSKHCoreXmlBufferedReader
typealias XmlResourceParser = TIOSKHCoreXmlBufferedReader

val TIOSKHCoreXmlBufferedReader.Companion.START_DOCUMENT
    get() = cocoapods.ToppingCompose.TIOSKHCoreEventType.startDocument()

val TIOSKHCoreXmlBufferedReader.Companion.END_DOCUMENT
    get() = cocoapods.ToppingCompose.TIOSKHCoreEventType.endDocument()

val TIOSKHCoreXmlBufferedReader.Companion.END_TAG
    get() = cocoapods.ToppingCompose.TIOSKHCoreEventType.endElement()

val TIOSKHCoreXmlBufferedReader.Companion.START_TAG
    get() = cocoapods.ToppingCompose.TIOSKHCoreEventType.startElement()

val TIOSKHCoreXmlBufferedReader.depth
    get() = depth()

val TIOSKHCoreXmlBufferedReader.eventType
    get() = eventType()

val TIOSKHCoreXmlBufferedReader.name
    get() = localName()

typealias XmlPullParserException = Exception

//////////////

abstract class Spanned : CharSequence {
    companion object {
        val SPAN_INCLUSIVE_INCLUSIVE = 0
        val SPAN_INCLUSIVE_EXCLUSIVE = 1
        val SPAN_EXCLUSIVE_INCLUSIVE = 2
        val SPAN_EXCLUSIVE_EXCLUSIVE = 3
    }

    inner class SpanData(
        val flag: Int,
        val type: Any,
        val start: Int,
        val end: Int
    )

    val arr = MutableList<MutableList<SpanData>>(length) {
        mutableListOf()
    }

    fun setSpan(span: Span, start: Int, end: Int, flag: Int) {
        for(i in start until end) {
            val sd = SpanData(flag, span, start, end)
            span.setSpanData(sd)
            arr[i].add(sd)
        }
    }

    inline fun <reified T> getSpans(start: Int, length: Int, type: Any): List<T> {
        val result = mutableListOf<T>()
        for(i in start until length) {
            arr[i].forEach {
                if(it.type is T) {
                    result.add(it.type)
                }
            }
        }
        return result
    }

    fun getSpanStart(span: Span): Int {
        return span.spanData.start
    }

    fun getSpanEnd(span: Span): Int {
        return span.spanData.end
    }
}

abstract class Span {
    lateinit var spanData: Spanned.SpanData

    fun setSpanData(sd: Spanned.SpanData) {
        this.spanData = sd
    }
}

class TextAnnotation constructor(
    key: String,
    value: String,
) : Span() {
    private var mKey: String
    private var mValue: String

    init {
        mKey = key
        mValue = value
    }

    val key
        get() = mKey
    val value
        get() = mValue

    fun getKey() : String { return mKey }
    fun getValue() : String { return mValue }
}

class SpannableString(var text: String) : Spanned() {
    var attrStr: NSAttributedString = NSAttributedString.create(text)

    override val length: Int
        get() = text.length

    override fun get(index: Int): Char {
        return text[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return text.subSequence(startIndex, endIndex)
    }
}

//////////////

@OptIn(ExperimentalForeignApi::class)
fun decodeJson(json: String): MutableMap<String, Any?>? {
    val str: NSString = (json as NSString)
    val data = str.dataUsingEncoding(NSUTF8StringEncoding)!!
    val result = NSJSONSerialization.JSONObjectWithData(data, NSJSONReadingMutableContainers, null) as Map<String, Any?>?
    return result?.toMutableMap()
}

@OptIn(ExperimentalForeignApi::class)
fun encodeJson(obj: Any): String? {
    val str = NSJSONSerialization.dataWithJSONObject(
        obj,
        NSJSONWritingPrettyPrinted,
        null
    )
    return str?.let {
        NSString.create(
            str, NSUTF8StringEncoding
        ) as String
    }
}

class Parcel private constructor() {
    var arr = NSMutableArray()

    private val ROOT = "root"
    private val HEADER_BYTE = 0
    private val HEADER_INT = 1
    private val HEADER_FLOAT = 2
    private val HEADER_LONG = 3
    private val HEADER_STRING = 4
    private var position = 0UL

    companion object {
        fun obtain() : Parcel {
            return Parcel()
        }
    }

    fun recycle() {}

    fun marshall(): ByteArray {
        val dict = NSMutableDictionary()
        dict.setObject(arr, ROOT as NSString)
        return encodeJson(dict)?.encodeToByteArray() ?: ByteArray(0)
    }

    fun unmarshall(bytes: ByteArray, start: Int, size: Int) {
        val dict = decodeJson(bytes.decodeToString(start, size))
        if (dict != null && dict[ROOT] != null) {
            arr = dict[ROOT] as NSMutableArray
        }
    }

    fun writeByte(byte: Byte) {
        arr.addObject(HEADER_BYTE)
        arr.addObject(byte)
    }

    fun writeInt(int: Int) {
        arr.addObject(HEADER_INT)
        arr.addObject(int)
    }

    fun writeFloat(float: Float) {
        arr.addObject(HEADER_FLOAT)
        arr.addObject(float)
    }

    fun writeLong(long: Long) {
        arr.addObject(HEADER_LONG)
        arr.addObject(long)
    }

    fun writeString(string: String) {
        arr.addObject(HEADER_STRING)
        arr.addObject(string)
    }

    fun setDataPosition(position: Int) {
        this.position = position.toULong() * 2UL
    }

    fun readByte(): Byte {
        val header = arr.objectAtIndex(position)
        val value = arr.objectAtIndex(position + 1UL)
        position += 2UL
        return value as Byte
    }

    fun readInt(): Int {
        val header = arr.objectAtIndex(position)
        val value = arr.objectAtIndex(position + 1UL)
        position += 2UL
        return value as Int
    }

    fun readLong(): Long {
        val header = arr.objectAtIndex(position)
        val value = arr.objectAtIndex(position + 1UL)
        position += 2UL
        return value as Long
    }

    fun readFloat(): Float {
        val header = arr.objectAtIndex(position)
        val value = arr.objectAtIndex(position + 1UL)
        position += 2UL
        return value as Float
    }

    fun readString(): String? {
        val header = arr.objectAtIndex(position)
        val value = arr.objectAtIndex(position + 1UL)
        position += 2UL
        return value as String
    }

    fun dataAvail(): Int {
        return (arr.count - position).toInt()
    }
}

//////////////

typealias SparseLongArray = SparseArrayCompat<Long>
typealias SparseBooleanArray = SparseArrayCompat<Boolean>

fun SparseLongArray.delete(key: Int) {
    remove(key)
}

fun SparseBooleanArray.delete(key: Int) {
    remove(key)
}

fun <T> SparseArrayCompat<T>.clone(): SparseArrayCompat<T> {
    val newArr = SparseArrayCompat<T>(size())

    this.forEach { key, value ->
        newArr[key] = value
    }

    return newArr
}

//////////////

typealias Locale = NSLocale

//////////////

typealias UUID = NSUUID

fun NSUUID.Companion.randomUUID(): NSUUID {
    return UUID()
}

//////////////

//TODO(ios fetch proper vsync clock)
class AnimationUtils {
    companion object {
        fun currentAnimationTimeMillis(): Long {
            return SystemClock.uptimeMillis()
        }
    }
}

//////////////

typealias MotionEvent = TIOSKHMotionEvent

typealias PointerProperties = TIOSKHMotionEventPointerProperties
typealias PointerCoords = TIOSKHMotionEventPointerCoords

fun TIOSKHMotionEvent.Companion.obtain(
    downTime: Long,
    eventTime: Long,
    action: Int,
    f1: Float,
    f2: Float,
    actionV: Int
) : MotionEvent {
    return TIOSKHMotionEventCompanion.shared().obtainDownTime(downTime, eventTime, action, f1, f2, actionV)
}

fun TIOSKHMotionEvent.Companion.obtainNoHistory(
    other : MotionEvent
) : MotionEvent {
    return TIOSKHMotionEventCompanion.shared().obtainNoHistoryOther(other)
}

fun TIOSKHMotionEvent.Companion.obtain(
    downTime: Long,
    eventTime: Long,
    action: Int,
    pointerCount: Int,
    pointerProperties: Array<PointerProperties>,
    pointerCoords: Array<PointerCoords>,
    metaState: Int,
    buttonState: Int,
    xPrecision: Float,
    yPrecision: Float,
    edgeFlags: Int,
    source: Int,
    flags: Int
) : MotionEvent {
    val pointerPropertiesConverted = TIOSKHKotlinArray.arrayWithSize(pointerCount) {
        pointerProperties[it!!.intValue]
    }
    val pointerCoordsConverted = TIOSKHKotlinArray.arrayWithSize(pointerCount) {
        pointerCoords[it!!.intValue]
    }
    return TIOSKHMotionEventCompanion.shared().obtainDownTime(downTime, eventTime, action, pointerCount, pointerPropertiesConverted, pointerCoordsConverted, metaState, buttonState, xPrecision, yPrecision, edgeFlags, source, flags)!!
}

var MotionEvent.action
    get() = action()
    set(value) {
        setAction(value)
    }
val MotionEvent.actionMasked
    get() = actionMasked()
val MotionEvent.actionIndex
    get() = actionIndex()
val MotionEvent.x
    get() = x()
val MotionEvent.y
    get() = y()
val MotionEvent.eventTime
    get() = eventTime()
val MotionEvent.pointerCount
    get() = pointerCount()
val MotionEvent.downTime
    get() = downTime()
val MotionEvent.xPrecision
    get() = xPrecision()
val MotionEvent.yPresicion
    get() = yPrecision()
val MotionEvent.edgeFlags
    get() = edgeFlags()
val MotionEvent.flags
    get() = flags()
var MotionEvent.source
    get() = source()
    set(value) {
        setSource(value)
    }
val MotionEvent.buttonState
    get() = buttonState()
val MotionEvent.rawX
    get() = rawX()
val MotionEvent.rawY
    get() = rawY()
val MotionEvent.historySize
    get() = historySize()
val MotionEvent.metaState
    get() = metaState()

fun MotionEvent.offsetLocation(deltaX: Float, deltaY: Float) {
    offsetLocationDeltaX(deltaX, deltaY)
}

fun MotionEvent.getPointerId(pointerIndex: Int): int32_t {
    return getPointerIdPointerIndex(pointerIndex)
}

fun MotionEvent.getToolType(pointerIndex: Int): int32_t {
    return getToolTypePointerIndex(pointerIndex)
}

fun MotionEvent.getPressure(pointerIndex: Int): Float {
    return getPressurePointerIndex(pointerIndex)
}

fun MotionEvent.getX(pointerIndex: Int): Float {
    return getXPointerIndex(pointerIndex)
}

fun MotionEvent.getY(pointerIndex: Int): Float {
    return getYPointerIndex(pointerIndex)
}

fun MotionEvent.getRawX(pointerIndex: Int): Float {
    return getRawXPointerIndex(pointerIndex)
}

fun MotionEvent.getRawY(pointerIndex: Int): Float {
    return getRawYPointerIndex(pointerIndex)
}

fun MotionEvent.getHistoricalEventTime(pointerIndex: Int): int64_t {
    return getHistoricalEventTimePos(pointerIndex)
}

fun MotionEvent.getHistoricalX(pointerIndex: Int, position: Int): Float {
    return getHistoricalXPointerIndex(pointerIndex, position)
}

fun MotionEvent.getHistoricalY(pointerIndex: Int, position: Int): Float {
    return getHistoricalYPointerIndex(pointerIndex, position)
}

fun MotionEvent.getAxisValue(axis: Int): Float {
    return getAxisValueAxis(axis)
}

fun MotionEvent.getAxisValue(axis: Int, pointerIndex: Int): Float {
    return getAxisValueAxis(axis, pointerIndex)
}

fun MotionEvent.getPointerProperties(pointerIndex: Int, pointerProperties: TIOSKHMotionEventPointerProperties) {
    return getPointerPropertiesPointerIndex(pointerIndex, pointerProperties)
}

fun MotionEvent.getPointerCoords(pointerIndex: Int, pointerCoords: TIOSKHMotionEventPointerCoords) {
    return getPointerCoordsPointerIndex(pointerIndex, pointerCoords)
}

fun MotionEvent.recycle() {
}

val ACTION_UP = TIOSKHMotionEventCompanion.shared().ACTION_UP()
val ACTION_DOWN = TIOSKHMotionEventCompanion.shared().ACTION_DOWN()
val ACTION_MOVE = TIOSKHMotionEventCompanion.shared().ACTION_MOVE()
val ACTION_POINTER_UP = TIOSKHMotionEventCompanion.shared().ACTION_POINTER_UP()
val ACTION_POINTER_DOWN = TIOSKHMotionEventCompanion.shared().ACTION_POINTER_DOWN()
val ACTION_OUTSIDE = TIOSKHMotionEventCompanion.shared().ACTION_OUTSIDE()
val ACTION_CANCEL = TIOSKHMotionEventCompanion.shared().ACTION_CANCEL()
val ACTION_HOVER_EXIT = TIOSKHMotionEventCompanion.shared().ACTION_HOVER_EXIT()
val ACTION_HOVER_MOVE = TIOSKHMotionEventCompanion.shared().ACTION_HOVER_MOVE()
val ACTION_HOVER_ENTER = TIOSKHMotionEventCompanion.shared().ACTION_HOVER_ENTER()
val ACTION_SCROLL = TIOSKHMotionEventCompanion.shared().ACTION_SCROLL()

val AXIS_HSCROLL = TIOSKHMotionEventCompanion.shared().AXIS_HSCROLL()
val AXIS_VSCROLL = TIOSKHMotionEventCompanion.shared().AXIS_VSCROLL()

val BUTTON_PRIMARY = TIOSKHMotionEventCompanion.shared().BUTTON_PRIMARY()
val BUTTON_SECONDARY = TIOSKHMotionEventCompanion.shared().BUTTON_SECONDARY()
val BUTTON_TERTIARY = TIOSKHMotionEventCompanion.shared().BUTTON_TERTIARY()
val BUTTON_STYLUS_PRIMARY = TIOSKHMotionEventCompanion.shared().BUTTON_STYLUS_PRIMARY()
val BUTTON_STYLUS_SECONDARY = TIOSKHMotionEventCompanion.shared().BUTTON_STYLUS_SECONDARY()
val BUTTON_BACK = TIOSKHMotionEventCompanion.shared().BUTTON_BACK()
val BUTTON_FORWARD = TIOSKHMotionEventCompanion.shared().BUTTON_FORWARD()

val TOOL_TYPE_ERASER = TIOSKHMotionEventCompanion.shared().TOOL_TYPE_ERASER()
val TOOL_TYPE_FINGER = TIOSKHMotionEventCompanion.shared().TOOL_TYPE_FINGER()
val TOOL_TYPE_MOUSE = TIOSKHMotionEventCompanion.shared().TOOL_TYPE_MOUSE()
val TOOL_TYPE_PALM = TIOSKHMotionEventCompanion.shared().TOOL_TYPE_PALM()
val TOOL_TYPE_STYLUS = TIOSKHMotionEventCompanion.shared().TOOL_TYPE_STYLUS()
val TOOL_TYPE_UNKNOWN = TIOSKHMotionEventCompanion.shared().TOOL_TYPE_UNKNOWN()

///////////////

class DisplayLinkConditions(
    val setPausedCallback: (Boolean) -> Unit
) {
    var needsToBeProactive: Boolean = false
        set(value) {
            field = value

            update()
        }

    /**
     * Indicates that scene is invalidated and next display link callback will draw
     */
    var needsRedrawOnNextVsync: Boolean = false
        set(value) {
            field = value

            update()
        }

    /**
     * Indicates that application is running foreground now
     */
    var isApplicationActive: Boolean = false
        set(value) {
            field = value

            update()
        }

    private fun update() {
        val isUnpaused = isApplicationActive && (needsToBeProactive || needsRedrawOnNextVsync)
        setPausedCallback(!isUnpaused)
    }
}

@OptIn(ExperimentalForeignApi::class)
class ApplicationStateListener(
    /**
     * Callback which will be called with `true` when the app becomes active, and `false` when the app goes background
     */
    private val callback: (Boolean) -> Unit
) : NSObject() {
    init {
        val notificationCenter = NSNotificationCenter.defaultCenter

        notificationCenter.addObserver(
            this,
            NSSelectorFromString(::applicationWillEnterForeground.name),
            UIApplicationWillEnterForegroundNotification,
            null
        )

        notificationCenter.addObserver(
            this,
            NSSelectorFromString(::applicationDidEnterBackground.name),
            UIApplicationDidEnterBackgroundNotification,
            null
        )
    }

    @ObjCAction
    fun applicationWillEnterForeground() {
        callback(true)
    }

    @ObjCAction
    fun applicationDidEnterBackground() {
        callback(false)
    }

    /**
     * Deregister from [NSNotificationCenter]
     */
    fun dispose() {
        val notificationCenter = NSNotificationCenter.defaultCenter

        notificationCenter.removeObserver(this, UIApplicationWillEnterForegroundNotification, null)
        notificationCenter.removeObserver(this, UIApplicationDidEnterBackgroundNotification, null)
    }
}

class DisplayLinkProxy(
    private val callback: () -> Unit
) : NSObject() {
    @ObjCAction
    fun handleDisplayLinkTick() {
        callback()
    }
}

//////////////////////

@OptIn(ExperimentalForeignApi::class)
class Choreographer private constructor(val looper: Looper){
    private val lock = SynchronizedObject()
    private val callbackList = mutableListOf<FrameCallback>()
    private var caDisplayLink: CADisplayLink? = CADisplayLink.displayLinkWithTarget(
    target = DisplayLinkProxy {
        this.step()
    },
    selector = NSSelectorFromString(DisplayLinkProxy::handleDisplayLinkTick.name)
    )

    private val displayLinkConditions = DisplayLinkConditions { paused ->
        caDisplayLink?.setPaused(paused)
    }

    private val applicationStateListener = ApplicationStateListener { isApplicationActive ->
        displayLinkConditions.isApplicationActive = isApplicationActive
    }

    companion object {
        private var mMainInstance: Choreographer? = null
        private val sThreadInstance: ThreadLocal<Choreographer?> =
            object : ThreadLocal<Choreographer?>() {
                override fun getKey(): String {
                    val looper = Looper.myLooper()
                    return looper.label + "_Choreographer"
                }

                override fun initialValue(): Choreographer {
                    val looper = Looper.myLooper()
                    val choreographer = Choreographer(
                        looper
                    )
                    if (looper == Looper.getMainLooper()) {
                        mMainInstance = choreographer
                    }
                    return choreographer
                }
            }

        fun getInstance(): Choreographer {
            if(Looper.myLooper() == Looper.getMainLooper()) {
                if(mMainInstance == null)
                    mMainInstance = Choreographer(Looper.myLooper())
                return mMainInstance!!
            }
            return sThreadInstance.get()!!
        }

        fun getMainThreadInstance(): Choreographer {
            return mMainInstance!!
        }
    }

    init {
        if(looper.label == "main")
            caDisplayLink?.addToRunLoop(NSRunLoop.mainRunLoop, NSRunLoop.mainRunLoop.currentMode)
        else
            caDisplayLink?.addToRunLoop(NSRunLoop.currentRunLoop, NSRunLoop.currentRunLoop.currentMode)
    }

    interface FrameCallback {
        fun doFrame(frameTimeNanos: Long)
    }

    fun dispose() {
        applicationStateListener.dispose()
        if(looper.label == "main")
            caDisplayLink?.removeFromRunLoop(NSRunLoop.mainRunLoop, NSRunLoop.mainRunLoop.currentMode)
        else
            caDisplayLink?.addToRunLoop(NSRunLoop.currentRunLoop, NSRunLoop.currentRunLoop.currentMode)
        caDisplayLink?.invalidate()
        caDisplayLink = null
        sThreadInstance.dispose()
        if(looper == Looper.getMainLooper())
            mMainInstance = null
    }

    fun step() {
        var callbacksCopy: MutableList<FrameCallback>? = null
        synchronized(lock) {
            callbacksCopy = callbackList.toMutableList()
        }
        val nanos = caDisplayLink!!.timestamp * 1000000000
        callbacksCopy?.forEach {
            it.doFrame(nanos.toLong())
        }
        synchronized(lock) {
            callbacksCopy?.let {
                callbackList.removeAll(it)
            }
        }
        /*synchronized(lock) {
            val nanos = caDisplayLink!!.timestamp * 100000
            callbackList.forEach {
                it.doFrame(nanos.toLong())
            }
            callbackList.clear()
        }*/
    }

    fun removeFrameCallback(frameCallback: FrameCallback) {
        synchronized(lock) {
            callbackList.remove(frameCallback)
        }
    }

    fun postFrameCallback(frameCallback: FrameCallback) {
        synchronized(lock) {
            callbackList.add(frameCallback)
        }
    }

    fun postFrameCallback(block: (Long) -> Unit) {
        postFrameCallback(object : FrameCallback
        {
            override fun doFrame(frameTimeNanos: Long) {
                block.invoke(frameTimeNanos)
            }
        })
    }
}

//////////////

typealias Menu = LuaMenu

//////////////

var PointerCoords.x
    get() = x()
    set(value) {
        setX(value)
    }

var PointerCoords.y
    get() = y()
    set(value) {
        setY(value)
    }

//////////////

fun TIOSKHTCanvasProtocol.clipRect(left: Float, top: Float, right: Float, bottom: Float) {
    clipRectLeft(left, top, right, bottom, TIOSKHSkikoClipMode.difference())
}

//////////////

fun LuaColor.willDraw(): Boolean {
    return colorValue != UIColor.clearColor
}

var LuaColor.color : Int
    get() {
        return TIOSKHTColorCompanion.shared().toIntInternalColor(this.colorValue!!)
    }
    set(value) {
        colorValue = LuaColor.colorFromInt(value)?.colorValue
    }

val DisplayMetrics.Companion.density
    get() = this.getDensity()

class ResourcesCompat {
    companion object {
        fun getFont(context: Context, resId: String) : Typeface {
            return Resources(context).getFont(resId)!!
        }

        fun getFont(context: Context, resId: String, callback: FontCallback, handler: Any?) {
            val font = Resources(context).getFont(resId)!!
            callback.onFontRetrieved(font)
        }
    }

    abstract class FontCallback {
        open fun onFontRetrieved(typeface: Typeface) {}
        open fun onFontRetrievalFailed(reason: Int) {}
    }
}

class Resources(val context: LuaContext)
{
    companion object {
    }

    val displayMetrics = DisplayMetrics.Companion
    object Theme {

    }

    fun getFont(resId: ID) : Typeface? {
        val lfr = LGFontParser.getInstance()!!.getFont(resId)
        if(lfr == null)
            return null

        return Typeface(lfr)
    }

    fun getDrawable(id: ID, nothing: Nothing?): LGDrawableReturn? {
        return LGDrawableParser.getInstance()!!.parseDrawable(id)
    }

    fun getString(id: ID): String {
        return context.resources.getStringKey(null, id)
    }

    fun getString(id: ID, vararg args: Any): String {
        val str = context.resources.getStringKey(null, id)
        //TODO(args)
        return str
    }

    fun getStringArray(id: ID): Array<String> {
        val arr = LGValueParser.getInstance()!!.getValue(id) as NSArray
        val arrOut = Array(arr.count.toInt()) { "" }
        for(i in 0 until arr.count.toInt())
            arrOut[i] = arr.objectAtIndex(i.toULong()) as String
        return arrOut
    }

    fun getQuantityString(id: ID, count: Int): String {
        return getString(id)
    }

    fun getQuantityString(id: ID, count: Int, vararg args: Any): String {
        return getString(id, args)
    }

    fun getXml(resId: ID): TIOSKHCoreXmlBufferedReader {
        return TIOSKHXmlCompanion.shared().getBufferedReaderValue(LGXmlParser.getInstance()!!.getXml(resId)!!)
    }
}

class TypedValue {
    var key = ""
    var type: Int = 0
    var string = ""

    companion object {
        val TYPE_ATTRIBUTE: Int = TIOSKHTypedValueCompanion.shared().TYPE_ATTRIBUTE()
        val TYPE_DIMENSION: Int = TIOSKHTypedValueCompanion.shared().TYPE_DIMENSION()
        val TYPE_FLOAT: Int = TIOSKHTypedValueCompanion.shared().TYPE_FLOAT()
        val TYPE_FIRST_INT: Int = TIOSKHTypedValueCompanion.shared().TYPE_FIRST_INT()
        val TYPE_FIRST_COLOR_INT: Int = TIOSKHTypedValueCompanion.shared().TYPE_FIRST_COLOR_INT()
        val TYPE_INT_BOOLEAN: Int = TIOSKHTypedValueCompanion.shared().TYPE_INT_BOOLEAN()
        val TYPE_LAYOUT: Int = TIOSKHTypedValueCompanion.shared().TYPE_LAYOUT()
        val TYPE_XML: Int = TIOSKHTypedValueCompanion.shared().TYPE_XML()
        val TYPE_NULL: Int = TIOSKHTypedValueCompanion.shared().TYPE_NULL()
    }
}

class TypedArray(val context: LuaContext, val attrs: AttributeSet) {
    fun getInt(key: String?, value: String, defValue: Int): Int {
        val v = attrs.objectForKey(value) as String
        return context.getResources().getIntKey(key, v, defValue)
    }

    fun getFloat(key: String?, value: String, defValue: Float): Float {
        val v = attrs.objectForKey(value) as String
        return context.getResources().getFloatKey(key, v, defValue)
    }

    fun getString(key: String?, value: String): String {
        val v = attrs.objectForKey(value) as String
        return context.getResources().getStringKey(key, v)
    }

    fun getDimension(value: String, defValue: Float): Float {
        val v = attrs.objectForKey(value) as String
        return context.getResources().getDimensionValue(v, defValue)
    }

    fun recycle() {

    }

    fun hasValue(resId: Pair<String?, String>): Boolean {
        return LGValueParser.getInstance()!!.getValue(resId.first, resId.second) != null
    }

    fun getValue(id: Pair<String?, String>, value: TypedValue) {
        value.type = context.getResources().getResourceTypeId(id.second)
    }
}

object TypedArrayUtils {
    fun getNamedInt(
        arr: TypedArray,
        xmlParser: XmlPullParser,
        attrName: String,
        resId: Pair<String?, String>,
        defaultValue: Int
    ): Int {
        return arr.context.getResources().getIntKey(resId.first, attrName, defaultValue)
    }

    fun getNamedFloat(
        arr: TypedArray,
        xmlParser: XmlPullParser,
        attrName: String,
        resId: Pair<String?, String>,
        defaultValue: Float
    ): Float {
        return arr.context.getResources().getFloatKey(resId.first, attrName, defaultValue)
    }

    fun getNamedBoolean(
        arr: TypedArray,
        xmlParser: XmlPullParser,
        attrName: String,
        resId: Pair<String?, String>,
        defaultValue: Boolean
    ): Boolean {
        return arr.context.getResources().getBooleanValue(attrName, defaultValue)
    }

    fun hasAttribute(xmlParser: XmlPullParser, s: String): Boolean {
        for(i in 0 until xmlParser.attributeCount())
        {
            if(xmlParser.getAttributeLocalNameIndex(i) == s)
                return true
        }
        return false
    }

    fun getNamedComplexColor(
        arr: TypedArray,
        xmlParser: XmlPullParser,
        theme: Resources.Theme?,
        attrName: String,
        resId: Pair<String?, String>,
        defaultValue: Int
    ): LuaColor {
        val color = LGColorParser.getInstance()!!.parseColor(resId.second)
        if(color == null)
            return LuaColor.colorFromInt(defaultValue)!!
        else {
            val lColor = LuaColor()
            lColor.colorValue = color
            return lColor
        }
    }

    fun getNamedColorStateList(
        arr: TypedArray,
        xmlParser: XmlPullParser,
        theme: Resources.Theme?,
        attrName: String,
        resId: Pair<String?, String>
    ): LGColorState? {
        return LGColorParser.getInstance()!!.getColorState(resId.second)
    }

}
