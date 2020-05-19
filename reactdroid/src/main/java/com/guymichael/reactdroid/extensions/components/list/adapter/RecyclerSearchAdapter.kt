package com.guymichael.reactdroid.extensions.components.list.adapter

import android.os.Handler
import android.view.View
import android.widget.Filter
import android.widget.Filter.FilterListener
import android.widget.Filterable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.reactdroid.extensions.components.list.ComponentListUtils
import com.guymichael.reactdroid.extensions.components.list.adapter.model.RecyclerComponentViewHolder
import com.guymichael.reactdroid.extensions.components.list.model.ListItemProps
import java.util.*

/** Adds [Filter] to [RecyclerComponentAdapter], to be used with AutoCompleteTextView, for example  */
class RecyclerSearchAdapter(
        recyclerView: RecyclerView
        , filterSelector: (ListItemProps<*>) -> String
        , layoutManager: RecyclerView.LayoutManager
        , viewHolderSupplier: (View) -> RecyclerComponentViewHolder = ::RecyclerComponentViewHolder
    ) : RecyclerComponentAdapter(recyclerView, layoutManager, viewHolderSupplier)
    , Filterable {


    constructor(
        recyclerView: RecyclerView
        , filterSelector: (ListItemProps<*>) -> String
        , @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL
        , viewHolderSupplier: (View) -> RecyclerComponentViewHolder = ::RecyclerComponentViewHolder
    ): this(recyclerView, filterSelector
        , LinearLayoutManager(recyclerView.context, orientation, false)
        , viewHolderSupplier
    )



    /**
     * Takes the regular adapter's 'items' job as the 'all' list.<br></br>
     * The old 'items' list will be used as the filtered list
     */
    private var allItems: List<ListItemProps<*>> = ArrayList()

    /** Performs list filtering */
    private val filter = MyFilter(filterSelector)
    private var animateOnFilter = false
    private var customFilter: SearchFilter? = null

    /**
     * @param addRemove if true, items will be animated in and out when filtered
     */
    fun setAnimateOnFilter(addRemove: Boolean) {
        animateOnFilter = addRemove
    }

    /**
     * Updates all items and doesn't filter them
     * @param newList
     */
    override fun notifyDataSetChanged(newList: List<ListItemProps<*>>) {
        this.notifyDataSetChanged(newList, false)
    }

    /**
     * Updates all items
     * @param newList
     * @param filter if true, the new list is filtered against the old constraint ([MyFilter.lastFilterText])
     */
    fun notifyDataSetChanged(newList: List<ListItemProps<*>>, filter: Boolean) {
        allItems = ArrayList(newList)
        super.notifyDataSetChanged(newList)
        if (filter) {
            this.filter.notifyDataSetChanged()
        }
    }

    /**
     * @param newList MUST be a list which all of it's items are contained in [.allItems]
     */
    private fun notifyFilteredDataSetChanged(newList: List<ListItemProps<*>>) {
        super.notifyDataSetChanged(newList)
    }

    /** All items count, including filtered-out */
    fun getCachedItemCount(): Int = allItems.size

    fun getCachedItem(index: Int): ListItemProps<*>? {
        return if (index > -1 && index < allItems.size) {
            allItems[index]
        } else null
    }

    fun getCachedItem(id: String): ListItemProps<*>? {
        for (item in allItems) {
            if (id == item.id) {
                return item
            }
        }
        return null
    }

    fun getAllCachedItems(): List<ListItemProps<*>> {
        return ArrayList(allItems)
    }




    private inner class MyFilter(val selector: (ListItemProps<*>) -> String) : Filter() {
        var lastFilterText: CharSequence? = ""

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            if (animateOnFilter) {
                notifyFilteredDataSetChanged(results.values as List<ListItemProps<*>>)
            } else {
                //take the animator off
                val animator = recyclerView.itemAnimator
                recyclerView.itemAnimator = null

                //now update
                notifyFilteredDataSetChanged(results.values as List<ListItemProps<*>>)

                //restore animator (backstack)
                Handler().postDelayed({ recyclerView.itemAnimator = animator }, 0)
            }

            onFilterResultsPublished()
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredArray = this@RecyclerSearchAdapter.performFiltering(constraint, selector)
            val results = FilterResults()
            results.count = filteredArray.size
            results.values = filteredArray
            return results
        }

        fun getFilteredList(constraint: CharSequence?): List<ListItemProps<*>> {
            @Suppress("UNCHECKED_CAST")
            return performFiltering(constraint).values as List<ListItemProps<*>>
        }

        /** Filters using [.lastFilterText] */
        fun notifyDataSetChanged() {
            filter(lastFilterText)
        }

        //probably used only by AutoCompleteTextView for suggestions
        override fun convertResultToString(resultValue: Any?): CharSequence {
            return if (resultValue == null) "" else try {
                (resultValue as ListItemProps<*>).id
            } catch (e: NullPointerException) {
                super.convertResultToString(resultValue)
            } catch (e: ClassCastException) {
                super.convertResultToString(resultValue)
            }
        }
    }

    /**
     * **PLEASE DO NOT USE**
     * <br></br>Please use [.filter] or [.filter]
     * to filter, for the 'constraint' text to be saved for better use with data setters (which call filter()
     * with last 'constraint')
     * @return The default filter.
     */
    override fun getFilter(): Filter {
        return filter
    }

    /**
     * Returns the if-was-filtered list: Does not actually filters the items of this adapter.
     * @param constraint
     * @return filtered items.
     */
    fun getFilteredList(constraint: CharSequence?): List<ListItemProps<*>> {
        return filter.getFilteredList(constraint)
    }

    fun filter(constraint: CharSequence?) {
        filter.lastFilterText = constraint
        filter.filter(constraint)
    }

    fun filter(constraint: CharSequence?, listener: FilterListener?) {
        filter.lastFilterText = constraint
        filter.filter(constraint, listener)
    }

    fun setCustomFilter(filter: SearchFilter?) {
        customFilter = filter
    }

    /**
     * This is the place to add/change/override the way to filter items. This method is called off
     * the main thread so it is safe to apply long processing filters (such as Internet api calls).
     * <br></br>Default implementation calls [RecyclerSearchAdapter.filter] to filter
     * the current items by name
     * @param constraint
     * @return The filtered list
     */
    fun performFiltering(constraint: CharSequence?, selector: (ListItemProps<*>) -> String)
        : List<ListItemProps<*>> {

        return customFilter?.filter(
            allItems, constraint, selector
        )
        //or default
        ?: ComponentListUtils.filterAnyWordStartsWith(allItems, constraint, selector)
    }

    /**
     * Called on the thread that called 'filter'. Normally the main thread.
     */
    protected fun onFilterResultsPublished() {}




    interface SearchFilter {
        fun filter(list: List<ListItemProps<*>>, constraint: CharSequence?
           , selector: (ListItemProps<*>) -> String
        ): List<ListItemProps<*>>
    }


    /*@Override
    public void addItem$reactdroid(@NotNull ListItemProps item) {
        allItems.add(item);
        filter.notifyDataSetChanged();
    }

    @Override
    public void addItem$reactdroid(@NotNull ListItemProps item, int position) {
        int currentSize = allItems.size();
        if (position > currentSize) {position = currentSize; }
        if (position < 0) {position = 0; }

        allItems.add(position, item);
        filter.notifyDataSetChanged();
    }*/
}