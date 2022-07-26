package com.beyondeye.kbloc.compose.concurrent

import java.util.concurrent.ConcurrentHashMap

//public actual class ThreadSafeMap<K, V> : MutableMap<K, V> by ConcurrentHashMap()
typealias ThreadSafeMap<K, V> = ConcurrentHashMap<K,V>
