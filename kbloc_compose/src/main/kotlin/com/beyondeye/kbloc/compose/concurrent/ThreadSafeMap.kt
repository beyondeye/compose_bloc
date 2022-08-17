package com.beyondeye.kbloc.compose.concurrent

import java.util.concurrent.ConcurrentHashMap
/*
    public expect class ThreadSafeMap<K, V>() : MutableMap<K, V>
 */

//public actual class ThreadSafeMap<K, V> : MutableMap<K, V> by ConcurrentHashMap()
typealias ThreadSafeMap<K, V> = ConcurrentHashMap<K,V>
