package com.guymichael.reactdroid.extensions.components.radiobtn

import android.widget.RadioGroup
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.model.AComponent

/** Fully controlled component - must receive props for `current state` and user-change `callback` */
class CRadioGroup(v: RadioGroup) : AComponent<RadioGroupProps, EmptyOwnState, RadioGroup>(v) {
    override fun createInitialState(props: RadioGroupProps) = EmptyOwnState

    init {
        mView.setOnCheckedChangeListener { _, checkedId -> //never null (-1 for none)
            //note: apparently this callback may be called multiple (2) times when
            // state programmatically changes (from inside render() )
            if (getPropsCheckedIdOrMinus1() != checkedId) {
                //we do double-check & post (at of execution queue) to let the RadioGroup update
                // its state (from the last render() call) -> this callback is called before the
                // actual
                mView.post {
                    val actualCheckedId = mView.checkedRadioButtonId
                    if (getPropsCheckedIdOrMinus1() != actualCheckedId) {
                        if (actualCheckedId != 0 && actualCheckedId != -1) {
                            props.initial_onChecked.invoke(actualCheckedId)

                            //THINK check existence of a button with 'checkedId'
                            /*try {
                                group.findViewById<RadioButton>(checkedId)
                                props.initial_onChecked.invoke(checkedId)
                            } catch (e: IllegalStateException) {
                                //findViewById() must not be null..

                                //on errors, clear the group
                                props.initial_onChecked.invoke(null)
                            }*/
                        } else {
                            props.initial_onChecked.invoke(null)
                        }
                    }
                }

            } //else - prop (checkedId) was changed programmatically by parent (not by a user)
        }
    }




    /** Replaces 'null' and '0' with '-1' to align with Android's RadioGroup */
    private fun getPropsCheckedIdOrMinus1(props: RadioGroupProps = this.props): Int {
        return props.checkedBtnId?.takeIf { it != 0 } ?: -1
    }



    override fun render() {
        getPropsCheckedIdOrMinus1().takeIf { it != mView.checkedRadioButtonId }?.also {
            if (it == -1) { //RadioGroup treats '-1' as no-id/null
                mView.clearCheck()
            } else {
                mView.check(it)
            }
        }
    }
}





fun AComponent<*, *, *>.withRadioGroup(@IdRes id: Int) = CRadioGroup(mView.findViewById(id))