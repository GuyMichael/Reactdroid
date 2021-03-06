package com.guymichael.reactdroid.core.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.kotlinreact.model.OwnProps

/**
 * A reactdroid base-class for a [Fragment], serving as a [Component].
 * It is encouraged to use it for your `Fragment`s base-class, especially for it to be treated
 * as a component from outside (delivering `props` to open, instead of the Android way of Arguments).
 *
 * **Best practice** is **not** to use **any** `Fragment` as the actual UI component, but use a single
 * `AComponent` as the top "page" component, and use a `Fragment` just to wrap it for Android-related
 * usages (e.g. a `PageViewer`).
 *
 * **Note** that while this `Fragment`'s lifecycle is converted to a reactdroid lifecycle just like any
 * other `AComponent`, this is in fact a [Component] (`AComponent`'s pure kotlin interface),
 * so it is best to avoid using it as a component or for UI rendering at all.
 */
abstract class ComponentFragment<P : OwnProps> : Fragment(), Component<P, EmptyOwnState> {
    /** **DO NOT set yourself.** */
    final override lateinit var ownState: EmptyOwnState
//        private set THINK

    /** **DO NOT set yourself.** */
    final override lateinit var props: P
//        private set THINK

    final override val forceReRenderOnRemount: Boolean = true

    /** **DO NOT set yourself.** */
    final override var reRenderPendingRemountDueToNewProps: Boolean = false
//        private set THINK
    final override var reRenderPendingRemountDueToNewState: Boolean = false
//        private set THINK




    /* API */

    /** This is called whenever the (android) system starts this fragment, with no arguments */
    abstract fun createDefaultProps(): P

    /** @return apiProps from Intent, or null if Intent doesn't have enough information to create props.
     * If null is returned, the activity will not remain open, and close immediately */
    protected open fun mapArgumentsToProps(bundle: Bundle): P? {
        @Suppress("UNCHECKED_CAST")
        return (bundle.getSerializable(ARGS_KEY_PROPS) as? P?)
    }

    /** @return a layout resource id. If you prefer to manually inflate a layout, override [inflateLayout],
     * in which case you may return 0 here */
    @LayoutRes
    protected abstract fun getLayoutRes(): Int
    protected open fun inflateLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(getLayoutRes(), container, false)
    }
    protected abstract fun onBindViews(fragmentView: View)
    protected abstract fun onBindViewListeners()









    //**** Android hooks *****/

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notifyComponentWillMount()
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?
        , savedInstanceState: Bundle?
    ): View {
        return inflateLayout(inflater, container).also {
            onBindViews(it)
            onBindViewListeners()
        }
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //first render. As Fragments don't have component parents, we have to force the first render ourselves
        (arguments?.let(::mapArgumentsToProps) ?: createDefaultProps()).also {
            UNSAFE_forceRender(it) //we use force because fragments can outlive their views!
                                   // For example a fragment in a drawer has its view recreated
                                   // when resuming, but the props (which are owned by the fragment, not the view)
                                   // are already initialized. And in most cases (e.g. props are EmptyOwnProps)
                                   // they won't get changed in-between fragment switches, so the fragment
                                   // might not re-render, even though the inner components has never been
                                   // rendered yet! (view just created)
        }
    }

    //this is where we declare ourselves as unmounted. We do is because (from the docs) :
    // "view has been detached from the fragment. The next time the fragment needs to be displayed,
    // a **new view** will be created. This is called after onStop() and before onDestroy()"
    //THINK make final (extending classes crash on runtime -
    // "...onDestroyView() overrides final method in class ...ComponentFragment..."
    /** DO NOT override. Impl. is crucial for the lifecycle */
    override fun onDestroyView() {
        waitForMountStateChangeConsumer?.invoke(false)
        super.onDestroyView()
    }


    //make final
    final override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        return super.onGetLayoutInflater(savedInstanceState)
    }
    final override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
    }
    final override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    final override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
    }
    @Suppress("DEPRECATION")
    final override fun onAttach(activity: Activity) {
        super.onAttach(activity)
    }
    final override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
    final override fun onStart() {
        super.onStart()
    }
    final override fun onResume() {
        super.onResume()
    }
    final override fun onPause() {
        super.onPause()
    }
    final override fun onStop() {
        super.onStop()
    }
    final override fun onDestroy() {
        super.onDestroy()
    }
    final override fun onDetach() {
        super.onDetach()
    }
    final override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
    }
    final override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }





    //*** react hooks ***/

    final override fun isMounted(): Boolean {
        return this.isAdded && !this.isDetached && this.view != null
    }

    private var waitForMountStateChangeConsumer: ((Boolean) -> Unit)? = null
    final override fun listenOnMountStateChanges(consumer: (Boolean) -> Unit) {
        waitForMountStateChangeConsumer = consumer
        //Already mounted (Fragment just after (see) onViewCreated).
        // onDestroyView will call back this consumer for un-mounts.
    }

    //make final and better logic
    final override fun isPropsInitialized(): Boolean {
        return this::props.isInitialized
    }

    //make final and better logic
    final override fun isStateInitialized(): Boolean {
        return this::ownState.isInitialized
    }

    //make final and implement
    final override fun createInitialState(props: P) = EmptyOwnState

    //make final
    final override fun setState(nextState: EmptyOwnState) {
        super.setState(nextState)
    }
    final override fun updateProps(nextProps: P) {
        super.updateProps(nextProps)
    }
    final override fun notifyComponentWillMount() {
        super.notifyComponentWillMount()
    }
    final override fun UNSAFE_componentWillUnmountHint() {
        super.UNSAFE_componentWillUnmountHint()
    }
    final override fun UNSAFE_componentDidMountHint() {
        super.UNSAFE_componentDidMountHint()
    }
    final override fun UNSAFE_componentDidUpdateHint(prevProps: P, prevState: EmptyOwnState, snapshot: Any?) {
        super.UNSAFE_componentDidUpdateHint(prevProps, prevState, snapshot)
    }
    final override fun onRender(nextProps: P) {
        super.onRender(nextProps)
    }
    final override fun UNSAFE_forceRender(nextProps: P) {
        super.UNSAFE_forceRender(nextProps)
    }




    companion object {
        const val ARGS_KEY_PROPS = "argsProps"
    }
}