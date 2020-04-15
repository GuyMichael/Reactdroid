package com.guymichael.kotlinflux.model

import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.HOC
import com.guymichael.kotlinreact.model.OwnProps
import io.reactivex.rxjava3.disposables.Disposable

/**
 * An interface representing a Component that is connected to a 'flux' [Store].
 * It is the binding point between 3 streams, and we are in charge of merging them:
 * 1. COMPONENT_PROPS - This HOC's 'inner'component's props
 * 2. global state
 * 3. our own onRender(apiProps: API_PROPS) - us being a Component<API_PROPS> -
 *    for parent components to render us from their render() method, with easy, minimized 'api' props,
 *     e.g. some model id
 * --> enters [mapStateToProps], to merge all the 3 : (state, apiProps) -> componentProps
 */
//TODO try to implement StoreObserver
interface ConnectedHOC<API_PROPS: OwnProps, COMPONENT_PROPS: OwnProps, C : Component<COMPONENT_PROPS, *>>
    : HOC<API_PROPS, COMPONENT_PROPS, C> {

    /** **CONTRACT:**
     * 1. DO NOT set yourself! Never!
     * 2. Just override and init. with null
     * */
    var storeDisposable: Disposable?

    //used to cache the merged props from onControlledRender (called off main-thread) until it's
    //following render(), so not to create them again (on the main thread)
    /** **CONTRACT:**
     * 1. DO NOT set or get (use) yourself! Never!
     * 2. Just override and init. with null
     * */
    var innerComponentProps: COMPONENT_PROPS?

    /**
     * Pure and without any side effects or outside params
     * THINK enforce
     * */
    fun getStore() : Store

    /**
     * Pure and without any side effects or outside params THINK enforce
     *
     * This is where we connect to the global state (props-wise)
     *
     * Note: This is where we merge all 3 streams: (state, apiProps) -> componentProps
     *       this method will be called from two flows:
     *       1. api props changes (from parent Component)
     *       2. (global) state changes
     */
    fun mapStateToProps(state: GlobalState, apiProps: API_PROPS): COMPONENT_PROPS







    /* Privates / internals */

    /* Component*/

    /** **CONTRACT:** DO NOT call or override yourself, consider as a private method */
    override fun UNSAFE_componentDidMountHint() {
        this.getStore().subscribe(this)
    }

    /** **CONTRACT:** DO NOT call or override yourself, consider as a private method */
    override fun UNSAFE_componentWillUnmountHint() {
        this.getStore().unsubscribe(this)
    }

    /** **CONTRACT:** DO NOT call or override yourself, consider as a private method */
    override fun render() {
        this.innerComponentProps.let { cachedProps ->
            this.innerComponentProps = null//ASAP

            if (cachedProps == null) {
                //this render was from a parent, use default HOC implementation
                super.render()
            } else {
                //this render was from a Store (controlled), so we know we should (re)render
                mComponent.UNSAFE_forceRender(cachedProps)
            }
        }
    }





    /* HOC */

    override fun mapToComponentProps(hocProps: API_PROPS): COMPONENT_PROPS {
        return mapStateToProps(getStore().state, hocProps)
    }




    /* ConnectedHOC */

    /** **CONTRACT:** DO NOT override or call yourself! consider private/internal and final */
    fun UNSAFE_onControlledRender(props: COMPONENT_PROPS) {

        try {
            this.props.let {
                //cache props to be used by the next render() so not to recreate
                innerComponentProps = props

                //continue normally with the onRender chain, but with forceUpdate = true,
                //considering the controller (e.g. a Store) has already checked 'shouldComponentUpdate()',
                //off the main-read
                super.UNSAFE_forceRender(it)
            }
        } catch (e: UninitializedPropertyAccessException) {
            //this means we were called to render by the Store, and before any parent (with API_PROPS),
            //so we haven't passed first render, and don't intend to (until a parent wants us to).
        }
    }

    //this is where we use the fact that a Store calls us off main-thread, and already return
    //the inner component (merged) props
    @Throws(UninitializedPropertyAccessException::class)
    fun mapStateToProps(state: GlobalState): COMPONENT_PROPS = mapStateToProps(state, this.props)

    /** **CONTRACT:** DO NOT call or override yourself, consider as a private method */
    fun shouldComponentReceiveStateChanges(): Boolean = isMounted()

    //we use this method to let the Store decide if we should re-render, because it does it off the main-thread
    /** **CONTRACT:** DO NOT call or override yourself, consider as a private method */
    fun shouldInnerComponentUpdate(nextProps: COMPONENT_PROPS): Boolean {
        return mComponent.shouldComponentUpdate(nextProps)
    }
}