package com.beyondeye.kbloc.utils

/**
 * the code in this file  is partially derived from KoinPlatformTools in https://github.com/InsertKoinIO/koin
 */
import kotlin.reflect.KClass

public expect object KBlocPlatformTools {
    public fun getClassName(kClass: KClass<*>): String
    public fun <K, V> safeHashMap(): MutableMap<K, V>
}
