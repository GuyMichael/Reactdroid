package com.guymichael.reactdroid.extensions.components.image

import android.view.View
import androidx.annotation.DrawableRes
import com.guymichael.reactdroid.applyOrGone
import com.guymichael.reactdroid.applyOrInvisible
import com.guymichael.reactdroid.model.AComponent

fun <P : BaseImageProps> AComponent<P, *, *>.renderBaseImageOrGone(props: P?, vararg visibilityBoundViews: View) {
    mView.applyOrGone(props, {
        onRender(it)
    }, *visibilityBoundViews)
}

fun <P : BaseImageProps> AComponent<P, *, *>.renderBaseImageOrInvisible(props: P?, vararg visibilityBoundViews: View) {
    mView.applyOrInvisible(props, {
        onRender(it)
    }, *visibilityBoundViews)
}

fun AComponent<ImageProps, *, *>.renderLocalImageOrGone(@DrawableRes src: Int?, vararg visibilityBoundViews: View) {
    renderBaseImageOrGone(src?.takeIf { it != 0 }?.let { ImageProps(null, it, null) }, *visibilityBoundViews) //will also check for empty
}

fun AComponent<ImageProps, *, *>.renderLocalImageOrInvisible(@DrawableRes src: Int?, vararg visibilityBoundViews: View) {
    renderBaseImageOrInvisible(src?.takeIf { it != 0 }?.let { ImageProps(null, it, null) }, *visibilityBoundViews) //will also check for empty
}

fun AComponent<ImageProps, *, *>.renderRemoteImageOrGone(src: String?, @DrawableRes onError: Int?
                                                         , @DrawableRes placeholder: Int? = onError
                                                         , vararg visibilityBoundViews: View) {

    renderBaseImageOrGone(src?.let { ImageProps(it, onError, placeholder) }, *visibilityBoundViews) //will also check for empty
}

fun AComponent<ImageProps, *, *>.renderRemoteImageOrInvisible(src: String?, @DrawableRes placeholder: Int?
                                                              , @DrawableRes onError: Int? = placeholder, vararg visibilityBoundViews: View) {

    renderBaseImageOrInvisible(src?.let { ImageProps(it, onError, placeholder) }, *visibilityBoundViews) //will also check for empty
}