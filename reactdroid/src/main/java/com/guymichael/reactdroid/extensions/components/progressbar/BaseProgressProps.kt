package com.guymichael.reactdroid.extensions.components.progressbar

import android.os.Build
import androidx.annotation.RequiresApi
import com.guymichael.kotlinreact.model.OwnProps

abstract class BaseProgressProps(
        open val progress: Int
        , @RequiresApi(Build.VERSION_CODES.O) open val min: Int?
        , @RequiresApi(Build.VERSION_CODES.O) open val max: Int?
        , @RequiresApi(Build.VERSION_CODES.N) open val animateChanges: Boolean = false
    ): OwnProps() {

    override fun getAllMembers(): List<*> = listOf(
        progress, min, max
//        animateChanges    - should not affect re-renders
    )
}

data class SimpleProgressProps(
    override val progress: Int
    , @RequiresApi(Build.VERSION_CODES.O) override val min: Int? = null
    , @RequiresApi(Build.VERSION_CODES.O) override val max: Int? = null
    , @RequiresApi(Build.VERSION_CODES.N) override val animateChanges: Boolean
): BaseProgressProps(progress, min, max, animateChanges)