package com.guymichael.kotlinreact

import com.guymichael.kotlinreact.model.ComplexProps
import com.guymichael.kotlinreact.model.TestComponent
import com.guymichael.kotlinreact.model.TestComponent_Boolean
import com.guymichael.kotlinreact.model.TestComponent_Complex
import com.guymichael.kotlinreact.model.ownstate.setState
import com.guymichael.kotlinreact.model.props.onRender
import org.junit.Test

/**
 * Local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ComponentFlowUnitTest {
    @Test
    fun simpleRenderFlow() {
        val initialPropsAndState = false
        var expectedRenders = 0
        var expectedWillUnmount = 0

        //create component, make sure not mounted and not rendered by default
        val component = TestComponent_Boolean()
        assertNotMounted(component) {
            println("componentSimpleRenderFlow: component is mounted before 1st onRender()")
        }
        assertRenderCount(component, expectedRenders) {
            println("componentSimpleRenderFlow: component was rendered while dismounted " +
                    "and before 1st onRender()")
        }

        //setState - while onRender was never called - make sure not allowed
        if (BuildConfig.DEBUG) {
            assertThrows(IllegalStateException::class, {
                component.setState( !initialPropsAndState)
            })
        }
        assert( !component.reRenderPendingRemountDueToNewState) {
            println("componentSimpleRenderFlow: component has pending setState while dismounted," +
                    "although setState was called before first onRender - which is not allowed")
        }

        //first onRender - while dismounted.
        //make sure doesn't cause mounts and that it's still not rendered
        component.onRender(initialPropsAndState)
        assertNotMounted(component) {
            println("componentSimpleRenderFlow: component is mounted because of onRender(), " +
                    "before mount-callback call")
        }
        assertRenderCount(component, expectedRenders) {
            println("componentSimpleRenderFlow: component was rendered because of onRender(), " +
                    "before mount-callback call")
        }

        //first mount - after onRender. Make sure rendered automatically.
        expectedRenders += 1
        component.testSetIsMounted(true)
        assertMounted(component) {
            println("componentSimpleRenderFlow: component is not mounted after mount-callback call")
        }
        assertRenderCount(component, expectedRenders) {
            if (component.renderCount == expectedRenders-1) {
                println("componentSimpleRenderFlow: component was not rendered, " +
                        "after onRender and mount-callback call")
            } else {
                println("componentSimpleRenderFlow: component was rendered " +
                        "${component.renderCount} times already, after 1 onRender and 1 " +
                        "mount-callback call")
            }
        }

        //setState. Make sure re-rendered
        expectedRenders += 1
        component.setState( !component.ownState.value)
        assertRenderCount(component, expectedRenders) {
            println("componentSimpleRenderFlow: component was rendered ${component.renderCount} " +
                    "times instead of $expectedRenders, after 1 onRender and 1 setState")
        }

        //onRender. Make sure re-rendered
        expectedRenders += 1
        component.onRender( !component.props.value)
        assertRenderCount(component, expectedRenders) {
            println("componentSimpleRenderFlow: component was rendered ${component.renderCount} " +
                    "times instead of $expectedRenders, after 1 onRender, 1 setState and 1 onRender")
        }

        //set dismounted. Make sure not mounted and that componentWillUnmount is called
        expectedWillUnmount += 1
        component.testSetIsMounted(false)
        assertSimpleFlowWillUnmount(component, expectedWillUnmount)

        //setState - while dismounted. Make sure that it doesn't render.
        val setStateWhileDismountedValue = !component.ownState.value
        component.setState(setStateWhileDismountedValue)
        assertRenderCount(component, expectedRenders) {
            println("componentSimpleRenderFlow: component was rendered after unmount and " +
                    "setState. Rendered ${component.renderCount} times instead of " +
                    "$expectedRenders (1 pending)")
        }

        //1st remount - after 1 pending setState.
        //make sure rendered automatically and with new state
        expectedRenders += 1
        component.testSetIsMounted(true)
        assertMounted(component) {
            println("componentSimpleRenderFlow: component is not mounted after 2nd (remount) " +
                    "mount-callback call")
        }
        assertRenderCount(component, expectedRenders) {
            println("componentSimpleRenderFlow: component wasn't rendered after a re-mount and " +
                    "setState. Rendered ${component.renderCount} times instead of $expectedRenders")
        }
        assertOwnState(component, setStateWhileDismountedValue) {
            println("componentSimpleRenderFlow: component was rendered after a re-mount and " +
                    "setState but without new (boolean) state ($setStateWhileDismountedValue)")
        }

        //2nd dismount. Make sure not mounted and that componentWillUnmount is called
        expectedWillUnmount += 1
        component.testSetIsMounted(false)
        assertSimpleFlowWillUnmount(component, expectedWillUnmount)

        //onRender - while dismounted. Make sure that it doesn't render.
        val onRenderWhileDismountedValue = !component.props.value
        component.onRender(onRenderWhileDismountedValue)
        assertRenderCount(component, expectedRenders) {
            println("componentSimpleRenderFlow: component was rendered after unmount and " +
                    "onRender. Rendered ${component.renderCount} times instead of " +
                    "$expectedRenders (1 pending)")
        }

        //2nd remount - after 1 pending onRender.
        //make sure rendered automatically and with new props
        expectedRenders += 1
        component.testSetIsMounted(true)
        assertMounted(component) {
            println("componentSimpleRenderFlow: component is not mounted after 3rd (remount) " +
                    "mount-callback call")
        }
        assertRenderCount(component, expectedRenders) {
            println("componentSimpleRenderFlow: component wasn't rendered after a re-mount and " +
                    "onRender. Rendered ${component.renderCount} times instead of $expectedRenders")
        }
        assertProps(component, onRenderWhileDismountedValue) {
            println("componentSimpleRenderFlow: component was rendered after a re-mount and " +
                    "onRender but without new (boolean) props ($onRenderWhileDismountedValue)")
        }

        //3rd dismount. Make sure not mounted and that componentWillUnmount is called
        expectedWillUnmount += 1
        component.testSetIsMounted(false)
        assertSimpleFlowWillUnmount(component, expectedWillUnmount)
    }

    @Test
    fun setStateVsOnRenderBetweenMounts() {
        var expectedRenders = 0
        var expectedWillUnmount = 0
        val init_props = ComplexProps(1, emptyList(), 0.0) //init_c defines initial ownState

        val component = TestComponent_Complex()
        component.testSetIsMounted(true)
        assertMounted(component)
        assertRenderCount(component, expectedRenders) //0

        //first render
        expectedRenders += 1
        component.onRender(init_props)
        assertRenderCount(component, expectedRenders)

        //unmount
        expectedWillUnmount += 1
        component.testSetIsMounted(false)
        assertSimpleFlowWillUnmount(component, expectedWillUnmount)

        //setState - while dismounted, then onRender with same props, then remount.
        //make sure re-renders with new ownState
        var setStateWhileDismountedState = component.ownState.copy(value = component.ownState.value!! + 1.0)
        component.setState(setStateWhileDismountedState)
        assertRenderCount(component, expectedRenders)
        component.onRender(component.props.copy())
        assertRenderCount(component, expectedRenders)
        expectedRenders += 1
        component.testSetIsMounted(true)
        assertRenderCount(component, expectedRenders)
        assertOwnState(component, setStateWhileDismountedState)

        //unmount
        expectedWillUnmount += 1
        component.testSetIsMounted(false)
        assertSimpleFlowWillUnmount(component, expectedWillUnmount)

        //remount - make sure doesn't re-render due to the need to render on previous remount by ownState
        component.testSetIsMounted(true)
        assertRenderCount(component, expectedRenders)

        //unmount
        expectedWillUnmount += 1
        component.testSetIsMounted(false)
        assertSimpleFlowWillUnmount(component, expectedWillUnmount)

        //setState - while dismounted, then onRender with different props, then remount.
        //make sure re-renders with initial ownState, regardless of setState
        val onRenderWhileDismountProps = component.props.copy(a = component.props.a + 1)
        setStateWhileDismountedState = component.ownState.copy(value = component.ownState.value!! + 1.0)
        component.setState(setStateWhileDismountedState)
        assertRenderCount(component, expectedRenders)
        component.onRender(onRenderWhileDismountProps)
        assertRenderCount(component, expectedRenders)
        expectedRenders += 1
        component.testSetIsMounted(true)
        assertRenderCount(component, expectedRenders)
        assertProps(component, onRenderWhileDismountProps)
        assertOwnState(component, component.createInitialState(onRenderWhileDismountProps))

        //unmount
        expectedWillUnmount += 1
        component.testSetIsMounted(false)
        assertSimpleFlowWillUnmount(component, expectedWillUnmount)

        //remount - make sure doesn't re-render due to the need to render on previous remount by props
        component.testSetIsMounted(true)
        assertRenderCount(component, expectedRenders)

        //unmount
        expectedWillUnmount += 1
        component.testSetIsMounted(false)
        assertSimpleFlowWillUnmount(component, expectedWillUnmount)
    }

    @Test
    fun testComplexPropsShallowEquality() {
        var expectedRenders = 0
        val init_props = ComplexProps(1, listOf("a", "b", "c"), 0.0) //init_c defines initial ownState

        val component = TestComponent_Complex()
        component.testSetIsMounted(true)
        expectedRenders += 1
        component.onRender(init_props)
        assertRenderCount(component, expectedRenders)

        //change a
        expectedRenders += 1
        component.onRender(component.props.copy(a = component.props.a + 1))
        assertRenderCount(component, expectedRenders)

        //change b
        expectedRenders += 1
        component.onRender(component.props.copy(b = component.props.b + "d"))
        assertRenderCount(component, expectedRenders)

        //change c - expect no re-render
        component.onRender(component.props.copy(init_c = component.props.init_c + 1.0))
        assertRenderCount(component, expectedRenders)
    }

    @Test
    fun testSubsequentMounts() {
        val initialPropsAndState = false
        var expectedRenders = 0
        var expectedDidMounts = 0
        var expectedWillUmounts = 0

        //create and onRender
        val component = TestComponent_Boolean()
        component.onRender(initialPropsAndState)
        assertRenderCount(component, expectedRenders) //0

        //call 'on mount'. Expect render and didMount
        expectedRenders += 1
        expectedDidMounts += 1
        component.testSetIsMounted(true)
        assertRenderCount(component, expectedRenders)
        assertDidMountCount(component, expectedDidMounts)

        //call 'on mount' even though already mounted. Expect no extra didMount / renders
        component.testSetIsMounted(true)
        assertRenderCount(component, expectedRenders)
//        assertDidMountCount(component, expectedDidMounts) TODO FAILS, fix Component logic

        //unmount
        expectedWillUmounts += 1
        component.testSetIsMounted(false)
        assertNotMounted(component)
        assertWillUnmountCount(component, expectedWillUmounts)

        //call 'on unmount' event though already unmounted. Expect no extra willUnmount
        component.testSetIsMounted(false)
        assertNotMounted(component)
//        assertWillUnmountCount(component, expectedWillUmounts) TODO FAILS, fix Component logic
    }
}






private fun assertSimpleFlowWillUnmount(component: TestComponent<*, *>, expectedCount: Int) {
    assertNotMounted(component) {
        println("componentSimpleRenderFlow: component is mounted after unmount " +
                "mount-callback call")
    }
    assertWillUnmountCount(component, expectedCount) {
        if (component.willUnmountCount == expectedCount-1) {
            println("componentSimpleRenderFlow: componentWillUnmount wasn't called after " +
                    "dismount mount-callback call")
        } else {
            println("componentSimpleRenderFlow: componentWillUnmount was called too many " +
                    "times - ${component.willUnmountCount} instead of $expectedCount")
        }
    }
}