package cafe.adriel.voyager.core.concurrent

import java.util.concurrent.ConcurrentHashMap

// ThreadSafeMap has been removed
//now we use instead immutable map from /kotlinx.collections.immutable + atomicfu for updating
//public actual class ThreadSafeMap<K, V> : MutableMap<K, V> by ConcurrentHashMap()
