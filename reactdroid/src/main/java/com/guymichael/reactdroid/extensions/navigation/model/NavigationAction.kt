package com.guymichael.reactdroid.extensions.navigation.model

import android.view.View
import androidx.core.util.Pair
import com.guymichael.kotlinreact.model.OwnProps

class NavigationAction<P : OwnProps>(
    val props: P
    , val inOutAnimations: Pair<Int?, Int?>?
    , val transitions: Array<Pair<View, String>>?
    , val forResult_requestCode: Int?
    , val showLoader: Boolean
)