package com.guymichael.reactdroid.extensions.components.image

import android.widget.ImageView
import com.guymichael.kotlinreact.model.EmptyOwnState


/**
 * Extend and add your remote image rendering logic
 */
abstract class CImage(v: ImageView) : BaseAImageComponent<AImageProps, EmptyOwnState, ImageView>(v) {

    override fun createInitialState(props: AImageProps) = EmptyOwnState
}