package com.beyondeye.kbloc.core

/**
 * this is multiplatform code: so we cannot use the standard android annotation: CallSuper from
 * androidx.annotation package: see https://developer.android.com/reference/kotlin/androidx/annotation/CallSuper
 * so we define here a dummy annotation for now
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
public annotation class MustCallSuper

/**
 * An interface for observing the behavior of [Bloc] instances.
 */
public interface BlocObserver<State:Any> {
    /**
     * Called whenever a [Bloc] is instantiated.
     * In many cases, a cubit may be lazily instantiated and
     * [onCreate] can be used to observe exactly when the cubit
     * instance is created.
     * NOTE: lazy bloc intantiation is not actually current supported in in compose_bloc
     *       it is supported in the original flutter_bloc implementation
     */
    //@protected
    @MustCallSuper
    public fun onCreate(bloc:BlocBase<State>) {  }
    /**
     * Called whenever an [event] is `added` to any [bloc] with the given [bloc]
     * and [event].
     */
    //@protected
    @MustCallSuper
    public fun onEvent(bloc:Bloc<Any,State>,event:Any?)  {}
    /**
     * Called whenever a [Change] occurs in any [bloc]
     * A [change] occurs when a new state is emitted.
     * [onChange] is called before a bloc's state has been updated.
     */
    //@protected
    @MustCallSuper
    public fun onChange(bloc:BlocBase<State>,change: Change<State>)  {}
    /**
     * Called whenever a transition occurs in any [bloc] with the given [bloc]
     * and [transition].
     * A [transition] occurs when a new `event` is added
     * and a new state is `emitted` from a corresponding [EventHandler].
     * [onTransition] is called before a [bloc]'s state has been updated.
     */
    //@protected
    @MustCallSuper
    public fun onTransition(bloc:Bloc<Any,State>, transition: Transition<Any?,State>)  {}
    /**
     * Called whenever an [error] is thrown in any [Bloc] or [Cubit].
     * the stackTrace can be obtained (if present) from field [error.stackTraceToString()]
     */
    //@protected
    @MustCallSuper
    public fun onError(bloc:BlocBase<State>,error:Throwable)  {}

    /**
     * Called whenever a [Bloc] is closed.
     * [onClose] is called just before the [Bloc] is closed
     * and indicates that the particular instance will no longer
     * emit new states.
     */
    //@protected
    @MustCallSuper
    public fun onClose(bloc:BlocBase<State>)  {}
}

