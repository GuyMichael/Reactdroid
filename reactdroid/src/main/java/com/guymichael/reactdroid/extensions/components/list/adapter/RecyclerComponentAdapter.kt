package com.guymichael.reactdroid.extensions.components.list.adapter

import android.view.*
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.*
import com.guymichael.kotlinreact.Logger
import com.guymichael.kotlinreact.R
import com.guymichael.kotlinreact.model.OwnProps
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.guymichael.reactdroid.core.IntervalUtils
import com.guymichael.reactdroid.core.ViewUtils
import com.guymichael.reactdroid.core.getDimenPx
import com.guymichael.reactdroid.extensions.components.list.ComponentListUtils
import com.guymichael.reactdroid.extensions.components.list.layouts.ListIndicatorLayout
import com.guymichael.reactdroid.extensions.components.list.model.ListItemProps
import com.guymichael.reactdroid.core.model.IntervalRunnable
import com.guymichael.reactdroid.extensions.components.list.adapter.model.*
import com.guymichael.reactdroid.extensions.components.list.adapter.model.SimpleAdapterDataObserver
import com.guymichael.reactdroid.extensions.components.list.dividers.ListDivider
import com.guymichael.reactdroid.extensions.components.list.dividers.ListDividerOrientation
import com.guymichael.reactdroid.extensions.components.list.dividers.DividerItemDecoration
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max

/**
 * **Note:** Please use android:clickable="true" (maybe also android:focusable="true"
 * , android:focusableInTouchMode="true" for auto smooth scroll on click)
 * on the ItemView's layout in xml to allow visual feedback (e.g fast clicks press state)
 *
 * <br></br>**NOTE:** Please use [RecyclerView.setHasFixedSize] if all items are same size,
 * for performance.
 */
open class RecyclerComponentAdapter constructor(
        val recyclerView: RecyclerView
        , val layoutManager: RecyclerView.LayoutManager
    ) : BaseComponentAdapter<ListItemProps<*>>() {


    constructor(
        recyclerView: RecyclerView
        , @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL
    ): this(recyclerView, LinearLayoutManager(recyclerView.context, orientation, false))



    /**True if [.layoutManager] is a [LinearLayoutManager] and orientation is Horizontal */
    private val isLinearLayoutOrientationHorizontal = layoutManager is LinearLayoutManager
            && layoutManager.orientation == LinearLayoutManager.HORIZONTAL

    private val mScrollListener = RecyclerComponentAdapterScrollListener(this)//THINK leaking 'this' in constructor

    private var customShortClickListener:
        ((parent: RecyclerView.Adapter<*>, view: View, props: ListItemProps<*>, position: Int) -> Boolean)? = null
    private var customLongClickListener:
        ((parent: RecyclerView.Adapter<*>, view: View, props: ListItemProps<*>, position: Int) -> Unit)? = null
    private val customPerClassClickListeners by lazy {
        HashMap<Class<*>, ((ListItemProps<*>, position: Int) -> Boolean)>()
    }
    private var emptyView: View? = null
    private var itemDecoration: RecyclerView.ItemDecoration? = null
    private var mEmptyStateObserver: EmptyStateDataObserver? = null
    private var emptyStateChangedListeners: MutableSet<OnListEmptyStateChangedListener>? = null
    private var onEmptyViewStateChangeListener: OnEmptyViewStateChangeListener? = null
    private var isHorizontalRelativeItemWidthEnabled = true
    private var autoscrollRunnableKey: Long? = null
    private var snapHelper: SnapHelper? = null

    var customItemWidthFactor = -1f

    var isItemsClickable: Boolean
        get() = this.onItemTouchListener != null
        set(clickable) {
            if (clickable) {
                setRecyclerTouchClickListener(this.recyclerView)
            } else {
                removeRecyclerTouchClickListener(this.recyclerView)
            }
        }

    /*Listeners*/
    private var onItemTouchListener: RecyclerView.OnItemTouchListener? = null
    private var autoScrollCancelledListener: OnListAutoScrollCancelledListener? = null

    init {
        recyclerView.layoutManager = layoutManager

        // Setting the adapter.
        recyclerView.adapter = this//THINK leaking 'this' in constructor
        setRecyclerTouchClickListener(recyclerView)
        recyclerView.addOnScrollListener(mScrollListener)

        EmptyStateDataObserver().let {
            mEmptyStateObserver = it
            this.registerAdapterDataObserver(it)
        }
    }







    override fun onItemViewCreated(itemView: View) {
        /*listener to size changes?*/
        if (this.itemDecoration != null && !hasFixedSize()) {
            itemView.addOnLayoutChangeListener(OnItemChangedSizeListener())
        }
    }

    override fun onItemViewBound(props: ListItemProps<*>, itemView: View) {
        /*calculate horizontal width*/
        if (isHorizontalRelativeItemWidthEnabled && isLinearLayoutOrientationHorizontal) {
            setRelativeCustomWidth(
                customItemWidthFactor.takeIf { it > 0f } ?: props.horizontalWidthFactor
                , itemView
            )
        }
    }

    override fun getItemLayoutRes(item: ListItemProps<*>): Int {
        return item.horizontalLayoutRes.takeIf { isLinearLayoutOrientationHorizontal && it != 0 }
            ?: super.getItemLayoutRes(item)
    }

    override fun onCyclicMiddleIndexUpdated(cyclicMiddleIndex: Int) {
        recyclerView.post { scrollImmediately(cyclicMiddleIndex) }
    }





    //************ All other methods are just utility methods ***************


    /*internal open fun addItem(item: ListItemProps) {
        addItem(item, this.items.size)
    }

    internal open fun addItem(item: ListItemProps, position: Int) {
        var realPosition = position
        val currentSize = this.items.size
        if (realPosition > currentSize) {
            realPosition = currentSize
        }
        if (realPosition < 0) {
            realPosition = 0
        }

        this.items.add(realPosition, item)

        if (isCyclic) {
            updateCyclicMiddleIndex()
        }

        super.notifyItemInserted(realPosition)
    }*/

    fun isEmptyViewShowing(): Boolean {
        return emptyView != null && emptyView!!.visibility == View.VISIBLE
    }

    protected fun shouldShowEmptyView(): Boolean {
        return itemCount <= 0
    }

    fun isGridLayout(): Boolean {
        return androidx.recyclerview.widget.GridLayoutManager::class.java.isInstance(layoutManager)
    }

    fun isSnapEnabled(): Boolean {
        return snapHelper != null
    }

    protected fun getFirstVisibleItem(): ListItemProps<*>? {
        return getItem( ComponentListUtils.getFirstVisiblePosition(layoutManager) )
    }

    fun getActualFirstFullyVisiblePosition(): Int {
        var position = ComponentListUtils.getFirstFullyVisiblePosition(layoutManager)

        if (this.isCyclic) {
            position = getActualPosition(position)
        }

        return position
    }

    fun getActualLastFullyVisiblePosition(): Int {
        var position = ComponentListUtils.getLastFullyVisiblePosition(layoutManager)

        if (this.isCyclic) {
            position = getActualPosition(position)
        }

        return position
    }

    /**
     * for cases where [.setCyclic] is used
     * @return adapter position
     */
    fun getActualFirstVisiblePosition(): Int {
        var position = ComponentListUtils.getFirstVisiblePosition(layoutManager)

        if (this.isCyclic) {
            position = getActualPosition(position)
        }

        return position
    }

    /**
     * for cases where [.setCyclic] is used
     * @return adapter position
     */
    fun getActualLastVisiblePosition(): Int {
        var position = ComponentListUtils.getLastVisiblePosition(layoutManager)

        if (this.isCyclic) {
            position = getActualPosition(position)
        }

        return position
    }
    
    fun getVisibleItemsCount(): Int {
        return max(0, getActualLastVisiblePosition()) - max(0, getActualFirstVisiblePosition())
    }

    /** Method that allows us to get the scroll Y position of the [RecyclerView]
     */
    fun getScrollY(): Int {
        return mScrollListener.getScrollY()
    }

    /** Method that allows us to get the scroll X position of the [RecyclerView] */
    fun getScrollX(): Int {
        return mScrollListener.getScrollX()

    }

    fun isScrolling(): Boolean {
        return mScrollListener.isScrolling
    }

    private fun setRecyclerTouchClickListener(recycler: RecyclerView) {
        if (this.onItemTouchListener == null) {
            ItemTouchListener(recycler).let {
                this.onItemTouchListener = it
                recycler.addOnItemTouchListener(it)
            }
        }
    }

    private fun removeRecyclerTouchClickListener(recycler: RecyclerView) {
        this.onItemTouchListener?.let {
            recycler.removeOnItemTouchListener(it)
            this.onItemTouchListener = null
        }
    }

    /**
     * Rechecks the emptyView state and updates.
     */
    fun updateEmptyView() {
        if (emptyView != null) {
            val shouldShow = shouldShowEmptyView()
            val isNowShowing = isEmptyViewShowing()

            if (!isNowShowing && shouldShow) {
                //about to show
                if (onEmptyViewStateChangeListener != null && !onEmptyViewStateChangeListener!!.onListEmptyViewBeforeStateChange(true)) {
                    //listener wants to prevent showing.
                    return
                }
            }

            if (isNowShowing && !shouldShow) {
                //about to hide
                if (onEmptyViewStateChangeListener != null && !onEmptyViewStateChangeListener!!.onListEmptyViewBeforeStateChange(false)) {
                    //listener wants to prevent hiding.
                    return
                }
            }

            //update state
            emptyView!!.visibility = if (shouldShow) View.VISIBLE else View.GONE
        }
    }

    /**
     * @param emptyView
     * @param updateNow
     * @param listener to get notified when visibility state is about to change and be able to prevent it.
     */
    @JvmOverloads
    fun setEmptyView(emptyView: View?, updateNow: Boolean = true, listener: OnEmptyViewStateChangeListener? = null) {
        if (this.emptyView != null && this.emptyView !== emptyView) {
            //assume previous view should now be gone
            this.emptyView!!.visibility = View.GONE
        }

        this.emptyView = emptyView
        this.onEmptyViewStateChangeListener = listener
        if (updateNow) {
            updateEmptyView()
        }
    }

    /**
     * @param layout
     * @param itemLayoutRes for every indicator item. Normally with a selector drawable background,
     * with a 'selected' state (and default state)
     * @param areIndicatorsClickable if `true`, clicks on every item will scroll (smooth) the list
     * to the relevant position
     */
    fun setPageIndicator(layout: ListIndicatorLayout
            , @LayoutRes itemLayoutRes: Int = R.layout.list_indicator_item_default
            , areIndicatorsClickable: Boolean = false
        ) {

        layout.setup(this, itemLayoutRes, areIndicatorsClickable)
    }

    fun setDividers(decor: RecyclerView.ItemDecoration): RecyclerComponentAdapter {
        this.itemDecoration = decor

        recyclerView.addItemDecoration(decor)

        return this
    }

    fun setDividers(divider: ListDivider
        , topDivider: ListDivider? = divider
        , bottomDivider: ListDivider? = topDivider
        , orientation: @ListDividerOrientation Int = ComponentListUtils.getDividerOrientation(layoutManager)
    ): RecyclerComponentAdapter {

        setDividers(DividerItemDecoration(
            orientation
            , divider, topDivider, bottomDivider
        ))

        return this
    }

    fun setDividers(
            @DimenRes dividerSizeRes: Int
            , @DimenRes topDividerSizeRes: Int? = dividerSizeRes
            , @DimenRes bottomDividerSizeRes: Int? = topDividerSizeRes
        ): RecyclerComponentAdapter {

        val divider = ListDivider(recyclerView.getDimenPx(dividerSizeRes))
        val topDivider = if (topDividerSizeRes == dividerSizeRes) divider
            else topDividerSizeRes?.let { ListDivider(recyclerView.getDimenPx(it)) }

        setDividers(
            divider
            , topDivider
            , when (bottomDividerSizeRes) {
                topDividerSizeRes -> topDivider
                dividerSizeRes -> divider
                else -> bottomDividerSizeRes?.let { ListDivider(recyclerView.getDimenPx(it)) }
            }
        )

        return this
    }

    /**
     * Use when the data hasn't change, but when the dividers should update. e.g. **when an item changes it's size**.<br></br>
     * **Note: **The notify-on-size-changed can/should be used by calling [.setHasFixedSize] with 'fixed' = false.
     * @see {@link .setDividers
     */
    fun invalidateDividers() {
        if (this.itemDecoration != null) {
            recyclerView.invalidateItemDecorations()
        }
    }

    fun setHasFixedSize(fixed: Boolean) {
        this.recyclerView.setHasFixedSize(fixed)

    }

    fun hasFixedSize(): Boolean {
        return this.recyclerView.hasFixedSize()
    }

    /**
     *
     * @param animator If null, default [DefaultItemAnimator] will be used - which is the default
     * of [RecyclerView] anyway...
     */
    fun setItemAnimator(animator: RecyclerView.ItemAnimator?) {
        var itemAnimator = animator
        if (itemAnimator == null) {
            itemAnimator = DefaultItemAnimator()
        }
        // this is the default;
        // this call is actually only necessary with custom ItemAnimators
        recyclerView.itemAnimator = itemAnimator
    }

    /**
     * Note: once enabled, cannot be disabled. Call on adapter initialization.
     * @param enabled
     * @param snapOneAtAtime
     */
    fun setSnapEnabled(enabled: Boolean, snapOneAtAtime: Boolean = enabled) {
        this.snapHelper = GravityPagerSnapHelper(Gravity.START).also {
            it.attachToRecyclerView(recyclerView)
        }
    }

    @JvmOverloads
    fun setAutoScroll(intervalMs: Int, stopAtEnd: Boolean, cancelOnTouch: Boolean, listener: OnListAutoScrollCancelledListener? = null) {
        this.autoScrollCancelledListener = listener

        if (intervalMs > 0) {
            cancelAutoScroll()
            autoscrollRunnableKey = IntervalUtils.setInterval(object : IntervalRunnable() {
                override fun onTick(): Boolean {
                    if (this@RecyclerComponentAdapter.recyclerView.context == null
                            || !this@RecyclerComponentAdapter.recyclerView.isAttachedToWindow) {
                        //recyclerView is out of UI already
                        cancelAutoScroll()
                    } else {
                        //smooth scroll (loop)
                        var nextIndex = ComponentListUtils.getFirstVisiblePosition(layoutManager) + 1
                        if (nextIndex >= itemCount) {//it's actually ==
                            if (stopAtEnd) {
                                cancelAutoScroll()
                            } else {
                                nextIndex = 0
                                scrollImmediately(nextIndex, true)
                            }
                        } else {
                            smoothScroll(nextIndex)
                        }
                    }

                    return true
                }
            }, intervalMs.toLong())

            if (cancelOnTouch) {
                recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                        return true
                    }

                    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                        cancelAutoScroll()
                        rv.removeOnItemTouchListener(this)
                    }

                    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                })
            }
        }
    }

    fun setOnListAutoScrollCancelledListener(listener: OnListAutoScrollCancelledListener) {
        this.autoScrollCancelledListener = listener
    }

    fun cancelAutoScroll() {
        autoscrollRunnableKey?.let {
            if (IntervalUtils.clearInterval(it)) {
                if (this.autoScrollCancelledListener != null) {
                    this.autoScrollCancelledListener!!.onListAutoScrollCancelled()
                }
            }
        }
    }

    /** @param max may be 0 to disable recycling for this `viewType` */
    fun setMaxRecycledViewsForType(viewType: Int, max: Int) {
        recyclerView.recycledViewPool.setMaxRecycledViews(viewType, max)
    }

    /**
     * For [LinearLayoutManager] and [horizontal][LinearLayoutManager.HORIZONTAL] orientation,
     * set whether the width of the items should be relative to screen (enabled), or just wrap_content (disabled).<br></br>
     * @param enabled default is [true][.isHorizontalRelativeItemWidthEnabled]
     */
    fun setHorizontalRelativeItemWidthEnabled(enabled: Boolean) {
        this.isHorizontalRelativeItemWidthEnabled = enabled
    }

    private fun setRelativeCustomWidth(widthFactor: Float, view: View) {
        if (recyclerView.width <= 0) {
            ViewUtils.waitForViewMeasure(recyclerView) { v, _, _ -> setRelativeCustomWidth(widthFactor, v) }

            return
        }

        //TODO consider including divider width...
        val lp = view.layoutParams
        lp.width = (widthFactor * recyclerView.width).toInt()
    }

    protected fun getFirstFullyVisibleItem(): ListItemProps<*>? {
        val position = ComponentListUtils.getFirstFullyVisiblePosition(layoutManager)
        return getItem(position)
    }

    protected fun getLastFullyVisibleItem(): ListItemProps<*>? {
        val position = ComponentListUtils.getLastFullyVisiblePosition(layoutManager)
        return getItem(position)
    }

    /**
     * @param adapterPosition refers to the overall data position!
     * @return The (if visible) view at 'position' or null if not visible
     * @see .getChildIfVisible
     */
    fun getChildIfVisible(adapterPosition: Int): View? {
        return getChildIfVisible(getItem(adapterPosition))
    }

    /** @return found position or -1 if view is not visible */
    fun getViewPositionOf(adapterPosition: Int): Int {
        return getChildIfVisible(adapterPosition)?.let { //THINK performance
            recyclerView.indexOfChild(it)
        } ?: -1
    }

    /**
     * @param id of the item
     * @return The (if visible) view at 'position' or null if not visible
     * @see .getChildIfVisible
     */
    fun getChildIfVisible(id: String): View? {
        var child: View?
        for (i in 0 until recyclerView.childCount) {
            child = recyclerView.getChildAt(i)
            if (child != null) {
                val childItem = getItem(recyclerView.getChildAdapterPosition(child))
                if (childItem != null && childItem.id == id) {
                    return child
                }
            }
        }

        return null
    }

    /**
     * @param item
     * @return The 'item's View, or null if 'item' is not visible
     */
    fun getChildIfVisible(item: ListItemProps<*>?): View? {
        return if (item != null) {
            getChildIfVisible(item.id)
        } else {
            null
        }
    }

    fun scrollImmediately(item: ListItemProps<*>) {
        val pos = items.indexOf(item)
        if (pos > -1) {
            scrollImmediately(pos)
        }
    }

    /**
     * Convenience method scroll to bottom. Calls [.scrollImmediately] with notifyScrollListener = false
     */
    fun scrollImmediatelyToBottom() {
        scrollImmediately(items.size - 1, false)
    }

    /**
     * @param position
     * @param notifyScrollListener When scrolling immediately, the listener is not called (with state IDLE)
     */
    @JvmOverloads
    fun scrollImmediately(position: Int, notifyScrollListener: Boolean = false) {
        if (this.isCyclic || position > -1 && position < items.size) {
            if (recyclerView.width <= 0 && recyclerView.height <= 0) {
                ViewUtils.waitForViewMeasure(recyclerView) { _, _, _ -> scrollImmediately(position, notifyScrollListener) }

                return
            }

            recyclerView.scrollToPosition(position)

            if (notifyScrollListener) {
                this.recyclerView.postDelayed({
                    for (customScrollListener in mScrollListener.customScrollListeners) {
                        customScrollListener.onScrollStateChanged(this@RecyclerComponentAdapter.recyclerView, RecyclerView.SCROLL_STATE_IDLE)
                    }
                }, 0)
            }
        }
    }

    fun smoothScroll(item: ListItemProps<*>) {
        val pos = items.indexOf(item)
        if (pos > -1) {
            smoothScroll(pos)
        }
    }


    fun smoothScrollToBottom() {
        val pos = items.size - 1
        if (pos > -1) {
            smoothScroll(pos)
        }
    }

    /**
     * @param position safe to call out of bounds position. Will do nothing.
     */
    fun smoothScroll(position: Int) {
        if (this.isCyclic || position > -1 && position < items.size) {
            if (recyclerView.width <= 0 && recyclerView.height <= 0) {
                ViewUtils.waitForViewMeasure(recyclerView) { _, _, _ -> smoothScroll(position) }

                return
            }

            recyclerView.smoothScrollToPosition(position)
        }
    }

    fun smoothScrollBy(dx: Int, dy: Int) {
        if (recyclerView.width <= 0 && recyclerView.height <= 0) {
            ViewUtils.waitForViewMeasure(recyclerView) { _, _, _ -> smoothScrollBy(dx, dy) }

            return
        }

        recyclerView.smoothScrollBy(dx, dy)
    }

    /** @param itemCount may be negative to scroll left */
    fun smoothScrollItemOffset(itemCount: Int = 1) {
        //remember, may be cyclic
        if (itemCount != 0) {
            smoothScroll(getActualLastVisiblePosition() + itemCount)
        }
    }

    /**
     * Note: note working while scrolling
     * @param position
     * @return
     * @see {@link .isScrolling
     */
    fun isItemCompletelyVisible(position: Int): Boolean {
        return if (position < 0 || position >= itemCount) {
            false
        } else position >= ComponentListUtils.getFirstFullyVisiblePosition(layoutManager)
                && position <= ComponentListUtils.getLastFullyVisiblePosition(layoutManager)
    }

    /**
     * Listen on when the state between has data and empty list changes.
     * @param listener
     * @param notifyListenerNow if true and listener was added (this method returns true), this listener (and only this one) gets notified immediately of the current state.
     * @return True if listener was added.
     */
    fun addOnListEmptyStateChangedListener(listener: OnListEmptyStateChangedListener?, notifyListenerNow: Boolean): Boolean {
        if (listener == null) {
            return false
        }
        if (emptyStateChangedListeners == null) {
            emptyStateChangedListeners = HashSet()
        }
        val added = emptyStateChangedListeners!!.add(listener)

        if (added && notifyListenerNow) {
            listener.onListEmptyStateChanged(itemCount <= 0)
        }

        return added
    }

    fun removeOnListEmptyStateChangedListener(listener: OnListEmptyStateChangedListener?): Boolean {
        return listener != null && emptyStateChangedListeners != null && emptyStateChangedListeners!!.remove(listener)
    }

    /*
     * <b>NOTE:</b> Currently only works with
     * {@link LinearLayoutManager}
     */
    fun showRowsFromBottom(fromBottom: Boolean) {
        if (layoutManager is LinearLayoutManager) {
            layoutManager.stackFromEnd = fromBottom
        }
    }

    /**
     * Sets a listener which listens to regular item clicks.
     * @param listener
     */
    fun onItemClick(
            listener: (parent: RecyclerView.Adapter<*>, view: View, props: ListItemProps<*>, position: Int) -> Boolean
        ): RecyclerComponentAdapter {

        this.customShortClickListener = listener
        return this
    }

    /**
     * Sets a listener which listens to long item clicks.
     * @param listener
     */
    fun onItemLongClick(
            listener: (parent: RecyclerView.Adapter<*>, view: View, props: ListItemProps<*>, position: Int) -> Unit
        ): RecyclerComponentAdapter {

        this.customLongClickListener = listener
        return this
    }

    /**
     * @param listener return true if click handled to show click feedback (View.isPressed = true)
     */
    fun <P : OwnProps> onItemClick(cls: Class<P>, listener: (P, position: Int) -> Boolean) : RecyclerComponentAdapter {
        this.customPerClassClickListeners[cls] = { item, position ->
            @Suppress("UNCHECKED_CAST")
            try {
                (item.props as? P)?.let { props -> listener(props, position) } ?: false
            } catch (e: ClassCastException) {
                //THINK know the type before-hand
                false
            }
        }

        return this
    }


    /** Use this method when this adapter's [RecyclerView] is located inside a [SwipeRefreshLayout] */
    /*fun bindSwipeRefreshLayout(swipeLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout) {
        mScrollListener.bindSwipeRefreshLayout(swipeLayout)
    }*/

    fun addOnListScrollListener(listener: RecyclerView.OnScrollListener) {
        mScrollListener.addOnListScrollListener(listener)
    }

    fun removeOnListScrollListener(listener: RecyclerView.OnScrollListener) {
        mScrollListener.removeOnListScrollListener(listener)
    }

    /**
     * Sets a listener to be called when the user is currently scrolling
     * up and is close to the top of the list.
     * <br></br>Usable for refreshing/adding more items to make the scrolling smooth.
     * @param listener
     */
    fun setOnReachingTopListener(listener: RecyclerComponentAdapterScrollListener.OnReachingTopListener) {
        mScrollListener.setOnReachingTopListener(listener)
    }

    /**
     * Sets a listener to be called when the user is currently scrolling
     * down and is close to the bottom of the list.
     * <br></br>Usable for refreshing/adding more items to make the scrolling smooth.
     */
    fun setOnReachingBottomListener(listener: RecyclerComponentAdapterScrollListener.OnReachingBottomListener) {
        mScrollListener.setOnReachingBottomListener(listener)
    }

    override fun toString(): String {
        return super.toString() +
                "\n${recyclerView.javaClass.simpleName}: " +
                "${layoutManager.javaClass.simpleName}{orientation: ${ComponentListUtils.getOrientation(layoutManager)}}"
    }






    private inner class ItemTouchListener(recycler: RecyclerView) : ClickItemTouchListener(recycler) {

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        override fun performItemLongClick(parent: RecyclerView, view: View, position: Int, id: Long) {
            customLongClickListener?.let { listener ->
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                getItem(position)?.let { item ->
                    listener.invoke(this@RecyclerComponentAdapter, view, item, position)
                }
            }
        }

        override fun performItemClick(parent: RecyclerView, view: View, position: Int, id: Long): Boolean {
            return if (view.isClickable) {
                val propsItem = getItem(position) ?: return false
                var handled = false

                //simple listener
                handled = handled || customShortClickListener?.let {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    it(this@RecyclerComponentAdapter, view, propsItem, position)
                } ?: false

                //per-class listener(s)
                handled || customPerClassClickListeners.mapNotNull { (_, listener) ->
                    listener.invoke(propsItem, position)
                }.any { it }//handled response

            } else { false }
        }
    }

    private inner class OnItemChangedSizeListener : View.OnLayoutChangeListener {
        internal var wentThroughFirstLayout = false

        override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            if (!wentThroughFirstLayout) {
                wentThroughFirstLayout = true
                return
            }

            val widthWas = oldRight - oldLeft // right exclusive, left inclusive
            if (widthWas > 0 && v.width != widthWas && ComponentListUtils.getOrientation(layoutManager) == OrientationHelper.HORIZONTAL) {
                // width has changed
                onWidthChanged(widthWas, v.width)
            }

            val heightWas = oldBottom - oldTop // bottom exclusive, top inclusive
            if (heightWas > 0 && v.height != heightWas && ComponentListUtils.getOrientation(layoutManager) == OrientationHelper.VERTICAL) {
                // height has changed
                onHeightChanged(heightWas, v.height)
            }
        }

        private fun onWidthChanged(oldWidth: Int, newWidth: Int) {
            //            Log.e("RECYCLER ADAPTER", "WIDTH CHANGED: " + oldWidth + " to " +newWidth);
            onSizeChangedIntl()
        }

        private fun onHeightChanged(oldHeight: Int, newHeight: Int) {
            //            Log.e("RECYCLER ADAPTER", "HEIGHT CHANGED: " + oldHeight + " to " +newHeight);
            onSizeChangedIntl()
        }

        private fun onSizeChangedIntl() {
            //TODO
            //invalidateDividers();
        }
    }

    interface OnListAutoScrollCancelledListener {
        fun onListAutoScrollCancelled()
    }

    private inner class EmptyStateDataObserver : SimpleAdapterDataObserver() {
        internal var isPrevStateEmpty: Boolean = false

        init {
            this.isPrevStateEmpty = itemCount <= 0
        }

        override fun onChanged() {
            onChanged(itemCount)
        }

        fun onChanged(itemCount: Int) {
            val isNewStateEmpty = itemCount <= 0

            if (isNewStateEmpty != isPrevStateEmpty) {
                isPrevStateEmpty = isNewStateEmpty

                updateEmptyView()

                if (emptyStateChangedListeners != null) {
                    for (listener in emptyStateChangedListeners!!) {
                        listener.onListEmptyStateChanged(isNewStateEmpty)
                    }
                }
            } else if (isPrevStateEmpty && !isEmptyViewShowing()) {
                //first notifyDataSetChanged call after constructor
                updateEmptyView()
            }
        }
    }

    interface OnListEmptyStateChangedListener {
        fun onListEmptyStateChanged(isListEmpty: Boolean)
    }

    interface OnEmptyViewStateChangeListener {
        /**
         * @param aboutToShow if true, view is about to be shown. If false, about to be hidden
         * @return True to allow state change, false to prevent it.
         */
        fun onListEmptyViewBeforeStateChange(aboutToShow: Boolean): Boolean
    }
}