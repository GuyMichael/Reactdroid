package com.guymichael.reactdroid.extensions.components.list.model

import android.view.View
import androidx.annotation.LayoutRes
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.model.AComponent
import java.io.Serializable

abstract class AdapterItemProps(
        open val id: String,
        @LayoutRes open val layoutRes: Int,
        open val props: OwnProps,
        open val initial_componentCreator: (layout: View) -> AComponent<*, *, *>
    ) : OwnProps(), Serializable {

    final override fun getAllMembers(): List<*> = listOf(
        id, layoutRes, props
    ).plus(getExtraMembers())

    abstract fun getExtraMembers(): List<*>
}