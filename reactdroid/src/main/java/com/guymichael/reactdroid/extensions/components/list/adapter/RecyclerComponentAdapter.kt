package com.guymichael.reactdroid.extensions.components.list.adapter

import android.os.Handler
import android.text.TextUtils
import android.view.*
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.guymichael.reactdroid.IntervalUtils
import com.guymichael.reactdroid.Utils
import com.guymichael.reactdroid.ViewUtils
import com.guymichael.reactdroid.extensions.components.list.ComponentListUtils
import com.guymichael.reactdroid.extensions.components.list.layouts.ListIndicatorLayout
import com.guymichael.reactdroid.extensions.components.list.layouts.recycler.SnappingRecyclerView
import com.guymichael.reactdroid.extensions.components.list.model.ListItemProps
import com.guymichael.reactdroid.model.IntervalRunnable
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
open class RecyclerComponentAdapter @JvmOverloads constructor( //THINK make Component
        val recyclerView: RecyclerView
        , items: List<ListItemProps> = emptyList()
        , orientation: Int = RecyclerView.VERTICAL
        , val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(recyclerView.context, orientation, false)
        , val viewHolderSupplier: (View) -> BaseRecyclerComponentViewHolder
    ) : RecyclerView.Adapter<BaseRecyclerComponentViewHolder>() {

    private val items: MutableList<ListItemProps> = ArrayList()
    /**Holds the layout id's of the different views this adapter is currently holding */
    private val viewTypes = ArrayList<Int>()
    /**True if [.layoutManager] is a [LinearLayoutManager] and orientation is Horizontal */
    private val isLinearLayoutOrientationHorizontal: Boolean
    private val mScrollListener = RecyclerComponentAdapterScrollListener(this)//THINK leaking 'this' in constructor

    private var customShortClickListener:
        ((parent: RecyclerView.Adapter<*>, view: View, props: ListItemProps, position: Int) -> Boolean)? = null
    private var customLongClickListener:
        ((parent: RecyclerView.Adapter<*>, view: View, props: ListItemProps, position: Int) -> Boolean)? = null
    private var customPerClassClickListeners: HashMap<Class<*>, ((ListItemProps, position: Int) -> Boolean)?>? = null
    private var emptyView: View? = null
    @LayoutRes
    private var customItemLayoutResId = 0
    private var customItemWidthFactor = -1f
    private var itemDecoration: RecyclerView.ItemDecoration? = null
    private var mEmptyStateObserver: EmptyStateDataObserver? = null
    private var emptyStateChangedListeners: MutableSet<OnListEmptyStateChangedListener>? = null
    private var onEmptyViewStateChangeListener: OnEmptyViewStateChangeListener? = null
    private var isHorizontalRelativeItemWidthEnabled = true
    private var autoscrollRunnableKey: Long? = null
    var isItemsClickable: Boolean
        get() = this.onItemTouchListener != null
        set(clickable) {
            if (clickable) {
                setRecyclerTouchClickListener(this.recyclerView)
            } else {
                removeRecyclerTouchClickListener(this.recyclerView)
            }
        }

    /*cyclic mode*/
    var isCyclic = false
        set(cyclic) {
            field = cyclic
            updateCyclicMiddleIndex()
        }
    var MIDDLE: Int = 0

    /*Listeners*/
    private var onItemTouchListener: RecyclerView.OnItemTouchListener? = null
    private var autoScrollCancelledListener: OnListAutoScrollCancelledListener? = null

    init {
        this.items.addAll(items)
        this.isLinearLayoutOrientationHorizontal = layoutManager is LinearLayoutManager
            && layoutManager.orientation == LinearLayoutManager.HORIZONTAL

        this.setHasStableIds(onSetHasStableIds())

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

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewTypeIndex: Int): BaseRecyclerComponentViewHolder {
        //        Log.e(getClass().getSimpleName(), "onCreateViewHolder: viewTypeIndex: " + viewTypeIndex);
        @LayoutRes val layoutRes = this.customItemLayoutResId.takeIf { it != 0 }
            ?: getViewRes(viewTypeIndex)//viewTypeIndex is an index/id to the actual layout resId

        /*inflate View*/
        val itemView = LayoutInflater.from(viewGroup.context).inflate(layoutRes, viewGroup, false)

        /*listener to size changes?*/
        if (this.itemDecoration != null && !hasFixedSize()) {
            itemView.addOnLayoutChangeListener(OnItemChangedSizeListener())
        }

        /*create ViewHolder*/
        return viewHolderSupplier(itemView)
    }

    override fun onBindViewHolder(viewHolder: BaseRecyclerComponentViewHolder, position: Int) {
        //        Log.e(getClass().getSimpleName(), "onBindViewHolder(position: " + position + ", viewTypeIndex: " + getItemViewType(position) + ", viewHolder.getItemViewType: " + viewHolder.getItemViewType() + ", item.getLayoutRes(): " + getItem(position).getLayoutRes() + ")");
        getItem(position)?.let {

            viewHolder.bind(it)

            /*calculate horizontal width*/
            if (isHorizontalRelativeItemWidthEnabled && isLinearLayoutOrientationHorizontal) {
                val widthFactor = if (customItemWidthFactor > 0f) customItemWidthFactor else it.getHorizontalWidthFactor()
                setRelativeCustomWidth(widthFactor, viewHolder.itemView)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var layoutRes = 0
        val item = getItem(position) ?: return -1

        if (isLinearLayoutOrientationHorizontal) {
            layoutRes = item.getHorizontalLayoutRes()
        }
        if (layoutRes == 0) {
            layoutRes = item.getLayoutRes()
        } //default getHorizontalLayoutId now does exactly this

        return getAndAddItemViewTypeIfMissing(layoutRes)
    }

    private fun getAndAddItemViewTypeIfMissing(layoutRes: Int): Int {
        var index = viewTypes.indexOf(layoutRes)
        if (index == -1) {
            //not yet saved, lock types and get/add
            synchronized(viewTypes) {
                index = viewTypes.indexOf(layoutRes)
                if (index == -1) {
                    viewTypes.add(layoutRes)
                    index = viewTypes.size - 1
                }
            }
        }

        return index
    }

    @LayoutRes
    fun getViewRes(viewTypeIndex: Int): Int {
        return viewTypes.getOrNull(viewTypeIndex)
            //if null, getAndAddItemViewTypeIfMissing() is currently running
            ?: synchronized(viewTypes) {
                viewTypes[viewTypeIndex]
            }
    }

    override fun getItemCount(): Int {
        return if (this.isCyclic) {
            Integer.MAX_VALUE
        } else this.items.size

    }

    //Note: onSingleTapUp() can sometimes ask for position -1 (!). Check also onLongPress()?
    override fun getItemId(position: Int): Long {
        if (this.isCyclic) {
            return position.toLong()
        }

        if (position < 0 || position >= this.items.size || getItem(position) == null) {
            return -1
        }

        //better to use permanent id, if possible.
        //THINK is it better? Is it worth the exception overhead?
        return getItem(position)?.id?.toLongOrNull() ?: position.toLong()
    }





    //************ All other methods are just utility methods ***************


    fun addItem(item: ListItemProps) {
        addItem(item, this.items.size)
    }

    fun addItem(item: ListItemProps, position: Int) {
        var position = position
        val currentSize = this.items.size
        if (position > currentSize) {
            position = currentSize
        }
        if (position < 0) {
            position = 0
        }

        this.items.add(position, item)

        if (isCyclic) {
            updateCyclicMiddleIndex()
        }

//        updateViewTypes()
        super.notifyItemInserted(position)
    }

    fun isEmptyViewShowing(): Boolean {
        return emptyView != null && emptyView!!.visibility == View.VISIBLE
    }

    protected fun shouldShowEmptyView(): Boolean {
        return itemCount <= 0
    }

    fun isGridLayout(): Boolean {
        return androidx.recyclerview.widget.GridLayoutManager::class.java.isInstance(layoutManager)
    }

    fun isSnappingEnabled(): Boolean {
        return (recyclerView is SnappingRecyclerView
                && recyclerView.isSnappingEnabled)
    }

    /**
     * @return actual items count, for cases where cyclic is set
     * @see {@link .setCyclic
     */
    fun getActualItemCount(): Int {
        return this.items.size
    }

    internal fun getAllItems(): List<ListItemProps> {
        return ArrayList(this.items)
    }

    protected fun getFirstVisibleItem(): ListItemProps? {
        return getItem(
                ComponentListUtils.getFirstVisiblePosition(layoutManager)
        )
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

    private fun updateCyclicMiddleIndex() {
        MIDDLE = if (this.items.size == 0) 0 else HALF_MAX_VALUE - HALF_MAX_VALUE % this.items.size
        Handler().postDelayed({ scrollImmediately(MIDDLE) }, 2000)//TODO change this ugly thing to listen to when the RecyclerView finished inflating the Views
    }

    /**
     * Override to return the value for [.setHasStableIds]. Default returns true.
     */
    protected fun onSetHasStableIds(): Boolean {
        return true
    }

    /**
     *
     * @param cyclicPosition
     * @return actual position, for cases where the list is cyclic
     * @see {@link .setCyclic
     */
    fun getActualPosition(cyclicPosition: Int): Int {
        return if (this.isCyclic) {
            cyclicPosition % getActualItemCount()
        } else cyclicPosition

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

    override fun toString(): String {
        val info = HashMap<String, Any?>()

        info["itemCount"] = itemCount
        info["viewTypeCount"] = viewTypes.size
        info["recycler"] = recyclerView.toString()
        val manager = recyclerView.layoutManager
        if (manager != null) {
            info["layoutManager"] = manager.javaClass.simpleName
            info["layoutDirection"] = if (manager.layoutDirection == OrientationHelper.VERTICAL)
                "vertical"
            else
                "horizontal"
        } else {
            info["layoutManager"] = null
        }

        return info.toString()
    }

    /**
     * @param emptyView
     * @param updateNow
     * @param listener to get notified when visibility state is about to change and be able to prevent it.
     */
    @JvmOverloads
    fun setEmptyView(emptyView: View?, updateNow: Boolean = true, listener: OnEmptyViewStateChangeListener?) {
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
     * @param tabViewResId with a drawable background with a selected and normal states
     * @param areIndicatorsClickable
     */
    fun setPageIndicator(layout: ListIndicatorLayout?, tabViewResId: Int, areIndicatorsClickable: Boolean) {
        layout?.setup(this, tabViewResId, areIndicatorsClickable)
    }

    /**
     * Uses a default [DividerItemDecoration]
     * @param drawableResId
     * @param orientation
     */
    fun setDividers(drawableResId: Int, orientation: DividerItemDecoration.DECOR_ORIENTATION): RecyclerComponentAdapter {
        if (drawableResId != 0) {
            setDividers(
                    DividerItemDecoration(this.recyclerView.context
                            , orientation
                            , Utils.getDrawable(this.recyclerView.context, drawableResId)
                    )
            )
        }

        return this
    }

    fun setDividers(decor: RecyclerView.ItemDecoration): RecyclerComponentAdapter {
        this.itemDecoration = decor

        recyclerView.addItemDecoration(decor)

        return this
    }

    //TODO setDividers with width

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

    fun setItemLayout(layoutResId: Int) {
        this.customItemLayoutResId = layoutResId
    }

    /**
     * Set item width to *factor* of total Recycler width
     * <br></br>**Relevant for [LinearLayoutManager] with horizontal layout**
     * @param factor
     */
    fun setCustomItemWidthFactor(factor: Float) {
        this.customItemWidthFactor = factor
    }

    /**
     * @param enabled
     * @param snapOneAtAtime
     */
    fun setSnapEnabled(enabled: Boolean, snapOneAtAtime: Boolean = enabled) {
        if (recyclerView == null) {
            return
        }

        if (recyclerView is SnappingRecyclerView) {
            recyclerView.setSnappingEnabled(enabled, snapOneAtAtime)
        } else {
            //TODO set disabled!
            val snapHelper = GravityPagerSnapHelper(Gravity.START)
            snapHelper.attachToRecyclerView(recyclerView)
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

    /**
     * @param newList copied to a new [List] (not deep copy!), so you can do whatever you want
     * with *newList*
     */
    fun notifyDataSetChanged(newList: List<ListItemProps>) {
        this.items.clear()
        this.items.addAll(newList)
        if (this.isCyclic) {
            updateCyclicMiddleIndex()
        }

        super.notifyDataSetChanged()
    }

    fun notifyOnItemChanged(id: String) {
        val pos = getItemPosition(id)
        if (pos >= 0) {
            super.notifyItemChanged(pos)
        }
    }

    fun notifyOnItemChanged(id: String, payload: Any) {
        val pos = getItemPosition(id)
        if (pos >= 0) {
            super.notifyItemChanged(pos, payload)
        }
    }

    fun getItemPosition(item: ListItemProps): Int {
        return this.items.indexOf(item)
    }

    /**
     * @param id
     * @return id's item position, or -1 if not found
     */
    fun getItemPosition(id: String): Int {
        if( !TextUtils.isEmpty(id)) {
            for (i in items.indices) {
                if (id == items[i].id) {
                    return i
                }
            }
        }
        return -1
    }

    protected fun getItem(index: Int): ListItemProps? {
        var position = index
        position = getActualPosition(position)

        return if (position > -1 && position < this.items.size) {
            this.items[position]
        } else null

    }

    protected fun getItem(id: String): ListItemProps? {
        for (item in items) {
            if (id == item.id) {
                return item
            }
        }
        return null
    }

    protected fun getFirstFullyVisibleItem(): ListItemProps? {
        val position = ComponentListUtils.getFirstFullyVisiblePosition(layoutManager)
        return getItem(position)
    }

    protected fun getLastFullyVisibleItem(): ListItemProps? {
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
    fun getChildIfVisible(item: ListItemProps?): View? {
        return if (item != null) {
            getChildIfVisible(item.id)
        } else {
            null
        }
    }

    fun scrollImmediately(item: ListItemProps) {
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

    fun smoothScroll(item: ListItemProps) {
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
            listener: (parent: RecyclerView.Adapter<*>, view: View, props: ListItemProps, position: Int) -> Boolean)
                : RecyclerComponentAdapter {
        this.customShortClickListener = listener
        return this
    }

    /**
     * Sets a listener which listens to long item clicks.
     * @param listener
     */
    fun onItemLongClick(
            listener: (parent: RecyclerView.Adapter<*>, view: View, props: ListItemProps, position: Int) -> Boolean)
                : RecyclerComponentAdapter {
        this.customLongClickListener = listener
        return this
    }

    /**
     * @param listener return true if click handled
     */
    fun <T : ListItemProps> onItemClick(cls: Class<T>, listener: (T, position: Int) -> Boolean) : RecyclerComponentAdapter {
        this.customPerClassClickListeners = this.customPerClassClickListeners ?: HashMap()

        this.customPerClassClickListeners?.run {
            this.put(cls) { item, position ->
                //invoke  this listener fit the class
                (item as? T?)?.let { listener(it, position) } ?: false
            }
        }

        return this
    }

    /**
     * Use this method when this adapter's [RecyclerView] is located inside a [SwipeRefreshLayout].<br></br>
     * **Note** that is method sets a [OnScrollListener] on the listView
     */
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

    private inner class ItemTouchListener(recycler: RecyclerView) : ClickItemTouchListener(recycler) {

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        override fun performItemLongClick(parent: RecyclerView, view: View, position: Int, id: Long): Boolean {
            if (view.isClickable) {
                customLongClickListener?.let {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    getItem(position)?.let { item ->
                        return it(this@RecyclerComponentAdapter, view, item, position)
                    }
                }
            }

            return true//a regular click will be called upon onSingleTapUp()
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
                handled || customPerClassClickListeners?.mapNotNull { (_, listener) ->
                    listener?.invoke(propsItem, position)
                }?.any { it }//handled response
                ?: false


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

    companion object {
        val HALF_MAX_VALUE = Integer.MAX_VALUE / 2
    }
}