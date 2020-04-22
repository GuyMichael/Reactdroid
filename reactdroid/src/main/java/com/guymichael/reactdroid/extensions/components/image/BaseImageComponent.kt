package com.guymichael.reactdroid.extensions.components.image

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.model.AComponent

abstract class BaseImageComponent<P : BaseImageProps, S : OwnState, V : ImageView>(v: V)
    : AComponent<P, S, V>(v) {

    /** @param url trimmed and not empty
     * @param onErrorRes never 0
     * @param placeholder never 0*/
    protected abstract fun renderRemoteImage(url: String, @DrawableRes onErrorRes: Int?, @DrawableRes placeholder: Int? = onErrorRes)

    /** @param res never zero */
    fun renderLocalImage(@DrawableRes res: Int) {
        mView.setImageResource(res)
    }

    open fun renderNoImage() {
        mView.visibility = View.GONE
    }

    final override fun render() {
        //render remote
        props.remoteUrl?.trim()?.takeIf { it.isNotEmpty() }?.also { url ->
            renderRemoteImage(url, props.localOrOnError?.takeIf { it != 0 }, props.remotePlaceholder?.takeIf { it != 0 })
            setVisibility(View.VISIBLE)
        }
        //or local
        ?: props.localOrOnError?.takeIf { it != 0 }?.also {
            renderLocalImage(it)
            setVisibility(View.VISIBLE)
        }
        //fallback if no src
        ?: renderNoImage()
    }
}