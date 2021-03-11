package com.guymichael.kotlinreact

import com.guymichael.kotlinreact.model.TestComponent
import com.guymichael.kotlinreact.model.TestComponent_Boolean
import com.guymichael.kotlinreact.model.ownstate.setState
import com.guymichael.kotlinreact.model.props.onRender
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ComponentFlowUnitTest {
    @Test
    fun componentSimpleRenderFlow() {
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

        //setState - while onRender was never called - make sure it throws
        assertThrows(IllegalStateException::class, {
            component.setState( !initialPropsAndState)
        })

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
        var setStateWhileDismountedValue = !component.ownState.value
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
        var onRenderWhileDismountedValue = !component.props.value
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

        //setState AND onRender - while dismounted. Make sure doesn't render
        setStateWhileDismountedValue = !component.ownState.value
        component.setState(setStateWhileDismountedValue)
        assertRenderCount(component, expectedRenders) {
            println("componentSimpleRenderFlow: component was rendered after unmount and " +
                    "setState. Rendered ${component.renderCount} times instead of " +
                    "$expectedRenders (1 pending)")
        }
        onRenderWhileDismountedValue = !component.props.value
        component.onRender(onRenderWhileDismountedValue)
        assertRenderCount(component, expectedRenders) {
            println("componentSimpleRenderFlow: component was rendered after unmount and " +
                    "onRender. Rendered ${component.renderCount} times instead of " +
                    "$expectedRenders (1 pending)")
        }

        //3rd remount - after 2 pending renders - setState & onRender.
        //make sure rendered once and will both ownState&props updated
        expectedRenders += 1
        component.testSetIsMounted(true)
        assertMounted(component) {
            println("componentSimpleRenderFlow: component is not mounted after 2nd (remount) " +
                    "mount-callback call")
        }
        assertRenderCount(component, expectedRenders) {
            if (component.renderCount == expectedRenders-1) {
                println("componentSimpleRenderFlow: component wasn't rendered after a re-mount " +
                        "and setState+onRender." +
                        "Rendered ${component.renderCount} times instead of $expectedRenders")
            } else {
                println("componentSimpleRenderFlow: component was rendered too many times " +
                        "after a re-mount and setState+onRender." +
                        "Rendered ${component.renderCount} times instead of $expectedRenders")
            }
        }
        assertOwnState(component, setStateWhileDismountedValue) {
            println("componentSimpleRenderFlow: component was rendered after a re-mount and " +
                    "setState but without new (boolean) state ($setStateWhileDismountedValue)")
        }
        assertProps(component, onRenderWhileDismountedValue) {
            println("componentSimpleRenderFlow: component was rendered after a re-mount and " +
                    "onRender but without new (boolean) props ($onRenderWhileDismountedValue)")
        }

        //4th dismount. Make sure not mounted and that componentWillUnmount is called
        expectedWillUnmount += 1
        component.testSetIsMounted(false)
        assertSimpleFlowWillUnmount(component, expectedWillUnmount)
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