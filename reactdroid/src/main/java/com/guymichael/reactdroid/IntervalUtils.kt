package com.guymichael.reactdroid

import android.os.Handler
import android.os.Looper
import android.util.LongSparseArray
import com.guymichael.reactdroid.model.IntervalRunnable
import java.util.concurrent.atomic.AtomicLong

object IntervalUtils {
    private val timeoutKeyGenerator = AtomicLong(1L)
    private val mRunnableMap = LongSparseArray<Runnable>()
    internal val mTimeoutHandler = Handler(Looper.getMainLooper())

    /**
     * Sets interval using [Handler.postDelayed].<br></br>Cancels previous 'key' runnable, if 'key' != null
     * @param runnable to onTick. You can use [.clearTimeout] to cancel.
     * @param startDelayMs delay the first tick by. This runnable's key will still be set right away, so [.isRunnablePending] will return *true* for *key*
     * @param intervalMs
     * @return the timeout key, to use with [clearTimeout]
     */
    fun setInterval(runnable: IntervalRunnable, intervalMs: Long, startDelayMs: Long): Long {
        val interval = if (intervalMs >= 0) intervalMs else 0
        val key = timeoutKeyGenerator.getAndIncrement()

        synchronized(mRunnableMap) {
            mRunnableMap.put(key, runnable)
        }

        runnable.setInterval(interval)
        synchronized(mTimeoutHandler) {
            if (startDelayMs <= 0) {
                mTimeoutHandler.post(runnable)
            } else {
                mTimeoutHandler.postDelayed(runnable, startDelayMs)
            }
        }

        return key
    }

    /**
     * Convenience method. Calls [ with &#39;startNow&#39; = false][.setInterval]
     */
    fun setInterval(runnable: IntervalRunnable, intervalMs: Long): Long {
        return setInterval(runnable, intervalMs, false)
    }

    /**
     * Convenience method. Calls [.setInterval]} with *startDelayMs:*<br></br>
     * If *true*, then *0*, else *intervalMs*
     */
    fun setInterval(runnable: IntervalRunnable, intervalMs: Long, startNow: Boolean): Long {
        return setInterval(runnable, intervalMs, if (startNow) 0 else intervalMs)
    }

    /**
     * Convenience method. Calls [.clearTimeout].
     */
    fun clearInterval(key: Long): Boolean {
        synchronized(mRunnableMap) {//THINK synchronization without synchronize
            mRunnableMap.get(key)?.let {
                mRunnableMap.remove(key)

                synchronized(mTimeoutHandler) {
                    mTimeoutHandler.removeCallbacks(it)
                    return true
                }
            }
        }

        return false
    }

    /**
     * @param runnable Same one used to call [.setTimeout] or
     * [.setInterval]
     * @return True if the [Runnable] was found and removed.
     */
    fun clearInterval(runnable: Runnable): Boolean {
        synchronized(mTimeoutHandler) {
            mTimeoutHandler.removeCallbacks(runnable)
        }

        synchronized(mRunnableMap) {
            val index = mRunnableMap.indexOfValue(runnable)
            if (index > -1) {
                mRunnableMap.removeAt(index)
            }
        }

        return true
    }

    fun isRunnablePending(key: Long): Boolean {
        synchronized(mRunnableMap) {
            return mRunnableMap.indexOfKey(key) > -1
        }
    }

    fun isRunnablePending(runnable: Runnable): Boolean {
        synchronized(mRunnableMap) {
            return mRunnableMap.indexOfValue(runnable) > -1
        }
    }
}