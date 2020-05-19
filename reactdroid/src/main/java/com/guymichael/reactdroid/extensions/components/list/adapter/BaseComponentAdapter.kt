package com.guymichael.reactdroid.extensions.components.list.adapter

import android.text.TextUtils
import android.view.*
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.reactdroid.extensions.components.list.adapter.model.*
import com.guymichael.reactdroid.extensions.components.list.model.AdapterItemProps
import java.util.*

open class BaseComponentAdapter<ITEM_PROPS : AdapterItemProps>(
        open val viewHolderSupplier: (View) -> BaseComponentViewHolder<ITEM_PROPS>
    ) : RecyclerView.Adapter<BaseComponentViewHolder<ITEM_PROPS>>() {

    protected val items: MutableList<ITEM_PROPS> = ArrayList()

    /**Holds the layout id's of the different views this adapter is currently holding */
    private val viewTypes = ArrayList<Int>()

    @LayoutRes var customItemLayoutResId = 0

    private var autoscrollRunnableKey: Long? = null

    /*cyclic mode*/
    var isCyclic = false
        set(cyclic) {
            field = cyclic
            updateCyclicMiddleIndex()
        }

    private var cyclicMiddleIndex: Int = 0

    init {
        this.setHasStableIds(true)
    }

    final override fun onCreateViewHolder(viewGroup: ViewGroup, viewTypeIndex: Int): BaseComponentViewHolder<ITEM_PROPS> {
        @LayoutRes val layoutRes = this.customItemLayoutResId.takeIf { it != 0 }
            ?: getViewRes(viewTypeIndex)//viewTypeIndex is an index/id to the actual layout resId

        /*inflate View*/
        val itemView = LayoutInflater.from(viewGroup.context).inflate(layoutRes, viewGroup, false)

        /*apply optional listeners on itemView*/
        onItemViewCreated(itemView)

        /*create ViewHolder*/
        return viewHolderSupplier(itemView)
    }

    /** Callback from [onCreateViewHolder] after the relevant view `itemView` has been created
     * and just before it is wrapped with a view holder */
    open fun onItemViewCreated(itemView: View) {}

    final override fun onBindViewHolder(viewHolder: BaseComponentViewHolder<ITEM_PROPS>, position: Int) {
        getItem(position)?.let {

            viewHolder.bind(it)

            onItemViewBound(it, viewHolder.itemView)
        }
    }

    /** Callback from [onBindViewHolder] after the relevant view `itemView` has been
     * [bound][BaseComponentViewHolder.bind] to `props` */
    open protected fun onItemViewBound(props: ITEM_PROPS, itemView: View) {}

    final override fun getItemViewType(position: Int): Int {
        return getItem(position)?.let {
            getAndAddItemViewTypeIfMissing(getItemLayoutRes(it))
        } ?: -1
    }

    /** Supply a layout for `item` */
    @LayoutRes
    open protected fun getItemLayoutRes(item: ITEM_PROPS): Int {
        return item.layoutRes
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

    /**
     * @return actual items count, for cases where cyclic is set
     * @see {@link .setCyclic
     */
    fun getActualItemCount(): Int {
        return this.items.size
    }

    /**
     *
     * @param cyclicPosition
     * @return actual position, for cases where the list is cyclic
     * @see {@link .setCyclic
     */
    fun getActualPosition(cyclicPosition: Int): Int {
        return if (this.isCyclic) {
            cyclicPosition % (getActualItemCount().takeIf { it > 0 } ?: 1)
        } else cyclicPosition
    }

    /**
     * @param newList copied to a new [List] (not deep copy!), so you can do whatever you want
     * with *newList*
     */
    open fun notifyDataSetChanged(newList: List<ITEM_PROPS>) {
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

    fun getItemPosition(item: ITEM_PROPS): Int {
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





    /* privates/protected */

    protected open fun onCyclicMiddleIndexUpdated(cyclicMiddleIndex: Int) {}

    protected open fun getItem(index: Int): ITEM_PROPS? {
        var position = index
        position = getActualPosition(position)

        return if (position > -1 && position < this.items.size) {
            this.items[position]
        } else null

    }

    protected open fun getItem(id: String): ITEM_PROPS? {
        for (item in items) {
            if (id == item.id) {
                return item
            }
        }
        return null
    }

    internal open fun getAllItems(): List<ITEM_PROPS> {
        return ArrayList(this.items)
    }

    private fun getAndAddItemViewTypeIfMissing(@LayoutRes layoutRes: Int): Int {
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

    private fun updateCyclicMiddleIndex() {
        cyclicMiddleIndex = if (this.items.size == 0) 0 else HALF_MAX_VALUE - HALF_MAX_VALUE % this.items.size
        onCyclicMiddleIndexUpdated(cyclicMiddleIndex)
    }



    override fun toString(): String {
        return "${javaClass}{itemCount: $itemCount, viewTypeCount: ${viewTypes.size}}"
    }



    companion object {
        private const val HALF_MAX_VALUE = Integer.MAX_VALUE / 2
    }
}