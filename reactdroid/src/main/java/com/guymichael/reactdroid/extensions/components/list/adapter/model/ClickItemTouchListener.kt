package com.guymichael.reactdroid.extensions.components.list.adapter.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

abstract class ClickItemTouchListener(hostView: RecyclerView) : RecyclerView.OnItemTouchListener {

    private val mGestureDetector: GestureDetector = ItemClickGestureDetector(
        hostView.context
        , ItemClickGestureListener(hostView)
    )


    @SuppressLint("NewApi")
    private fun isAttachedToWindow(hostView: RecyclerView): Boolean {
        return if (Build.VERSION.SDK_INT >= 19) {
            hostView.isAttachedToWindow
        } else {
            hostView.handler != null
        }
    }

    private fun hasAdapter(hostView: RecyclerView): Boolean {
        return hostView.adapter != null
    }

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
        if (!isAttachedToWindow(recyclerView) || !hasAdapter(recyclerView)) {
            return false
        }

        mGestureDetector.onTouchEvent(event)
        return false
    }

    override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
        // We can silently track tap and and long presses by silently
        // intercepting touch events in the host RecyclerView.
    }

    protected abstract fun performItemClick(parent: RecyclerView, view: View, position: Int, id: Long): Boolean
    protected abstract fun performItemLongClick(parent: RecyclerView, view: View, position: Int, id: Long)

    private inner class ItemClickGestureDetector(context: Context, private val mGestureListener: ItemClickGestureListener) : GestureDetector(context, mGestureListener) {

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val handled = super.onTouchEvent(event)

            val action = event.action and MotionEvent.ACTION_MASK
            if (action == MotionEvent.ACTION_UP) {
                mGestureListener.dispatchSingleTapUpIfNeeded(event)
            }

            return handled
        }
    }






    private inner class ItemClickGestureListener(private val mHostView: RecyclerView)
        : GestureDetector.SimpleOnGestureListener() {

        private val mPressStateDuration = ViewConfiguration.getPressedStateDuration()
        private var mClickTarget: WeakReference<View>? = null

        fun dispatchSingleTapUpIfNeeded(event: MotionEvent) {
            // When the long press hook is called but the long press listener
            // returns false, the target child will be left around to be
            // handled later. In this case, we should still treat the gesture
            // as potential item click.
            if (mClickTarget?.get() != null) {
                onSingleTapUp(event)
            }
        }

        override fun onDown(event: MotionEvent): Boolean {
            return mHostView.findChildViewUnder(event.x, event.y)
                ?.takeIf { shouldHandleAsAdapterClick(it, event) }
                ?.also { mClickTarget = WeakReference(it) } != null //handling as adapter click if true
        }

        override fun onShowPress(event: MotionEvent) {
            mClickTarget?.get()?.isPressed = true
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            var handled = false

            mClickTarget?.get()?.also { clickTarget ->
                getPosAndId(clickTarget)?.also { (position, id) ->
                    //clickTarget is an itemView w/o custom listeners - handle as adapter-item-click
                    handled = performItemClick(mHostView, clickTarget, position, id)

                    if (handled) {
                        //post + delayed pressed-feedback
                        clickTarget.post { clickTarget.isPressed = true } //onShowPress() doesn't get called on single-tap
                        clickTarget.postDelayed(ClearPressRunnable(clickTarget), mPressStateDuration.toLong())
                    } else {
                        clickTarget.isPressed = false
                    }
                }

                //end of click event-chain, clear target
                mClickTarget = null
            }

            return handled
        }

        override fun onScroll(event: MotionEvent, event2: MotionEvent, v: Float, v2: Float): Boolean {
            return mClickTarget?.get()?.let {
                //consider as "cancel" touch event
                it.isPressed = false
                mClickTarget = null
                true

            } ?: false
        }

        override fun onLongPress(event: MotionEvent) {
            mClickTarget?.get()?.also { clickTarget ->
                getPosAndId(clickTarget)?.also { (position, id) ->
                    //clickTarget is an itemView w/o custom listeners - handle as adapter-item-click
                    performItemLongClick(mHostView, clickTarget, position, id)
                    clickTarget.isPressed = false
                }

                //end of click event-chain, clear target
                mClickTarget = null
            }
        }

        /** @return if not null, `position >= 0` and  */
        private fun getPosAndId(itemView: View): Pair<Int, Long>? {
            return itemView.let { clickTarget ->
                val position = mHostView.getChildAdapterPosition(clickTarget)
                val id = mHostView.adapter?.takeIf { position != RecyclerView.NO_POSITION }?.getItemId(position)
                if (id == null || id == RecyclerView.NO_ID) {
                    //id / position is probably '-1'
                    //THINK (usually next click/tap works so it's a matter of race)
//                    Utils.toastDebug(mHostView.context, "adapter: position $position, id $id !\n$it")
                    null
                } else {
                    Pair(position, id)
                }
            }
        }

        private fun shouldHandleAsAdapterClick(itemView: View, event: MotionEvent): Boolean {
            return findItemInnerClickableViewOrNull(itemView, event) == null //no item inner-view (e.g. some button) listener
                && !itemView.hasOnClickListeners()                           //no item custom/specific click listener
                && itemView.isClickable                                      //or standard using the adapter click listener
        }

        private inner class ClearPressRunnable internal constructor(
            clickedView: View
        ) : Runnable {

            private val viewRef = WeakReference(clickedView)

            override fun run() {
                viewRef.get()?.isPressed = false
            }
        }
    }
}








private fun isTouchOnView(view: View, x: Float, y: Float, extraClickAreaPx: Int? = null): Boolean {
    val viewBounds = Rect()
    view.getGlobalVisibleRect(viewBounds)
    return x >= viewBounds.left - (extraClickAreaPx?:0)
        && x <= viewBounds.right + (extraClickAreaPx?:0)
        && y >= viewBounds.top - (extraClickAreaPx?:0)
        && y <= viewBounds.bottom + (extraClickAreaPx?:0)
}

private fun findChild(parent: ViewGroup, predicate: (child: View) -> Boolean): View? {
    var i = 0
    while (i < parent.childCount) {
        parent.getChildAt(i)?.let {child ->
            //If it's a viewGroup, try to find deeper views which match this predicate (recursive)
            if (ViewGroup::class.java.isInstance(child)) {
                (child as? ViewGroup)?.also { viewGroupChild ->
                    //try to find a child of this viewGroup child
                    findChild(viewGroupChild, predicate)?.also {
                        return it
                    }
                }
            }

            //inner child not found, try 'this'
            if (predicate(child)) {
                return child
            }
        }

        i += 1
    }

    //no child found
    return null
}

private fun findItemInnerClickableViewOrNull(itemView: View, event: MotionEvent): View? {
    return (itemView as? ViewGroup)?.let {
        findChild(it) { child -> child.hasOnClickListeners() && isTouchOnView(child, event.rawX, event.rawY) }
    }
}