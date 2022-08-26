package com.beyondeye.kbloc.utils
/**
 * the code in this file  is partially derived from KoinPlatformTools in https://github.com/InsertKoinIO/koin
 */
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass



public actual object KBlocPlatformTools {
    public actual fun getClassName(kClass: KClass<*>): String = kClass.java.name
    public actual fun <K, V> safeHashMap(): MutableMap<K, V> = ConcurrentHashMap<K, V>()
}
