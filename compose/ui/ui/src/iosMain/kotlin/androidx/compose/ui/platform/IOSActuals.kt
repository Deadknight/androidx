package androidx.compose.ui.platform

import androidx.compose.runtime.SyncLock
import format
import kotlin.native.identityHashCode

actual class AtomicInt actual constructor(value: Int) {
    val atomicInt: kotlin.native.concurrent.AtomicInt = kotlin.native.concurrent.AtomicInt(value)
    actual fun addAndGet(delta: Int): Int {
        return atomicInt.addAndGet(delta)
    }

    actual fun compareAndSet(expected: Int, new: Int): Boolean {
        return atomicInt.compareAndSet(expected, new)
    }
}

internal actual fun simpleIdentityToString(obj: Any, name: String?): String {
    val className = name ?: obj::class.simpleName

    return className + "@" + String.format("%07x", obj.identityHashCode())
}

internal actual fun Any.nativeClass(): Any = this::class

internal actual inline fun <R> synchronized(lock: SyncLock, block: () -> R): R {
    return threading.synchronized(lock, block)
}
