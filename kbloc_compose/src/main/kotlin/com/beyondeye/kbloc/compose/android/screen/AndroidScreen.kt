package com.beyondeye.kbloc.compose.android.screen

import com.beyondeye.kbloc.compose.lifecycle.DefaultScreenLifecycleOwner
import com.beyondeye.kbloc.compose.lifecycle.ScreenLifecycleOwner
import com.beyondeye.kbloc.compose.lifecycle.ScreenLifecycleProvider
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.compose.screen.ScreenKey
import com.beyondeye.kbloc.compose.screen.uniqueScreenKey

public abstract class AndroidScreen : Screen, ScreenLifecycleProvider {

    override val key: ScreenKey = uniqueScreenKey

    /**
     *  *DARIO* don't use AndroidScreenLifecycleOwner in AndroidScreen
     *  the activity should handle lifecycle, not the screen. this was a bug in original Voyager code
     */
//    override fun getLifecycleOwner(): ScreenLifecycleOwner = AndroidScreenLifecycleOwner.get(this)
    override fun getLifecycleOwner(): ScreenLifecycleOwner = DefaultScreenLifecycleOwner
}

