package com.beyondeye.kbloc.core

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
     *  * [package:bloc_concurrency](https://pub.dev/packages/bloc_concurrency) for an
     *  opinionated set of event transformers.
     * 
    */
    public open val eventTransformer:EventTransformer<Any> get() = _defaultEventTransformer
    
    public companion object {
        private const val _token = "_token" //=Object()
        /** Returns the current [BlocOverrides] instance.
         * 
         *  This will return `null` if the current [Zone] does not contain
         *  any [BlocOverrides].
         * 
         *  See also:
         *  * [BlocOverrides.runZoned] to provide [BlocOverrides] in a fresh [Zone].
        */
        public val current:BlocOverrides? get()  {
            //  static BlocOverrides? get current => Zone.current[_token] as BlocOverrides?;
            TODO()
        }
        /**
         * Runs [body] in a fresh [Zone] using the provided overrides.
         */
        public fun <R>runZoned(
            body:()->R,
            blocObserver: BlocObserver<Any>?,
            eventTransformer:EventTransformer<Any>?
        ) {
            //final overrides = _BlocOverridesScope(blocObserver, eventTransformer);
            //return _asyncRunZoned(body, zoneValues: {_token: overrides});
            TODO()
        }
    }
}
private class _BlocOVerridesScope(
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
internal val _defaultEventTransformer: EventTransformer<Any> = { events: Flow<Any>, mapper: EventMapper<Any> ->
    events
        .map(mapper).flattenMerge()
}

private class _DefaultBlocObserver: BlocObserver<Any> {}
