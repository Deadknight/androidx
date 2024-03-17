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

package androidx.compose.runtime

import androidx.compose.runtime.internal.ComposableLambda
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotContextElement
import androidx.compose.runtime.snapshots.SnapshotMutableState
import kotlin.coroutines.CoroutineContext
import kotlin.native.identityHashCode
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import platform.Foundation.NSDate
import platform.Foundation.NSString
import platform.Foundation.NSThread
import platform.Foundation.NSUUID.Companion.UUID
import platform.Foundation.date
import platform.Foundation.timeIntervalSince1970
import platform.posix.pthread_mach_thread_np
import platform.posix.pthread_self

internal actual object Trace {
    actual fun beginSection(name: String): Any? {
        // Do nothing.
        return null
    }

    actual fun endSection(token: Any?) {
        // Do nothing.
    }
}

actual class AtomicReference<V> actual constructor(value: V) {

    val ref = atomic(value)

    actual fun get(): V {
        return ref.value
    }

    actual fun set(value: V) {
        ref.value = value
    }

    actual fun getAndSet(value: V): V {
        return ref.getAndSet(value)
    }

    actual fun compareAndSet(expect: V, newValue: V): Boolean {
        return ref.compareAndSet(expect, newValue)
    }
}

internal actual open class ThreadLocal<T> actual constructor(
    private val initialValue: () -> T
) {
    private val uuidString = UUID().UUIDString

    @Suppress("UNCHECKED_CAST")
    actual fun get(): T {
        val t = NSThread.currentThread
        val value = t.threadDictionary.objectForKey(uuidString) as T?
        if(value != null) {
            return value
        }

        return setInitialValue()
    }

    actual fun set(value: T) {
        val t = NSThread.currentThread
        t.threadDictionary.setObject(value, uuidString as NSString)
    }

    private fun setInitialValue(): T {
        val value = initialValue()
        val t = NSThread.currentThread
        t.threadDictionary.setObject(value, uuidString as NSString)
        return value!!
    }

    fun initialValue(): T? {
        return initialValue.invoke()
    }

    actual fun remove() {
        val t = NSThread.currentThread
        t.threadDictionary.removeObjectForKey(uuidString as NSString)
    }
}

internal actual class SnapshotThreadLocal<T> {
    private val map = AtomicReference<MutableMap<String, T?>>(mutableMapOf())
    private val writeMutex = SyncLock()

    private var mainThreadValue: T? = null

    actual fun get(): T? {

        val threadId = currentThreadId().toString()
        return if (NSThread.currentThread.isMainThread) {
            mainThreadValue
        } else {
            map.get().get(threadId) as T?
        }
    }

    actual fun set(value: T?) {
        val key = currentThreadId().toString()
        if (NSThread.currentThread.isMainThread) {
            mainThreadValue = value
        } else {
            synchronized(writeMutex) {
                val current = map.get()
                current.put(key!!, value)
            }
        }
    }
}

actual fun identityHashCode(instance: Any?): Int = instance.identityHashCode()

actual typealias SyncLock = SynchronizedObject

@PublishedApi
internal actual inline fun <R> synchronized(lock: SyncLock, block: () -> R): R {
    return kotlinx.atomicfu.locks.synchronized(lock as SynchronizedObject, block)
}

actual annotation class TestOnly

internal actual fun invokeComposable(composer: Composer, composable: @Composable () -> Unit) {
    @Suppress("UNCHECKED_CAST")
    val realFn = composable as Function2<Composer, Int, Unit>
    realFn(composer, 1)
}

internal actual fun <T> invokeComposableForResult(
    composer: Composer,
    composable: @Composable () -> T
): T {
    @Suppress("UNCHECKED_CAST")
    val realFn = composable as Function2<Composer, Int, T>
    return realFn(composer, 1)
}

internal actual class AtomicInt actual constructor(value: Int) {
    val delegate = AtomicReference(value)
    actual fun get(): Int = delegate.get()
    actual fun set(value: Int)  { delegate.set(value) }
    actual fun add(amount: Int): Int {
        val v = delegate.get() + amount
        delegate.set(v)
        return v
    }
}

internal actual fun ensureMutable(it: Any) { /* NOTHING */ }

internal actual class WeakReference<T : Any> actual constructor(reference: T) {
    val ref = kotlin.native.ref.WeakReference<T>(reference)
    actual fun get(): T? {
        return ref.get()
    }
}

actual interface ThreadContextElement<S> : CoroutineContext.Element {
    /**
     * Updates context of the current thread.
     * This function is invoked before the coroutine in the specified [context] is resumed in the current thread
     * when the context of the coroutine this element.
     * The result of this function is the old value of the thread-local state that will be passed to [restoreThreadContext].
     * This method should handle its own exceptions and do not rethrow it. Thrown exceptions will leave coroutine which
     * context is updated in an undefined state and may crash an application.
     *
     * @param context the coroutine context.
     */
    actual fun updateThreadContext(context: CoroutineContext): S

    /**
     * Restores context of the current thread.
     * This function is invoked after the coroutine in the specified [context] is suspended in the current thread
     * if [updateThreadContext] was previously invoked on resume of this coroutine.
     * The value of [oldState] is the result of the previous invocation of [updateThreadContext] and it should
     * be restored in the thread-local state by this function.
     * This method should handle its own exceptions and do not rethrow it. Thrown exceptions will leave coroutine which
     * context is updated in an undefined state and may crash an application.
     *
     * @param context the coroutine context.
     * @param oldState the value returned by the previous invocation of [updateThreadContext].
     */
    actual fun restoreThreadContext(context: CoroutineContext, oldState: S)
}

/**
 * Implementation of [SnapshotContextElement] that enters a single given snapshot when updating
 * the thread context of a resumed coroutine.
 */
@ExperimentalComposeApi
internal actual class SnapshotContextElementImpl actual constructor(
    private val snapshot: Snapshot
) : SnapshotContextElement, ThreadContextElement<Snapshot?> {
    override val key: CoroutineContext.Key<*>
        get() = SnapshotContextElement

    override fun updateThreadContext(context: CoroutineContext): Snapshot? =
        snapshot.unsafeEnter()

    override fun restoreThreadContext(context: CoroutineContext, oldState: Snapshot?) {
        snapshot.unsafeLeave(oldState)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun currentThreadId(): Long {
    return pthread_mach_thread_np(pthread_self()).toLong()
}

internal actual fun currentThreadName(): String = NSThread.currentThread.name.toString()

actual annotation class CheckResult(actual val suggest: String)

actual val DefaultMonotonicFrameClock: MonotonicFrameClock get() = SixtyFpsMonotonicFrameClock

private object SixtyFpsMonotonicFrameClock : MonotonicFrameClock {
    private const val fps = 60

    override suspend fun <R> withFrameNanos(
        onFrame: (Long) -> R
    ): R {
        delay(1000L / fps)
        return onFrame((NSDate.date().timeIntervalSince1970 * 1000000000).toLong())
    }
}

internal actual fun <T> createSnapshotMutableState(
    value: T,
    policy: SnapshotMutationPolicy<T>
): SnapshotMutableState<T> = SnapshotMutableStateImpl(value, policy)

internal actual fun createSnapshotMutableIntState(
    value: Int
): MutableIntState = SnapshotMutableIntStateImpl(value)

internal actual fun createSnapshotMutableLongState(
    value: Long
): MutableLongState = SnapshotMutableLongStateImpl(value)

internal actual fun createSnapshotMutableFloatState(
    value: Float
): MutableFloatState = SnapshotMutableFloatStateImpl(value)

internal actual fun createSnapshotMutableDoubleState(
    value: Double
): MutableDoubleState = SnapshotMutableDoubleStateImpl(value)

internal actual fun logError(message: String, e: Throwable) {
    println(message)
    e.printStackTrace()
}