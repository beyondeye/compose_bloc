# What is it
A port for [Android Jetpack Compose](https://developer.android.com/jetpack/compose)  of [flutter bloc](https://github.com/felangel/bloc) for better state management 
integrated with a navigation library
(a fork of [voyager](https://github.com/adrielcafe/voyager) library)

# Documentation

see it [here](https://beyondeye.gitbook.io/compose-bloc/)

# Motivation
State management in Compose as described in the [official documentation](https://developer.android.com/jetpack/compose/state) 
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

# License
Apache 2.0
see details [here](License.md)