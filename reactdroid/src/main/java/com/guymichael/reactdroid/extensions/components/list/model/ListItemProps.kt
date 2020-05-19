package com.guymichael.reactdroid.extensions.components.list.model

import android.view.View
import androidx.annotation.LayoutRes
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.model.AComponent
import java.io.Serializable

/**
 * @param initial_componentCreator create (new) component to be used as a list item (view)
 * @param props for the component created using `initial_componentCreator`
 */
data class ListItemProps(
        override val id: String,
        @LayoutRes override val layoutRes: Int,
        override val props: OwnProps,
        override val initial_componentCreator: (layout: View) -> AComponent<*, *, *>,
        @LayoutRes val horizontalLayoutRes: Int = layoutRes,
        val horizontalWidthFactor: Float = 1F
    ) : AdapterItemProps(id, layoutRes, props, initial_componentCreator)
    , Serializable {

    override fun getExtraMembers() = listOf(
        horizontalLayoutRes, horizontalWidthFactor
    )
}