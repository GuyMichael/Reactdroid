package com.guymichael.reactdroid.extensions.components.list.adapter.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.*
import androidx.recyclerview.widget.RecyclerView

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
    protected abstract fun performItemLongClick(parent: RecyclerView, view: View, position: Int, id: Long): Boolean

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

    private inner class ItemClickGestureListener(private val mHostView: RecyclerView) : GestureDetector.SimpleOnGestureListener() {
        private val CLICK_PRESSED_STATE_DURATION = ViewConfiguration.getPressedStateDuration()
        private var mTargetChild: View? = null

        fun dispatchSingleTapUpIfNeeded(event: MotionEvent) {
            // When the long press hook is called but the long press listener
            // returns false, the target child will be left around to be
            // handled later. In this case, we should still treat the gesture
            // as potential item click.
            if (mTargetChild != null) {
                onSingleTapUp(event)
            }
        }

        override fun onDown(event: MotionEvent): Boolean {
            return mHostView.findChildViewUnder(event.x, event.y).also {
                mTargetChild = it
            } != null
        }

        override fun onShowPress(event: MotionEvent) {
            mTargetChild?.isPressed = true
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            var handled = false

            mTargetChild?.also {
                it.isPressed = false

                //changed by @Guy from deprecated getChildPosition()
                val position = mHostView.getChildAdapterPosition(it)
                val id = mHostView.adapter?.getItemId(position)
                //changed by @Guy to check for id -1 (which also means position -1)
                if (id == null || id < 0) {
                    //THINK (usually next click/tap works so it's a matter of race)
//                    Utils.toastDebug(mHostView.context, "adapter: id $id!\n$it")
                } else {
                    handled = findBestViewToClickAndPerform(mHostView, it, position, id, event)

                    if (handled) {
                        it.isPressed = true
                        it.postDelayed(ClearPressRunnable(it), CLICK_PRESSED_STATE_DURATION.toLong())
                    }
                }
            }

            mTargetChild = null

            return handled
        }

        override fun onScroll(event: MotionEvent, event2: MotionEvent, v: Float, v2: Float): Boolean {
            return mTargetChild?.let {
                it.isPressed = false
                mTargetChild = null
                true

            } ?: false
        }

        override fun onLongPress(event: MotionEvent) {
            mTargetChild?.also {
                //changed by @Guy from deprecated getChildPosition()
                val position = mHostView.getChildAdapterPosition(it)
                mHostView.adapter?.getItemId(position)?.let {id ->
                    val handled = findBestViewToLongClickAndPerform(mHostView, it, position, id)

                    if (handled) {
                        it.isPressed = false
                    }
                }
            }

            mTargetChild = null
        }

        private fun findBestViewToClickAndPerform(recycler: RecyclerView, itemView: View
                , position: Int, id: Long, event: MotionEvent
            ): Boolean {

            return tryToClickOnItemInnerView(itemView, event)   //some item inner view (e.g. some button)
                || tryToClickOnItemView(itemView)               //custom on-view item click
                || performItemClick(recycler, itemView, position, id)  //or standard using the adapter click listener
        }

        private fun findBestViewToLongClickAndPerform(recycler: RecyclerView, itemView: View
                , position: Int, id: Long
            ): Boolean {

            //THINK currently we don't want to support inner view long clicks, part because 'why?' and part because
            // View doesn't have a hasOnLongClickListeners() method which help us understand if a (long) click should be initiated,
            // but we could try to use View.isLongClickable() and/or View.performLongClick() to infer that
            return performItemLongClick(recycler, itemView, position, id)
        }

        private inner class ClearPressRunnable internal constructor(
            private val clickedView: View?
        ) : Runnable {

            override fun run() {
                clickedView?.isPressed = false
            }
        }
    }
}







private fun isClickOnView(view: View, x: Float, y: Float, extraClickAreaPx: Int? = null): Boolean {
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
                (child as? ViewGroup)?.let {viewGroupChild ->
                    //try to find a child of this viewGroup child
                    findChild(
                        viewGroupChild,
                        predicate
                    )?.let {
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

    return null
}

private fun tryToClickOnItemInnerView(itemView: View, event: MotionEvent): Boolean {
    return (itemView as? ViewGroup)?.let {
        findChild(it) { child -> isClickOnView(child, event.rawX, event.rawY) }
    }
    ?.hasOnClickListeners() //if an inner child has listeners, assume their click is/was handled
//    ?.let { it.hasOnClickListeners() && (it.performClick() || true) }
    ?: false
}

private fun tryToClickOnItemView(itemView: View): Boolean {
    return itemView.hasOnClickListeners() //if a list item has listeners, assume their click is/was handled

//    return itemView.hasOnClickListeners() && (itemView.performClick() || true)
}