package com.beyondeye.kbloc.core

import com.beyondeye.kbloc.concurrency.EventTransformer_concurrent
import com.beyondeye.kbloc.concurrency.EventTransformer_sequential
import kotlinx.coroutines.flow.*

/**
 *  This class facilitates overriding [BlocObserver] and [EventTransformer].
 *  It should be extended by another class in client code with overrides
 *  that construct a custom implementation. The implementation in this class
 *  defaults to the base [BlocObserver] and [EventTransformer] implementation.
 *  For example:
 * 
 *  ```dart
 *  class MyBlocObserver extends BlocObserver {
 *    ...
 *     *  A custom BlocObserver implementation.
 *    ...
 *  }
 * 
 *  void main() {
 *    BlocOverrides.runZoned(() {
 *      ...
 *       *  Bloc instances will use MyBlocObserver instead of the default BlocObserver.
 *      ...
 *    }, blocObserver: MyBlocObserver());
 *  }
 *  ```
abstract class BlocOverrides 
{
    
}
*/
public abstract class BlocOverrides {
    /** The [BlocObserver] that will be used within the current [Zone].
     *
     *  By default, a base [BlocObserver] implementation is used.
     */
    public open val blocObserver:BlocObserver<Any> get() = _defaultBlocObserver

    /** The [EventTransformer] that will be used within the current [Zone].
     * 
     *  By default, all events are processed concurrently.
     * 
     *  If a custom transformer is specified for a particular event handler,
     *  it will take precendence over the global transformer.
     * 
     *  See also:
     *
     *  see also [EventTransformer_sequential], [EventTransformer_concurrent] and others in
     *   package com.beyondeye.kbloc.concurrency for an opinionated set of event transformers.
     * 
    */
    public open val eventTransformer:EventTransformer<Any> get() = _defaultEventTransformer
    
    public companion object {
        //TODO use instead CompositionLocal? see https://developer.android.com/jetpack/compose/compositionlocal
        private var curOverrides:BlocOverrides? = null
        /** Returns the current [BlocOverrides] instance.
         * 
         *  This will return `null` if the current [Zone] does not contain
         *  any [BlocOverrides].
         * 
         *  See also:
         *  * [BlocOverrides.runWithOverrides] to provide [BlocOverrides] in a fresh [Zone].
        */
        public val current:BlocOverrides? get()  {
            //original code:  static BlocOverrides? get current => Zone.current[_token] as BlocOverrides?;
            return  curOverrides
        }
        /**
         * Runs [body] using the provided overrides.
         * NOTE: in Dart there is a concept of zone: see https://api.dart.dev/stable/2.17.6/dart-async/Zone-class.html
         *     in kotlin we have mantained the name of the method, although there is no such concept in kotlin
         *     we simply run [body] ovveriding [blocObserver] and [eventTransformer]
         *     *DARIO* original method name was runZoned
         */
        public fun <R>runWithOverrides(
            blocObserver: BlocObserver<Any>?=null,
            eventTransformer:EventTransformer<Any>?=null,
            body:()->R,
        ): R {
            val overrides = _BlocOverridesScope(blocObserver, eventTransformer)
            //original code: return _asyncRunZoned(body, zoneValues: {_token: overrides});
            //TODO in the
            val prev = curOverrides
            curOverrides=overrides
            val res= body()
            curOverrides=prev
            return res
        }

        /**
         * set the current and global ovverides for [BlocObserver] and [EventTransformer]
         * NOTE: This method is equivalent to setting the global BlocObserver and EventTransformer in flutter bloc
         */
        public fun setGlobalOverrides(
            blocObserver: BlocObserver<Any>?=null,
            eventTransformer:EventTransformer<Any>?=null,
            )
        {
            curOverrides = _BlocOverridesScope(blocObserver,eventTransformer)
        }
    }
}
private class _BlocOverridesScope(
    val _blocObserver: BlocObserver<Any>?,
    val _eventTransformer: EventTransformer<Any>?
) : BlocOverrides() {
    val _previous: BlocOverrides? = BlocOverrides.current
    override val blocObserver: BlocObserver<Any>
        get() {
            val blocObserver = _blocObserver
            if (blocObserver != null) return blocObserver

            val previous = _previous
            if (previous != null) return previous.blocObserver

            return super.blocObserver
        }
    override val eventTransformer: EventTransformer<Any>
        get() {
            val eventTransformer = _eventTransformer
            if (eventTransformer != null) return eventTransformer

            val previous = _previous
            if (previous != null) return previous.eventTransformer

            return super.eventTransformer
        }
}

private val _defaultBlocObserver = _DefaultBlocObserver()
/* TODO I am not sure that this implementation of _defaultEventTransformer is right
original code:
late final _defaultEventTransformer = (Stream events, EventMapper mapper) {
  return events
      .map(mapper)
      .transform<dynamic>(const _FlatMapStreamTransformer<dynamic>());
};
*
*/
// TODO change the default event transformer to flattenConcat from flattenMerge
// NOTE that this exactly the same as EventTransformer_concurrent
internal val _defaultEventTransformer: EventTransformer<Any> = { events: Flow<Any>, mapper: EventMapper<Any> ->
    events
        .map(mapper).flattenMerge()
}

private class _DefaultBlocObserver: BlocObserver<Any> {}
