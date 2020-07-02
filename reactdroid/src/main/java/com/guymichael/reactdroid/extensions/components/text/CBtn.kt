package com.guymichael.reactdroid.extensions.components.text

import android.app.Activity
import android.view.View
import android.widget.Button
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.model.AComponent

class CBtn(v: Button
        , onClickDebounceMs: Int = 100
        , passClicksWhenDisabled: Boolean = false
        , onClick: ((Button) -> Unit)? = null
    ) : BaseTextComponent<TextProps, EmptyOwnState, Button>(v) {

    init {
        if (onClick != null) {
            setOnDebouncedClickListener(onClickDebounceMs, passClicksWhenDisabled, onClick)
        }
    }

    override fun createInitialState(props: TextProps) = EmptyOwnState
}

fun withBtn(textView: Button
    , onClickDebounceMs: Int = 100
    , passClicksWhenDisabled: Boolean = false
    , onClick: ((Button) -> Unit)? = null
) = CBtn(textView, onClickDebounceMs, passClicksWhenDisabled, onClick)

fun AComponent<*, *, *>.withBtn(@IdRes id: Int
    , onClickDebounceMs: Int = 100
    , passClicksWhenDisabled: Boolean = false
    , onClick: ((Button) -> Unit)? = null
) = CBtn(mView.findViewById(id), onClickDebounceMs, passClicksWhenDisabled, onClick)

fun View.withBtn(@IdRes id: Int
    , onClickDebounceMs: Int = 100
    , passClicksWhenDisabled: Boolean = false
    , onClick: ((Button) -> Unit)? = null
) = CBtn(findViewById(id), onClickDebounceMs, passClicksWhenDisabled, onClick)

fun Activity.withBtn(@IdRes id: Int
    , onClickDebounceMs: Int = 100
    , passClicksWhenDisabled: Boolean = false
    , onClick: ((Button) -> Unit)? = null
) = CBtn(findViewById(id), onClickDebounceMs, passClicksWhenDisabled, onClick)