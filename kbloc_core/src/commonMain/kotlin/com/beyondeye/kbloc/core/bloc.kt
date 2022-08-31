package com.beyondeye.kbloc.core

import com.beyondeye.kbloc.concurrency.EventTransformer_concurrent
import com.beyondeye.kbloc.concurrency.EventTransformer_sequential
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlin.reflect.KClass


/**
 * An [ErrorSink] that supports adding events.
 *
 * Multiple events can be reported to the sink via [add_sync].
 */
public interface BlocEventSink<Event:Any?> :ErrorSink
{
    /**
     * Adds an [event] to the sink.
     *
     * Must not be called on a closed sink.
     * note: unlike the original dart code, events are always added serially in the same sequence as
     * calls to the [add_sync]
     */
    public suspend fun add_sync(event:Event)

    /**
     * same as [add_sync] but event is added to event stream without waiting for completion
     * Note: the original dart code actually behave like this method,
     */
    public fun add(event:Event)
}

/**
 * Signature for a function which converts an incoming event
 * into an outbound stream of events.
 *  Used when defining custom [EventTransformer]s.
 */
public typealias EventMapper<Event> = (Event) -> Flow<Event>

/**
 *  Used to change how events are processed.
 *  By default events are processed concurrently.
 *  see also [EventTransformer_concurrent] and [EventTransformer_sequential]
 */
public typealias EventTransformer<Event> = (events:Flow<Event>,mapper:EventMapper<Event>) -> Flow<Event>

/**
 * An event handler is responsible for reacting to an incoming [event]
 * and can emit zero or more states via the [Emitter].
 * NOTE: in the original DART code: EventHandler<Event,State> return a future that is automatically awaited().
 *       In kotlin, with suspend functions, this is introduces useless complexity, so in our implementation
 *       EventHandler is a SUSPEND function that return Unit
 */
public typealias EventHandler<Event,State> = suspend (event:Event,emit:Emitter<State>) ->Unit

@PublishedApi internal class _Handler<Event:Any>(val isType:(value:Any)->Boolean, val type: KClass<Event>)

/**
 * This is the central class of the library.
 * In brief what it does is:
 * Takes a `Stream` of `Events` as input and transforms them into a `Stream` of `States` as output.
 */
public abstract class Bloc<Event : Any, State : Any>
 public constructor(
    /**
     * the coroutine scope used for running async state update function (queueStateUpdate)
     * and suspend functions in event handlers
     */
    cscope: CoroutineScope, initialState: State,
    /**
     * in the original bloc dart code this is always false
     */
    useReferenceEqualityForStateChanges: Boolean=true,
    /**
     * flag that set change parallel/sequential behavior of event processing for a bloc
     * in the original flutter_bloc events are processed in parallel by  default. This means
     * it is possible to have race condition when multiple events are processed in parallel.
     * So if multiple event handler will try to update the bloc state in parallel, not necessarily we will
     * see the effect of all state changes from all event handler. This very problematic for complex bloc state
     * (i.e. bloc state with more than one field that can change independently)
     * With bloc with such complex bloc state. you should really use sequential event processing, unless
     * you really know what you are doing.
     * Note that if [BlocOverrides.current.eventTransformer] is defined that it will take precedence
     * on what you specify with the flag [useSequentialEventProcessing]
     */
    useSequentialEventProcessing:Boolean=true,
) : BlocBase<State>(initialState, cscope, useReferenceEqualityForStateChanges), BlocEventSink<Event> {

    private var eventFlowJob:Job?=null
    //see https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-shared-flow/index.html
    @PublishedApi
    internal val _eventController:MutableSharedFlow<Event> = MutableSharedFlow(0,EVENT_BUFFER_CAPACITY,BufferOverflow.SUSPEND)
    @PublishedApi
    internal val _subscriptions:MutableList<Job> = mutableListOf()
    @PublishedApi
    internal val _handlers:MutableList<_Handler<out Event>> = mutableListOf()
    private val _emitters:MutableList<_Emitter<State>> = mutableListOf()
    @PublishedApi
    internal val _eventTransformer:EventTransformer<Any> =
    BlocOverrides.current?.eventTransformer ?: if(useSequentialEventProcessing) EventTransformer_sequential() else EventTransformer_concurrent()

    init {
        _startEventHandlerJob()
    }
    private fun _startEventHandlerJob() {
        //TODO use a different coroutine scope here? probably not
        eventFlowJob = cscope.launch {
            _eventController.collect()
        }
    }
    private fun _stopEventHandlerJob() {
        eventFlowJob?.cancel()
        eventFlowJob=null
    }
    /**
     * Notifies the [Bloc] of a new [event] which triggers
    * all corresponding [EventHandler] instances.
    *
    * * A [StateError] will be thrown if there is no event handler
    * registered for the incoming [event].
    *
    * * A [StateError] will be thrown if the bloc is closed and the
    * [event] will not be processed.
    * events are sent asynchronously using the bloc coroutinescope
    */
    override fun add(event: Event) {
        if (event==null) return
        check_event_registered(event)
        cscope.async {
            try {
                onEvent(event)
                _eventController.emit(event)
            } catch (error: Throwable) {
                onError(error)
                throw error
            }
        }
    }

    /**
     * same as [add_sync] but add event serially (from the point of view of the calling suspend function)
     */
    override suspend fun add_sync(event: Event) {
        if (event == null) return
        check_event_registered(event)
        try {
            onEvent(event)
            _eventController.emit(event)
        } catch (error: Throwable) {
            onError(error)
            throw error
        }
    }

    private fun check_event_registered(event: Event) {
        if (CHECK_IF_EVENT_HANDLER_REGISTERED) { //TODO should i leave this debug mode or use a flag
            val eventType = event!!::class
            val handlerExists = _handlers.find { it.type == eventType } != null
            if (!handlerExists)
                throw StateError(getHandlerMissingErrorMessage(eventType))
        }
    }


    /** Called whenever an [event] is [add_sync]ed to the [Bloc].
    *  A great spot to add logging/analytics at the individual [Bloc] level.
    * 
    *  **Note: `super.onEvent` should always be called first.**
    *  override fun onEvent(event:Event) {
    *    // Always call super.onEvent with the current event
    *    super.onEvent(event)
    * 
    *    // Custom onEvent logic goes here
     *   // ...
    *  }
    *
    *  See also:
    * 
    *  * [BlocObserver.onEvent] for observing events globally.
    */
    @MustCallSuper
    protected open fun onEvent(event: Event) {
        // ignore: invalid_use_of_protected_member
        _blocObserver?.onEvent(this as Bloc<Any,Any>, event)
    }

    /** **[emit] is only for internal use and should never be called directly
    *  outside of tests. The [Emitter] instance provided to each [EventHandler]
    *  should be used instead. See below for example**
    * 
    *  class MyBloc(cscope:CoroutineScope) :Bloc<MyEvent, MyState>(cscope,MyInitialState())  {
    *    init {
    *      on<MyEvent>{ event, emit ->
    *        // use `emit` to update the state.
    *        emit(MyOtherState())
    *      }
    *    }
    *  }
    *
    *  Updates the state of the bloc to the provided [state].
    *  A bloc's state should only be updated by `emitting` a new `state`
    *  from an [EventHandler] in response to an incoming event.
    */
    //@visibleForTesting
    override fun emit(state: State) {
        super.emit(state)
    }


    
    /** Called whenever a [transition] occurs with the given [transition].
    *  A [transition] occurs when a new `event` is added
    *  and a new state is `emitted` from a corresponding [EventHandler].
    *  executed.
    *  [onTransition] is called before a [Bloc]'s [state] has been updated.
    *  A great spot to add logging/analytics at the individual [Bloc] level.
    * 
    *  **Note: `super.onTransition` should always be called first.**
    *  override fun onTransition(transition:Transition<Event, State>) {
    *    // Always call super.onTransition with the current transition
    *    super.onTransition(transition)
    * 
    *    // Custom onTransition logic goes here
     *   // ...
    *  }
    *
    *  See also:
    * 
    *  * [BlocObserver.onTransition] for observing transitions globally.
    *     
    */
    @MustCallSuper
    @PublishedApi
    internal open fun onTransition(transition:Transition<Event,State>) {
        // ignore: invalid_use_of_protected_member
        _blocObserver?.onTransition(this as Bloc<Any, Any>, transition as Transition<Any?, Any>)
    }

    /** Closes the `event` stream and the `state` stream.
    *  This method should be called when a [Bloc] is no longer needed.
    *  Once [close] is called, `events` that are [add_sync]ed will not be
    *  processed.
    *  In addition, if [close] is called while `events` are still being
    *  processed, the [Bloc] will finish processing the pending `events`.
    * *important* this method must be called when a bloc is not more needed to avoid memory leaks
    * this is something similar to the lifecycle of a Screen in voyager: when the screen is popped the dispose() method is automatically called
    */
    @MustCallSuper
    override suspend fun close() {
        _stopEventHandlerJob() //_eventController.close()
        for(emitter in _emitters) {
            emitter.cancel() //TODO need to check that this call actually do what it is supposed to do: check the dart implementation
        }
        //wait for current events processing to complete before close
        for (e in _emitters) {
            e.future.await()
        }
        for(s in _subscriptions) {
            s.cancel()
        }
        super.close()
    }
    @PublishedApi
    internal suspend fun handleEvent(handler: EventHandler<Event, State>, event: Event, emitter: _Emitter<State>) {
        //println("handleEvent called for $event")
        try {
            _emitters.add(emitter)
            handler(event, emitter)
        } catch (error: Throwable) {
            onError(error)
            throw error
        } finally {
            emitter.complete()
            _emitters.remove(emitter)
        }
    }
    /** Register event handler for an event of type `E`.
     *  There should only ever be one event handler per event type `E`.
     *
     *  interface CounterEvent
     *  class CounterIncrementPressed :CounterEvent
     *
     *  class CounterBloc(cscope:CoroutineScope) : Bloc<CounterEvent, int>(cscope,0) {
     *    init {
     *      on<CounterIncrementPressed>( event, emit  -> emit(state + 1) }
     *    }
     *  }
     *
     *
     *
     *  * A [StateError] will be thrown if there are multiple event handlers
     *  registered for the same type `E`.
     *
     *  By default, events will be processed concurrently.
     *
     *  See also:
     *
     *  * [EventTransformer] to customize how events are processed.
     *  see also [EventTransformer_sequential], [EventTransformer_concurrent] and others in
     *   package com.beyondeye.kbloc.concurrency for an opinionated set of event transformers.
     *
     */
    //todo rewrite this method using queuestateupdate and deferredstate
    public inline fun <reified E:Event>  on(noinline transformer:EventTransformer<E>?=null,noinline handler:EventHandler<E,State>) {
        val eventType = E::class
        val handlerExists = _handlers.find { it.type == eventType } != null
        if (handlerExists) {
            throw StateError(
                getEventMultipleRegistrationErrorMessage(eventType)
            )
        }
        _handlers.add(_Handler<E>({ e -> e is E},eventType))

        val _transformer= transformer ?: _eventTransformer

        //extract from the general flow of events the specific flow of event of this type
        val filtered_events_flow=_eventController.filterIsInstance<E>()
        val transformed_filtered_events_flow = _transformer(filtered_events_flow,
            {event:Any->
                //wrap Bloc.emit() method, in order to force call to onTransition callback in addition to regular emit
                val emitter = _Emitter({ newState:State->
                    //local method void onEmit(State state) in original bloc.dart  code
                    if(isClosed) return@_Emitter
                    val curState=this.state
                    val notchanged = if(_useReferenceEqualityForStateChanges) curState===newState else curState==newState
                    //TODO: currently the behavior of the stream of states is different from dart implementation: in dart the initial state
                    //   is optional, in kotlin an initial state is required: I suspect that this difference also means that the logic here that
                    //   make use of the _emitted flag is no more needed: need to decide on this
                    if(notchanged && _emitted) return@_Emitter
                    onTransition(Transition(curState,event as Event,newState))
                    emit(newState)
                })
                // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/callback-flow.html
                // The resulting flow completes as soon as the code in the block completes.
                // awaitClose should be used to keep the flow running, otherwise the channel will be closed immediately when block completes
                val controller = callbackFlow<E> {
                    async { //TODO use instead _cscope_stateUpdate.async?
                        try {
                            handleEvent(handler as EventHandler<Event, State>, event as Event,emitter)
                            channel.close() //signal that we completed completed
                        }catch (e:Throwable) {
                            cancel(e.message?:"",e)
                        }
                    }
                    /*
                     * Suspends until either channel.close()  or cancel()  is invoked
                     * or flow collector is cancelled (e.g. by 'take(1)' or because a collector's coroutine was cancelled).
                     * In both cases, callback will be properly unregistered.
                     */
                    awaitClose{ /* execute block here on close */}
                }
                controller
            })

        //TODO: does it make sense to use _cscope_stateUpdate here?
        val subscription=
            cscope.launch { transformed_filtered_events_flow.cancellable().collect() }


        _subscriptions.add(subscription)
    }


    public companion object {
        @PublishedApi
        internal fun getEventMultipleRegistrationErrorMessage(eventType: KClass<out Any>):String =
            "on<$eventType> was called multiple times. There should only be a single event handler per event type."
        internal  fun  getHandlerMissingErrorMessage(eventType: KClass<out Any>): String {
            val msg = """add($eventType) was called without a registered event handler.
                        Make sure to register a handler via on<$eventType>((event, emit) {...})"""
            return msg
        }
        @PublishedApi
        internal const val CHECK_IF_EVENT_HANDLER_REGISTERED:Boolean=true
        private const val EVENT_BUFFER_CAPACITY:Int=100
    }
}

