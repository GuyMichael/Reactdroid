package com.guymichael.kotlinreact.model

import com.guymichael.kotlinreact.Logger
import com.guymichael.kotlinreact.BuildConfig
import com.guymichael.kotlinreact.Utils
import com.guymichael.kotlinreact.letIfTrue

//won't be rendered until the first call to onRender(nextProps)
interface Component<P : OwnProps, S : OwnState> {
    /** **CONTRACT:**
     * 1. DO NOT set yourself! Never!
     * 2. Just override and define as 'lateinit var'
     * */
    var ownState : S //THINK protected (against outside setters)

    /** **CONTRACT:**
     * 1. DO NOT set yourself! Never!
     * 2. Just override and define as 'lateinit var'
     * */
    var props : P //THINK protected (against outside setters)
    //THINK what if not defined as lateinit ? This will break the first render logic! We assume props aren't initiatlized before first render

    val forceReRenderOnRemount: Boolean

    /** **CONTRACT:**
     * 1. DO NOT set yourself! Never!
     * 2. Just override and define as 'var' and init with `false`
     * */
    var reRenderOnRemountDueToNewProps: Boolean//if props have been updated during unmount period

    /* To override (or optional) */

    /**
     * @return true if this component is mounted on the window and 'alive', false otherwise
     */
    fun isMounted(): Boolean

    /**
     * @param consumer invoke (multiple times) when this Component is either attached (mounted)
     * to the window (with `true`), or detached (unmounted) (with `false`).
     * It is OK to call back immediately if conditions are already met (but not mandatory)
     *
     * Keep in mind that `consumer` is considered to be registered only when this component
     * is alive, so:
     *
     * 1. no side effects when implementing the registration
     * (remain in this component and its members' scope)
     *
     * 2. sanity check for #1 - when this component is destroyed (e.g. garbage collected), the
     * `consumer` should already by unregistered.
     * Normally the extending component will hold a view member and listen on it, so when the view
     * is collected, so is the registration
     */
    fun listenOnMountStateChanges(consumer: (Boolean) -> Unit)

    /** create the initial state. This is where 'ownState' will be first initialized.
     * You may use 'this.props' to derive your initial state */
    fun createInitialState(props: P): S



    /* Lifecycle */

    /** **CONTRACT:** DO NOT call yourself.
     * On re-mounts, may be called after the actual mounting/attach-to-window, but still as part of a correct lifecycle order
     * NOTICE: in order to make use for before first-mount/render, you must call [notifyComponentWillMount] when applicable on your platform */
    fun componentWillMount() {}
    /** **CONTRACT:** DO NOT call yourself, called by the system */
    fun componentDidMount() {}
    /** **CONTRACT:** DO NOT call yourself, called by the system */
    fun shouldComponentUpdate(nextProps: P): Boolean {
        return isPassedOrDuringRender() && !Utils.shallowEquality(this.props, nextProps)
    }
    /** **CONTRACT:** DO NOT call yourself, called by the system */
    fun shouldComponentUpdate(nextState: S): Boolean {
        return isPassedOrDuringRender() && !Utils.shallowEquality(this.ownState, nextState)
    }
    /** **CONTRACT:**
     * 1. DO NOT call yourself, called by the system
     * 2. MUST call super to let the whole inheritance chain get a peak
     *
     * @return anything you'd like to be passed to componentDidUpdate as third parameter*/
    fun getSnapshotBeforeUpdate(prevProps: P, prevState: S): Any? { return null }
    /** **CONTRACT:** DO NOT call yourself, called by the system */
    fun render()
    /** **CONTRACT:** DO NOT call yourself, called by the system */
    fun componentDidUpdate(prevProps: P, prevState: S, snapshot: Any?) {}
    /** **CONTRACT:** DO NOT call yourself, called by the system */
    fun componentWillUnmount() {}
    /** **CONTRACT:** DO NOT override or call this method unless you know what you're doing,
     * use [UNSAFE_forceRender] */
    fun UNSAFE_forceRender(nextProps: P) {
        onRenderImpl(nextProps, true)
    }






    /* API's */

    /** **CONTRACT:** DO NOT override. call this method inside a parent Components' render()
     * @param nextProps
     */
    fun onRender(nextProps: P) {
        onRenderImpl(nextProps, false)
    }

    @Throws(IllegalArgumentException::class)
    fun onRenderOrThrow(nextProps: OwnProps) {
        //THINK casting
        try {
            @Suppress("UNCHECKED_CAST")
            onRender(nextProps as P)
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("onRenderOrThrow() : nextProps do not match this Component's props type")
        }
    }

    /** **CONTRACT:** DO NOT override, call to update this Component's OwnState.
     * You must never call this method before component is mounted - just implement [createInitialState] */
    //TODO use rx to combine emittions with onRender()
    fun setState(nextState: S) {
//        Logger.w(getDisplayName(), "setState called: $nextState")
        when {
            //before first render call - not yet ready to render - this is an illegal (prohibited) call
            !isPassedOrDuringRender() -> {
                Logger.w(getDisplayName(), "setState() called before first mount(render)")
                //note: we can't update(init) the state at this point,
                //      as we count on its initialization status, inside isPassedFirstRender()
                if (BuildConfig.DEBUG) {
                    throw IllegalStateException("setState() called before first mount")
                }
            }

            //normal render
            isMounted() -> onStandardRender(nextState)

            //not mounted, skip this render
            else -> {} //no need/desire(anti-pattern) to keep track of state. State resets on re-mounts
        }
    }

    /** **CONTRACT:** DO NOT override, call depending on the platform before first mount to eventually call [componentWillMount]
     * which will not be called otherwise before first mount/render (!) */
    fun notifyComponentWillMount() {
//        Logger.e(getDisplayName(), "componentWillMount(isMounted = ${isMounted()})")
        componentWillMount()
    }

    /**
     * Default impl resets to the initial state
     *
     * @return true if handled (consumed), false otherwise
     */
    fun onHardwareBackPressed(): Boolean {
        //init state on back presses

        return isPassedOrDuringRender()
            .letIfTrue { createInitialState(props) }
            ?.takeIf { !it.equals(this.ownState) }
            ?.let { initialState ->
                setState(initialState)
                true
            }

            //not rendered yet or same state as initial. no op
            ?: false
    }






    /* Privates */

    //TODO use rx to combine emittions with setState()
    private fun onRenderImpl(nextProps: P, forceUpdate: Boolean) {
        when {
            //first render call
            !isPropsInitialized() -> onFirstRenderRequest(nextProps)   //may not be attached to window
            //note: ironically, we can't replace isPropsInitialized() with isPassedFirstRender()
            //      as the latter is true only after the first actual render - we could
            //      be calling onFirstRenderRequest() multiple times

            //normal render THINK synchronization (lock) when during a render
            isMounted() && isPassedOrDuringRender() -> onStandardRender(nextProps, forceUpdate)

            //not mounted OR state not init., skip this render
            else -> {
//                Logger.w(getDisplayName(), "onRender() - not yet mounted, updating props")

                //keep track of props, in case we're still waiting for the first render or a re-mount

                if( !reRenderOnRemountDueToNewProps
                        && (forceReRenderOnRemount || shouldComponentUpdate(nextProps))) { //THINK performance
                    reRenderOnRemountDueToNewProps = true
                    //if forceReRenderOnRemount is true, reRenderOnRemountDueToNewProps's value doesn't matter
                }

                updateProps(nextProps) //update anyway, in case shallow equality is true, but they're actual different,
                                        // -> in which case their developer should've known that this difference
                                        //    should not cause re-renders, by design (OwnProps.getAllMembers())

                //note: if state is not init. (not passed first render), it means onFirstRenderRequest was called but is waiting
                //for onMount to (first) render (which first initializes the ownState).
                //we know this because our props are already initialized..
            }
        }
    }

    /** **CONTRACT:** DO NOT override or call yourself, consider private */
    fun isPropsInitialized(): Boolean {
        return try {
            this.props.let { true }
        } catch (e: UninitializedPropertyAccessException) {
            false
        }
    }

    /** **CONTRACT:** DO NOT override or call yourself, consider private */
    fun isStateInitialized(): Boolean {
        return try {
            this.ownState.let { true }
        } catch (e: UninitializedPropertyAccessException) {
            false
        }
    }

    private fun updateState(nextState: S) {
        this.ownState = nextState
    }

    /** **CONTRACT:** DO NOT override or call yourself, consider private */
    fun updateProps(nextProps: P) {
        this.props = nextProps
    }

    /** **CONTRACT:** DO NOT override or call yourself, consider private.
     * If you do override, call super to let the whole inheritance chain have a peak */
    fun UNSAFE_componentDidMountHint() {}

    /** **CONTRACT:** DO NOT override or call yourself, consider private.
     * If you do override, call super to let the whole inheritance chain have a peak */
    fun UNSAFE_componentDidUpdateHint(prevProps: P, prevState: S, snapshot: Any?) {}

    /** **CONTRACT:** DO NOT override or call yourself, consider private.
     * If you do override, call super to let the whole inheritance chain have a peak
     *
     * NOTICE: currently called AFTER un-mount!*/
    fun UNSAFE_componentWillUnmountHint() {}

    fun getDisplayName(): String = javaClass.simpleName





    private fun onComponentMounted(isRemount: Boolean) {
        if (isRemount) {
            notifyComponentWillMount()//THINK first time is(should) be called from actual View implementation as we can't know before first render
        }

        performFirstRenderChain(isRemount)
    }

    private fun performFirstRenderChain(isRemount: Boolean) {
        if (forceReRenderOnRemount || reRenderOnRemountDueToNewProps || !isRemount) {
            reRenderOnRemountDueToNewProps = false

            //first render (not a remount), or a remount and force due to props updated during unmount time

            //if its the first render - its the first time we initialize the lateinit state.
            // doing this (only) here is of huge importance -
            // initialized 'state' is how we know if passed first render or not
            updateState(createInitialState(this.props))
            //if it's not the first render, we reset the state using the latest props,
            // as props may have changed during unmount period, and state may be derived from them

            render()
        }

//        Logger.e(getDisplayName(), "componentDidMount()")

        //handle base components did mount
        UNSAFE_componentDidMountHint()

        //finally, call end-user callback
        componentDidMount()
    }

    private fun onFirstRenderRequest(nextProps: P) {
//        Logger(getDisplayName()).w("onFirstRenderRequest(isMounted = ${isMounted()})")

        // First, save new props. As this is the first render.
        // From this point on, there will be no calls to onFirstRenderRequest as our props will be initialized
        if (BuildConfig.DEBUG && (isPropsInitialized() || isStateInitialized())) {
            throw IllegalStateException("Component props initialized before first render")
        }
        updateProps(nextProps)//note: updating props at this point is very important, also because it prevents multiple calls to this method
        //note: we (first) update the 'state' inside performFirstRenderChain

        val wasMountedBeforeWait = isMounted()
        val shouldRenderDueToAlreadyMounted = { wasMountedBeforeWait && !isPassedOrDuringRender() }

        // Prepare the didMount & willUnmount callbacks
        listenOnMountStateChanges { mounted ->
            val isPassedFirstRender = isPassedOrDuringRender()

            if (mounted) {                      //may be called multiple times during lifecycle
                //may be called before method continues to below onComponentMounted(false), in which case the latter will be skipped
                //due to shouldRenderDueToAlreadyMounted
                onComponentMounted(isPassedFirstRender)

            } else {                            //may be called multiple times during lifecycle
                if (isPassedFirstRender) {
                    performUnmountChain()       //THINK how to call BEFORE actual detachment?
                                                //THINK 2 - should we even?? This way
                                                // users can dispatch (Store) changes
                                                // without the component being re-rendered (already unmounted),
                                                // which is awesome
                }
            }
        }

        //render now if already mounted
        //THINK for lists, the whole point is to render items before they're visible. Think about that..
        if (shouldRenderDueToAlreadyMounted()) {//in the rare case of being mounted before listenOnMountStateChanges,
                                                //and listenOnMountStateChanges being called (with true) before we reach here
//            Logger.w(getDisplayName(), "performing first render - already mounted")
            onComponentMounted(false)
        }
        //note: reason we keep this after the call to 'listenOnMountStateChanges', is that rendering may take much more time than waiting,
        //so we could miss the un-mount event.
    }

    private fun onStandardRender(nextState: S) {
//        Logger.w(getDisplayName(), "onStandardRender(nextState = $nextState)")

        val prevState = this.ownState
        val prevProps = this.props

        if (shouldComponentUpdate(nextState)) {//no render until first props are set
            updateState(nextState)
            performStandardRenderChain(prevProps, prevState)

        } else {
            //update anyway, in case not fully equal (see state's getAllMembers)
            updateState(nextState)  //safe to update because this is not the first render
                                    //thus 'state' is already initialized
        }
    }

    private fun onStandardRender(nextProps: P, forceUpdate: Boolean) {
//        Logger(getDisplayName()).w("onStandardRender(nextProps, forceUpdate = $forceUpdate)")

        val prevState = this.ownState
        val prevProps = this.props

        //if from parent component, check if should update. Else, assume this question has been already asked
        // TODO in rx (shouldComponentUpdate off-main-thread), but think about synchronization! Inside lists it will surely matter
        if (forceUpdate || shouldComponentUpdate(nextProps)) {

            updateProps(nextProps)
            performStandardRenderChain(prevProps, prevState)
        }
    }

    /** @return `true` if this component has ever rendered or is currently undergoing a render cycle */
    fun isPassedOrDuringRender(): Boolean {
        return isStateInitialized()//weird but effective way. State isn't init. until the first render
    }

    private fun performUnmountChain() {
//        Logger.e(getDisplayName(), "componentWillUnmount() isMounted - ${isMounted()}")
        UNSAFE_componentWillUnmountHint()
        componentWillUnmount()
    }

    private fun performStandardRenderChain(prevProps: P, prevState: S) {
        val snapshot = getSnapshotBeforeUpdate(prevProps, prevState)
//        Logger.e(getDisplayName(), "render()")
        render()
//        Logger.e(getDisplayName(), "componentDidUpdate()")
        componentDidUpdate(prevProps, prevState, snapshot)//THINK end of execution queue (extend in APromise) to let View update
        UNSAFE_componentDidUpdateHint(prevProps, prevState, snapshot)
    }
}