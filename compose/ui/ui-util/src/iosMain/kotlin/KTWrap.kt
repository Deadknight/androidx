@file:OptIn(ExperimentalForeignApi::class)

import kotlin.reflect.KCallable
import kotlin.reflect.KFunction0
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KFunction3
import kotlin.reflect.KFunction4
import kotlin.reflect.KFunction5
import kotlin.reflect.KFunction6
import kotlin.reflect.KFunction7
import kotlin.reflect.KFunction8
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import platform.darwin.NSObject

class KTWrap {

    var obj: Any? = null
    var func: KCallable<Any?>? = null
    var funcL: Function<Any?>? = null
    var funcStore: KFunction0<Any?>? = null
    var funcStore1: KFunction1<Any?, Any?>? = null
    var funcStore2: KFunction2<Any?, Any?, Any?>? = null
    var funcStore3: KFunction3<Any?, Any?, Any?, Any?>? = null
    var funcStore4: KFunction4<Any?, Any?, Any?, Any?, Any?>? = null
    var funcStore5: KFunction5<Any?, Any?, Any?, Any?, Any?, Any?>? = null
    var funcStore6: KFunction6<Any?, Any?, Any?, Any?, Any?, Any?, Any?>? = null
    var funcStore7: KFunction7<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>? = null
    var funcStore8: KFunction8<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>? = null
    var funcStoreL: Function0<Any?>? = null
    var funcStoreL1: Function1<Any?, Any?>? = null
    var funcStoreL2: Function2<Any?, Any?, Any?>? = null
    var funcStoreL3: Function3<Any?, Any?, Any?, Any?>? = null
    var funcStoreL4: Function4<Any?, Any?, Any?, Any?, Any?>? = null
    var funcStoreL5: Function5<Any?, Any?, Any?, Any?, Any?, Any?>? = null
    var funcStoreL6: Function6<Any?, Any?, Any?, Any?, Any?, Any?, Any?>? = null
    var funcStoreL7: Function7<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>? = null
    var funcStoreL8: Function8<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>? = null

    fun Init(
        obj: Any?,
        func: KCallable<Any?>?
    ): CPointer<CFunction<(COpaquePointer?, Int, List<Any?>?) -> NSObject?>> {
        this.obj = obj
        this.func = func

        return staticCFunction<COpaquePointer?, Int, List<Any?>?, NSObject?> { obj: COpaquePointer?, self: Int, vars: List<Any?>? ->
            val v = obj?.asStableRef<KTWrap>()
            v?.get()?.funToCall(self, vars!!) as NSObject?
        }
    }

    fun Init(
        obj: Any?,
        func: Function<Any?>?
    ): CPointer<CFunction<(COpaquePointer?, Int, List<Any?>?) -> NSObject?>> {
        this.obj = obj
        this.funcL = func

        return staticCFunction<COpaquePointer?, Int, List<Any?>?, NSObject?> { obj: COpaquePointer?, self: Int, vars: List<Any?>? ->
            val v = obj?.asStableRef<KTWrap>()
            v?.get()?.funToCall(self, vars!!) as NSObject?
        }
    }

    fun Init(obj: Any?, func: KFunction0<Any?>?) {
        this.obj = obj
        this.funcStore = func
    }

    fun Init(obj: Any?, func: KFunction1<Any?, Any?>?) {
        this.obj = obj
        this.funcStore1 = func
    }

    fun Init(obj: Any?, func: KFunction2<Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStore2 = func
    }

    fun Init(obj: Any?, func: KFunction3<Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStore3 = func
    }

    fun Init(obj: Any?, func: KFunction4<Any?, Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStore4 = func
    }

    fun Init(obj: Any?, func: KFunction5<Any?, Any?, Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStore5 = func
    }

    fun Init(obj: Any?, func: KFunction6<Any?, Any?, Any?, Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStore6 = func
    }

    fun Init(obj: Any?, func: KFunction7<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStore7 = func
    }

    fun Init(obj: Any?, func: KFunction8<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStore8 = func
    }

    fun Init(obj: Any?, func: Function0<Any?>?) {
        this.obj = obj
        this.funcStoreL = func
    }

    fun Init(obj: Any?, func: Function1<Any?, Any?>?) {
        this.obj = obj
        this.funcStoreL1 = func
    }

    fun Init(obj: Any?, func: Function2<Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStoreL2 = func
    }

    fun Init(obj: Any?, func: Function3<Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStoreL3 = func
    }

    fun Init(obj: Any?, func: Function4<Any?, Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStoreL4 = func
    }

    fun Init(obj: Any?, func: Function5<Any?, Any?, Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStoreL5 = func
    }

    fun Init(obj: Any?, func: Function6<Any?, Any?, Any?, Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStoreL6 = func
    }

    fun Init(obj: Any?, func: Function7<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStoreL7 = func
    }

    fun Init(obj: Any?, func: Function8<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>?) {
        this.obj = obj
        this.funcStoreL8 = func
    }

    fun funToCall(self: Int, vars: List<Any?>?): Any? {
        var ret: Any? = null
        var varsSize = vars?.size ?: 0
        if (this.obj != null && self == 0)
            varsSize++
        if (func != null) {
            if (varsSize == 0)
                Init(this.obj, this.func as KFunction0<Any?>)
            else if (varsSize == 1)
                Init(this.obj, this.func as KFunction1<Any?, Any?>)
            else if (varsSize == 2)
                Init(this.obj, this.func as KFunction2<Any?, Any?, Any?>)
            else if (varsSize == 3)
                Init(this.obj, this.func as KFunction3<Any?, Any?, Any?, Any?>)
            else if (varsSize == 4)
                Init(this.obj, this.func as KFunction4<Any?, Any?, Any?, Any?, Any?>)
            else if (varsSize == 5)
                Init(this.obj, this.func as KFunction5<Any?, Any?, Any?, Any?, Any?, Any?>)
            else if (varsSize == 6)
                Init(this.obj, this.func as KFunction6<Any?, Any?, Any?, Any?, Any?, Any?, Any?>)
            else if (varsSize == 7)
                Init(
                    this.obj,
                    this.func as KFunction7<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>
                )
            else /*if(func is KFunction8<*, *, *, *, *, *, *, *, *>)*/
                Init(
                    this.obj,
                    this.func as KFunction8<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>
                )
        } else {
            if (varsSize == 0)
                Init(this.obj, this.funcL as Function0<Any?>)
            else if (varsSize == 1)
                Init(this.obj, this.funcL as Function1<Any?, Any?>)
            else if (varsSize == 2)
                Init(this.obj, this.funcL as Function2<Any?, Any?, Any?>)
            else if (varsSize == 3)
                Init(this.obj, this.funcL as Function3<Any?, Any?, Any?, Any?>)
            else if (varsSize == 4)
                Init(this.obj, this.funcL as Function4<Any?, Any?, Any?, Any?, Any?>)
            else if (varsSize == 5)
                Init(this.obj, this.funcL as Function5<Any?, Any?, Any?, Any?, Any?, Any?>)
            else if (varsSize == 6)
                Init(this.obj, this.funcL as Function6<Any?, Any?, Any?, Any?, Any?, Any?, Any?>)
            else if (varsSize == 7)
                Init(
                    this.obj,
                    this.funcL as Function7<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>
                )
            else /*if(func is KFunction8<*, *, *, *, *, *, *, *, *>)*/
                Init(
                    this.obj,
                    this.funcL as Function8<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?>
                )
        }

        if (self == 1)
            this.obj = null
        if (funcStore != null) {
            ret = funcStore?.invoke()
        } else if (funcStore1 != null) {
            if (obj != null)
                ret = funcStore1?.invoke(obj)
            else
                ret = funcStore1?.invoke(vars!![0])
        } else if (funcStore2 != null) {
            if (obj != null)
                ret = funcStore2?.invoke(obj, vars!![0])
            else
                ret = funcStore2?.invoke(vars!![0], vars[1])
        } else if (funcStore3 != null) {
            if (obj != null)
                ret = funcStore3?.invoke(obj, vars!![0], vars[1])
            else
                ret = funcStore3?.invoke(vars!![0], vars[1], vars[2])
        } else if (funcStore4 != null) {
            if (obj != null)
                ret = funcStore4?.invoke(obj, vars!![0], vars[1], vars[2])
            else
                ret = funcStore4?.invoke(
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3]
                )
        } else if (funcStore5 != null) {
            if (obj != null)
                ret = funcStore5?.invoke(
                    obj,
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3]
                )
            else
                ret = funcStore5?.invoke(
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4]
                )
        } else if (funcStore6 != null) {
            if (obj != null)
                ret = funcStore6?.invoke(
                    obj,
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4]
                )
            else
                ret = funcStore6?.invoke(
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4],
                    vars[5]
                )
        } else if (funcStore7 != null) {
            if (obj != null)
                ret = funcStore7?.invoke(
                    obj,
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4],
                    vars[5]
                )
            else
                ret = funcStore7?.invoke(
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4],
                    vars[5],
                    vars[6]
                )
        } else if (funcStore8 != null) {
            if (obj != null)
                ret = funcStore8?.invoke(
                    obj,
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4],
                    vars[5],
                    vars[6]
                )
            else
                ret = funcStore8?.invoke(
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4],
                    vars[5],
                    vars[6],
                    vars[7]
                )
        } else if (funcStoreL != null) {
            ret = funcStoreL?.invoke()
        } else if (funcStoreL1 != null) {
            if (obj != null)
                ret = funcStoreL1?.invoke(obj)
            else
                ret = funcStoreL1?.invoke(vars!![0])
        } else if (funcStoreL2 != null) {
            if (obj != null)
                ret = funcStoreL2?.invoke(obj, vars!![0])
            else
                ret = funcStoreL2?.invoke(vars!![0], vars[1])
        } else if (funcStoreL3 != null) {
            if (obj != null)
                ret = funcStoreL3?.invoke(obj, vars!![0], vars[1])
            else
                ret = funcStoreL3?.invoke(vars!![0], vars[1], vars[2])
        } else if (funcStoreL4 != null) {
            if (obj != null)
                ret = funcStoreL4?.invoke(obj, vars!![0], vars[1], vars[2])
            else
                ret = funcStoreL4?.invoke(
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3]
                )
        } else if (funcStoreL5 != null) {
            if (obj != null)
                ret = funcStoreL5?.invoke(
                    obj,
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3]
                )
            else
                ret = funcStoreL5?.invoke(
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4]
                )
        } else if (funcStoreL6 != null) {
            if (obj != null)
                ret = funcStoreL6?.invoke(
                    obj,
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4]
                )
            else
                ret = funcStoreL6?.invoke(
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4],
                    vars[5]
                )
        } else if (funcStoreL7 != null) {
            if (obj != null)
                ret = funcStoreL7?.invoke(
                    obj,
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4],
                    vars[5]
                )
            else
                ret = funcStoreL7?.invoke(
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4],
                    vars[5],
                    vars[6]
                )
        } else if (funcStoreL8 != null) {
            if (obj != null)
                ret = funcStoreL8?.invoke(
                    obj,
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4],
                    vars[5],
                    vars[6]
                )
            else
                ret = funcStoreL8?.invoke(
                    vars!![0],
                    vars[1],
                    vars[2],
                    vars[3],
                    vars[4],
                    vars[5],
                    vars[6],
                    vars[7]
                )
        }

        return ret
    }
}