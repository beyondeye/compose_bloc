# What is it
A port for [Android Jetpack Compose](https://developer.android.com/jetpack/compose)  of [flutter bloc](https://github.com/felangel/bloc) for better state management 
integrated with a navigation library
(a fork of [voyager](https://github.com/adrielcafe/voyager) library)

# setup
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

look at the [full documentation here](https://beyondeye.gitbook.io/compose-bloc/)

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
Apache 2.0
see details [here](License.md)