Reactdroid
=====

Reactdroid is a reactive component-based MVI architecture, along with a flux, global-state architecture (2 library cores).
It is a comprehensive architecture to creating modern mobile apps.

Reactdroid makes it extremely easy to build a modern app. With
clear contracts between components, clear and easy managed app state,
standardized coding, and predictable, bug free app flow with very fast development times.

While it is currently built for Android, it's core is pure kotlin (and RxKotlin)
and is destined to be separated from this project, to serve iOS (ReactIOs) as well, 
using Kotlin Multiplatform abilities.
Any developer that would like to take part in this project, please contact me. 

The MVI core is built using pure Kotlin.
The flux core also uses RxKotlin for Store management.
The Android layer on top contains Android component implementations,
such as List, Text and more, and uses RxAndroid, mainly to support Android's main thread capabilities.

To import project using Gradle:
```kotlin
implementation 'com.github.GuyMichael:Reactdroid:0.1.81'
``` 

## Quick Examples
We will start with quick examples to showcase the style.
Will be followed by a deeper explanation of the architecture.
Hint: the environment is similar to React.js and Redux.

Below is how to wrap an Android `View`s with AComponents,
from inside an Activity, Fragment, View or (preferably) another AComponent.
It makes use of Kotlin Extensions:

```kotlin
//bind views to components.
val cText = withText(R.id.textView)
val cBtn = withBtn(R.id.button)
val cList = withList(R.id.recyclerView)

//inside the parent component's render method,
// and only here(!) we call 'onRender' for all children.
override fun render() {
    val state = this.state
    
    //standard usage
    cText.onRender(TextProps(state.txt))
    
    //OR utility extension
    cText.renderText(state.txt)
    
    //OR utility to handle hide when text is null
    cText.renderTextOrGone(state.txt)
}
```




### Store and global app state



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

