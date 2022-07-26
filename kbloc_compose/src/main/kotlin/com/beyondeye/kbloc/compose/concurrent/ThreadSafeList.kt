package com.beyondeye.kbloc.compose.concurrent

import java.util.concurrent.CopyOnWriteArrayList

//public actual class ThreadSafeList<T> : MutableList<T> by CopyOnWriteArrayList()
typealias  ThreadSafeList<T> = CopyOnWriteArrayList<T>