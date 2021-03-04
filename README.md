Reactdroid
=====

_Reactdroid_ is a reactive component-based MVI architecture, along with a _Flux_, global-state architecture (2 library cores).
It is a comprehensive architecture to creating modern mobile apps.

_Reactdroid_ makes it extremely easy to build a modern app. With
clear contracts between components, clear and easy managed app state,
standardized coding, and predictable, bug free app flow with very fast development times.

_Note: while it is currently built for _Android_, its core is pure _Kotlin_ (and _RxKotlin_)
      and is destined to be separated from this project, to serve _iOS_ (_ReactIOs_) as well,
      using [_Kotlin_ _Multiplatform_](https://kotlinlang.org/lp/mobile/).
      **Any developer that would like to take part in this project, please contact me.**
      The _MVI_ core is built using pure _Kotlin_.
      The _Flux_ core also uses _RxKotlin_ for _Store_ management.
      The _Android_ layer on top contains _Android_ UI _Component_ implementations,
      such as _List_, _Text_ and more, and uses _RxAndroid_, mainly to support _Android_'s _MainThread_._


## Quick Start
For a fully working app which uses this library, [just go here](https://github.com/GuyMichael/ReactiveAppExample)

For a deeper explanation of the architecture, please read these [Medium articles](https://medium.com/@gguymi/587726a5045f)
_TL;DR_: it is similar to [React](https://reactjs.org/tutorial/tutorial.html#what-is-react) and [Redux](https://redux.js.org/introduction/core-concepts#core-concepts).

To import the project using Gradle:
```kotlin
implementation 'com.github.GuyMichael:Reactdroid:0.1.84'
```

Below are some (UI) _Component_ examples and explanation to showcase the style of this library.

### Components Core ('React' - like) - Overview
Basically, the whole architecture is based on the _Component_ _class_, that makes it easier to code UI,
as everything UI is located inside 1 method only - `render()`.
A _Component_'s 'API' is defined simply by an (complex) _Object_ - `OwnProps`.
By passing (particular-type) `OwnProps` to some _Component_, you control it and tell it _when_ and _what_ to _render_.
Simply put - if the new _props_ differ from the previous ones a _Component_ already has - it will (re) _render_.

An _AComponent_ is the _Android_'s implementation of the _Component_ model.
An _AComponent_ wraps an _Android View_ and controlls it.
_Note: there are also _ComponentActivity_ and _ComponentFragment_ which are extensions to the _Android_ models,
      to 'convert' their usage to a _Component_-like one. But you should avoid using them, if possible - 
      you should just wrap your whole _Activity/Fragment_'s layout with an _AComponent_._

#### AComponents - Examples
Below is an example showcasing how to wrap an _Android_ `TextView` with an _AComponent_,
specifically, _ATextComponent_, from inside another _AComponent_.
_Note: `withText()` makes use of _Kotlin Extensions_._
```kotlin
//bind views to components.
val cText = withText(R.id.textView)       //cText stands for 'componentText'

//inside the parent component's render method,
// and only here(!) we call 'onRender' for all children.
override fun render() {
    val props = this.props
    
    //standard usage
    cText.onRender(TextProps(props.childTxt))
    
    //OR utility extension
    cText.renderText(props.childTxt)
    
    //OR utility to handle hide when text is null
    cText.renderTextOrGone(props.childTxt)
}
```

Here is what does a button look like:
```kotlin
val cBtn = withBtn(R.id.button) {
    //onClick - update (own, internal) state
    this.setState(MyState(this.state.counter+1))
}

override fun render() {
    val state = this.ownState
    
    cBtn.renderText(
        if (state.counter > 0)
          "ACTIVE" 
        else "DISABLED"
    )
    
    //normally you won't need to use the View directly, 
    // as the props and utility methods are suitable for
    // most cases. But for the sake of this example,
    // let's use the underlying View to disable the button.
    cBtn.mView.setEnabled(state.counter > 0)
}
```

A (_RecyclerView_ wrapper) list, to show _Netflix_ titles/movies:
```kotlin
val cList = withList(R.id.recyclerView)

override fun render() {
    cList.onRender( ListProps(
        this.props.netflixTitles
            ?.map { ListItemProps(
                //id.     layout.                 item props.    item AComponent creator
                it.title, R.layout.netflix_title, DataProps(it), ::NetflixTitleItem
            )}
            
            ?.sortedBy { it.props.data.title }
            ?: emptyList()
    ))
}
```
_Note: `ListItemProps` contains everything for the underlying _Adapter_ to know what to _render_.
      There is absolutely no need to have a custom _Adapter_ or _ViewHolder_(!).
      You need 2 things: an 'item' layout (xml file) and an 'item' _AComponent_ to wrap it.
      You can use as many _View_ types and layouts as you like, as well as change them between _renders_._


Below is an example of a _NetflixTitleItem_ _AComponent_.
Except for a layout xml file, this is the only code you need to _render_ lists in _Reactdroid_:

```kotlin
class NetflixTitleItem(v: View) : ASimpleComponent< DataProps<NetflixTitle> >(v) {

    private val cTxtName = v.withText(R.id.netflix_title_name)

    override fun render() {
        cTxtName.renderText(this.props.data.title.name)
    }
}
```


Here is a text input (_EditText_ wrapper). A _String_ input in this case,
but you can use an _Int_ input for example - to automatically parse input as numbers into your _state_.
```kotlin
private val cInput = withStringInput(R.id.editText) {
    //user changed EditText's text. Update (own) state to re-render
    setState(MyState(inputTxt = it))
}

override fun render() {
    cInput.onRender(this.state.inputTxt)
}
```


### Flux core ('Redux' - like) - Overview - Store and global app state
A *Store* is basically a *global* app *state* handler which UI *components* can use to update
the app state (an action that is called to *Dispatch*). When that *state* is updated,
the *Store* notifies all connected UI *components* to 'tell' them to (re) *render* (update the UI).
This way the data flow in the app is uni-directional and also very simple:
**AComponent** -> (dispatches *Action* - an update request) -> **Store** updates the *GlobalState* -> notifies back to (all) **AComponent**(s)

Below is how to define a *global* application *state* by creating a *Store* that manages it:
```kotlin
object MainStore : AndroidStore(combineReducers(
    MainDataReducer, FeatureReducerA, FeatureReducerB
))
```
The app *state* consists of many *reducers*. Each *reducer* holds and manages
some part of the whole *state* and, each part, consists of enum keys that may hold some value.
It is most easy to think of a reducer as a mapping of (state) keys to objects - `Map<String, Object>`
and so the whole *app state* can be thought of as a mapping of *reducers* to their own `map`s - *Map<Reducer, Map<String, Object>>*.
Below is how we define a basic *reducer*:
```kotlin
object FeatureReducerA : Reducer() {
    override fun getSelfDefaultState() = GlobalState(
        FeatureReducerAReducerKey.isFeatureEnabled to true
    )
}

//and below is a way to define the *reducer*'s keys.
//note: future versions will hopefully make use of Kotlin's Sealed classes
// to eliminate the need for using enums in Android and help with having *typed* keys
enum class FeatureReducerAReducerKey : StoreKey {
    isFeatureEnabled    //should hold a Boolean
    ;

    override fun getName() = this.name
    override fun getReducer() = FeatureReducerA
}
```

Now we have a *global* app *state*! Let's see how we can *dispatch* actions to change it.
We just need to provide the (reducer) key to update and the (new) value:
```kotlin
MainStore.dispatch(FeatureReducerAReducerKey.isFeatureEnabled, false)
```

Only thing missing is a way to *listen* to *state* changes so UI *Components* will 'know' when
to (re) *render*. Connecting to *Store* is done by encapsulating an *AComponent* inside another, special *component*
that handles everything for you. Technically speaking, that (other) *component* is a [*HOC* - High Order Component](https://reactjs.org/docs/higher-order-components.html). Below is how you can connect an *AComponent* to the *Store*:

```kotlin
val myAComponent: AComponent<...>
...
val connectedComponent = connect(
    // AComponent to connect (by encapsulation)
    myAComponent
    
    //mapStateToProps -> a function that converts the whole GlobalState
    // to the AComponent's props
    , { globalState -> MyComponentProps(
            isFeatureEnabled = state.get(FeatureReducerAReducerKey.isFeatureEnabled)
      )}
      
     // Store supplier
    , { MainStore }
)

//from now on, 'myAComponent' we be re-rendered whenever FeatureReducerAReducerKey.isFeatureEnabled's
value is changed.
```

That's a basic example, but it explains exactly how this Flux architecture works.
You *dispatch* some *Action* to the (global) *Store* (e.g. from your Button *Component*)
and the *Store* handles the update for you, telling your *component* when to (re) *render*.
simple as that.



R8 / ProGuard
--------

No requirements at this stage, except for RxKotlin & RxAndroid related rules
which may apply.
Please contact me if you encounter any issues.


License
--------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

