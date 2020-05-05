package com.guymichael.reactdroid.extensions.components.list.model

import android.view.View
import androidx.annotation.LayoutRes
import com.guymichael.reactdroid.core.model.AComponent

/**
 * [props][ListItemProps] implementation for list items (`AComponents`) which need only data-class `T` as props
 * @param data a kotlin data class instance, or any class with proper hashCode & equals implementations
 * @param id the id of the `data`. Normally, data.id
 * @param layout the layout to inflate for this data
 * @param componentSupplier supplies the `AComponent` to wrap the inflated `layout`
 */
data class DataItemProps<T : Any>(
        val data: T
        , override val id: String
        , @LayoutRes private val layout: Int
        , private val componentSupplier: (View) -> AComponent<DataItemProps<T>, *, *>
    ) : ListItemProps(id) {

    override fun getLayoutRes() = layout
    override fun createComponent(v: View) = componentSupplier.invoke(v)

    override fun getAllMembersImpl() = listOf(
        layout, data
    )
}