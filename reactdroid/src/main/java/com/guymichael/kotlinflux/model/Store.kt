package com.guymichael.kotlinflux.model

import com.guymichael.kotlinflux.Utils
import com.guymichael.kotlinflux.model.actions.Action
import com.guymichael.kotlinflux.model.reducers.Reducer
import com.guymichael.kotlinreact.BuildConfig
import com.guymichael.kotlinreact.Logger
import com.guymichael.kotlinreact.model.OwnProps
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.Timed
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class Store protected constructor(
        private val mainReducer: Reducer
        , preloadedState: GlobalState?
        , private val mainThreadScheduler: Scheduler
            /**state changes computation scheduler/thread. Basically main thread for old devices and computation for newer ones*/
        , private val storeObserversComputationScheduler: Scheduler
    ) {

    /**used to dispatch actions*/
    private val dispatchSubject : PublishSubject<(GlobalState) -> GlobalState>
    /**used to share state changes published by the dispatchSubject*/
    private val stateChangeObservable : Observable<StateChange>
    /**used internally just to log states*/
    private val mStateChangeObserver: Observer<StateChange>

    var state: GlobalState = preloadedState ?: mainReducer.getDefaultState()
        private set

    companion object {
        fun combineReducers(reducers: List<Reducer>) : Reducer {
            return MainReducer(reducers)
        }

        fun combineReducers(vararg reducers: Reducer) : Reducer {
            return combineReducers(reducers.asList())
        }

        private class MainReducer(reducers: List<Reducer>) : Reducer(reducers) {
            //no-op - will loop on child reducers
            override fun onAction_mapToNextState(state: GlobalState, action: Action) = state
            override fun getSelfDefaultState() = GlobalState()
        }
    }

    init {
        this.mStateChangeObserver = object : Observer<StateChange> {
            override fun onComplete() {}
            override fun onSubscribe(d: Disposable) {}
            override fun onError(e: Throwable) {
                Logger.e(this@Store::class, "dispatch error: ${e.message}")
            }
            override fun onNext(stateChange: StateChange) {
//                log.iLateInit { "new state (dispatch affected state):\n${stateChange.nextState}" }
            }
        }

        this.dispatchSubject = PublishSubject.create()

        //init the observable that emits state changes to store observers
        this.stateChangeObservable = this.dispatchSubject
            .subscribeOn(storeObserversComputationScheduler)
            .observeOn(storeObserversComputationScheduler)
                //skip (debounce) and aggregate (buffer) if a new state is published, to prevent frequent subsequent new state computations (0 = end of execution queue)
            .debouncedBuffer(0, TimeUnit.MILLISECONDS, storeObserversComputationScheduler)
                //compute all buffered actions in sequence and map to next state
            .map {actions ->
//                val actions = listOf(it)
                if (BuildConfig.DEBUG && actions.isEmpty()) {
                    Logger.w(this@Store.javaClass, "Store: debouncedBuffer triggered with an empty buffer and thus not working properly")
                    dispatchSubject.onNext{ it } //fixes empty buffers by forcing another 'dispatch' which 'flushes' the last failed-to-emit action along with it
                    //THINK reject? Just let everything continue?
                }

                //THINK maybe possible with rx?
                //THINK is it atomic?
                var nextState = this@Store.state
                actions.forEach {action ->
                    nextState = action(nextState)
                }

                //map to
                StateChange(this@Store.state, nextState).apply {
                    if (BuildConfig.DEBUG && actions.isNotEmpty() && Utils.shallowEquality(this.prevState, this.nextState)) {
                        Logger.e(this@Store::class, "action had no effect on state ${if (this.prevState === this.nextState) "(same reference - mutable!)" else "(different reference - immutable)"}")
                    }
                }
            }
                //save new state
            .doOnNext {//THINK doAfterNext?
                //THINK is it atomic?
                this@Store.state = it.nextState
            }
                //prevent no-change actions from notifying observers
            .filter {
                it.prevState !== it.nextState//note: this just checks for object ref, not equals
//                it.prevState != it.nextState //THINK resume (equals). Question is: why do we need to check state changes if we check prop changes? Normally an action WILL change the state
            }
            .doOnError {
                Logger.e(this@Store::class, "Caught dispatch exception: ${it.message}")
                it.printStackTrace()
            }
            .doOnComplete {
                Logger.e(this@Store::class, "store dispatchSubject completed!")
            }
            .retry(1)//re-subscribe on errors, instead of completing and preventing the store from emitting more items
            .onErrorResumeNext { Observable.empty() } //swallow all errors for Store subscribers - actions will be skipped
            .share()//cold. publish to store observers when all previous conditions are met, without running the code above for each one

        this.stateChangeObservable.subscribe(this.mStateChangeObserver)

        Logger.iLazy(this@Store.javaClass) { "Store ready:\n$state" }
    }

    @JvmOverloads
    fun <API_PROPS : OwnProps, MERGED_PROPS : OwnProps> subscribe(component: ConnectedHOC<API_PROPS, MERGED_PROPS, *>
            , observeOnScheduler: Scheduler = mainThreadScheduler) {

        val connectedComponent = ConnectedComponentContainer(component)//THINK garbage collector. Cache for unsubscribe?

        stateChangeObservable
            .subscribeOn(storeObserversComputationScheduler)
            .observeOn(storeObserversComputationScheduler)
            .takeWhile { connectedComponent.shouldComponentReceiveStateChanges() }
            .mapSkipNull { connectedComponent.mapStateToProps(it.nextState) }
            .filter { connectedComponent.shouldComponentUpdate(it) }
            .observeOn(observeOnScheduler)
            .subscribe(connectedComponent)
    }

    @JvmOverloads
    fun <P : OwnProps> subscribe(observer: StoreObserver<P>
            , observeOnScheduler: Scheduler = mainThreadScheduler) {

        val container = StoreObserverContainer(observer)//THINK garbage collector. Cache for unsubscribe?

        stateChangeObservable
            .subscribeOn(storeObserversComputationScheduler)
            .observeOn(storeObserversComputationScheduler)
            .takeWhile { container.shouldReceiveStateChanges() }
            .mapSkipNull { container.mapStateToProps(it.nextState) }
            .filter { container.shouldUpdate(it) }
            .observeOn(observeOnScheduler)
            .subscribe(container)
    }

    fun unsubscribe(component: ConnectedHOC<*, *, *>) {
        component.storeDisposable?.dispose()
    }

    fun unsubscribe(component: StoreObserver<*>) {
        component.storeDisposable?.dispose()
    }




    fun dispatch(action: Action) {
//        log.wLateInit { "dispatch: $action" }

        dispatchSubject.onNext { this@Store.mainReducer.onNewAction_mapToNextState(it, action) }
    }

    fun dispatch(key: StoreKey, value: Any?) {
        dispatch(Action(key, value))
    }

    fun dispatchMany(vararg actions: Action) {//THINK run on computation
//        log.wLateInit { "dispatch: $action" }

        actions.forEach(::dispatch)
    }

    fun dispatchClearState() {
//        log.wLateInit { "dispatchClearState()" }

        dispatchSubject.onNext {
            mainReducer.getDefaultState()
        }
    }

    fun <T : Reducer> dispatchClearState(firstLevelReducer: T) {
//        log.wLateInit { "dispatchClearState(${firstLevelReducer::class.java.simpleName})" }

        dispatchSubject.onNext {
            val nextState = it.clone()
            nextState.map[firstLevelReducer::class.java.simpleName] = firstLevelReducer.getDefaultState()
            nextState//return
        }
    }





    /** Convenience method. See [Action.addToList] */
    fun <T : Any> dispatchAppendToList(key: StoreKey, value: Timed<T>
                                       , mergeWithState: GlobalState = state) {
        dispatch(Action.addToList(key, value, mergeWithState))
    }

    /** Convenience method. See [Action.putList] */
    fun <T : Any> dispatchAppend(key: StoreKey, value: List<Timed<T>>
                                 , mergeWithState: GlobalState = state) {
        dispatch(Action.putList(key, value, mergeWithState))
    }

    /** Convenience method. See [Action.putList] */
    fun <T : Any> dispatchReplace(key: StoreKey, value: List<Timed<T>>) {
        dispatch(Action.putList(key, value, null))
    }




    private fun onMainThreadOrThrow() {
        if (!isOnUiThread()) {
            throw IllegalStateException("onMainThreadOrThrow(): Dispatch() called off the main thread")
        }
    }

    abstract fun isOnUiThread(): Boolean
}











private fun <T> Observable<T>.debouncedBuffer(timeout: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()): Observable<List<T>> {
    return this.publish {
        it.buffer<T>(
            it.debounce(timeout, timeUnit, scheduler)
            .takeUntil<T>(it.ignoreElements().toObservable())//prevent 0 items on complete (not that important?)
        )
    }

//    return this.buffer(this.debounce(timeout, timeUnit, scheduler))//fictitious/value-ignored emitter just to emit the buffer x time after the 'first' emitted item
}

/** Maps non null mappings, and skips (silently) nulls */
//THINK it is unclear that the usage of onErrorResumeNext absorbs ALL errors (not just this method's)
private inline fun <T, R> Observable<T>.mapSkipNull(crossinline mapper: (T) -> R?): Observable<R> {
    return this.map { mapper(it) ?: throw NullPointerException("Observable mapSkipNull: mapping returned null, skipping this one") }
            .onErrorResumeNext { Observable.empty() }
}