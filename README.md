![Kotlin version](https://img.shields.io/static/v1?label=Kotlin&message=1.7.10&color=Orange&style=for-the-badge)
![Release](https://img.shields.io/github/v/release/beyondeye/compose_bloc?style=for-the-badge)
![Issues](https://img.shields.io/github/issues/beyondeye/compose_bloc?style=for-the-badge)
![License Apache 2.0](https://img.shields.io/github/license/beyondeye/compose_bloc?style=for-the-badge)
# What is it
A port for Compose of [flutter bloc](https://github.com/felangel/bloc) for better state management 
integrated with a navigation library
(a fork of [voyager](https://github.com/adrielcafe/voyager) library)

# Setup
Add Maven Central to your repositories if needed
```groovy
repositories {
    mavenCentral()
}
```
Add the main library dependency to your module `build.gradle`.
```groovy
    // Bloc+Navigator core library 
    implementation "io.github.beyondeye:kbloc-navigator:$version"
```
For available  versions look at  [compose_bloc releases](https://github.com/beyondeye/compose_bloc/releases)
the library is a multiplatform library that support both Android and Desktop compose.

# Documentation
- [Navigator Overview](https://beyondeye.gitbook.io/compose-bloc/navigator-documentation/navigator-overview)
- [Bloc and Cubit Overview](https://beyondeye.gitbook.io/compose-bloc/bloc-documentation/bloc-and-cubit-overview)
- [Blocs and Compose Overview](https://beyondeye.gitbook.io/compose-bloc/bloc-documentation/blocs-and-compose-overview)

# Show me some code
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //standard setContent to define UI for an activity with Compose
        setContent {
            //use RootNavigator to initialize compose_bloc navigator for activity
            RootNavigator(MainScreen(userName="Albert Einstein"))
        }
    }
}

// A Screen is what the name suggests, and is the basic UI entity to which you can
// navigate to with a Navigator. Entities associated with a Screen like a ScreenModel
// or Bloc are automatically disposed when a Screen is disposed, for example when we
// receive an "onBackPressed" event in a Screen.
// Note that a Screen must be Serializable, so that its fields, that you can think as 
// it "arguments" can be saved and restored when an Activity is paused and then restarted
// or when screen is rotated.
class MainScreen(val userName:String): Screen {
    @Composable
    override fun Content() {
        // the current navigator, in this case it is the root navigator.
        // It is possible to define nested navigators
        val navigator= LocalNavigator.currentOrThrow
        Compose_blocTheme {
            Scaffold(
                topBar = { TopAppBar{ Text("Counter test app") } },
                backgroundColor = MaterialTheme.colors.background,
                    Column {
                        Text("Hello $userName")
                        // when the user click the button we navigate to a new screen
                        Button(onClick={ navigator.push(CounterScreen())})
                             { Text("click to open Counter Screen") }
                    }
                }
            )
        }
    }
}

// The idea behind the Bloc architecture, very similar to Redux, is that we
// don't modify directly the state of the application (the state associated to the Bloc)
// but instead we send events ("actions" in Redux) that are processed
// by the Bloc registered event handlers 

// Here is the definition of the events CounterBloc can handle, all events
// must inherit to some base event type that the bloc is supposed to handle
interface CounterEvent
class AdditionEvent(val value:Int):CounterEvent
class SubtractionEvent(val value:Int):CounterEvent

// this is the state of the bloc. It should be an immutable object, that we don't
// update in-place. instead we create a new instance with the modified values.
// a data class is perfect for this purpose
data class CounterState(val counter:Int=0)

// here we define the Bloc itself. The bloc wrap together
// - some application state (CounterState)
// - associated event handlers to handle changes to the application state
// - a kotlin Flow with the current value of the bloc state, that we can transform to
//   compose MutableState and listen to, for  automatically updating the UI 
//   on state changes
class CounterBloc(cscope: CoroutineScope, startCounter:Int=0):
 Bloc<CounterEvent, CounterState>(
     // every bloc has an associated coroutineScope that is automatically cancelled
     // when the bloc is disposed.
     cscope,   CounterState(startCounter),false) 
 {
    // in the bloc constructor we define the bloc event handlers
    init {
        // each event handler define a function that is called when
        // an event of the specified type is received. the function
        // has two arguments
        // - the received event
        // - an "emit" method to call to emit the updated state according to
        //   the received event. 
        // Note that unlike Redux reducers, event handler are not necessarily pure
        // function without side-effects. On the contrary there can be event handlers
        // whose only purpose are their side effects, and that do not emit a new state
        // at all. Use the coroutine scope associated to the bloc to run the side effects
        on<AdditionEvent> { event, emit ->
            val s=state
            emit(s.copy(counter =s.counter+event.value ))
        }
        on<SubtractionEvent> { event, emit ->
            val s=state
            emit(s.copy(counter =s.counter-event.value ))
        }
    }
}

class CounterScreen: Screen {
    @Composable
    override fun Content() {
          Column(modifier=Modifier.fillMaxWidth(), 
                 horizontalAlignment = Alignment.CenterHorizontally) {
            // out of the BlocProvider composable subtree the bloc is not available
            val bnull= rememberProvidedBlocOf<CounterBloc>()
            Log.e(LOGTAG,"obtained bnull counter bloc: $bnull")  //this must be null
            // BlocProvider makes available the specified bloc (CounterBloc)
            // to the associated composable subtree
            BlocProvider(create = {cscope-> CounterBloc(cscope,1)} ) {
                //rememberProvidedBlocOf is similar to dependency injection:
                //  it retrieves the specified bloc type as defined by the closest
                //  enclosing BlocProvider
                val b= rememberProvidedBlocOf<CounterBloc>()?:return@BlocProvider
                // define some callbacks to wrap sending events to the bloc so
                // that the actual UI does need to know anything about the bloc
                val onIncrement = { b.add(AdditionEvent(1)) }
                val onDecrement = { b.add(SubtractionEvent(1) }
                //BlocBuilder search for the specified bloc type as defined by 
                // the closest enclosing blocProvider and subscribes to its states
                // updates, as a Composable mutableState  that when changes trigger
                // recomposition
                // note that the 2nd template argument type  (bloc state type) 
                // is inferred automatically
                BlocBuilder(b) { counterState->
                    //this is the actual ui composable
                    CounterControls(
                        "Counter display updated always",
                        counterState.counter,
                        onDecrement, onIncrement)
                }
            }
            // out of the BlocProvider composable subtree the bloc is not available
            val bnull2= rememberProvidedBlocOf<CounterBloc>()
            Log.e(LOGTAG,"obtained bnull2 counter bloc: $bnull2") //this must be null

        }
    }

}

@Composable
fun CounterControls(
    explanatoryText:String,
    counterValue: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Text(explanatoryText)
    Text("Counter value: ${counterValue}")
    Row {
        Button(
            onClick = onDecrement,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) { Text(text = "-") }
        Button(
            onClick = onIncrement,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) { Text(text = "+") }
    }
}
```
![image](https://user-images.githubusercontent.com/5619462/185851971-39ec56bb-f4ce-4f8b-9c3a-bbec98b49bc9.png)


# Motivation
State management in Compose as described in the [compose documentation](https://developer.android.com/jetpack/compose/state) 
is basically relying on [Android ViewModel](https://developer.android.com/jetpack/compose/state#viewmodels-source-of-truth)
This is very limiting in general and in particular if we want to develop for multiple platforms not 
only for Android. 

A similar and connected problem is the way the [official compose docs](https://developer.android.com/jetpack/compose/navigation)
expect developers to handle navigation, relying on the Android specific [Jetpack Navigation Library](https://developer.android.com/guide/navigation)

Navigation is such a fundamental component of an app architecture that this makes porting to
another platform very time consuming.

[Flutter](https://flutter.dev/), also from Google,  have a very similar architecture to Android Compose and has actually
inspired a lot the development of Compose.
Flutter is much more mature than Compose so we thought to look at how state management is handled 
there. 

Flutter documentation has a [list of the possible recommended ways](https://docs.flutter.dev/development/data-and-backend/state-mgmt/options) to handle complex state management
The list is quite impressive, with a lot of options that can fit the needs of any developer.
By the ways almost all the options listed in the official Flutter docs are actually 
open source third party libraries. This is very typical of Flutter, which embrace open source and
community developed library much more than Android.

Among all the options there, we were particular impressed by the
[BloC](https://felangel.github.io/bloc)  library  by Felix Angelov. This is one of the most popular Flutter libraries with almost 10k stars on github, 
very mature (already version 8), and very well integrated with Flutter architecture (which as we said
is very similar to Compose). So we decided to port it for Android Compose.

The result of this work is this library.
Because state management is usually linked to specific app screens and their lifecycle, we found the need
to integrate state management with navigation. So we have integrated our port of [flutter_bloc](https://bloclibrary.dev/#/flutterbloccoreconcepts)
with a fork of the [Voyager](https://voyager.adriel.cafe/) navigation library for compose multiplatform, that is one of the most 
popular 3rd party navigation library for Compose.

Voyager is generally a very well designed library
but is currently unmantained, with several outstanding issues and bugs, that we fixed in our fork of the library.

# Differences from original voyager library
The original package for the code from the original library has been preserved, so switching
to `kbloc` in the dependencies should work seamlessy:
```groovy
dependencies {
    // Navigator: (multiplatform library)
    //implementation "cafe.adriel.voyager:voyager-navigator:$currentVersion"
    implementation "io.gihub.beyondeye:kbloc-navigator:$currentVersion"
    
    // BottomSheetNavigator  (multiplatform library)
    //implementation "cafe.adriel.voyager:voyager-bottom-sheet-navigator:$currentVersion"
    implementation "io.gihub.beyondeye:kbloc-bottom-sheet-navigator:$currentVersion"
    
    // TabNavigator  (multiplatform library)
    //implementation "cafe.adriel.voyager:voyager-tab-navigator:$currentVersion"
    implementation "io.gihub.beyondeye:kbloc-tab-navigator:$currentVersion"
    
    // Transitions  (multiplatform library)
    //implementation "cafe.adriel.voyager:voyager-transitions:$currentVersion"
    implementation "io.gihub.beyondeye:kbloc-transitions:$currentVersion"
    
    // Android ViewModel integration (android library)
    //implementation "cafe.adriel.voyager:voyager-androidx:$currentVersion"
    implementation "io.gihub.beyondeye:kbloc-androidx:$currentVersion"
    
    // Koin integration (android library)
    //implementation "cafe.adriel.voyager:voyager-koin:$currentVersion"
    implementation "io.gihub.beyondeye:kbloc-koin:$currentVersion"
    
    // Kodein integration ( (multiplatform library)
    //implementation "cafe.adriel.voyager:voyager-kodein:$currentVersion"
    implementation "io.gihub.beyondeye:kbloc-kodein:$currentVersion"
    
    // Hilt integration (android library)
    //implementation "cafe.adriel.voyager:voyager-hilt:$currentVersion"
    implementation "io.gihub.beyondeye:kbloc-hilt:$currentVersion"
    
    // RxJava integration (JVM library)
    //implementation "cafe.adriel.voyager:voyager-rxjava:$currentVersion"
    implementation "io.gihub.beyondeye:kbloc-rxjava:$currentVersion"
    
    // LiveData integration (android library)
    //implementation "cafe.adriel.voyager:voyager-livedata:$currentVersion"
    implementation "io.gihub.beyondeye:kbloc-livedata:$currentVersion"
}

```
- ``voyager-androidx`` now work differently: all lifecycle handling hooks from the original code have been removed
  (``AndroidScreenLifecycleOwner`` is not used anymore). This is because it caused many issues:
  see for example [issue 62](https://github.com/adrielcafe/voyager/issues/62).
  But this does not mean that android lifecycle events are ignored. When an activity is destroyed 
  then all associated `ScreenModels` and `Blocs` are automatically disposed and the associated flows cancelled
- Also flows associated to blocs of an ``Activity`` are automatically paused when the ``Activity`` is paused
- Now Screen lifecycle is handled in the following way: A `Screen` (and associated screen model and blocs)
  is disposed in the following cases
  - When the `Screen` is popped from the navigator stack
  - When the parent `Activity` where the `Screen` composable was started is destroyed

  In order to all this to work, now is required to declare the top level navigator in an
  activity with `RootNavigator`:
```kotlin
class MainScreen: Screen {
    @Composable
        override fun Content() {
            //... main screen implementation here
        }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //*IMPORTANT* need to use RootNavigator to initialize root navigator for activity, not as in original voyager
            RootNavigator(MainScreen())
        }
    }
}
```
- If Activity and Screen lifecycle as it is currently  handled is not good enough for you please open [a new issue](https://github.com/beyondeye/compose_bloc/issues/new/choose), and we will happy to improve it.
# Will this library merged back with voyager
Currently the original voyager library does not seems to be mantained any more. I will be quite happy to
join forces with the original author(s), if they will decide that they are interested.
The original library was well designed and I learned a lot about Compose and Multiplatform Kotlin while working on this fork.
# License
Copyright 2022 by Dario Elyasy
see details [here](License.md)