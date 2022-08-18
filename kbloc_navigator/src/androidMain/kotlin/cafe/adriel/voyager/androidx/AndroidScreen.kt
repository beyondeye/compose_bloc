package cafe.adriel.voyager.androidx

import cafe.adriel.voyager.core.lifecycle.DefaultScreenLifecycleOwner
import cafe.adriel.voyager.core.lifecycle.ScreenLifecycleOwner
import cafe.adriel.voyager.core.lifecycle.ScreenLifecycleProvider
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey

public abstract class AndroidScreen : Screen, ScreenLifecycleProvider {

    override val key: ScreenKey = uniqueScreenKey

    /**
     *  *DARIO* don't use AndroidScreenLifecycleOwner in AndroidScreen
     *  the activity should handle lifecycle, not the screen. this was a bug in original Voyager code
     */
//    override fun getLifecycleOwner(): ScreenLifecycleOwner = AndroidScreenLifecycleOwner.get(this)
    override fun getLifecycleOwner(): ScreenLifecycleOwner = DefaultScreenLifecycleOwner
}

