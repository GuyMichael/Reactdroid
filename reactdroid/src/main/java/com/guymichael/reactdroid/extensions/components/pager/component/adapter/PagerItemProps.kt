package com.guymichael.reactdroid.extensions.components.pager.component.adapter

import android.view.View
import androidx.annotation.LayoutRes
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.extensions.components.list.model.AdapterItemProps
import java.io.Serializable

/**
 * @param props for the component created using `initial_componentCreator`
 * @param initial_componentCreator create (new) component to be used as a list item (view)
 */
data class PagerItemProps<COMPONENT_PROPS : OwnProps>(
        override val id: String,
        @LayoutRes override val layoutRes: Int,
        override val props: COMPONENT_PROPS,
        override val initial_componentCreator: (layout: View) -> AComponent<COMPONENT_PROPS, *, *>
    ) : AdapterItemProps(id, layoutRes, props, initial_componentCreator)
    , Serializable {

    override fun getExtraMembers() = emptyList<Any?>()
}