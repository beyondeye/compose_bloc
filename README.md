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
Add the desired dependencies to your module `build.gradle`.
```groovy
    // Bloc core library 
    // (no need to include if you include the kbloc-compose depedency)
    implementation "io.github.beyondeye:kbloc-core:$version"

    // Android Compose and Bloc integration library
    // (no need to include if you include the kbloc-navigator depedency)
    implementation "io.github.beyondeye:kbloc-compose:$version"
    
    // Navigator library (fork of adrielcafe/voyager)
    implementation "io.github.beyondeye:kbloc-navigator:$version"
```
For available  versions look at  [compose_bloc releases](https://github.com/beyondeye/compose_bloc/releases)
All libraries are multiplatform libraries that support both Android and Desktop compose.

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
to `kbloc-navigator` in the dependencies should work seamlessy. But there are some difference:
- All the following libraries has been merged into a single one (`kbloc-navigator`)
  - `voyager-navigator`
  - `voyager-bottom-sheet-navigator`
  - `voyager-tab-navigator`
  - `voyager-transitions`
  - `voyager-androidx`
  - `voyager-livedata`
- Dependency injection integration libraries `voyager-koin`, `voyager-kodein`, `voyager-hilt`
  has not published yet as part of compose_bloc. Open an issue if you want them published also.
  It is quite strightforward to do it. The same goes for rxjava integration library 'voyager-rxjava'
- `voyager-androidx` now work differently: all lifecycle handling hooks has been removed
  (`AndroidScreenLifecycleOwner' is not used anymore). This is because it caused many issues:
  see for example [issue 62](https://github.com/adrielcafe/voyager/issues/62).
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
- If you want to listen to Android Lifecycle events then you should use the [Android specific Api](https://developer.android.com/jetpack/compose/side-effects#disposableeffect).
  True multiplatform support for all lifecycle events can be added in the future. Please open an issue and vote for it, if you really need this feature 
# Will this library merged back with voyager
Currently the original voyager library does not seems to be mantained any more. I will be quite happy to
join forces with the original author(s), if they will decide that they are interested.
The original library was well designed and I learned a lot about Compose and Multiplatform Kotlin while working on this fork.
# License
Apache 2.0
see details [here](License.md)