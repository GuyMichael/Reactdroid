package com.guymichael.reactdroid.extensions.components.list.model

import android.view.View
import androidx.annotation.LayoutRes
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.model.AComponent
import java.io.Serializable

abstract class ListItemProps(open val id: String) : OwnProps(), Serializable {
    @LayoutRes
    open fun getHorizontalLayoutRes(): Int = getLayoutRes()
    open fun getHorizontalWidthFactor(): Float = 1F
    final override fun getAllMembers(): List<*> = listOf(id).plus(getAllMembersImpl())

    @LayoutRes
    abstract fun getLayoutRes(): Int
    abstract fun getAllMembersImpl(): List<*>

    abstract fun createComponent(v: View): AComponent<*, *, *>
    open fun mapToComponentProps(): OwnProps = this
}