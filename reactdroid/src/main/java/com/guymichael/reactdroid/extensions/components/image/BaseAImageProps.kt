package com.guymichael.reactdroid.extensions.components.image

import androidx.annotation.DrawableRes
import com.guymichael.kotlinreact.model.OwnProps

abstract class BaseAImageProps(
        open val remoteUrl: String?
        , @DrawableRes open val localOrOnError: Int?
        , @DrawableRes open val remotePlaceholder: Int?//excluded from 'all members' as changing it should result in no action
    ) : OwnProps() {


    override fun getAllMembers(): List<*> = listOf(
        remoteUrl, localOrOnError
    )
}