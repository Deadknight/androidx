package android.os

import kotlinx.coroutines.Runnable
import platform.Foundation.NSOperation
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSString
import platform.Foundation.NSThread
import platform.Foundation.NSUUID
import platform.Foundation.operationCount
import platform.Foundation.operations
import platform.darwin.DISPATCH_QUEUE_PRIORITY_BACKGROUND
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_SEC
import platform.darwin.NSObject
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_current_queue
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_concurrent_t
import platform.darwin.dispatch_queue_t
import platform.darwin.dispatch_time

class Looper : NSObject {
    val label: String
    val queue: dispatch_queue_t

    constructor(label: String, queue: dispatch_queue_t) {
        this.label = label
        this.queue = queue
    }

    companion object {

        private fun getLooperForKey(key: String) : Looper {
            var looper = if(key == "main")
                NSThread.mainThread.threadDictionary.objectForKey(key) as Looper?
            else
                NSThread.currentThread.threadDictionary.objectForKey(key) as Looper?

            if(looper == null) {
                if(key == "main") {
                    looper = Looper("main", dispatch_get_main_queue())
                    NSThread.mainThread.threadDictionary.setObject(looper, key as NSString)
                }
                else {
                    looper = Looper(key, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND.toLong(), 0u))
                    NSThread.currentThread.threadDictionary.setObject(looper, key as NSString)
                }
            }
            return looper
        }

        fun getMainLooper(): Looper {
            return getLooperForKey("main")
        }

        fun myLooper(): Looper {
            var label = NSThread.currentThread.threadDictionary.objectForKey("gcd_label") as NSString?
            val looper: Looper
            if(label == null)
            {
                if(NSThread.isMainThread) {
                    label = "main" as NSString
                }
                else {
                    label = NSUUID().UUIDString as NSString
                    NSThread.currentThread.threadDictionary.setObject(label as NSString, "gcd_label" as NSString)
                }
            }
            looper = getLooperForKey(label as String)
            return looper
        }
    }

    override fun isEqual(`object`: Any?): Boolean {
        return label == label
    }
}

class Handler : NSObject {
    enum class TYPE {
        ASYNC,
        SYNC
    }

    inner class HandlerOperation(val runnable: Runnable, val delay: Long = 0) : NSOperation() {
        override fun main() {
            if(isCancelled())
                return

            val popTime = dispatch_time(DISPATCH_TIME_NOW, (delay * NSEC_PER_SEC.toLong()))
            dispatch_after(popTime, looper.queue) {
                if(isCancelled())
                    return@dispatch_after
                runnable.run()
            }
        }
    }

    private var type: TYPE = TYPE.ASYNC

    internal var looper: Looper
    private val queue: NSOperationQueue

    constructor() : super() {
        looper = Looper.getMainLooper()
        queue = NSOperationQueue.mainQueue
    }

    constructor(looper: Looper) : super() {
        this.looper = looper
        queue = if(looper == Looper.getMainLooper())
            NSOperationQueue.mainQueue
        else
            NSOperationQueue()
    }

    fun setType(type: TYPE) {
        this.type = type
        when(type) {
            TYPE.ASYNC -> {
                var count = NSProcessInfo.processInfo.processorCount
                if(count > 1UL)
                {
                    count = kotlin.math.floor(count.toFloat() / 2).toULong()
                }
                queue.maxConcurrentOperationCount = count.toLong()
            }
            TYPE.SYNC -> {
                queue.maxConcurrentOperationCount = 1
            }
        }
    }

    fun post(runnable: Runnable): Boolean {
        val op = HandlerOperation(runnable)
        queue.addOperation(op)
        return true
    }

    fun removeCallbacks(runnable: Runnable) {
        for(i in 0 until queue.operationCount.toInt()) {
            val op = queue.operations[i] as HandlerOperation
            if(op.runnable == runnable) {
                op.cancel()
                break
            }
        }
    }

    fun postDelayed(runnable: Runnable, coerceAtMost: Long): Boolean {
        val op = HandlerOperation(runnable, coerceAtMost)
        queue.addOperation(op)
        return true
    }
}

object HandlerCompat
{
    fun createAsync(looper: Looper) : Handler {
        return Handler(looper).also {
            it.setType(Handler.TYPE.ASYNC)
        }
    }
}