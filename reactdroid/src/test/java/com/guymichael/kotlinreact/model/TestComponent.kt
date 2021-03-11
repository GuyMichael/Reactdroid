package com.guymichael.kotlinreact.model

import com.guymichael.kotlinreact.model.ownstate.BooleanState
import com.guymichael.kotlinreact.model.ownstate.DoubleState
import com.guymichael.kotlinreact.model.ownstate.IntState
import com.guymichael.kotlinreact.model.props.BooleanProps

internal abstract class TestComponent<P : OwnProps, S : OwnState> : Component<P, S> {
    override lateinit var ownState: S
    override lateinit var props: P
    override val forceReRenderOnRemount: Boolean = false
    override var reRenderPendingRemountDueToNewProps: Boolean = false
    override var reRenderPendingRemountDueToNewState: Boolean = false

    /* handle (test) mounts */
    private var mIsMounted = false
    private var isMountedCallback: ((Boolean) -> Unit)? = null
    fun testSetIsMounted(mounted: Boolean) {
        mIsMounted = mounted
        isMountedCallback?.invoke(mounted)
    }
    final override fun listenOnMountStateChanges(consumer: (Boolean) -> Unit) {
        isMountedCallback = consumer
    }
    final override fun isMounted() = mIsMounted



    /* handle (test) lifecycle */
    var didMountCount = 0
        private set
    final override fun componentDidMount() {
        this.didMountCount += 1
    }

    var didUpdateCount = 0
        private set
    final override fun componentDidUpdate(prevProps: P, prevState: S, snapshot: Any?) {
        didUpdateCount += 1
        assert(didUpdateCount + didMountCount == renderCount) {
            println("${javaClass.simpleName}: componentDidUpdate count ($didUpdateCount)" +
                    "and didMount count ($didMountCount) don't match render count ($renderCount)")
        }
        assert(shouldUpdatePropsCount + shouldUpdateStateCount == didUpdateCount) {
            println("${javaClass.simpleName}: componentDidUpdate called more times ($didMountCount) " +
                    "than shouldUpdatePropsCount($shouldUpdatePropsCount) and/or " +
                    "shouldUpdateStateCount($shouldUpdateStateCount)")
        }
        assert(prevShouldUpdateProps == true || prevShouldUpdateState == true) {
            println("${javaClass.simpleName}: componentDidUpdate called while neither " +
                    "prevShouldUpdateProps($prevShouldUpdateProps) " +
                    "or prevShouldUpdateState($prevShouldUpdateState) is 'true'")
        }
    }

    private var shouldUpdatePropsCount = 0
    private var prevShouldUpdateProps: Boolean? = null
    final override fun shouldComponentUpdate(nextProps: P): Boolean {
        shouldUpdatePropsCount += 1
        return super.shouldComponentUpdate(nextProps).also {
            prevShouldUpdateProps = it
        }
    }

    private var shouldUpdateStateCount = 0
    private var prevShouldUpdateState: Boolean? = null
    final override fun shouldComponentUpdate(nextState: S): Boolean {
        shouldUpdateStateCount += 1
        return super.shouldComponentUpdate(nextState).also {
            prevShouldUpdateState = it
        }
    }

    var willUnmountCount = 0
        private set
    override fun componentWillUnmount() {
        willUnmountCount += 1
    }


    /* handle (test) renders */
    var renderCount = 0
        private set
    final override fun render() {
        assert(isMounted()) {
            println("${javaClass.simpleName}: render #${renderCount+1} called while dismounted")
        }
        renderCount += 1
        println("${javaClass.simpleName}: render #$renderCount")
    }
}

internal class TestComponent_Boolean : TestComponent<BooleanProps, BooleanState>() {
    override fun createInitialState(props: BooleanProps) = BooleanState(props.value)
}

internal class TestComponent_Complex : TestComponent<ComplexProps, DoubleState>() {
    override fun createInitialState(props: ComplexProps) = DoubleState(props.init_c)
}
internal data class ComplexProps(val a: Int, val b: List<String>, val init_c: Double) : OwnProps() {
    override fun getAllMembers() = listOf(
        a, b
    )
}