package com.beyondeye.kbloc.utils

//based on https://stackoverflow.com/questions/64251153/how-to-get-current-unixtime-on-kotlin-standard-library-multiplatform
/*
// for common
expect fun epochMillis(): Long

// for jvm
actual fun epochMillis(): Long = System.currentTimeMillis()

// for js
actual fun epochMillis(): Long = Date.now().toLong()

// for native it depends on target platform
// but posix can be used on MOST (see below) of posix-compatible native targets
actual fun epochMillis(): Long = memScoped {
    val timeVal = alloc<timeval>()
    gettimeofday(timeVal.ptr, null)
    (timeVal.tv_sec * 1000) + (timeVal.tv_usec / 1000)
}

Note: Windows Posix implementation doesn't have gettimeofday so it will not compile on MinGW target
 */
expect public fun epochMillis(): Long

