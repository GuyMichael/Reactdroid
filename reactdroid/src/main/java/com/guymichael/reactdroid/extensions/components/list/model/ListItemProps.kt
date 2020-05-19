package com.guymichael.reactdroid.extensions.components.list.model

import android.view.View
import androidx.annotation.LayoutRes
import com.guymichael.kotlinreact.model.EmptyOwnProps
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.model.AComponent
import java.io.Serializable

/**
 * @param initial_componentCreator create (new) component to be used as a list item (view)
 * @param props for the component created using `initial_componentCreator`
 */
data class ListItemProps<COMPONENT_PROPS : OwnProps>(
        override val id: String,
        @LayoutRes override val layoutRes: Int,
        override val props: COMPONENT_PROPS,
        override val initial_componentCreator: (layout: View) -> AComponent<COMPONENT_PROPS, *, *>,
        @LayoutRes val horizontalLayoutRes: Int = layoutRes,
        val horizontalWidthFactor: Float = 1F
    ) : AdapterItemProps(id, layoutRes, props, initial_componentCreator)
    , Serializable {

    override fun getExtraMembers() = listOf(
        horizontalLayoutRes, horizontalWidthFactor
    )




    companion object {
        /**
         * Convenience method for creating [items][ListItemProps] with **unique** layouts -
         * the `layoutRes` (first param of the `Pair`) will serve as the item's `id`.
         * The `items` should receive only empty props. Basically this method is to be used
         * when using a list-component as a pager - and so every page is unique.
         *
         * @param items `List` of `Pair`s : `@LayoutRes Int` to `AComponent` creator
         */
        fun listFromUniqueLayouts(vararg items: Pair<Int, (View) -> AComponent<EmptyOwnProps, *, *>>)
        : List<ListItemProps<*>> {
            return items.map { (layoutRes, componentCreator) ->
                ListItemProps("$layoutRes", layoutRes, EmptyOwnProps, componentCreator)
            }
        }
    }
}