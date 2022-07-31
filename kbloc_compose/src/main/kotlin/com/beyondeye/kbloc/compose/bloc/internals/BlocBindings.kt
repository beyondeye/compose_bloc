package com.beyondeye.kbloc.compose.bloc.internals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.toPersistentHashMap

/**
 * store binding for retrieving a specific bloc instance from [BlocStore]
 * [bindingMaps]:
 * key is Bloc class qualifiedName
 * value is blocKey for retrieving the bloc instance from [BlocStore]
 * The bindings are associated to a specific node in the Composable tree (usually a [Screen])and its subtree
 *
 */
class BlocBindings(val bindingMaps: PersistentMap<String, String> = persistentHashMapOf()) {

}

private fun blocBindingsSaver(): Saver<BlocBindings, Map<String,String>> =
    Saver(
        save = { blocBindings -> blocBindings.bindingMaps.toMap() },
        restore = { map -> BlocBindings((map).toPersistentHashMap())}
    )

@Composable
fun <B:BlocBase<*>>rememberBlocBindings(blocKey:String,b:B,curBlocBindings: BlocBindings):BlocBindings {
    return rememberSaveable(saver = blocBindingsSaver()) {
        val curMap=curBlocBindings.bindingMaps
        val newMap=curMap.put(b::class.qualifiedName!!,blocKey)
        BlocBindings(newMap)
    }
}
@PublishedApi
internal val LocalBlocBindings = staticCompositionLocalOf { BlocBindings() }

@Composable
public inline fun BindBloc(b:BlocBase<*>, blocKey:String, crossinline content:@Composable ()->Unit) {
    val newBindings= rememberBlocBindings(blocKey, b, curBlocBindings = LocalBlocBindings.current)
    CompositionLocalProvider(LocalBlocBindings provides newBindings) {
        content()
    }
}
