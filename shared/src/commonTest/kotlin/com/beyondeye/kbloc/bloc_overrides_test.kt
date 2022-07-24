package com.beyondeye.kbloc

import com.beyondeye.kbloc.async.asyncExpand
import com.beyondeye.kbloc.core.BlocObserver
import com.beyondeye.kbloc.core.BlocOverrides
import com.beyondeye.kbloc.core.EventTransformer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
class FakeBlocObserver<T:Any> :BlocObserver<T> { }

class BlocOverridesTest {
    @Test
    fun BlocOverrides_uses_default_BlocObserver_when_not_specified() {
        BlocOverrides.runZoned {
            val overrides=BlocOverrides.current
            assertTrue { overrides?.blocObserver is BlocObserver }
        }
    }
    @Test
    fun BlocOverrides_uses_default_EventTransformer_when_not_specified() {
        BlocOverrides.runZoned {
            val overrides=BlocOverrides.current
            assertTrue { overrides?.eventTransformer is EventTransformer<Any> }
        }
    }
    @Test
    fun BlocOverrides_uses_custom_BlocObserver_when_specified() {
        val blocObserver=FakeBlocObserver<Any>()
        BlocOverrides.runZoned(blocObserver=blocObserver) {
            val overrides=BlocOverrides.current
            assertTrue { overrides?.blocObserver === blocObserver }
        }
    }

    @Test
    fun BlocOverrides_uses_custom_EventTransformer_when_specified() {
        val eventTransformer:EventTransformer<Any> = {events, mapper ->
            events.asyncExpand(mapper)
        }
        BlocOverrides.runZoned(eventTransformer = eventTransformer) {
            val overrides=BlocOverrides.current
            assertTrue { overrides?.eventTransformer === eventTransformer }
        }
    }

    @Test
    fun BlocOverrides_uses_current_BlocObserver_when_not_specified() {
        val blocObserver=FakeBlocObserver<Any>()
        BlocOverrides.runZoned(blocObserver=blocObserver) {
            BlocOverrides.runZoned {
                val overrides=BlocOverrides.current
                assertTrue { overrides?.blocObserver === blocObserver }
            }
        }
    }
    @Test
    fun BlocOverrides_uses_current_EventTransformer_when_not_specified() {
        val eventTransformer:EventTransformer<Any> = {events, mapper ->
            events.asyncExpand(mapper)
        }
        BlocOverrides.runZoned(eventTransformer = eventTransformer) {
            BlocOverrides.runZoned {
                val overrides=BlocOverrides.current
                assertTrue { overrides?.eventTransformer === eventTransformer }
            }
        }
    }
    @Test
    fun BlocOverrides_uses_nested_BlocObserver_when_specified() {
        val rootBlocObserver=FakeBlocObserver<Any>()
        BlocOverrides.runZoned(blocObserver=rootBlocObserver) {
            val overrides=BlocOverrides.current
            assertTrue { overrides?.blocObserver === rootBlocObserver }
            val nestedBlocObserver=FakeBlocObserver<Any>()
            BlocOverrides.runZoned(blocObserver = nestedBlocObserver) {
                val overrides=BlocOverrides.current
                assertTrue { overrides?.blocObserver === nestedBlocObserver }
            }
        }
    }

    @Test
    fun BlocOverrides_uses_nested_EventTransformer_when_specified() {
        val rootEventTransformer:EventTransformer<Any> = {events, mapper ->
            events.asyncExpand(mapper)
        }
        BlocOverrides.runZoned(eventTransformer = rootEventTransformer) {
            val overrides = BlocOverrides.current
            assertTrue { overrides?.eventTransformer === rootEventTransformer }
            val nestedEventTransformer: EventTransformer<Any> = { events, mapper ->
                events.asyncExpand(mapper)
            }
            BlocOverrides.runZoned(eventTransformer = nestedEventTransformer) {
                val overrides = BlocOverrides.current
                assertTrue { overrides?.eventTransformer === nestedEventTransformer }
            }
        }
    }
    //TODO: I cannot implement this test
    @Test
    fun BlocOverrides_overrides_cannot_be_mutated_after_zone_created() {
        val originalBlocObserver=FakeBlocObserver<Any>()
        val otherBlocObserver=FakeBlocObserver<Any>()
        var blocObserver=originalBlocObserver
        BlocOverrides.runZoned(blocObserver=blocObserver)
        {
            blocObserver=otherBlocObserver
            val overrides=BlocOverrides.current
            assertTrue(originalBlocObserver===overrides?.blocObserver)
            assertTrue { overrides?.blocObserver!==otherBlocObserver }
        }
    }
}
