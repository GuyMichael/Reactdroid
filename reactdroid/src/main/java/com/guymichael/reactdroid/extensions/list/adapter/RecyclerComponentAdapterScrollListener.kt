package com.guymichael.reactdroid.extensions.list.adapter

//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.reactdroid.extensions.list.ComponentListUtils
import java.util.*

class RecyclerComponentAdapterScrollListener(private val adapter: RecyclerComponentAdapter) : RecyclerView.OnScrollListener() {
    internal val customScrollListeners = HashSet<RecyclerView.OnScrollListener>()
    private val sRecyclerViewItemHeights = Hashtable<Int, Int>()
    private val sRecyclerViewItemWidths = Hashtable<Int, Int>()

    private var reachingTopListener: OnReachingTopListener? = null
    private var reachingBottomListener: OnReachingBottomListener? = null
//    private var boundSwipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null
    var isScrolling = false
        private set
    private var isScrollingUp = false
    private var mPrevFirstVisibleItem = 0
    private var okToNotifyReachingTop = true
    private var okToNotifyReachingBottom = true
    //private int mPrevScrollY = 0, mPrevScrollX = 0;


    //            Log.d("", "getScrollY() : firstVisiblePosition - " + firstVisiblePosition);
    //        Log.d("", "getScrollY() : scrollY - " + scrollY);
    // (this is a sanity check)
    //add all heights of the views that are gone
    //speculate out-of-view item height according to current visible
    fun getScrollY(): Int {
        val c = adapter.recyclerView.getChildAt(0) ?: return 0

        val firstVisiblePosition = adapter.getActualFirstVisiblePosition()

        var scrollY = -c.top

        sRecyclerViewItemHeights[firstVisiblePosition] = c.height

        if (scrollY < 0) {
            scrollY = 0
        }

        for (i in 0 until firstVisiblePosition) {
            scrollY += if (sRecyclerViewItemHeights[i] != null) {
                sRecyclerViewItemHeights[i] ?: 0
            } else {
                sRecyclerViewItemHeights[firstVisiblePosition] ?: 0
            }
        }

        return scrollY
    }

    //            Log.d("", "getScrollX() : firstVisiblePosition - " + firstVisiblePosition);
    //        Log.d("", "getScrollX() : scrollX - " + scrollX);
    // (this is a sanity check)
    //add all heights of the views that are gone
    fun getScrollX(): Int {
        val c = adapter.recyclerView.getChildAt(0) ?: return 0

        var firstVisiblePosition = ComponentListUtils.getFirstVisiblePosition(adapter.layoutManager)

        if (adapter.isCyclic) {
            firstVisiblePosition = adapter.getActualPosition(firstVisiblePosition)
        }

        var scrollX = -c.left

        sRecyclerViewItemWidths[firstVisiblePosition] = c.width

        if (scrollX < 0) {
            scrollX = 0
        }

        for (i in 0 until firstVisiblePosition) {
            if (sRecyclerViewItemWidths[i] != null)
                scrollX += sRecyclerViewItemWidths[i] ?: 0
        }

        return scrollX
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        //detect scrolling direction
        var scrollChanged = true
        val prevIsScrollingUp = isScrollingUp
        val firstVisibleItem = ComponentListUtils.getFirstVisiblePosition(adapter.layoutManager)
        val lastVisibleItem = ComponentListUtils.getLastVisiblePosition(adapter.layoutManager)

        when {
            firstVisibleItem > mPrevFirstVisibleItem -> isScrollingUp = false
            firstVisibleItem < mPrevFirstVisibleItem -> isScrollingUp = true
            else -> scrollChanged = false
        }

        if (prevIsScrollingUp != isScrollingUp) {
            //direction changed
            //Logger.logE("", "DIRECTION CHANGED");
            okToNotifyReachingBottom = true
            okToNotifyReachingTop = true
        }

        mPrevFirstVisibleItem = firstVisibleItem

        /*detect edge reaching (reachingBottomListener || reachingTopListener)*/
        if (reachingBottomListener != null || reachingTopListener != null) {
            val visibleItemCount = ComponentListUtils.getVisibleItemCount(adapter.layoutManager)
            if (isScrolling && scrollChanged) {
                if (isScrollingUp && firstVisibleItem <= 4) {
                    if (reachingTopListener != null && okToNotifyReachingTop) {
                        okToNotifyReachingTop = false
                        //Logger.toast(recyclerView.getContext(), "reaching top " + firstVisibleItem);
                        reachingTopListener!!.onReachingTop(firstVisibleItem, visibleItemCount)
                    }
                } else if (!isScrollingUp && lastVisibleItem >= adapter.itemCount - 5) {
                    if (reachingBottomListener != null && okToNotifyReachingBottom) {
                        okToNotifyReachingBottom = false
                        //Logger.toast(recyclerView.getContext(), "reaching bottom " + lastVisibleItem);
                        reachingBottomListener!!.onReachingBottom(lastVisibleItem, visibleItemCount)
                    }
                }
            }
        }

        /*swipeRefreshLayoutListener*/
        /*boundSwipeRefreshLayout?.let { srl ->
            var enable = false
            if (recyclerView.childCount > 0) {
                // check if the first item of the list is visible
                val firstItemVisible = ComponentListUtils.getFirstVisiblePosition(adapter.layoutManager) == 0
                // check if the top of the first item is visible
                val topOfFirstItemVisible = recyclerView.getChildAt(0).top == 0
                // enabling or disabling the refresh layout
                enable = firstItemVisible && topOfFirstItemVisible
            }
            srl.isEnabled = enable
        }*/

        /*custom listener*/
        for (customScrollListener in customScrollListeners) {
            customScrollListener.onScrolled(recyclerView, dx, dy)
        }

        //mPrevScrollY = dy;
        //mPrevScrollX = dx;

        //Logger.toast(recyclerView.getContext(), "SCROLLING " + dy);
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                //Logger.logE("", "IDLE");
                isScrolling = false
                okToNotifyReachingBottom = true
                okToNotifyReachingTop = true
            }
            RecyclerView.SCROLL_STATE_SETTLING, RecyclerView.SCROLL_STATE_DRAGGING -> isScrolling = true
        }

        /*custom listener*/
        for (customScrollListener in customScrollListeners) {
            customScrollListener.onScrollStateChanged(recyclerView, newState)
        }
    }

    /**
     * Use this method when this adapter's [RecyclerView] is located inside a [SwipeRefreshLayout]
     */
    /*fun bindSwipeRefreshLayout(swipeLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout) {
        this.boundSwipeRefreshLayout = swipeLayout
    }*/

    fun addOnListScrollListener(listener: RecyclerView.OnScrollListener?) {
        if (listener != null) {
            this.customScrollListeners.add(listener)
        }
    }

    fun removeOnListScrollListener(listener: RecyclerView.OnScrollListener?) {
        if (listener != null) {
            this.customScrollListeners.remove(listener)
        }
    }

    /**
     * Sets a listener to be called when the user is currently scrolling
     * up and is close to the top of the list.
     * <br></br>Usable for refreshing/adding more items to make the scrolling smooth.
     * @param listener
     */
    fun setOnReachingTopListener(listener: OnReachingTopListener) {
        reachingTopListener = listener
    }

    /**
     * Sets a listener to be called when the user is currently scrolling
     * down and is close to the bottom of the list.
     * <br></br>Usable for refreshing/adding more items to make the scrolling smooth.
     */
    fun setOnReachingBottomListener(listener: OnReachingBottomListener) {
        reachingBottomListener = listener
    }

    fun getCustomScrollListeners(): Set<RecyclerView.OnScrollListener> {
        return customScrollListeners
    }

    interface OnReachingTopListener {
        /**
         * Called when the user is currently scrolling up and is
         * close to the first List item.
         */
        fun onReachingTop(firstVisibleItemPos: Int, visibleItemCount: Int)
    }

    interface OnReachingBottomListener {
        /**
         * Called when the user is currently scrolling down and is
         * close to the last List item.
         */
        fun onReachingBottom(lastVisibleItemPos: Int, visibleItemCount: Int)
    }
}