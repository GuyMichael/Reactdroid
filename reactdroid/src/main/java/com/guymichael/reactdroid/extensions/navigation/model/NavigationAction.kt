package com.guymichael.reactdroid.extensions.navigation.model

import android.view.View
import androidx.core.util.Pair
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.extensions.navigation.ClientPageIntf

class NavigationAction
    private constructor(
        val page: ClientPageIntf
        , val opened: Boolean
        , val props: OwnProps?
        , val inOutAnimations: Pair<Int?, Int?>?
        , val transitions: Array<Pair<View, String>>?
        , val forResult_requestCode: Int?
        , val showLoader: Boolean
    ) {


    companion object {
        /** If already open, page/activity will re-render will the new props (if changed) */
        @JvmStatic
        @JvmOverloads
        fun open(page: ClientPageIntf, props: OwnProps
                 , inOutAnimations: Pair<Int?, Int?>? = null
                 , transitions: Array<Pair<View, String>>? = null
                 , forResult_requestCode: Int? = null
                 , showLoader: Boolean = false
            )
            = NavigationAction(page, true, props
                , inOutAnimations, transitions, forResult_requestCode, showLoader
            )

        fun close(page: ClientPageIntf)
            = NavigationAction(page, false, null, null, null
                , null, false
            )
    }
}